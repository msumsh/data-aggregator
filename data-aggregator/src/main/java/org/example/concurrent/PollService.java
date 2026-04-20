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
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PollService {
    private final int maxThreads;
    private final long intervalSeconds;

    private final ApiService apiService;
    private final DataSaver dataSaver;
    private final String fileName;
    private final FileMode initialMode;

    private final AtomicBoolean firstWriteDone = new AtomicBoolean(false);
    private volatile boolean running = false;

    private ScheduledExecutorService scheduler;

    private final Map<ApiType, Integer> apisCount = new ConcurrentHashMap<>();

    public PollService(int maxThreads, long intervalSeconds,
                       ApiService apiService, DataSaver dataSaver,
                       String fileName, FileMode initialMode) {
        this.maxThreads = maxThreads;
        this.intervalSeconds = intervalSeconds;
        this.apiService = apiService;
        this.dataSaver = dataSaver;
        this.fileName = fileName;
        this.initialMode = initialMode;
    }

    public void start(List<ApiType> apiTypes) {
        if (running) return;
        running = true;

        scheduler = Executors.newScheduledThreadPool(maxThreads);

        for (ApiType apiType : apiTypes) {
            int index = apisCount.getOrDefault(apiType, 0);
            apisCount.put(apiType, index + 1);
        }

        for (Map.Entry<ApiType, Integer> entry : apisCount.entrySet()) {
            ApiType type = entry.getKey();
            int count = entry.getValue();
            scheduleApiSeries(type, count, 0);
        }

        System.out.println("Polling started. Threads: " + maxThreads + ", interval: " + intervalSeconds + "s");
    }

    public void stop() {
        if (!running) return;
        running = false;

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
        return running;
    }

    private void pollAndSave(ApiType apiType) {
        ApiSource api = ApiFactory.newSource(apiType);

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

    private void scheduleApiSeries(ApiType apiType, int remainingRequests, long delay) {
        scheduler.schedule(() -> {
            if (!running) return;

            pollAndSave(apiType);

            if (remainingRequests > 1) {
                scheduleApiSeries(apiType, remainingRequests - 1, intervalSeconds);
            } else {
                scheduleApiSeries(apiType, apisCount.getOrDefault(apiType, 1), intervalSeconds);
            }
        }, delay, TimeUnit.SECONDS);
    }
}