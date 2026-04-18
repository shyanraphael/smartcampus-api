package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;


//Room represents a physical room in the Smart Campus
//Each room can contain multiple sensors
public class Room {
    //unique identifier for the room
    private String id;
    //human-readable name for the room
    private String name;
    //maximum number of people allowed in the room
    private int capacity;
    // IDs of sensors in this room
    private List<String> sensorIds = new ArrayList<>();

    //constructors
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    //Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
