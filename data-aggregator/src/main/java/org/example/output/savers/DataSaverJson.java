package org.example.output.savers;

import org.example.exceptions.data.SavingJsonException;
import org.example.model.ApiRecord;
import org.example.model.modes.FileMode;
import org.example.output.DataSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DataSaverJson extends DataSaver {
    @Override
    public void save(ApiRecord record, String fileName, FileMode mode) throws SavingJsonException, IOException {
        FILE_LOCK.lock();
        try {
            Path path = Paths.get(getBaseDir() + "json/", fileName);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            boolean append = (mode == FileMode.APPEND && Files.exists(path));

            if (append) {
                appendMode(path, null, record);
            } else {
                createMode(path, null, record);
            }
        } finally {
            FILE_LOCK.unlock();
        }
    }

    @Override
    public void read(String fileName, String apiSource) throws IOException {
        FILE_LOCK.lock();

        try {
            Path path = Paths.get(getBaseDir() + "json/", fileName);

            System.out.println("\n===== File Content: " + fileName + " =====");

            List<ApiRecord> records = parseJsonToList(Files.readString(path));
            if (apiSource != null && !apiSource.isEmpty()) {
                for (ApiRecord record : records) {
                    if (record.getSource() != null && record.getSource().equals(apiSource)) {
                        String json = objectMapper.writeValueAsString(record);
                        System.out.println(json);
                    }
                }
            } else {
                for (ApiRecord record : records) {
                    String json = objectMapper.writeValueAsString(record);
                    System.out.println(json);
                }
            }
        } finally {
            FILE_LOCK.unlock();
            System.out.println("===== End of File =====\n");
        }
    }

    @Override
    protected void createMode(Path path, Map<String, String> flatData, ApiRecord record) throws SavingJsonException {
        try {
            String json = objectMapper.writeValueAsString(record);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }  catch (IOException e) {
            throw new SavingJsonException("I/O error saving to JSON: " + e.getMessage());
        }
    }

    @Override
    protected void appendMode(Path path, Map<String, String> flatData, ApiRecord record) throws SavingJsonException {
        try {
            String existingJson = Files.readString(path);
            List<ApiRecord> existingRecords = parseJsonToList(existingJson);
            existingRecords.add(record);
            existingRecords.sort(Comparator.comparingLong(ApiRecord::getId));

            String json = objectMapper.writeValueAsString(existingRecords);
            Files.writeString(path, json, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new SavingJsonException("I/O error saving to JSON: " + e.getMessage());
        }
    }
}
