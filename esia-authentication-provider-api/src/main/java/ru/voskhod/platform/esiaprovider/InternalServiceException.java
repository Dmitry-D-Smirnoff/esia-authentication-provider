package ru.voskhod.platform.esiaprovider;

public class InternalServiceException extends RuntimeException {
    public InternalServiceException(Throwable cause) {
        super(cause);
    }

    public InternalServiceException() {
    }
}
