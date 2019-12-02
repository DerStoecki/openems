package io.openems.common.exceptions;

public class HeatToLowException extends OpenemsException {
    public HeatToLowException(String message) {
        super(message);
    }

    public HeatToLowException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeatToLowException(Throwable cause) {
        super(cause);
    }
}
