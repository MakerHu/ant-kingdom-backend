package com.springboot.springbootlogindemo.service;

import com.springboot.springbootlogindemo.domain.CardRelation;

public interface CardRelationService {
    CardRelation findByCard1AndCard2(int card1, int card2);
}
