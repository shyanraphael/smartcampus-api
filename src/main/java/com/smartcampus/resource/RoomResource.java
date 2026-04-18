package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/*
    RoomResource handles all HTTP operations for the '/api/v1/rooms' path
    @Path("/rooms") - maps this class to the '/rooms' URL segment
    @Produces(APPLICATION_JSON) - all responses are JSON by default
    @Consumes(APPLICATION_JSON) - all request bodies must be JSON
*/
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    //shared in memory data sore is accessed through the singleton DataStore instance
    private final DataStore store = DataStore.getInstance();

    // GET '/api/v1/rooms' -returns a list of ALL rooms in the system
    @GET
    public Response getAllRooms() {
        //convert the HashMap values to a list for the response
        List<Room> roomList = new ArrayList<>(store.getRooms().values());

        Map<String, Object> response = new HashMap<>();
        response.put("count", roomList.size());
        response.put("rooms", roomList);

        // HTTP 200 OK
        return Response.ok(response).build(); 
    }

    // GET '/api/v1/rooms/{roomId}'
    // Returns details of a single room by its ID
    // {roomId} is a "path parameter" as it comes from the URL itself
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);

        if (room == null) {
            //this exception is caught by ResourceNotFoundExceptionMapper -> 404 not foound
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' was not found.");
        }

        // buuilds a rich response with HATEOAS links
        Map<String, Object> response = new HashMap<>();
        response.put("room", room);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/rooms/" + roomId);
        links.put("allRooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);

        // HTTP 200 OK
        return Response.ok(response).build(); 
    }

    // POST '/api/v1/rooms' - Creates a new room
    // The request body (JSON) is automatically deserialized into a Room object
    @POST
    public Response createRoom(Room room) {
        //validating required fields
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request",
                                   "message", "Room 'id' is required and cannot be empty."))
                    .build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request",
                                   "message", "Room 'name' is required and cannot be empty."))
                    .build();
        }

        //checking if a room with this ID already exists
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("status", 409, "error", "Conflict",
                                   "message", "A room with ID '" + room.getId() + "' already exists."))
                    .build();
        }

        //enssure sensorIds list is initialized
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        //saving the new room
        store.getRooms().put(room.getId(), room);

        //build success response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/rooms/" + room.getId());
        links.put("allRooms", "/api/v1/rooms");
        response.put("_links", links);

        //HTTP 201 Created - standard response for successful resource creation
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // DELETE '/api/v1/rooms/{roomId}' - deletes a room but only if it has no sensors assigned to it

    // IS DELETE IDEMPOTENT?
        // Yes - idempotent means calling it multiple times has the same result.
        // First call: room exists -> deleted -> 200 OK
        // Second call: room gone -> 404 Not Found
    // The server state after both calls is the same as the room doesnt exist
    // However, the HTTP status differs (200 vs 404), which is acceptabl per the REST spec. The key is the STATE doesn't change after the first call.
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);

        //if room doesn't exist 404 is returned
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' was not found.");
        }

        //preventing orphan sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            //exception is caught by RoomNotEmptyExceptionMapper -> 409 Conflict
            throw new RoomNotEmptyException(roomId);
        }

        //safe to delete the room
        store.getRooms().remove(roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully deleted.");
        response.put("deletedRoomId", roomId);

        return Response.ok(response).build(); // HTTP 200 OK
    }
}
