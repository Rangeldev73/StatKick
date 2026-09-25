package dev.rangel.statkick.integration.footballapi.exception;

public class ExternalTimeoutException extends IntegrationException {
    public ExternalTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}