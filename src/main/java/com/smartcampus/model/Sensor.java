package com.smartcampus.model;

//Sensor represents a physical sensor device installed in a Room
//Status can be either ACTIVE, MAINTENANCE, or OFFLINE
public class Sensor {

    //uniquw identifier for the sensor
    private String id;
    //category of the sensor
    private String type;
    //current operational status of the sensor (active, maintenance, offline)
    private String status;
    //most recent measurement value from the sensor
    private double currentValue;
    //ID of the Room this sensor belongs to
    private String roomId;

    //constructors
    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    //getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
