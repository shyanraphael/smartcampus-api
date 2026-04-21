package com.smartcampus.model;

import java.util.UUID;

/*
 SensorReading represents a single historical data point recorded by a Sensor.
 Every time a sensor captures a measurement, a new SensorReading is created.
 */
public class SensorReading {

    private String id;         // Unique reading ID (UUID)
    private long timestamp;    // When the reading was taken (milliseconds since epoch)
    private double value;      // The actual measured value

    //Constructors

    public SensorReading() {}

    public SensorReading(double value) {
        // Auto-generate a unique ID
        this.id = UUID.randomUUID().toString();   
        // Capture current time
        this.timestamp = System.currentTimeMillis(); 
        this.value = value;
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    //Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
