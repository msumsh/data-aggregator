package org.example.modes;

import org.example.api.ApiSource;
import org.example.api.CountriesApi;
import org.example.api.MuseumApi;
import org.example.api.WeatherApi;
import org.example.cli.Menu;
import org.example.exceptions.*;
import org.example.model.*;
import org.example.output.DataSaver;
import org.example.output.DataSaverCsv;
import org.example.output.DataSaverJson;
import org.example.service.ApiService;

import java.io.IOException;

public class Interactive {
    public static void runInteractive(Menu menu) throws
            InvalidUserInputException,
            IOException,
            SavingJsonException,
            SavingCsvException {
        ApiService service = new ApiService();
        boolean next = true;
        while (next) {
            ApiType apiType = menu.selectApi();
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

            FileFormat format = menu.selectFileFormat();
            FileMode fileMode = menu.selectFileMode();

            String fileName = menu.getFileName();

            DataSaver saver = switch (format) {
                case JSON -> new DataSaverJson();
                case CSV -> new DataSaverCsv();
            };

            saver.save(record, fileName + "." + format.getFormat(), fileMode);
            boolean read = menu.wantToViewFile();
            if (read) {
                ReadMode readMode = menu.selectReadMode();
                if (readMode == ReadMode.FULL) {
                    saver.read(fileName + "." + format.getFormat(), null);
                } else {
                    ApiType readApiType = menu.selectApi();
                    saver.read(fileName + "." + format.getFormat(), readApiType.getCodeName());
                }
            }

            next = menu.wantToContinue();
        }
    }
}
