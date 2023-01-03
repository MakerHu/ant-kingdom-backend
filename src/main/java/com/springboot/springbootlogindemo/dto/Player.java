package com.springboot.springbootlogindemo.dto;

import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.domain.User;

import java.util.HashMap;
import java.util.List;

public class Player {
    private User user;
    private String state;
    private HashMap<String,List<Card>> idleCardMap; //手中的空闲牌
    private List<Card> showCardList; //亮出的牌
    private List<Card> hideCardList; //翻面的牌
    private int rice;
    private int changeRice;
    private int score;
    private int finalValue;
    private boolean bankruptcy; //是否破产
    private boolean isOffLine;

    public HashMap<String, List<Card>> getIdleCardMap() {
        return idleCardMap;
    }

    public void setIdleCardMap(HashMap<String, List<Card>> idleCardMap) {
        this.idleCardMap = idleCardMap;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Card> getShowCardList() {
        return showCardList;
    }

    public void setShowCardList(List<Card> showCardList) {
        this.showCardList = showCardList;
    }

    public List<Card> getHideCardList() {
        return hideCardList;
    }

    public void setHideCardList(List<Card> hideCardList) {
        this.hideCardList = hideCardList;
    }

    public int getRice() {
        return rice;
    }

    public void setRice(int rice) {
        this.rice = rice;
    }

    public int getFinalValue() {
        return finalValue;
    }

    public void setFinalValue(int finalValue) {
        this.finalValue = finalValue;
    }

    public boolean isBankruptcy() {
        return bankruptcy;
    }

    public void setBankruptcy(boolean bankruptcy) {
        this.bankruptcy = bankruptcy;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getChangeRice() {
        return changeRice;
    }

    public void setChangeRice(int changeRice) {
        this.changeRice = changeRice;
    }

    public boolean isOffLine() {
        return isOffLine;
    }

    public void setOffLine(boolean offLine) {
        isOffLine = offLine;
    }
}
