package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/*
    SensorReadingResource handles the '/readings' sub-path for a specific sensor
    Full path: '/api/v1/sensors/{sensorId}/readings'

    This is a SUB-RESOURCE class - it is NOT directly registered in the Application.
    Instead, SensorResource acts as a "locator" and hands off control to this class when the path contains '/readings'
    
    WHY SEPARATE CLASSES? (Sub-Resource Locator Pattern)
        If we put all logic in SensorResource, that class would become enormous
        By delegating to SensorReadingResource, each class has a single responsibility:
            - SensorResource handles /sensors and /sensors/{id}
            - SensorReadingResource handles /sensors/{id}/readings
    This is the Single Responsibility Principle
*/
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    //constructor receives the sensorId from the parent SensorResource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET '/api/v1/sensors/{sensorId}/readings' - returns all historical readings for this specific sensor
    @GET
    public Response getReadings() {
        //first verify if the sensor exists
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        //get readings list
        //returns empty if list doesnt exist
        List<SensorReading> sensorReadings = store.getReadings()
                .getOrDefault(sensorId, new ArrayList<>());

        Map<String, Object> response = new HashMap<>();
        response.put("sensorId", sensorId);
        response.put("count", sensorReadings.size());
        response.put("readings", sensorReadings);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/sensors/" + sensorId + "/readings");
        links.put("sensor", "/api/v1/sensors/" + sensorId);
        response.put("_links", links);

        return Response.ok(response).build(); // HTTP 200 OK
    }

    // POST '/api/v1/sensors/{sensorId}/readings' - adds a new reading for this specific sensor
    // SIDE EFFECT: This also updates the sensor's currentValue to the new reading
    @POST
    public Response addReading(SensorReading reading) {
        //verify the existence of the sesor first
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        // BUSINESS LOGIC: Sensors in MAINTENANCE or OFFLINE cannot accept readings
        // They are physically disconnected from the building network
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            // Caught by SensorUnavailableExceptionMapper -> 403 Forbidden
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Validate the reading has a value
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request",
                                   "message", "Request body with a 'value' field is required."))
                    .build();
        }

        // Auto-generate ID and timestamp if not provided by client
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Initialize the readings list for this sensor if it doesn't exist yet
        store.getReadings().computeIfAbsent(sensorId, k -> new ArrayList<>());

        //add the new reading to the history
        store.getReadings().get(sensorId).add(reading);

        // SIDE EFFECT: Update the sensor's currentValue to the latest reading
        sensor.setCurrentValue(reading.getValue());

        //build success response with HATEOAS links
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reading added successfully.");
        response.put("reading", reading);
        response.put("updatedSensorCurrentValue", reading.getValue());

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/sensors/" + sensorId + "/readings");
        links.put("sensor", "/api/v1/sensors/" + sensorId);
        response.put("_links", links);

        return Response.status(Response.Status.CREATED).entity(response).build(); // HTTP 201 Created
    }
}
