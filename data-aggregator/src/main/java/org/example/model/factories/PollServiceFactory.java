package org.example.model.factories;

import org.example.concurrent.PollService;
import org.example.model.modes.FileMode;
import org.example.output.DataSaver;
import org.example.service.ApiService;

public class PollServiceFactory {
    public PollService newPollService(int maxThreads, long intervalSeconds,
                                             ApiService apiService,ApiFactory apiFactory, DataSaver dataSaver,
                                             String fileName, FileMode initialMode) {
        return new PollService(
                maxThreads, intervalSeconds, apiService, apiFactory, dataSaver, fileName, initialMode
        );
    }
}
