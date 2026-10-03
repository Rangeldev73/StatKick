package dev.rangel.statkick.integration.footballapi.exception;

public class ExternalServiceUnavailableException extends IntegrationException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }
    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}