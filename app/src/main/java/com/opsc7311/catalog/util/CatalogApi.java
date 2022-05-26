package com.opsc7311.catalog.util;
import android.app.Application;
public class CatalogApi extends Application {

    private String username;
    private String userId;
    private static CatalogApi instance;

    public static CatalogApi getInstance(){
        if (instance == null)
            instance = new CatalogApi();
        return instance;
    }

    public CatalogApi()
    {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
