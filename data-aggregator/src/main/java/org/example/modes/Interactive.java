package org.example.modes;

import org.example.cli.Menu;
import org.example.concurrent.PollService;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.exceptions.input.InvalidUserInputException;
import org.example.execution.JobExecutor;
import org.example.model.*;
import org.example.model.apitype.ApiType;
import org.example.model.factories.SaverFactory;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.model.modes.PollingMode;
import org.example.model.modes.ReadMode;
import org.example.output.DataSaver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Interactive {
    private final SaverFactory saverFactory;
    private final JobExecutor exec;

    public Interactive() {
        this(new SaverFactory(), new JobExecutor());
    }

    public Interactive(SaverFactory saverFactory, JobExecutor exec) {
        this.saverFactory = saverFactory;
        this.exec = exec;
    }

    public void runInteractive(Menu menu) throws
            InvalidUserInputException,
            IOException,
            SavingJsonException,
            SavingCsvException {
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

                singleMode(menu);

                next = menu.wantToDo("\nDo you want to continue?");
            } else if (poll == PollingMode.MULTIPLE_START) {
                schedulerRef[0] = startMode(menu, schedulerRef[0]);
                next = menu.wantToDo("\nDo you want to continue?");
            }  else if (poll == PollingMode.MULTIPLE_STOP) {
                schedulerRef[0] = stopMode(schedulerRef[0]);
                next = menu.wantToDo("\nDo you want to continue?");
            }
        }

        if (schedulerRef[0] != null && schedulerRef[0].isRunning()) {
            schedulerRef[0] = stopMode(schedulerRef[0]);
        }
    }

    private void singleMode(Menu menu) throws InvalidUserInputException, SavingCsvException, SavingJsonException, IOException {
        ApiType apiType = menu.selectApi();
        FileFormat format = menu.selectFileFormat();
        FileMode fileMode = menu.selectFileMode();
        String fileName = menu.getFileName();

        DataSaver saver = saverFactory.newSaver(format);

        exec.executeSingle(Collections.singletonList(apiType), format,
                fileMode,
                fileName,
                saver);

        fileRead(menu, format, fileName, saver);
    }

    private PollService startMode(Menu menu, PollService scheduler) throws InvalidUserInputException, IOException {
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

        DataSaver saver = saverFactory.newSaver(format);

        scheduler = exec.startMulti(threads,
                interval,
                apis,
                format,
                fileMode,
                fileName,
                saver);

        fileRead(menu, format, fileName, saver);

        return scheduler;
    }

    private PollService stopMode(PollService scheduler) {
        exec.stopMulti(scheduler);
        return null;
    }

    private void fileRead(Menu menu, FileFormat format, String fileName, DataSaver saver) throws IOException, InvalidUserInputException {
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
}
