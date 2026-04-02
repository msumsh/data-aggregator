package org.example.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.exceptions.SavingCsvException;
import org.example.exceptions.SavingJsonException;
import org.example.model.ApiRecord;
import org.example.model.FileMode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DataSaver {
    protected static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    protected static final String DEFAULT_DIR = "../data-aggregator/storage/";

    public DataSaver() {}
    public abstract void save(ApiRecord record, String fileName, FileMode mode) throws SavingCsvException, SavingJsonException, IOException;
    public abstract void read(String fileName, String apiSource) throws IOException;
    protected abstract void  createMode(Path path, Map<String, String> flatData, ApiRecord record) throws SavingCsvException, SavingJsonException;
    protected abstract void appendMode(Path path, Map<String, String> flatData, ApiRecord record) throws SavingCsvException, SavingJsonException;

    protected List<ApiRecord> parseJsonToList(String json) throws IOException {
        json = json.trim();

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        if (json.startsWith("{")) {
            ApiRecord singleObj = objectMapper.readValue(json, ApiRecord.class);
            return new ArrayList<>(List.of(singleObj));
        }

        if (json.startsWith("[")) {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ApiRecord.class)
            );
        }

        throw new IOException("failed to parse JSON file to List");
    }

}
