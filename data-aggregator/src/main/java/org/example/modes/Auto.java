package org.example.modes;

import org.example.api.ApiSource;
import org.example.concurrent.PollService;
import org.example.exceptions.request.InvalidResponseException;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.exceptions.request.SendingHttpRequestException;
import org.example.model.ApiRecord;
import org.example.model.apitype.ApiType;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.model.factories.ApiFactory;
import org.example.model.factories.SaverFactory;
import org.example.output.DataSaver;
import org.example.service.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Auto {
    private static final String DEFAULT_FILE_NAME = "result";

    public static void runAuto(String[] args) throws
            SavingJsonException,
            SavingCsvException,
            IOException,
            InterruptedException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments");
        } else {
            String[] apiNames = args[0].toLowerCase().split(",");
            List<ApiType> apiTypes = new ArrayList<>();

            for (String name : apiNames) {
                try {
                    ApiType api = ApiType.valueOf(name.trim().toUpperCase());
                    apiTypes.add(api);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown API " + name);
                }
            }

            String formatArg = args[1].trim().toLowerCase();
            FileFormat format;
            try {
                format = FileFormat.valueOf(formatArg.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown format " + formatArg);
            }

            System.out.println("Working automatically with:");
            for (ApiType type : apiTypes) {
                System.out.println(type.getDisplayName());
            }
            System.out.println("Saving to " + format.getFormat());

            int threads;
            long interval;
            try {
                threads = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
                interval = args.length >= 4 ? Long.parseLong(args[3]) : 0;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Threads and interval must be the numbers");
            }

            long duration = getDuration(args, threads, interval);

            String fileName = DEFAULT_FILE_NAME + "." + format.getFormat();

            DataSaver saver = SaverFactory.newSaver(format);

            ApiService service = new ApiService();

            if (threads > 1) {
                PollService scheduler = new PollService(
                        threads, interval, service, saver, fileName, FileMode.CREATE
                );

                scheduler.start(apiTypes);

                System.out.println("Polling for " + duration + " s");

                CountDownLatch latch = new CountDownLatch(1);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (scheduler.isRunning()) {
                        scheduler.stop();
                    }
                    latch.countDown();
                }));

                if (!latch.await(duration, TimeUnit.SECONDS)) {
                    scheduler.stop();
                }
            } else {
                List<ApiRecord> allRecords = getApiRecords(apiTypes, service);
                for (int i = 0; i < allRecords.size(); i++) {
                    if (i > 0) {
                        saver.save(allRecords.get(i), fileName, FileMode.APPEND);
                    } else {
                        saver.save(allRecords.get(i), fileName, FileMode.CREATE);
                    }
                }
            }

            System.out.println("===== Done ======");
        }
    }

    private static long getDuration(String[] args, int threads, long interval) {
        try {
            long duration = args.length >= 5 ? Long.parseLong(args[4]) : 30;

            if (threads < 1) {
                throw new IllegalArgumentException("Number of threads must be greater than 0");
            }

            if (interval < 0) {
                throw new IllegalArgumentException("Interval must be greater than 0");
            }

            return duration;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Duration must be a number");
        }
    }

    private static List<ApiRecord> getApiRecords(List<ApiType> apiTypes, ApiService service) {
        List<ApiRecord> allRecords = new ArrayList<>();

        for (ApiType apiType : apiTypes) {
            ApiSource api = ApiFactory.newSource(apiType);

            ApiRecord record;
            try {
                record = service.fetchData(api);
            } catch (InvalidResponseException e) {
                System.err.println("Invalid response for " + apiType.getDisplayName() + ": " + e.getMessage());
                continue;
            } catch (SendingHttpRequestException e) {
                System.err.println("Failed to send request " + apiType.getDisplayName() + ": " + e.getMessage());
                continue;
            } catch (IOException e) {
                System.err.println("Failed parse response " + apiType.getDisplayName() + ": " + e.getMessage());
                continue;
            }

            allRecords.add(record);
        }
        return allRecords;
    }
}