package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.domain.CardRelation;
import com.springboot.springbootlogindemo.repository.CardRelationDao;
import com.springboot.springbootlogindemo.service.CardRelationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CardRelationServiceImpl implements CardRelationService {

    @Resource
    private CardRelationDao cardRelationDao;


    @Override
    public CardRelation findByCard1AndCard2(int card1, int card2) {
        return cardRelationDao.findByCard1AndCard2(card1,card2);
    }
}
