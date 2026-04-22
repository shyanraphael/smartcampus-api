package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        //Only developers see this
        // Log full details server-side for debugging
        System.err.println("[GLOBAL ERROR HANDLER] Caught unexpected exception:");
        System.err.println("  Type   : " + exception.getClass().getName());
        System.err.println("  Message: " + exception.getMessage());
        exception.printStackTrace(System.err);

        //Returns a safe, generic response to the client with no internal details
        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred on the server. Please contact the system administrator.");

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
