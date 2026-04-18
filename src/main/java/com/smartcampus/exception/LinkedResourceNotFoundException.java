package com.smartcampus.exception;


//This exception is thrown when a client tries to create a Sensor with a roomId that doesn't exist
//The JSON payload itself is valid but the problem is a missing referenced resource
//Maps to HTTP 422 Unprocessable Entity
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
