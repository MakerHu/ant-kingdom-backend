package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.dto.Room;
import com.springboot.springbootlogindemo.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RoomServiceImpl implements RoomService {



    @Override
    public Room addRoom(String roomName) {
        Room room = new Room();
        room.setId(getStringRandom(6));
        room.setName(roomName);
        room.setPeopleNum(0);
        room.setStatus(0);
        return room;
    }

//
//    @Override
//    public Room getRoomById(String id) {
//        return roomDao.findById(id);
//    }

    //生成随机数字和字母,
    public String getStringRandom(int length) {
        StringBuffer val = new StringBuffer();
        Random random = new Random();
        for(int i = 0; i < length; i++) {
            val.append((char)(random.nextInt(26) + 65));
        }
        return val.toString();
    }

}
