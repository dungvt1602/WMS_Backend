package com.project.wms.common.exception;

public class WarehouseZoneException extends RuntimeException {

    public WarehouseZoneException(String message) {
        super(message);
    }

    public WarehouseZoneException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseZoneException(Throwable cause) {
        super(cause);
    }
}
