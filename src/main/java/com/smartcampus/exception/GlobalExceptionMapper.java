package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
    Maps ANY unhandled Throwable through the HTTP 500 Internal Server Error
    This is the global "safety net" as It catches every exception that is not handled by the more specific mappers above

    WHY NEVER EXPOSE STACK TRACES? (Cybersecurity answer for the report)

        Exposing a Java stack trace to external API consumers is a serious security vulnerability for several reasons:

            1. REVEALS TECHNOLOGY STACK: The trace shows class names, package structure,
            and library versions. Attackers use this to look up known CVEs (public vulnerabilities) for those exact versions

            2. REVEALS APPLICATION STRUCTURE: Class names and method names expose internal
            design. An attacker learns how your code is organized, making targeted attacks much easier to craft

            3. REVEALS FILE PATHS: Stack traces often include full file system paths, confirming the server's Operating System
            and directory structure

            4. REVEALS BUSINESS LOGIC ERRORS: The line number and exception message often reveal what the server was trying to do,
            which can indicate unvalidated input paths or exploitable logic

            5. REVEALS DATABASE/DATA STRUCTURE DETAILS: A NullPointerException on a HashMap lookup reveals the data structure
            being used internally.

    This mapper ensures clients always receive a safe, generic 500 message while the full error is logged in server-side for developers to investigate
 */
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
        // HTTP 500 internal server error
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
