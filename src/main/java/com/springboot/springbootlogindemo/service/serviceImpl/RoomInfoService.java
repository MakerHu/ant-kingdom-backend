package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.domain.Room;
import com.springboot.springbootlogindemo.dto.Player;
import com.springboot.springbootlogindemo.dto.RoomInfo;
import com.springboot.springbootlogindemo.repository.RoomDao;
import com.springboot.springbootlogindemo.service.CardService;
import com.springboot.springbootlogindemo.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomInfoService {

    @Resource
    private RoomDao roomDao;

    @Resource
    private CardService cardService;

    //创建房间时，初始化房间信息
    public RoomInfo init(){
        RoomInfo roomInfo = new RoomInfo();
        List<Card> cardList = cardService.getCardList();
        List<Card> allCardList = new ArrayList<>();
        for(Card card:cardList){
            int num = 0;
            if(card.getType() == 0){
                num = 10;
            }else{
                num = 3;
            }
            for(int i = 0;i < num;i ++){
                allCardList.add(card);
            }
        }
        Collections.shuffle(allCardList);
        Stack<Card> shuffleCards = new Stack<>();
        for(int i = allCardList.size()-1;i >= 0;i --){
            shuffleCards.push(allCardList.get(i));
        }
        roomInfo.setCardStack(shuffleCards);

        return roomInfo;
    }
    //玩家准备
    public RoomInfo ready(RoomInfo roomInfo,int uid){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                player.setState("READY");
            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        return roomInfo;
    }
    //判断游戏是否要开始
//    public Boolean isAllStart(RoomInfo roomInfo){
//
//    }

    //游戏开始，发牌，随机选择环境牌，初始化玩家信息
    public RoomInfo deal(RoomInfo roomInfo,String roomId){
        //改变房间状态
        Room room = roomDao.findById(roomId);
        room.setStatus(1);
        roomDao.save(room);
        //随机选择环境牌
        List<Card> environmentCards = cardService.findByType(1);
        Random r = new Random();
        int x = r.nextInt(environmentCards.size());    //返回一个随机整数
//        System.out.println("size："+environmentCards.size());
//        System.out.println("随机数："+x);
        roomInfo.setEnvironmentCard(environmentCards.get(x));
        roomInfo.setEnvironmentRice(0);
        //发牌
        Stack<Card> cardStack = roomInfo.getCardStack();
        for(Player player:roomInfo.getPlayers()){
            List<Card> cardList = new ArrayList<>();
            for(int i = 0 ;i < 8;i ++){
                cardList.add(cardStack.pop());
            }
            player.setIdleCardList(cardList);
            player.setRice(100);
        }
        roomInfo.setCardStack(cardStack);
        return roomInfo;
    }
    //抽一张牌
    public RoomInfo brand(RoomInfo roomInfo,int uid){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                if(player.getRice() >= 5){
                    Stack<Card> cardStack = roomInfo.getCardStack();
                    Card card = cardStack.pop();
                    roomInfo.setCardStack(cardStack);

                    List<Card> idleCardList = player.getIdleCardList();
                    idleCardList.add(card);
                    player.setIdleCardList(idleCardList);
                    player.setRice(player.getRice()-5);
                    players.add(player);
//                    TODO 加是否破产
                }else{
                    players.add(player);
                }
            }else{
                players.add(player);
            }
        }
        return roomInfo;
    }
    //TODO 出两张明牌
    public RoomInfo showTwoCards(RoomInfo roomInfo,int uid,List<Integer> seq ){
        return roomInfo;
    }

}
