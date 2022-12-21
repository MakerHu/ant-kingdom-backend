package com.springboot.springbootlogindemo.controller;
import com.springboot.springbootlogindemo.dto.RoomInfo;
import com.springboot.springbootlogindemo.service.RoomService;
import com.springboot.springbootlogindemo.service.serviceImpl.RoomInfoService;
import com.springboot.springbootlogindemo.service.serviceImpl.WebSocketService;
import com.springboot.springbootlogindemo.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/websocket")
public class WebSocketController {

    @Resource
    private RoomInfoService roomInfoService;


    @GetMapping("/pushone")
    public void pushone()
    {
        WebSocketService.sendMessage(1,"公众号:霸道的程序猿");
    }

//    @PostMapping("/init")
//    public Result<RoomInfo> init(){
//        return Result.success(roomInfoService.init());
//    }
}
