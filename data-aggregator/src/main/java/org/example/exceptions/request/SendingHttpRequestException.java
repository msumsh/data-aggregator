package org.example.exceptions.request;

public class SendingHttpRequestException extends Exception {
    public SendingHttpRequestException(String message) {
        super(message);
    }
}
