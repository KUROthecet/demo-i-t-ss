package com.aims.dto.response;

public class ApiError extends com.aims.exception.ApiError {

    public ApiError() {
        super();
    }

    public ApiError(int status, String error, String message, String path) {
        super(status, error, message, path);
    }
}
