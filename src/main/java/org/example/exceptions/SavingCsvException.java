package org.example.exceptions;

public class SavingCsvException extends RuntimeException {
    public SavingCsvException(String message) {
        super(message);
    }
}
