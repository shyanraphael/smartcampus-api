package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

//This Maps ResourceNotFoundException through the HTTP 404 which results in a Not Found response
//Triggered when a client requests a specific Room or Sensor by ID but that ID does not exist in the system
@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        error.put("error", "Not Found");
        error.put("message", exception.getMessage());

        return Response
                .status(Response.Status.NOT_FOUND)  // HTTP 404
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
