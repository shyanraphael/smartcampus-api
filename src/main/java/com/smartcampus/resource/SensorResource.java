package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

//SensorResource handles all HTTP operations for the /api/v1/sensors path
//It also acts as a "Sub-Resource Locator" when the path includes '/sensors/{sensorId}/readings'
    //it creates and returns a SensorReadingResource object to handle those requests

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors
    // GET /api/v1/sensors?type=CO2

    // WHY QUERY PARAM vs PATH PARAM?
        // Query params are for FILTERING a collection and they're optional
        // Path segments (/sensors/type/CO2) are for identifying a specific resource
    // Filtering is not about a specific resource so query params are the right choice
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());

        // If a type filter was provided, filter the list
        if (type != null && !type.isBlank()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("count", sensorList.size());
        response.put("filteredByType", type != null ? type : "none");
        response.put("sensors", sensorList);

        return Response.ok(response).build(); // HTTP 200 OK
    }


    // GET /api/v1/sensors/{sensorId}
    //returning details of a single sensor by its ID
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sensor", sensor);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/sensors/" + sensorId);
        links.put("readings", "/api/v1/sensors/" + sensorId + "/readings");
        links.put("room", "/api/v1/rooms/" + sensor.getRoomId());
        links.put("allSensors", "/api/v1/sensors");
        response.put("_links", links);

        return Response.ok(response).build(); // HTTP 200 OK
    }

    // POST /api/v1/sensors - Creates a new sensor

    // KEY VALIDATION: The roomId in the request body must exist
    // @Consumes(APPLICATION_JSON): Only accepts JSON.
    // If a client sends text/plain or XML, JAX-RS automatically returns
    // HTTP 415 Unsupported Media Type before even calling this method
    @POST
    public Response createSensor(Sensor sensor) {
        //validating required fields
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request",
                                   "message", "Sensor 'id' is required."))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request",
                                   "message", "Sensor 'roomId' is required."))
                    .build();
        }

        // checking if a sensor with this ID already exists
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("status", 409, "error", "Conflict",
                                   "message", "A sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // INTEGRITY CHECK: Does the referenced room actually exist?
        // The JSON is valid, but the roomId inside it points to nothing.
        // This is why 422 is better than 404 here:
        //   - 404 means "the URL you requested doesn't exist"
        //   - 422 means "your request was understood but contains invalid data"
        // The URL /api/v1/sensors IS valid - the problem is the DATA inside the body.
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Cannot create sensor: the roomId '" + sensor.getRoomId() +
                "' does not reference any existing room in the system."
            );
        }

        //setting defaults if not provided by client
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            sensor.setType("Unknown");
        }

        //saving the sensor
        store.getSensors().put(sensor.getId(), sensor);

        //adding the sensor's ID to the room's sensorIds list
        room.getSensorIds().add(sensor.getId());

        //reponse for build succession
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor created successfully.");
        response.put("sensor", sensor);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/sensors/" + sensor.getId());
        links.put("readings", "/api/v1/sensors/" + sensor.getId() + "/readings");
        links.put("room", "/api/v1/rooms/" + sensor.getRoomId());
        response.put("_links", links);

        return Response.status(Response.Status.CREATED).entity(response).build(); // HTTP 201 Created
    }


    // DELETE /api/v1/sensors/{sensorId}
    // Removes a sensor and also removes it from the room's sensorIds list
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        //remove this sensor's ID from the room's sensorIds list
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        //remove the sensor itself
        store.getSensors().remove(sensorId);

        // remove all readings for this sensor
        store.getReadings().remove(sensorId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor '" + sensorId + "' has been successfully deleted.");
        response.put("deletedSensorId", sensorId);
        // HTTP 200 OK
        return Response.ok(response).build(); 
    }

    // SUB-RESOURCE LOCATOR: '/api/v1/sensors/{sensorId}/readings'
    // @GET or @POST on that returned object.
    // This delegates all '/readings' handling to SensorReadingResource
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        //creaintg and returning a SensorReadingResource instance for this specific sensor
        return new SensorReadingResource(sensorId);
    }
}
