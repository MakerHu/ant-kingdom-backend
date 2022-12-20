package com.springboot.springbootlogindemo.service.serviceImpl;

import com.springboot.springbootlogindemo.domain.Card;
import com.springboot.springbootlogindemo.repository.CardDao;
import com.springboot.springbootlogindemo.service.CardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    @Resource
    private CardDao cardDao;

    @Override
    public List<Card> getCardList() {
        return cardDao.findAll();
    }

    @Override
    public Card getCardById(int id) {
        return cardDao.findById(id);
    }

    @Override
    public List<Card> findByType(int type) {
        return cardDao.findByType(type);
    }
}
