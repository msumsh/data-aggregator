package org.example.execution;

import org.example.api.ApiSource;
import org.example.concurrent.PollService;
import org.example.exceptions.data.SavingCsvException;
import org.example.exceptions.data.SavingJsonException;
import org.example.exceptions.request.InvalidResponseException;
import org.example.exceptions.request.SendingHttpRequestException;
import org.example.model.ApiRecord;
import org.example.model.apitype.ApiType;
import org.example.model.factories.ApiFactory;
import org.example.model.factories.PollServiceFactory;
import org.example.model.format.FileFormat;
import org.example.model.modes.FileMode;
import org.example.output.DataSaver;
import org.example.service.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JobExecutor {
    private final ApiService apiService;
    private final ApiFactory apiFactory;
    private final PollServiceFactory pollServiceFactory;

    public JobExecutor() {
        this(new ApiService(), new ApiFactory(), new PollServiceFactory());
    }

    public JobExecutor(ApiService apiService, ApiFactory apiFactory, PollServiceFactory pollServiceFactory) {
        this.apiService = apiService;
        this.apiFactory = apiFactory;
        this.pollServiceFactory = pollServiceFactory;
    }

    public void executeSingle(List<ApiType> apiTypes, FileFormat format, FileMode fileMode, String baseFileName, DataSaver saver)
        throws SavingCsvException,
            SavingJsonException,
            IOException {
        List<ApiRecord> allRecords = getApiRecords(apiTypes);
        String fileName = baseFileName + "." + format.getFormat();
        for (int i = 0; i < allRecords.size(); i++) {
            FileMode currMode = (i == 0 && fileMode != FileMode.APPEND) ?
                    FileMode.CREATE :
                    FileMode.APPEND;
            saver.save(allRecords.get(i), fileName, currMode);
        }
    }
    
    public PollService startMulti(int threads, long interval, List<ApiType> apiTypes, FileFormat format, FileMode fileMode, String baseFileName, DataSaver saver){
        String fileName = baseFileName + "." + format.getFormat();
        PollService pollService = pollServiceFactory.newPollService(threads, interval, apiService, apiFactory, saver, fileName, fileMode);
        pollService.start(apiTypes);

        return pollService;
    }

    public void stopMulti(PollService scheduler) {
        if (scheduler == null || !scheduler.isRunning()) {
            System.out.println("Polling is not running.");
            return;
        }

        scheduler.stop();
    }

    private List<ApiRecord> getApiRecords(List<ApiType> apiTypes) {
        List<ApiRecord> allRecords = new ArrayList<>();

        for (ApiType apiType : apiTypes) {
            ApiSource api = apiFactory.newSource(apiType);

            ApiRecord record;
            try {
                record = apiService.fetchData(api);
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
