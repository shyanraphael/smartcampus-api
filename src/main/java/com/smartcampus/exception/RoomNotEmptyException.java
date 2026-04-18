package com.smartcampus.exception;


//This exception is thrown when someone tries to DELETE a room that still has sensors in it
//This prevents sensors with no valid rooms also known as "orphan" sensors
//Maps to HTTP 409 Conflict.
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' cannot be deleted because it still has sensors assigned to it. " +
              "Please remove or reassign all sensors before deleting this room.");
    }
}
