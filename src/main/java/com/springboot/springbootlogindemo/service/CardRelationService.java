package com.springboot.springbootlogindemo.service;

import com.springboot.springbootlogindemo.domain.CardRelation;

import java.util.List;

public interface CardRelationService {
    CardRelation findByCard1AndCard2(int card1, int card2);

    List<CardRelation> findByCard1(int card1);
}
