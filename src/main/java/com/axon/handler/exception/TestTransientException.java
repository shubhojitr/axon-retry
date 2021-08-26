package com.axon.handler.exception;

import org.axonframework.common.AxonTransientException;

public class TestTransientException extends AxonTransientException {

    public TestTransientException(String message) {
        super(message);
    }

    public TestTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}
