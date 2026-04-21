package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
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

        return Response.status(500)
                .entity("RAW 500 ERROR: " + exception.getMessage())
                .build();
    }
}
