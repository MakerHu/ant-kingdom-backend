package com.springboot.springbootlogindemo.domain;

import javax.persistence.*;

@Table(name = "card_relation")
@Entity
public class CardRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int card1;

    private int card2;

    private String valueImpact;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCard1() {
        return card1;
    }

    public void setCard1(int card1) {
        this.card1 = card1;
    }

    public int getCard2() {
        return card2;
    }

    public void setCard2(int card2) {
        this.card2 = card2;
    }

    public String getValueImpact() {
        return valueImpact;
    }

    public void setValueImpact(String valueImpact) {
        this.valueImpact = valueImpact;
    }
}
