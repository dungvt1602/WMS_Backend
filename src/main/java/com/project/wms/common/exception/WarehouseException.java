package com.project.wms.common.exception;

public class WarehouseException extends RuntimeException {

    public WarehouseException(String message) {
        super(message);
    }

    public WarehouseException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseException(Throwable cause) {
        super(cause);
    }
}
