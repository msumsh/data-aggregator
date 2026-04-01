package org.example.model;

public enum FileFormat {
    JSON("json"),
    CSV("csv");

    private final String format;

    FileFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static FileFormat parseInput(String input) {
        for (FileFormat format : FileFormat.values()) {
            if (format.name().equalsIgnoreCase(input) ||
            format.format.equalsIgnoreCase(input)) {
                return format;
            }
        }

        throw new IllegalArgumentException("Output file format is not supported: " + input);
    }
}
