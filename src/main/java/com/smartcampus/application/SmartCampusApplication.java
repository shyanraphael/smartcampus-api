package com.smartcampus.application;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
    SmartCampusApplication is the JAX-RS Application subclass
    @ApplicationPath("/api/v1") sets the BASE URL for all endpoints

    JAX-RS LIFECYCLE (report answer):
        By default, JAX-RS creates a NEW instance of each Resource class per each request
        Data stored in resource class fields would be lost after each request
        Solution: Use the DataStore singleton with ConcurrentHashMap for safety of the thread
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Main.java uses ResourceConfig.packages() to auto-scan allthe classes
}
