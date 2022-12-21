package com.springboot.springbootlogindemo.dto;

import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.domain.User;

import java.util.List;

public class Player {
    private User user;
    private String state;
    private List<Card> idleCardList; //手中的空闲牌
    private List<Card> showCardList; //亮出的牌
    private List<Card> hideCardList; //翻面的牌
    private int rice;
    private int finalValue;
    private boolean isBankruptcy; //是否破产

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Card> getIdleCardList() {
        return idleCardList;
    }

    public void setIdleCardList(List<Card> idleCardList) {
        this.idleCardList = idleCardList;
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
        return isBankruptcy;
    }

    public void setBankruptcy(boolean bankruptcy) {
        isBankruptcy = bankruptcy;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}