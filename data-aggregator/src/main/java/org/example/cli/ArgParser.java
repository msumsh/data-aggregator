package org.example.cli;

import org.example.model.apitype.ApiType;
import org.example.model.format.FileFormat;

import java.util.ArrayList;
import java.util.List;

public class ArgParser {
    private final String[] args;

    public ArgParser(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments");
        }
        this.args = args;
    }

    public List<ApiType> getListOfApiTypes() throws IllegalArgumentException {
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

        return apiTypes;
    }

    public FileFormat getFileFormat() throws IllegalArgumentException {
        String formatArg = args[1].trim().toLowerCase();
        FileFormat format;

        try {
            format = FileFormat.valueOf(formatArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown format " + formatArg);
        }

        return format;
    }

    public int getNumOfThreads() throws IllegalArgumentException {
        int threads;

        try {
            threads = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Threads must be the number");
        }

        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads must be greater than 0");
        }

        return threads;
    }

    public long getPollingInterval() throws IllegalArgumentException {
        long interval;

        try {
            interval = args.length >= 4 ? Long.parseLong(args[3]) : 0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Interval must be the number");
        }

        if (interval < 0) {
            throw new IllegalArgumentException("Interval must be non-negative number");
        }

        return interval;
    }

    public long getPollDuration() throws IllegalArgumentException {
        long duration;
        try {
            duration = args.length >= 5 ? Long.parseLong(args[4]) : 30;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Duration must be a number");
        }

        if (duration < 0) {
            throw new IllegalArgumentException("Duration must be non-negative number");
        }

        return duration;
    }
}
