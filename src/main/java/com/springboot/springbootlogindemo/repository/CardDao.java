package com.springboot.springbootlogindemo.repository;

import com.springboot.springbootlogindemo.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardDao extends JpaRepository<Card, Long> {
    Card findById (int id);
    List<Card> findByType (int type);
}
