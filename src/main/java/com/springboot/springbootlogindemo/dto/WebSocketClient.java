package com.springboot.springbootlogindemo.dto;
import javax.websocket.Session;

public class WebSocketClient {
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //连接的uri
    private String uri;

    private String roomId;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "WebSocketClient{" +
                "session=" + session +
                ", uri='" + uri + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
