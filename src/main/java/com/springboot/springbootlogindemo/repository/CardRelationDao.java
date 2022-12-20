package com.springboot.springbootlogindemo.repository;

import com.springboot.springbootlogindemo.domain.CardRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRelationDao extends JpaRepository<CardRelation, Long> {
    CardRelation findByCard1AndCard2(int card1,int card2);
}
