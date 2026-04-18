package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


// DiscoveryResource handles GET /api/v1
//This is the "root" endpoint of the API. When a client visits the base URL, they receive metadata about the API version contact info, and links toall available resource collections
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        //Build the response as a nested Map as Jackson will automatically convert it to JSON
        Map<String, Object> response = new HashMap<>();

        //API version info
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("status", "online");

        //admin contact details
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Smart Campus Admin");
        contact.put("email", "admin@smartcampus.ac.uk");
        contact.put("department", "Facilities & Building Management");
        response.put("contact", contact);

        // HATEOAS links which tells clients where to find resources
        // clients follow these links
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        //helpful links for navigating the API
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorReadings", "/api/v1/sensors/{sensorId}/readings");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}
