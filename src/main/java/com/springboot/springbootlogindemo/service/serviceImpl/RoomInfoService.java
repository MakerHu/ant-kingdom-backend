package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.domain.CardRelation;
import com.springboot.springbootlogindemo.dto.Room;
import com.springboot.springbootlogindemo.dto.Player;
import com.springboot.springbootlogindemo.dto.RoomInfo;
import com.springboot.springbootlogindemo.service.CardRelationService;
import com.springboot.springbootlogindemo.service.CardService;
import com.springboot.springbootlogindemo.utils.Result;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoomInfoService {


    @Resource
    private CardService cardService;

    @Resource
    private CardRelationService cardRelationService;

    //创建房间时，初始化房间信息
    public RoomInfo init(RoomInfo roomInfo){
        List<Card> cardList = cardService.getCardList();
        List<Card> allCardList = new ArrayList<>();
        for(Card card:cardList){
            card.setRelationList(new ArrayList<>());
            List<CardRelation> relateCardList = cardRelationService.findByCard1(card.getId());
            for (CardRelation relationCardInfo : relateCardList) {
                for (Card relationCard : cardList) {
                    if (relationCardInfo.getCard2() == relationCard.getId()){
                        Map<String, String> relationCardMap = new HashMap<>();
                        relationCardMap.put("name",relationCard.getName());
                        relationCardMap.put("valueImpact",relationCardInfo.getValueImpact());
                        card.getRelationList().add(relationCardMap);
                    }
                }
            }

            int num = 0;
            if(card.getType() == 0){
                num = 10;
            }else{
                num = 2;
            }
            for(int i = 0;i < num;i++){
                allCardList.add(card);
            }
        }
        Collections.shuffle(allCardList);
        Stack<Card> shuffleCards = new Stack<>();
        for(int i = allCardList.size()-1;i >= 0;i --){
            shuffleCards.push(allCardList.get(i));
        }
        roomInfo.setCardStack(shuffleCards);
        roomInfo.setWinners(new ArrayList<>());
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
    public Boolean isAllReady(RoomInfo roomInfo){
        if(roomInfo.getPlayers().size()==2){
            for(Player player:roomInfo.getPlayers()){
                if(!player.getState().equalsIgnoreCase("READY")){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }

    }

    //游戏开始，发牌，随机选择环境牌，初始化玩家信息
    public RoomInfo deal(RoomInfo roomInfo,String roomId){
        //改变房间状态
        Room room = WebSocketService.roomList.get(roomId);
        room.setStatus(1);
        WebSocketService.roomList.put(roomId,room);
        //随机选择环境牌
        List<Card> environmentCards = cardService.findByType(1);
        Random r = new Random();
        int x = r.nextInt(environmentCards.size());    //返回一个随机整数
//        System.out.println("size："+environmentCards.size());
//        System.out.println("随机数："+x);
        roomInfo.setEnvironmentCard(environmentCards.get(x));
        roomInfo.setEnvironmentRice(0);
        roomInfo.setWinners(new ArrayList<>());
        //发牌
        Stack<Card> cardStack = roomInfo.getCardStack();
        for(Player player:roomInfo.getPlayers()){
            HashMap<String,List<Card>> idleCardMap = new HashMap<>();
            List<Card> antList = new ArrayList<>();
            List<Card> envList = new ArrayList<>();
            for(int i = 0 ;i < 8;i ++){
                Card card = cardStack.pop();
                //判断牌类型
                if(card.getType() == 0){
                    antList.add(card);
                }else if(card.getType() == 1){
                    envList.add(card);
                }
            }
            idleCardMap.put("ant",antList);
            idleCardMap.put("env",envList);
            player.setIdleCardMap(idleCardMap);
            player.setRice(1000);
            player.setScore(0);
            player.setShowCardList(new ArrayList<>());
            player.setHideCardList(new ArrayList<>());
            player.setBankruptcy(false);
            player.setOffLine(false);
            player.setState("SHOW_START");
        }
        roomInfo.setCardStack(cardStack);
        return roomInfo;
    }

    public HashMap<String,List<Card>> addCard(HashMap<String,List<Card>> idleCardMap,Card card){
        if(card.getType() == 0){
            List<Card> antList = idleCardMap.get("ant");
            antList.add(card);
            idleCardMap.put("ant",antList);
        }else if(card.getType() == 1){
            List<Card> envList = idleCardMap.get("env");
            envList.add(card);
            idleCardMap.put("env",envList);
        }
        return idleCardMap;
    }
    //抽一张牌
    public Result<RoomInfo> brand(RoomInfo roomInfo, int uid){
        int buyAntCost = 50;
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
                if(player.getRice() < buyAntCost){
                    return Result.error("101","食物不够获得新的蚂蚁");
                }else if(getMapSize(idleCardMap) == 8){
                    return Result.error("101","蚂蚁上限不能超过8只");
                } else if(player.getRice() >= buyAntCost && getMapSize(idleCardMap)<8){
                    Stack<Card> cardStack = roomInfo.getCardStack();
                    Card card = cardStack.pop();
                    roomInfo.setCardStack(cardStack);

                    player.setIdleCardMap(addCard(idleCardMap,card));
                    player.setRice(player.getRice()-buyAntCost);

                    if(player.getRice()/50 + idleCardMap.get("ant").size() <4){
                        player.setBankruptcy(true);
                        return Result.success(roomInfo,"bankruptcy");
                    }
                }
            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        return Result.success(roomInfo,"success");
    }

    //出两张明牌
    public RoomInfo showTwoCards(RoomInfo roomInfo,int uid,List<Integer> seq,String type){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
                List<Card> antList = idleCardMap.get("ant");
                Card card1 = antList.get(seq.get(0));
                Card card2 = antList.get(seq.get(1));
                antList.remove(card1);
                antList.remove(card2);
                idleCardMap.put("ant",antList);
                player.setIdleCardMap(idleCardMap);

                if(type.equalsIgnoreCase("show")){
                    List<Card> showCardList = new ArrayList<>();
                    showCardList.add(card1);
                    showCardList.add(card2);
                    player.setShowCardList(showCardList);
                    player.setState("SHOW_END");
                }else if(type.equalsIgnoreCase("hide")){
                    List<Card> hideCardList = new ArrayList<>();
                    hideCardList.add(card1);
                    hideCardList.add(card2);
                    player.setHideCardList(hideCardList);
                    player.setState("HIDE_END");
                }

            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        return roomInfo;
    }

    //出两张明牌
//    public RoomInfo hideTwoCards(RoomInfo roomInfo,int uid,List<Integer> seq ){
//        List<Player> players = new ArrayList<>();
//        for(Player player:roomInfo.getPlayers()){
//            if(player.getUser().getUid() == uid){
//                HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
//                List<Card> antList = idleCardMap.get("ant");
//                Card card1 = antList.get(seq.get(0));
//                Card card2 = antList.get(seq.get(1));
//                antList.remove(card1);
//                antList.remove(card2);
//                idleCardMap.put("ant",antList);
//                player.setIdleCardMap(idleCardMap);
//
//
//                List<Card> hideCardList = new ArrayList<>();
//                hideCardList.add(card1);
//                hideCardList.add(card2);
//                player.setHideCardList(hideCardList);
//                player.setState("HIDE_END");
//            }
//            players.add(player);
//        }
//        roomInfo.setPlayers(players);
//        return roomInfo;
//    }

    //判断是否所有人出牌
    public Boolean isEveryone(RoomInfo roomInfo,String type){
        for(Player player:roomInfo.getPlayers()){
            if(type.equalsIgnoreCase("show")){
                if(!player.getState().equalsIgnoreCase("SHOW_END")){
                    return false;
                }
            }else if(type.equalsIgnoreCase("hide")){
                if(!player.getState().equalsIgnoreCase("HIDE_END")){
                    return false;
                }
            }
        }
        return true;
    }
    //换环境牌
//    public RoomInfo exchangeEnvironment(RoomInfo roomInfo,int uid,int rice,int seq){
//
//    }
    //算所有人的分数，判断赢家
    public RoomInfo calculateScore(RoomInfo roomInfo){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            int score = 0;
            List<Card> cards = new ArrayList<>();
            for(Card card:player.getShowCardList()){
                cards.add(card);
            }
            for(Card card:player.getHideCardList()){
                cards.add(card);
            }
            for(int i = 0;i < cards.size();i ++){
                Card card1 = cards.get(i);
                String formula = card1.getInitValue()+"";
                for(int j = 0; j < cards.size();j ++){
                    if(j != i){
                        Card card2 = cards.get(j);
                        CardRelation cardRelation = cardRelationService.findByCard1AndCard2(card1.getId(),card2.getId());
                        if(cardRelation != null){
                            formula+=cardRelation.getValueImpact();
                        }
                    }
                }
                Card card2 = roomInfo.getEnvironmentCard();
                CardRelation cardRelation = cardRelationService.findByCard1AndCard2(card1.getId(),card2.getId());
                if(cardRelation != null){
                    formula+=cardRelation.getValueImpact();
                }
                JexlEngine jexlEngine = new JexlBuilder().create();
                JexlExpression jexlExpression = jexlEngine.createExpression(formula);
                Object evaluate = jexlExpression.evaluate(null);
                score += (int)evaluate;
            }
            player.setScore(score);
            players.add(player);
        }
        roomInfo.setPlayers(players);

        return roomInfo;
    }

    //给赢家奖励
    public RoomInfo award(RoomInfo roomInfo){
        Player winner = roomInfo.getPlayers().get(0);
        Player player1 = roomInfo.getPlayers().get(1);
        if(player1.getScore()>winner.getScore()){
            winner = player1;
        }else if(player1.getScore() == winner.getScore()){  //如果分数相同都不奖励也不惩罚
            winner = null;
        }
        //        List<Player> winners = roomInfo.getWinners();
//        winners.add(winner);
//        roomInfo.setWinners(winners);
        if(winner != null){
            List<Player> players = new ArrayList<>();
            int awardRice = 0;
            for(Player player:roomInfo.getPlayers()){
                if(player.getUser().getUid() != winner.getUser().getUid()){
                    for(Card card:player.getShowCardList()){
                        awardRice += card.getRice();
                    }
                    for(Card card:player.getHideCardList()){
                        awardRice += card.getRice();
                    }
                }
            }
            for(Player player:roomInfo.getPlayers()){
                if(player.getUser().getUid() == winner.getUser().getUid()){
                    player.setRice(player.getRice()+awardRice);
                    player.setChangeRice(awardRice);
                }else {
                    int deficitRice = 0;
                    for(Card card:player.getShowCardList()){
                        deficitRice += card.getRice();
                    }
                    for(Card card:player.getHideCardList()){
                        deficitRice += card.getRice();
                    }
                    player.setRice(player.getRice()-deficitRice);
                    player.setChangeRice(0-deficitRice);
                }
//                player.setScore(0);
                players.add(player);
            }
            roomInfo.setPlayers(players);
        }

        return roomInfo;
    }

    //玩家结束本回合
    public RoomInfo end(RoomInfo roomInfo,int uid){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                player.setState("ROUND_END");
            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        return roomInfo;
    }
    //判断是否所有玩家选择结束本回合
    public Boolean isEveryoneEnd(RoomInfo roomInfo){
        for(Player player:roomInfo.getPlayers()){
            if(!player.getState().equalsIgnoreCase("ROUND_END")){
                return false;
            }
        }
        return true;
    }

    //获取手牌数量
    public int getMapSize(HashMap<String,List<Card>> idleCardMap){
        int size = 0;
        //获取手牌总数量
        for(String key : idleCardMap.keySet()){
            size += idleCardMap.get(key).size();
        }
        return size;
    }

    //玩家选择继续
    public Result<RoomInfo> playerSelectContinue(RoomInfo roomInfo,int uid){
        List<Player> players = new ArrayList<>();
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
                if(idleCardMap.get("ant").size() < 4){
                    return Result.error("101","玩家拥有的蚂蚁数量不够参与对战，请获取新的蚂蚁");
                }else{
                    player.setState("CONTINUE");
                }
            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        return Result.success(roomInfo,"success");
    }

    //判断是否所有人都继续
    public Boolean isEveryoneContinue(RoomInfo roomInfo){
        for(Player player:roomInfo.getPlayers()){
            if(!player.getState().equalsIgnoreCase("CONTINUE")){
                return false;
            }
        }
        return true;
    }

    //进行下一回合对战
    public RoomInfo startNew(RoomInfo roomInfo){
//随机选择环境牌
        List<Card> environmentCards = cardService.findByType(1);
        Random r = new Random();
        int x = r.nextInt(environmentCards.size());    //返回一个随机整数
//        System.out.println("size："+environmentCards.size());
//        System.out.println("随机数："+x);
        roomInfo.setEnvironmentCard(environmentCards.get(x));
        roomInfo.setEnvironmentRice(0);

        for(Player player:roomInfo.getPlayers()){
            player.setScore(0);
            player.setChangeRice(0);
            player.setShowCardList(new ArrayList<>());
            player.setHideCardList(new ArrayList<>());
            player.setState("SHOW_START");
        }

        return roomInfo;
    }

    //判断破产
    public Result<RoomInfo> isBankruptcy(RoomInfo roomInfo) {
        List<Player> players = new ArrayList<>();
        Boolean flag = false;
        for(Player player:roomInfo.getPlayers()){
            HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
            if(player.getRice()<0){
                player.setBankruptcy(true);
                flag = true;
            }
            else if(player.getRice()/50 + idleCardMap.get("ant").size() <4){
                player.setBankruptcy(true);
                flag = true;
            }
            players.add(player);
        }
        roomInfo.setPlayers(players);
        if(flag){
            return Result.success(roomInfo,"success");
        }else{
            return Result.error("101","无玩家破产");
        }

    }
    //判断是否游戏介绍
    public Boolean isGameOver(RoomInfo roomInfo){
        int num = 0;
        for(Player player:roomInfo.getPlayers()){
            if(!player.isBankruptcy()){
                num ++;
            }
        }
        if(num == 1){
            return true;
        }else{
            return false;
        }
    }

    //玩家取消准备
    public RoomInfo cancelReady(RoomInfo roomInfo,int uid){
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                player.setState("UNREADY");
            }
        }
        return roomInfo;
    }

    //更换环境
    public RoomInfo changeEnv(RoomInfo roomInfo,int uid,List<Integer> info){
        for(Player player:roomInfo.getPlayers()){
            if(player.getUser().getUid() == uid){
                HashMap<String,List<Card>> idleCardMap = player.getIdleCardMap();
                List<Card> envList = idleCardMap.get("env");
                Card card = envList.get(info.get(0));
                //删除环境手牌，并扣钱
                envList.remove(card);
                idleCardMap.put("env",envList);
                player.setIdleCardMap(idleCardMap);
                player.setRice(player.getRice()-info.get(1));

                //换环境
                roomInfo.setEnvironmentCard(card);
                roomInfo.setEnvironmentRice(info.get(1));
            }
        }
        return roomInfo;
    }

}
