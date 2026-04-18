package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

//Maps SensorUnavailableException through the HTTP 403 Forbidden
//Triggered when 'POST /api/v1/sensors/{sensorId}/readings' is called on a sensor whose status is "MAINTENANCE" or "OFFLINE"
//A sensor in maintenance mode is physically disconnected from the network and cannot receive or transmit data

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", exception.getMessage());
        error.put("hint", "Only sensors with status 'ACTIVE' can accept new readings.");

        return Response
                .status(Response.Status.FORBIDDEN)  // HTTP 403
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
