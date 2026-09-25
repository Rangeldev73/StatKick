package dev.rangel.statkick.integration.footballapi.exception;

public class ExternalServiceUnavailableException extends IntegrationException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }
}