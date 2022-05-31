package com.opsc7311.catalog.model;

import com.google.firebase.Timestamp;

public class Catalog {
    private String catalogId;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private String userId;
    private Timestamp timeAdded;
    private String userName;

    public Catalog() {
    }

    public Catalog(String catalogId, String title, String category, String description, String imageUrl, String userId, Timestamp timeAdded, String userName) {
        this.catalogId = catalogId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
