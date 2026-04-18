package com.smartcampus.exception;

//SensorUnavailableException is thrown when a client tries to POST a new reading to a sensor that is in "MAINTENANCE" or "OFFLINE" status.
//A sensor in maintenance is physically disconnected and cannot receive data by mapping to HTTP 403 Forbidden
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is currently in '" + status + "' status " +
              "and cannot accept new readings. Only ACTIVE sensors can record data.");
    }
}
