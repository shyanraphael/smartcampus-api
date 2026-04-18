package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
    Maps LinkedResourceNotFoundException through the HTTP 422 Unprocessable Entity
    Triggered when: POST /api/v1/sensors is called with a roomId that does not exist in the system
    
    WHY 422 AND NOT 404?
        - 404 "Not Found" means the URL endpoint itself doesn't exist
            eg: GET /api/v1/nonexistent -> 404 because that URL isn't real
        - 422 "Unprocessable Entity" means the URL is valid and the JSON is valid, but the CONTENT inside the JSON is significantly wrong
            eg: POST /api/v1/sensors with {"name":"Temp","roomId":999} -> 422 cause the roomId references to a  non-existent room
    The request body references a roomId that doesn't exist.
    The server understood what you sent — it just can't process it.
    422 is far more accurate and informative to API consumers
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());
        error.put("hint", "Ensure the roomId you provided references an existing room.");

        return Response
        // HTTP 422 Unprocessable Entity
                .status(422)                        
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
