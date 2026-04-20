package org.example.exceptions.request;

public class InvalidResponseException extends Exception {
    public InvalidResponseException(String message, int status) {
        super(message + status);
    }
}
