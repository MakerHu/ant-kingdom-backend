package com.springboot.springbootlogindemo.service;

import com.springboot.springbootlogindemo.domain.Card;

import java.util.List;

public interface CardService {
    List<Card> getCardList();
    Card getCardById(int id);
    List<Card> findByType (int type);
}
