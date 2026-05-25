// Cohesion Level: Logical Cohesion
// Reason Why: 
// Serves as a logical wrapper to group various business rule violations across different domains

package com.aims.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
