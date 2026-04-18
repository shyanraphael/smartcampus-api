package com.smartcampus.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.io.IOException;
import java.net.URI;

//Main is the application entry point.
//It starts an embedded Grizzly HTTP server - no Tomcat or external server needed

public class Main {

    // The base URL the server will listen to
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws InterruptedException {
        // Configure Jersey to scan these packages for resource/provider classes
        final ResourceConfig config = new ResourceConfig()
                .packages(
                    // Scan resource classes
                    "com.smartcampus.resource",
                    // Scan exception mappers      
                    "com.smartcampus.exception"      
                )
                // Enable JSON support
                .register(JacksonFeature.class);     

        // Start the embedded Grizzly HTTP server
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://0.0.0.0:8080/api/v1"),
                config
        );
        System.out.println("---Smart Campus API Server Started!---");
        System.out.println(" ");
        System.out.println("  01) Base URL  : http://localhost:8080/api/v1");
        System.out.println("  02) Discovery : http://localhost:8080/api/v1/");
        System.out.println("  03) Rooms     : http://localhost:8080/api/v1/rooms");
        System.out.println("  04)Sensors   : http://localhost:8080/api/v1/sensors");
        System.out.println(" ");
        System.out.println("  Press CTRL+C to stop the server.");

        // Keep the server running until the user presses CTRL+C to shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.shutdownNow();
            System.out.println("Server stopped. Thank You and Goodbye!");
        }));

        // Block the main thread so the server keeps running
        Thread.currentThread().join();
    }
}
