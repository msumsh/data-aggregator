package org.example.modes;

import org.example.api.ApiSource;
import org.example.cli.Menu;
import org.example.concurrent.PollService;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.exceptions.input.InvalidUserInputException;
import org.example.exceptions.request.InvalidResponseException;
import org.example.exceptions.request.SendingHttpRequestException;
import org.example.model.*;
import org.example.model.apitype.ApiType;
import org.example.model.factories.ApiFactory;
import org.example.model.factories.SaverFactory;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.model.modes.PollingMode;
import org.example.model.modes.ReadMode;
import org.example.output.DataSaver;
import org.example.service.ApiService;

import java.io.IOException;
import java.util.List;

public class Interactive {
    public static void runInteractive(Menu menu) throws
            InvalidUserInputException,
            IOException,
            SavingJsonException,
            SavingCsvException {
        ApiService service = new ApiService();

        PollService[] schedulerRef = {null};
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (schedulerRef[0] != null && schedulerRef[0].isRunning()) {
                schedulerRef[0].stop();
            }
        }));

        boolean next = true;
        while (next) {
            PollingMode poll = menu.selectPollingMode();
            if (poll == PollingMode.SINGLE) {
                if (schedulerRef[0] != null && schedulerRef[0].isRunning()) {
                    System.out.println("Cannot use single mode while polling is running. Stop polling first.");
                    next = menu.wantToDo("\nDo you want to continue?");
                    continue;
                }
                singleMode(menu, service);
                next = menu.wantToDo("\nDo you want to continue?");
            } else if (poll == PollingMode.MULTIPLE_START) {
                schedulerRef[0] = startMode(menu, service, schedulerRef[0]);
                next = menu.wantToDo("\nDo you want to continue?");
            }  else if (poll == PollingMode.MULTIPLE_STOP) {
                schedulerRef[0] = stopMode(schedulerRef[0]);
                next = menu.wantToDo("\nDo you want to continue?");
            }
        }

        if (schedulerRef[0] != null && schedulerRef[0].isRunning()) {
            schedulerRef[0].stop();
        }
    }

    private static void singleMode(Menu menu, ApiService service) throws InvalidUserInputException,
            SavingJsonException,
            IOException {
        ApiType apiType = menu.selectApi();
        ApiSource api = ApiFactory.newSource(apiType);

        ApiRecord record;
        try {
            record = service.fetchData(api);
        } catch (InvalidResponseException e) {
            System.err.println("Invalid response for " + apiType.getDisplayName() + ": " + e.getMessage());
            return;
        } catch (SendingHttpRequestException e) {
            System.err.println("Failed to send request " + apiType.getDisplayName() + ": " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Failed parse response " + apiType.getDisplayName() + ": " + e.getMessage());
            return;
        }

        FileFormat format = menu.selectFileFormat();
        FileMode fileMode = menu.selectFileMode();

        String fileName = menu.getFileName();

        DataSaver saver = SaverFactory.newSaver(format);

        saver.save(record, fileName + "." + format.getFormat(), fileMode);
        boolean read = menu.wantToDo("\nDo you want to read the file?");
        if (read) {
            ReadMode readMode = menu.selectReadMode();
            if (readMode == ReadMode.FULL) {
                saver.read(fileName + "." + format.getFormat(), null);
            } else {
                ApiType readApiType = menu.selectApi();
                saver.read(fileName + "." + format.getFormat(), readApiType.getCodeName());
            }
        }
    }

    private static PollService startMode(Menu menu, ApiService service, PollService scheduler) throws InvalidUserInputException, IOException {
        if (scheduler != null && scheduler.isRunning()) {
            System.out.println("Polling is already running. Stop it first.");
            return scheduler;
        }

        int        threads  = menu.getThreadCount();
        long       interval = menu.getIntervalSeconds();
        List<ApiType> apis  = menu.selectMultipleApis();
        FileFormat format   = menu.selectFileFormat();
        FileMode   fileMode = menu.selectFileMode();
        String     fileName = menu.getFileName();

        DataSaver saver = SaverFactory.newSaver(format);

        scheduler = new PollService(
                threads, interval, service, saver,
                fileName + "." + format.getFormat(), fileMode
        );

        scheduler.start(apis);

        boolean read = menu.wantToDo("\nDo you want to read the file?");
        if (read) {
            ReadMode readMode = menu.selectReadMode();
            if (readMode == ReadMode.FULL) {
                saver.read(fileName + "." + format.getFormat(), null);
            } else {
                ApiType readApiType = menu.selectApi();
                saver.read(fileName + "." + format.getFormat(), readApiType.getCodeName());
            }
        }

        return scheduler;
    }

    private static PollService stopMode(PollService scheduler) {
        if (scheduler == null || !scheduler.isRunning()) {
            System.out.println("Polling is not running.");
            return scheduler;
        }

        scheduler.stop();

        return null;
    }
}
