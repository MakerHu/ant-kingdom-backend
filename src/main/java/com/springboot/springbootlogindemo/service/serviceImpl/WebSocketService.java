package com.springboot.springbootlogindemo.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.dto.Room;
import com.springboot.springbootlogindemo.dto.Player;
import com.springboot.springbootlogindemo.dto.RoomInfo;
import com.springboot.springbootlogindemo.dto.WebSocketClient;
import com.springboot.springbootlogindemo.repository.UserDao;
import com.springboot.springbootlogindemo.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{uid}")
@Component
public class WebSocketService {

    private static UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao1) {
        userDao = userDao1;
    }

    private static RoomInfoService roomInfoService;

    @Autowired
    public void setRoomInfoService(RoomInfoService roomInfoService1) {
        roomInfoService = roomInfoService1;
    }


    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
    private static ConcurrentHashMap<String, WebSocketClient> webSocketMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, RoomInfo> roomMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Room> roomList = new ConcurrentHashMap<>();


    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userName*/
    private String uid="";
    /**接收房间号*/
    private static String roomId="";
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uidRoomId) {
//        System.out.println("uidRoomId"+uidRoomId);
        String[] infos = uidRoomId.split("-");
        this.roomId = infos[1];
        this.uid= infos[0];

        addOnlineCount(); // 在线数 +1

        if(!webSocketMap.containsKey(infos[0]))
        {

            this.session = session;

            WebSocketClient client = new WebSocketClient();
            client.setSession(session);
            client.setRoomId(roomId);
            client.setUri(session.getRequestURI().toString());
            webSocketMap.put(uid, client);

            log.info("----------------------------------------------------------------------------");
            log.info("用户连接:"+uid+",当前在线人数为:" + getOnlineCount());
            try {
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success("来自后台的反馈：连接成功","MSG"));
                sendMessage(jsonObject.toString());
            } catch (IOException e) {
                log.error("用户:"+uid+",网络异常!!!!!!");
            }
            enter();
        }else{
            WebSocketClient client = webSocketMap.get(uid);
            //如果这个账号多设备登录
            if(!isPlayerOffLine(uid)){
                try {
                    sendMessage(uid,"该账号在其他设备登录","DEVICE_REPLACE");
                    client.getSession().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            client.setSession(session);
            webSocketMap.put(uid,client);
            if(!webSocketMap.get(uid).getRoomId().equalsIgnoreCase(roomId)){
                Room room = roomList.get(webSocketMap.get(uid).getRoomId());
                sendMessage(uid,room,"ENTER_ERR");

                if(roomList.get(roomId).getPeopleNum() == 0){
                    roomList.remove(roomId);
                }
            }else{
                enter();
//                RoomInfo roomInfo = roomMap.get(roomId);
//                for(Player player:roomInfo.getPlayers()){
//                    if(player.getUser().getUid() == Integer.parseInt(uid)){
//                        player.setState("READY");
//                    }
//                }
//                roomMap.put(roomId,roomInfo);
//                sendMessage(roomInfo,"REFRESH");
            }

        }

    }

    public Boolean isPlayerOffLine(String uid){
        RoomInfo roomInfo = roomMap.get(webSocketMap.get(uid).getRoomId());
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == Integer.parseInt(uid)){
                return player.isOffLine();
            }
        }
        return true;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(uid)) {
//
            // 当玩家断开socket且没有退出房间时变更offline=true
            RoomInfo roomInfo = roomMap.get(roomId);
            if(roomInfo != null){
                for(Player player:roomInfo.getPlayers()){
                    if(player.getUser().getUid() == Integer.parseInt(uid)){
                        player.setOffLine(true);
                        break;
                    }
                }
            }
            if (webSocketMap.size() > 0) {
                //从set中删除
                subOnlineCount();
            }
        }
        log.info("----------------------------------------------------------------------------");
        log.info(uid+"用户退出,当前在线人数为:" + getOnlineCount());
    }

    //玩家主动离开游戏
    public void quit(){
        if(webSocketMap.containsKey(uid)){

            if(webSocketMap.size()>0)
            {
                //从set中删除
                subOnlineCount();
            }
            webSocketMap.remove(uid);
            RoomInfo roomInfo = roomMap.get(roomId);
            if(roomInfo != null){
                List<Player> players = roomInfo.getPlayers();
                //TODO 并发问题，亟待解决
                Iterator<Player> iterator = players.iterator();
                while(iterator.hasNext()){
                    Player player = iterator.next();
                    if(player.getUser().getUid() == Integer.parseInt(uid))
                        iterator.remove();   //注意这个地方
                }

//                for(Player player:players){
//                    if(player.getUser().getUid() == Integer.parseInt(uid)){
//                        players.remove(player);
//                    }
//                }
                roomInfo.setPlayers(players);
                roomMap.put(roomId,roomInfo);
                sendMessage(roomInfo,"REFRESH");

                //减少当前房间的人数
                Room room = roomList.get(roomId);
                updateRoomPeopleNum();



                //若当前房间人数为0则修改房间状态 在内存中删除该房间信息
                if(room.getPeopleNum() == 0){
                    room.setStatus(2);
                    roomList.remove(roomId);
                    roomMap.remove(roomId);
                }
                //如果游戏还未开始或者刚结束，清空手牌
                if(room.getStatus() == 0){
                    for(Player player:roomInfo.getPlayers()){
                        HashMap<String,List<Card>> idleCardMap = new HashMap<>();
                        idleCardMap.put("ant",new ArrayList<Card>());
                        idleCardMap.put("env",new ArrayList<Card>());
                        player.setIdleCardMap(idleCardMap);
                        player.setShowCardList(new ArrayList<>());
                        player.setHideCardList(new ArrayList<>());
                        player.setScore(0);
                        player.setRice(0);
                        player.setBankruptcy(false);
                    }
                    roomInfo.setCardStack(new Stack<>());
                    roomInfo.setEnvironmentCard(null);
                    roomInfo.setEnvironmentRice(0);
                    roomMap.put(roomId,roomInfo);
                    sendMessage(roomInfo,"REFRESH");
                }
                //如果正在游戏，玩家退出，直接结束
                if(room.getStatus() == 1){
                    for(Player player:roomInfo.getPlayers()){
                        HashMap<String,List<Card>> idleCardMap = new HashMap<>();
                        idleCardMap.put("ant",new ArrayList<Card>());
                        idleCardMap.put("env",new ArrayList<Card>());
                        player.setIdleCardMap(idleCardMap);
                        player.setShowCardList(new ArrayList<>());
                        player.setHideCardList(new ArrayList<>());
                        player.setBankruptcy(false);
                        player.setState("UNREADY");
                    }
                    roomInfo.setCardStack(new Stack<>());
                    roomInfo.setEnvironmentCard(null);
                    roomInfo.setEnvironmentRice(0);
                    roomMap.put(roomId,roomInfo);
                    sendMessage(roomInfo,"ENEMY_QUIT");

                    room.setStatus(0);
                    roomList.put(roomId,room);
                }
            }

        }
    }
    //更新房间人数
    public void updateRoomPeopleNum(){
        Room room = roomList.get(roomId);
        if(room != null){
            if(roomMap.containsKey(roomId)){
                RoomInfo roomInfo = roomMap.get(roomId);
                room.setPeopleNum(roomInfo.getPlayers().size());
            }else{
                room.setPeopleNum(0);
            }
        }
//        roomList.put(roomId,room);
    }

    //玩家进入游戏
    public void enter(){
        //房间人数+1
        Room room = roomList.get(roomId);

        if(room != null){
            if(!roomMap.containsKey(roomId)){

                RoomInfo roomInfo = new RoomInfo();
                Player player = new Player();
//            System.out.println("uid:"+uid);
                player.setUser(userDao.findByUid(Integer.parseInt(uid)));
                player.setState("UNREADY");
                player.setOffLine(false);
                List<Player> players = new ArrayList<>();
                players.add(player);
                roomInfo.setPlayers(players);
                roomMap.put(roomId,roomInfo);
            }else{
                RoomInfo roomInfo = roomMap.get(roomId);
                Player user = null;
                for(Player player:roomInfo.getPlayers()){
                    if(player.getUser().getUid() == Integer.parseInt(uid)){
                        user = player;
                        break;
                    }
                }
                if(user == null){
                    Player player = new Player();
//            System.out.println("uid:"+uid);
                    player.setUser(userDao.findByUid(Integer.parseInt(uid)));
                    player.setState("UNREADY");
                    player.setOffLine(false);
                    List<Player> players = roomInfo.getPlayers();
                    players.add(player);
//                    roomInfo.setPlayers(players);
//                    roomMap.put(roomId,roomInfo);
                }else{
//                    user.setState("UNREADY");
                    user.setOffLine(false);
                }
            }
//        WebSocketClient webSocketClient = webSocketMap.get(uid);
//        webSocketClient.setRoomId(roomId);
//        webSocketMap.put(uid,webSocketClient);
//        System.out.println("webSocketMap"+webSocketMap.toString());
//        System.out.println("roomMap"+roomMap.toString());
            RoomInfo roomInfo = roomMap.get(roomId);
            sendMessage(roomInfo,"REFRESH");
        }else{
            sendMessage(uid,"连接错误","CONN_ERR");
            if(webSocketMap.containsKey(uid)){
                if(webSocketMap.size()>0)
                {
                    //从set中删除
                    subOnlineCount();
                }
                webSocketMap.remove(uid);
            }

        }

        updateRoomPeopleNum();

    }

    //给每个玩家发送牌局信息
    public void sendMessage(RoomInfo roomInfo,String key){
        for(Player player:roomInfo.getPlayers()){
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success(roomInfo,key));
            sendMessage(player.getUser().getUid(),jsonObject.toString());
            System.out.println("jsonObject.toString()"+jsonObject.toString());
        }
    }
    //给单个玩家发送牌局信息
    public void sendMessage(String uid,Object object,String key){
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success(object,key));
        sendMessage(Integer.parseInt(uid),jsonObject.toString());
        System.out.println("jsonObject.toString()"+jsonObject.toString());
    }
    //玩家准备
    public void ready(){
        RoomInfo roomInfo = roomInfoService.ready(roomMap.get(roomId),Integer.parseInt(uid));
        roomMap.put(roomId,roomInfo);
        sendMessage(roomInfo,"REFRESH");
        if(roomInfoService.isAllReady(roomInfo)){
            roomInfo = roomInfoService.init(roomInfo);
            roomInfo = roomInfoService.deal(roomInfo,roomId);
            roomMap.put(roomId,roomInfo);
            sendMessage(roomInfo,"START");
            System.out.println("游戏开始！");
        }
    }
    //玩家结束本回合
    public void end(){
        RoomInfo roomInfo = roomInfoService.end(roomMap.get(roomId),Integer.parseInt(uid));
        roomMap.put(roomId,roomInfo);
        sendMessage(roomInfo,"REFRESH");
        if(roomInfoService.isEveryoneEnd(roomInfo)){
            roomInfo = roomInfoService.calculateScore(roomInfo);
            roomInfo = roomInfoService.award(roomInfo);
            roomMap.put(roomId,roomInfo);
            sendMessage(roomInfo,"END_OUT");
            Result result = roomInfoService.isBankruptcy(roomInfo);
            if(result.getMsg().equalsIgnoreCase("success")){
                roomMap.put(roomId, (RoomInfo) result.getData());
                roomInfo = roomMap.get(roomId);
                sendMessage(roomInfo,"BANKRUPTCY");
            }
            //如果游戏结束，玩家状态改为未准备
            if(roomInfoService.isGameOver(roomInfo)){
                for(Player player:roomInfo.getPlayers()){
                    player.setState("UNREADY");
                }
                Room room = roomList.get(roomId);
                room.setStatus(0);
                roomList.put(roomId,room);
                sendMessage(roomInfo,"GAME_OVER");
            }
            //如果可以继续，玩家状态改为未继续
            else{
                for(Player player:roomInfo.getPlayers()){
                    player.setState("UNCONTINUE");
                }
                sendMessage(roomInfo,"REFRESH");
            }
            roomMap.put(roomId,roomInfo);
        }
    }

    //亮两张牌
    public void showTwoCards(String[] instructions,String type){
        String[] show1 = instructions[1].split("@");
        String[] show2 = instructions[2].split("@");
        if(show1[0].equalsIgnoreCase("ant") && show2[0].equalsIgnoreCase("ant")){
            List<Integer> seq = new ArrayList<>();
            seq.add(Integer.parseInt(show1[1]));
            seq.add(Integer.parseInt(show2[1]));
            RoomInfo roomInfo = roomMap.get(roomId);
            roomInfo = roomInfoService.showTwoCards(roomInfo,Integer.parseInt(uid),seq,type);
            roomMap.put(roomId,roomInfo);
            if(roomInfoService.isEveryone(roomInfo,type)){
                roomInfo = roomInfoService.calculateScore(roomInfo);
                if(type.equalsIgnoreCase("show")){
                    for(Player player:roomInfo.getPlayers()){
                        player.setState("HIDE_START");
                    }
                    roomMap.put(roomId,roomInfo);
                    sendMessage(roomInfo,"SHOW_OUT");
                }else if(type.equalsIgnoreCase("hide")){
                    for(Player player:roomInfo.getPlayers()){
                        player.setState("BOTH_HIDE_END");
                    }
                    roomMap.put(roomId,roomInfo);
                    sendMessage(roomInfo,"HIDE_OUT");
                }
            }else{
                sendMessage(roomInfo,"REFRESH");
            }
        }else{
            sendMessage(uid,"所出的牌不全为蚂蚁牌","ALERT");
        }



    }
    //亮两张牌
//    public void hideTwoCards(String[] instructions){
//        List<Integer> seq = new ArrayList<>();
//        seq.add(Integer.parseInt(instructions[1]));
//        seq.add(Integer.parseInt(instructions[2]));
//        RoomInfo roomInfo = roomMap.get(roomId);
//        roomInfo = roomInfoService.hideTwoCards(roomInfo,Integer.parseInt(uid),seq);
//        roomMap.put(roomId,roomInfo);
//        if(roomInfoService.isEveryone(roomInfo,"hide")){
//            roomInfo = roomInfoService.calculateScore(roomInfo);
//            for(Player player:roomInfo.getPlayers()){
//                player.setState("BOTH_HIDE_END");
//            }
////            roomInfo = roomInfoService.award(roomInfo);
//            roomMap.put(roomId,roomInfo);
//            sendMessage(roomInfo,"HIDE_OUT");
//        }else{
//            sendMessage(roomInfo,"REFRESH");
//        }
//    }
    //抽牌
    public void brand(){
        RoomInfo roomInfo = roomMap.get(roomId);
        Result result = roomInfoService.brand(roomInfo,Integer.parseInt(uid));
        if(result.getMsg().equalsIgnoreCase("success")){
            roomMap.put(roomId, (RoomInfo) result.getData());
            roomInfo = roomMap.get(roomId);
            sendMessage(roomInfo,"REFRESH");
        }else if(result.getMsg().equalsIgnoreCase("bankruptcy")){
            roomMap.put(roomId, (RoomInfo) result.getData());
            roomInfo = roomMap.get(roomId);

            for(Player player:roomInfo.getPlayers()){
                player.setState("UNREADY");
            }
            Room room = roomList.get(roomId);
            room.setStatus(0);
            roomList.put(roomId,room);
            sendMessage(roomInfo,"GAME_OVER");

        }else{
            sendMessage(uid,result.getMsg(),"ALERT");
        }
    }

    //玩家继续
    public void playerContinue(){
        RoomInfo roomInfo = roomMap.get(roomId);
        Result result = roomInfoService.playerSelectContinue(roomInfo,Integer.parseInt(uid));
        if(result.getMsg().equalsIgnoreCase("success")){
            roomInfo = (RoomInfo) result.getData();
            roomMap.put(roomId, roomInfo);
            sendMessage(roomInfo,"REFRESH");
        }else{
            sendMessage(uid,result.getMsg(),"CONTINUE_ALERT");
        }
        roomInfo = roomMap.get(roomId);
        if(roomInfoService.isEveryoneContinue(roomInfo)){
            roomInfo = roomInfoService.startNew(roomInfo);
            roomMap.put(roomId,roomInfo);
            sendMessage(roomInfo,"START");
        }
    }

    //判断缓存是否被清空
    public Boolean isExist(String instruction){
        if(!roomId.equalsIgnoreCase("")){
            Room room = roomList.get(roomId);
            if(room == null){
                sendMessage(uid,"连接错误","CONN_ERR");
                if(webSocketMap.containsKey(uid)){
                    if(webSocketMap.size()>0)
                    {
                        //从set中删除
                        subOnlineCount();
                    }
                    webSocketMap.remove(uid);
                }

                return false;
            }else{
                return true;
            }
        }else{
            String[] instructions = instruction.split("#");
            if(instructions[0].equalsIgnoreCase("ENTER")){
                Room room = roomList.get(instructions[1]);
                if(room == null){
                    sendMessage(uid,"连接错误","CONN_ERR");
                    if(webSocketMap.containsKey(uid)){
                        if(webSocketMap.size()>0)
                        {
                            //从set中删除
                            subOnlineCount();
                        }
                        webSocketMap.remove(uid);
                    }
                    return false;
                }else{
                    return true;
                }
            }else{
                sendMessage(uid,"连接错误","CONN_ERR");
                if(webSocketMap.containsKey(uid)){
                    if(webSocketMap.size()>0)
                    {
                        //从set中删除
                        subOnlineCount();
                    }
                    webSocketMap.remove(uid);
                }
                return false;
            }

        }
    }

    //玩家取消准备
    public void cancelReady(){
        RoomInfo roomInfo = roomMap.get(roomId);
        roomInfo = roomInfoService.cancelReady(roomInfo,Integer.parseInt(uid));
        roomMap.put(roomId,roomInfo);
        sendMessage(roomInfo,"REFRESH");
    }

    //换环境牌
    public void changeEnv(String[] instructions){
        String[] env = instructions[1].split("@");
        if(env[0].equalsIgnoreCase("env")){
            List<Integer> infos = new ArrayList<>();
            infos.add(Integer.parseInt(env[1]));
            infos.add(Integer.parseInt(instructions[2]));
            RoomInfo roomInfo = roomMap.get(roomId);
            roomInfo = roomInfoService.changeEnv(roomInfo,Integer.parseInt(uid),infos);
            //重新计算分数
            roomInfo = roomInfoService.calculateScore(roomInfo);
            roomMap.put(roomId,roomInfo);
            sendMessage(roomInfo,"REFRESH");
        }else{
            sendMessage(uid,"所出的牌不是环境牌","ALERT");
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
        if(isExist(message)){
            String[] instructions = message.split("#");
            switch(instructions[0]){
//                case "ENTER":  //玩家进入
//                    enter();
//                    break;
                case "READY":  //玩家准备
                    ready();
                    break;
                case "UNREADY":  //玩家准备
                    cancelReady();
                    break;
                case "SHOW":   //玩家亮两张牌
                    showTwoCards(instructions,"show");
                    break;
                case "HIDE":  //玩家隐藏两张牌
//                    hideTwoCards(instructions);
                    showTwoCards(instructions,"hide");
                    break;
                case "END":
                    end();
                    break;
                case "BRAND":
                    brand();
                    break;
                case "CONTINUE":
                    playerContinue();
                    break;
                case "QUIT":
                    quit();
                    break;
                case "CHANGE_ENV":
                    changeEnv(instructions);
                    break;
            }
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
    public static void sendMessage(int uid,String message){
        try {
            WebSocketClient webSocketClient = webSocketMap.get(String.valueOf(uid));
            if(webSocketClient!=null){
                webSocketClient.getSession().getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RoomInfo roomInfo = roomMap.get(roomId);
            if(roomInfo != null){
                // 先更新掉线玩家的状态
                for(Player player:roomInfo.getPlayers()){
                    if(player.getUser().getUid() == uid){
                        player.setOffLine(true);
                        break;
                    }
                }
                // 再把更新后的状态发给未掉线的玩家
                for(Player player:roomInfo.getPlayers()){
                    if(player.getUser().getUid() != uid && !player.isOffLine()){
                        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(Result.success(roomInfo,"OFFLINE"));
                        sendMessage(player.getUser().getUid(),jsonObject.toString());
                        System.out.println("jsonObject.toString()"+jsonObject.toString());
                        break;
                    }
                }
            }
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
