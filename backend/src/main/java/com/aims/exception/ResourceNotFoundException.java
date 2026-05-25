// Cohesion Level: Logical Cohesion
// Reason Why: 
// Groups distinct constructors for looking up resources by ID or string fields together 
// under the single logical category of database missing resource exceptions

package com.aims.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(resourceType + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(resourceType + " not found with " + field + ": " + value);
    }
}
