package com.smartcampus.exception;


//Exception thrown when a requested resource (Room or Sensor) is not found by its ID
//This maps to HTTP 404 Not Found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
