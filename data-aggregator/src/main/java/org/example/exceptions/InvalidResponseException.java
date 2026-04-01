package org.example.exceptions;

public class InvalidResponseException extends Exception {
    public InvalidResponseException(String message, int status) {
        super(message + status);
    }
}
