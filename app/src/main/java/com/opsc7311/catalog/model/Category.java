package com.opsc7311.catalog.model;

import com.google.firebase.Timestamp;

public class Category {

    private String name;
    private int amount;

    private String userId;
    private Timestamp timeAdded;
    private String userName;

    public Category() {
    }

    public Category(String name, int amount, String userId, Timestamp timeAdded, String userName) {
        this.name = name;
        this.amount = amount;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
