package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
    Maps RoomNotEmptyException through the HTTP 409 which results in a Conflict response

    Triggered when 'DELETE /api/v1/rooms/{roomId}' is called on a room that still has sensors assigned to it
    @Provider tells JAX-RS to automatically discover and register this class as an exception mapper during package scanning
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        error.put("hint", "Remove or reassign all sensors from this room before deleting it.");

        return Response
                .status(Response.Status.CONFLICT)   // HTTP 409
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
