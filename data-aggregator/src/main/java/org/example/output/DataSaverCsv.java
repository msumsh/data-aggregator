package org.example.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.example.exceptions.SavingCsvException;
import org.example.model.ApiRecord;
import org.example.model.FileMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DataSaverCsv extends DataSaver {
    @Override
    public void save(ApiRecord record, String fileName, FileMode mode) throws SavingCsvException, IOException {
        Path path = Paths.get(DEFAULT_DIR, fileName);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        boolean isAppend = (mode == FileMode.APPEND && Files.exists(path) && Files.size(path) > 0);

        Map<String, String> flatData = new LinkedHashMap<>();
        if (record.getData() != null) {
            flattenJson(record.getData(), record.getSource(), flatData);
        }

        if (!isAppend) {
            createMode(path, flatData, record);
        } else {
            appendMode(path, flatData, record);
        }
    }

    private List<String> buildHeaders(Map<String, String> flatData) {
        List<String> headers = new ArrayList<>(List.of("id", "source", "timestamp"));
        headers.addAll(flatData.keySet());

        return headers;
    }

    private String[] buildDataRow(ApiRecord record, Map<String, String> flatData, List<String> headers) {
        String[] row = new String[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            row[i] = switch (headers.get(i)) {
                case "id"        -> String.valueOf(record.getId());
                case "source"    -> record.getSource() != null ? record.getSource() : "";
                case "timestamp" -> record.getTimestamp() != null ? record.getTimestamp().toString() : "";
                default          -> flatData.getOrDefault(headers.get(i), "");
            };
        }
        return row;
    }

    private void flattenJson(JsonNode node, String prefix, Map<String, String> result) {
        if (node == null || node.isMissingNode()) {
            return;
        }

        if (!node.isContainerNode()) {
            result.put(prefix, node.asText());
            return;
        }

        if (node.isObject()) {
            Set<Map.Entry<String, JsonNode>> fields = node.properties();
            for (Map.Entry<String, JsonNode> field : fields) {
                String newPrefix = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                flattenJson(field.getValue(), newPrefix, result);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPrefix = prefix + "[" + i + "]";
                flattenJson(node.get(i), newPrefix, result);
            }
        }
    }

    public void read(String fileName, String apiSource) throws IOException {
        List<String> lines;
        Path path = Paths.get(DEFAULT_DIR, fileName);

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new IOException("failed to read the file: " + e.getMessage());
        }

        System.out.println("\n===== File Content: " + fileName + " =====");

        if (fileName.endsWith(".json") && apiSource != null && !apiSource.isEmpty()) {
            List<ApiRecord> records = parseJsonToList(Files.readString(path));
            for (ApiRecord record : records) {
                if (record.getSource() != null && record.getSource().equals(apiSource)) {
                    String json = objectMapper.writeValueAsString(record);
                    System.out.println(json);
                }
            }
        } else {
            for (String line : lines) {
                if (apiSource == null || apiSource.isEmpty()) {
                    System.out.println(line);
                }  else if (line.contains(apiSource) && fileName.endsWith(".csv")) {
                    System.out.println(line);
                }
            }
        }

        System.out.println("===== End of File =====\n");
    }

    @Override
    protected void createMode(Path path, Map<String, String> flatData, ApiRecord record)throws SavingCsvException {
        List<String> headers = buildHeaders(flatData);
        String[] dataRow = buildDataRow(record, flatData, headers);

        try (CSVWriter writer = new CSVWriter(
                Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            writer.writeNext(headers.toArray(new String[0]));
            writer.writeNext(dataRow);
        } catch (IOException e) {
            throw new SavingCsvException("I/O error saving to CSV: " + e.getMessage());
        }
    }

    @Override
    protected void appendMode(Path path, Map<String, String> flatData, ApiRecord record) throws SavingCsvException {
        List<String[]> existingRows;
        try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(Files.newBufferedReader(path))) {
            existingRows = reader.readAll();
        } catch (IOException | CsvException e) {
            throw new SavingCsvException("Failed to parse CSV: " + e.getMessage());
        }

        List<String> headers = new ArrayList<>(Arrays.asList(existingRows.getFirst()));

        boolean headersExtended = false;
        for (String key : flatData.keySet()) {
            if (!headers.contains(key)) {
                headers.add(key);
                headersExtended = true;
            }
        }

        String[] dataRow = buildDataRow(record, flatData, headers);

        if (headersExtended) {
            existingRows.set(0, headers.toArray(new String[0]));
            existingRows.add(dataRow);
            try (CSVWriter writer = new CSVWriter(
                    Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {
                writer.writeAll(existingRows);
            }  catch (IOException e) {
                throw new SavingCsvException("I/O error saving to CSV: " + e.getMessage());
            }
        } else {
            try (CSVWriter writer = new CSVWriter(
                    Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {
                writer.writeNext(dataRow);
            }  catch (IOException e) {
                throw new SavingCsvException("I/O error saving to CSV: " + e.getMessage());
            }
        }
    }
}
