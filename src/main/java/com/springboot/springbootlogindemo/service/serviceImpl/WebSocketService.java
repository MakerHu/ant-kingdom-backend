package com.springboot.springbootlogindemo.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.springboot.springbootlogindemo.domain.Room;
import com.springboot.springbootlogindemo.dto.Player;
import com.springboot.springbootlogindemo.dto.RoomInfo;
import com.springboot.springbootlogindemo.dto.WebSocketClient;
import com.springboot.springbootlogindemo.repository.UserDao;
import com.springboot.springbootlogindemo.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{uid}")
@Component
public class WebSocketService {

    @Resource
    private UserDao userDao;

    @Resource
    private RoomInfoService roomInfoService;


    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
    private static ConcurrentHashMap<String, WebSocketClient> webSocketMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, RoomInfo> roomMap = new ConcurrentHashMap<>();


    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userName*/
    private String uid="";
    /**接收房间号*/
    private String roomId="";
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        if(!webSocketMap.containsKey(uid))
        {
            addOnlineCount(); // 在线数 +1
        }
        this.session = session;
        this.uid= uid;
        WebSocketClient client = new WebSocketClient();
        client.setSession(session);
        client.setUri(session.getRequestURI().toString());
        webSocketMap.put(uid, client);

        log.info("----------------------------------------------------------------------------");
        log.info("用户连接:"+uid+",当前在线人数为:" + getOnlineCount());
        try {
            sendMessage("来自后台的反馈：连接成功");
        } catch (IOException e) {
            log.error("用户:"+uid+",网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(uid)){

            if(webSocketMap.size()>0)
            {
                //从set中删除
                subOnlineCount();
            }
            webSocketMap.remove(uid);
        }
        log.info("----------------------------------------------------------------------------");
        log.info(uid+"用户退出,当前在线人数为:" + getOnlineCount());
    }

    //玩家进入游戏
    public void enter(String roomId){
        this.roomId = roomId;
        RoomInfo roomInfo = new RoomInfo();
        if(!roomMap.containsKey(roomId)){
            Player player = new Player();
            player.setUser(userDao.findByUid(Integer.parseInt(uid)));
            player.setState("UNREADY");
            List<Player> players = new ArrayList<>();
            players.add(player);
            roomInfo.setPlayers(players);
            roomMap.put(roomId,roomInfo);
        }
        WebSocketClient webSocketClient = webSocketMap.get(uid);
        webSocketClient.setRoomId(roomId);
        webSocketMap.put(uid,webSocketClient);
//        System.out.println("webSocketMap"+webSocketMap.toString());
//        System.out.println("roomMap"+roomMap.toString());
        for(Player player:roomInfo.getPlayers()){
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success(roomInfo,"REFRESH"));
            sendMessage(player.getUser().getUid(),jsonObject.toString());
        }
    }

    //玩家准备
    public void ready(){
        RoomInfo roomInfo = roomInfoService.ready(roomMap.get(roomId),Integer.parseInt(uid));
        roomMap.put(roomId,roomInfo);
        for(Player player:roomInfo.getPlayers()){
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success(roomInfo,"REFRESH"));
            sendMessage(player.getUser().getUid(),jsonObject.toString());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到用户消息:"+uid+",报文:"+message);
        //可以群发消息
        //消息保存到数据库、redis
        String[] instructions = message.split("#");
        switch(instructions[0]){
            case "ENTER":
                enter(instructions[1]);
                break;
            case "READY":
                ready();
                break;
        }
        if(StringUtils.isNotBlank(message)){

        }
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:"+this.uid+",原因:"+error.getMessage());
        error.printStackTrace();
    }

    /**
     * 连接服务器成功后主动推送
     */
    public void sendMessage(String message) throws IOException {
        synchronized (session){
            this.session.getBasicRemote().sendText(message);
        }
    }

    /**
     * 向指定客户端发送消息
     * @param uid
     * @param message
     */
    public static void sendMessage(long uid,String message){
        try {
            WebSocketClient webSocketClient = webSocketMap.get(uid);
            if(webSocketClient!=null){
                webSocketClient.getSession().getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketService.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketService.onlineCount--;
    }

    public static void setOnlineCount(int onlineCount) {
        WebSocketService.onlineCount = onlineCount;
    }


    public static ConcurrentHashMap<String, WebSocketClient> getWebSocketMap() {
        return webSocketMap;
    }

    public static void setWebSocketMap(ConcurrentHashMap<String, WebSocketClient> webSocketMap) {
        WebSocketService.webSocketMap = webSocketMap;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
