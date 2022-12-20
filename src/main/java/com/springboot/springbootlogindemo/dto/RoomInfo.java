package com.springboot.springbootlogindemo.dto;

import com.springboot.springbootlogindemo.domain.Card;

import java.util.List;
import java.util.Stack;

public class RoomInfo {
    private List<Player> players;
    private Stack<Card> cardStack;
    private Card environmentCard; //环境牌
    private int environmentRice; //环境牌价值
    private Player winner;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Stack<Card> getCardStack() {
        return cardStack;
    }

    public void setCardStack(Stack<Card> cardStack) {
        this.cardStack = cardStack;
    }

    public Card getEnvironmentCard() {
        return environmentCard;
    }

    public void setEnvironmentCard(Card environmentCard) {
        this.environmentCard = environmentCard;
    }

    public int getEnvironmentRice() {
        return environmentRice;
    }

    public void setEnvironmentRice(int environmentRice) {
        this.environmentRice = environmentRice;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }
}
