package com.springboot.springbootlogindemo.service;

import com.springboot.springbootlogindemo.domain.Room;

import java.util.List;

public interface RoomService {

    Room addRoom(String roomName);

    List<Room> getRoomList();

    Room getRoomById(String id);
}
