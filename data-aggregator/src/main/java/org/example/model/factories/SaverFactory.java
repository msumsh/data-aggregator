package org.example.model.factories;

import org.example.model.format.FileFormat;
import org.example.output.DataSaver;
import org.example.output.savers.DataSaverCsv;
import org.example.output.savers.DataSaverJson;

public class SaverFactory {
    public static DataSaver newSaver(FileFormat format) {
        return switch (format) {
            case JSON -> new DataSaverJson();
            case CSV -> new DataSaverCsv();
        };
    }
}
