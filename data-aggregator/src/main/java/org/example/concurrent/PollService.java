package org.example.concurrent;

import org.example.api.*;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.exceptions.request.InvalidResponseException;
import org.example.exceptions.request.SendingHttpRequestException;
import org.example.model.*;
import org.example.model.apitype.ApiType;
import org.example.model.factories.ApiFactory;
import org.example.model.modes.FileMode;
import org.example.output.DataSaver;
import org.example.service.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PollService {
    private final int maxThreads;
    private final long intervalSeconds;

    private final ApiService apiService;
    private final ApiFactory apiFactory;
    private final DataSaver dataSaver;
    private final String fileName;
    private final FileMode initialMode;

    private final AtomicBoolean firstWriteDone = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ScheduledExecutorService scheduler;

    public PollService(int maxThreads, long intervalSeconds,
                       ApiService apiService, ApiFactory apiFactory, DataSaver dataSaver,
                       String fileName, FileMode initialMode) {
        this.maxThreads = maxThreads;
        this.intervalSeconds = intervalSeconds;
        this.apiService = apiService;
        this.apiFactory = apiFactory;
        this.dataSaver = dataSaver;
        this.fileName = fileName;
        this.initialMode = initialMode;
    }

    public void start(List<ApiType> apiTypes) {
        if (running.get()) return;
        running.compareAndSet(false, true);

        scheduler = Executors.newScheduledThreadPool(maxThreads);

        Map<ApiType, Integer> apiCounts = new HashMap<>();
        for (ApiType type : apiTypes) {
            apiCounts.merge(type, 1, Integer::sum);
        }

        for (Map.Entry<ApiType, Integer> entry : apiCounts.entrySet()) {
            ApiType type = entry.getKey();
            scheduleNext(type, 0);
        }

        System.out.println("Polling started. Threads: " + maxThreads + ", interval: " + intervalSeconds + "s");
    }

    private void scheduleNext(ApiType type, long initDelay) {
        if (!running.get()) return;
        scheduler.schedule(() -> {
            if (!running.get()) return;
            pollAndSave(type);
            scheduleNext(type, intervalSeconds);
        }, initDelay, TimeUnit.SECONDS);
    }

    public void stop() {
        if (!running.get()) return;
        running.compareAndSet(true, false);

        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(15, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Polling stopped.");
    }

    public boolean isRunning() {
        return running.get();
    }

    private void pollAndSave(ApiType apiType) {
        ApiSource api = apiFactory.newSource(apiType);

        try {
            ApiRecord record = apiService.fetchData(api);
            FileMode mode = firstWriteDone.compareAndSet(false, true)
                    ? initialMode
                    : FileMode.APPEND;
            dataSaver.save(record, fileName, mode);
        } catch (InvalidResponseException | SendingHttpRequestException | IOException e) {
            System.err.println(apiType.getDisplayName() + " error: " + e.getMessage());
        } catch (SavingJsonException | SavingCsvException e) {
            System.err.println(apiType.getDisplayName() + " save error: " + e.getMessage());
        }
    }
}