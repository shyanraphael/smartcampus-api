package com.smartcampus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
    DataStore is a singleton class that acts as our in-memory database
    As we cannot use a real database, we store everything in HashMaps

    WHY use SINGLETON?
        JAX-RS creates a new instance of each resource class per request by default
        If each resource class held its own data, the data would be lost between requests
        By using a singleton DataStore, all resource classes share the SAME data throughttout the application lifecycle

    WHY ConcurrentHashMap?
        Multiple requests can arrive at the same time (concurrent requests).
        A regular HashMap is not thread-safe cause two threads writing at the same time can corrupt data therfore ConcurrentHashMap handles this safely.
 */
public class DataStore {

    // The single instance of this class which resulsts in Singleton pattern
    private static DataStore instance;

    // All rooms stored by their ID: "LIB-301" - Room object
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // All sensors stored by their ID: "TEMP-001" - Sensor object
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // All readings stored by sensors ID: "TEMP-001" - [reading1, reading2, etc.]
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor - nobody can call "new DataStore()" from outside
    private DataStore() {
        seedData(); // Pre-loading sample data
    }

    //Returns the single shared instance of DataStore
    //synchronized ensures only one thread can enter at a timefor thread safety
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }


    //Pre-loads sample rooms and sensors so the API is not empty when you start it which makes tesing easier
    private void seedData() {
        // Createing sample rooms
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room room3 = new Room("HALL-A", "Main Lecture Hall", 200);

        // Creating sample sensors
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LIB-301");
        Sensor sensor3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 15.0, "LAB-101");
        Sensor sensor4 = new Sensor("TEMP-002", "Temperature", "ACTIVE", 19.0, "HALL-A");
        Sensor sensor5 = new Sensor("LIGHT-001", "Light", "OFFLINE", 0.0, "LAB-101");

        //linking sensors to rooms by adding sensor IDs to the room list of sensor IDs
        room1.getSensorIds().add("TEMP-001");
        room1.getSensorIds().add("CO2-001");
        room2.getSensorIds().add("OCC-001");
        room2.getSensorIds().add("LIGHT-001");
        room3.getSensorIds().add("TEMP-002");

        // Saving rooms to the map
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        //sving sensors to map
        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        sensors.put(sensor3.getId(), sensor3);
        sensors.put(sensor4.getId(), sensor4);
        sensors.put(sensor5.getId(), sensor5);

        //creating sample reading for TEMP-001 sensor
        List<SensorReading> temp001Readings = new ArrayList<>();
        temp001Readings.add(new SensorReading("READ-001", System.currentTimeMillis() - 3600000, 21.0));
        temp001Readings.add(new SensorReading("READ-002", System.currentTimeMillis() - 1800000, 22.0));
        temp001Readings.add(new SensorReading("READ-003", System.currentTimeMillis(), 22.5));
        readings.put("TEMP-001", temp001Readings);
    }

    //GETTERS for each map
    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getReadings() { return readings; }
}
