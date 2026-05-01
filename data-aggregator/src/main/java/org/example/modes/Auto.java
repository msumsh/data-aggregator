package org.example.modes;

import org.example.cli.ArgParser;
import org.example.concurrent.PollService;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.execution.JobExecutor;
import org.example.model.apitype.ApiType;
import org.example.model.factories.SaverFactory;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.output.DataSaver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Auto {
    private static final String DEFAULT_FILE_NAME = "result";
    private final SaverFactory saverFactory;
    private final JobExecutor exec;

    public Auto() {
        this(new SaverFactory(), new JobExecutor());
    }

    public Auto(SaverFactory saverFactory, JobExecutor exec) {
        this.saverFactory = saverFactory;
        this.exec = exec;
    }

    public void runAuto(String[] args) throws
            SavingJsonException,
            SavingCsvException,
            IOException,
            InterruptedException {
        ArgParser argParser = new ArgParser(args);

        List<ApiType> apiTypes = argParser.getListOfApiTypes();

        FileFormat format = argParser.getFileFormat();

        DataSaver saver = saverFactory.newSaver(format);

        System.out.println("Working automatically with:");
        for (ApiType type : apiTypes) {
            System.out.println(type.getDisplayName());
        }
        System.out.println("Saving to " + format.getFormat());

        int threads = argParser.getNumOfThreads();
        long interval = argParser.getPollingInterval();

        long duration = argParser.getPollDuration();

        if (threads > 1) {
            PollService pollService = exec.startMulti(threads,
                    interval,
                    apiTypes,
                    format,
                    FileMode.CREATE,
                    DEFAULT_FILE_NAME,
                    saver);

            System.out.println("Polling for " + duration + " s");

            CountDownLatch latch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (pollService.isRunning()) {
                    exec.stopMulti(pollService);
                }
                latch.countDown();
            }));

            if (!latch.await(duration, TimeUnit.SECONDS)) {
                exec.stopMulti(pollService);
            }
        } else {
            exec.executeSingle(apiTypes,
                    format,
                    FileMode.CREATE,
                    DEFAULT_FILE_NAME,
                    saver);
        }

        System.out.println("===== Done ======");
    }
}