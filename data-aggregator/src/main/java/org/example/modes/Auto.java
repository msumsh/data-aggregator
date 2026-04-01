package org.example.modes;

import org.example.api.ApiSource;
import org.example.api.CountriesApi;
import org.example.api.MuseumApi;
import org.example.api.WeatherApi;
import org.example.exceptions.InvalidResponseException;
import org.example.exceptions.SavingCsvException;
import org.example.exceptions.SavingJsonException;
import org.example.exceptions.SendingHttpRequestException;
import org.example.model.ApiRecord;
import org.example.model.ApiType;
import org.example.model.FileFormat;
import org.example.model.FileMode;
import org.example.output.DataSaver;
import org.example.service.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Auto {
    private static final String DEFAULT_FILE_NAME = "result";

    public static void runAuto(String[] args) throws
            SavingJsonException,
            SavingCsvException,
            IOException {
        if (args.length >= 2) {
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

            String fileName = DEFAULT_FILE_NAME + "." + format.getFormat();

            List<ApiRecord> allRecords = getApiRecords(apiTypes);

            DataSaver saver = new DataSaver(); 
            for (int i = 0; i < allRecords.size(); i++) {
                if (i > 0) {
                    saver.save(allRecords.get(i), fileName, format, FileMode.APPEND);
                } else {
                    saver.save(allRecords.get(i), fileName, format, FileMode.CREATE);
                }
            }

            System.out.println("===== Done ======");
        } else {
            throw new IllegalArgumentException("Not enough arguments");
        }
    }

    private static List<ApiRecord> getApiRecords(List<ApiType> apiTypes) {
        List<ApiRecord> allRecords = new ArrayList<>();

        ApiService service = new ApiService();

        for (ApiType apiType : apiTypes) {
            ApiSource api = switch (apiType) {
                case WEATHER -> new WeatherApi();
                case MUSEUM -> new MuseumApi();
                case COUNTRIES -> new CountriesApi();
            };

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
