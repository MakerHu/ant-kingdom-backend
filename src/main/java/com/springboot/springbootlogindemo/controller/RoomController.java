package com.springboot.springbootlogindemo.controller;

import com.springboot.springbootlogindemo.dto.Room;
import com.springboot.springbootlogindemo.service.RoomService;
import com.springboot.springbootlogindemo.service.serviceImpl.WebSocketService;
import com.springboot.springbootlogindemo.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {
    @Resource
    private RoomService roomService;

    @PostMapping("/addRoom")
    public Result<Room> addRoom(@RequestParam String roomName){
        Room room = roomService.addRoom(roomName);
        WebSocketService.roomList.put(room.getId(),room);
        return Result.success(room,"房间创建成功！");
    }

//    @PostMapping("/getRoom")
//    public Result<Room> getRoom(@RequestParam String id){
//        return Result.success(roomService.getRoomById(id),"房间查询成功");
//    }

    @PostMapping("/getRoomList")
    public Result<List<Room>> getRoomList(){
        List<Room> roomList = new ArrayList<>(WebSocketService.roomList.values());

        return Result.success(roomList,"房间列表查询成功");
//        return Result.success(roomService.getRoomList(),"房间列表查询成功");
    }
}
