package com.springboot.springbootlogindemo.domain;

import javax.persistence.*;

@Table(name = "card")
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int type;

    private int initValue;

    private int rice;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getInitValue() {
        return initValue;
    }

    public void setInitValue(int initValue) {
        this.initValue = initValue;
    }

    public int getRice() {
        return rice;
    }

    public void setRice(int rice) {
        this.rice = rice;
    }
}
