package com.example.collabboard.service;

import com.example.collabboard.model.Room;
import com.example.collabboard.model.User;
import com.example.collabboard.repository.RoomRepository;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Creates a new collaboration room for a given user.
     * @param owner The user who is creating the room.
     * @return The newly created Room object with a unique code.
     */
    public Room createRoom(User owner) {
        Room newRoom = new Room();
        newRoom.setOwner(owner);
        return roomRepository.save(newRoom);
    }

    /**
     * Finds a room by its unique code.
     * @param roomCode The code of the room to find.
     * @return The Room object if found, otherwise null.
     */
    public Room findRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode).orElse(null);
    }
}