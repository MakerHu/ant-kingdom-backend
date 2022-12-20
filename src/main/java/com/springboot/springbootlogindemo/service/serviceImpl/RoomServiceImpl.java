package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.domain.Room;
import com.springboot.springbootlogindemo.repository.RoomDao;
import com.springboot.springbootlogindemo.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Resource
    private RoomDao roomDao;

    @Override
    public Room addRoom(Room room) {
        return roomDao.save(room);
    }

    @Override
    public List<Room> getRoomList() {
        return roomDao.findAll();
    }

    @Override
    public Room getRoomById(String id) {
        return roomDao.findById(id);
    }
}
