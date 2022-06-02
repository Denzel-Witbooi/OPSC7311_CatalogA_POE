package com.opsc7311.catalog.util;
import android.app.Application;

/**
 * Singleton
 * Creates a central point of access for a catalog
 * to access the user name and user Id
 */
public class CatalogApi extends Application {

    private String username;
    private String userId;
    private static CatalogApi instance;

    /**
     * Author: Chike Mgbemena
     * Date: Jul 19, 2017
     * Method to get current instance if
     * CatalogApi is null create a new one an
     * assign it.
     * URL: https://code.tutsplus.com/tutorials/android-design-patterns-the-singleton-pattern--cms-29153
     * @return
     */
    public static CatalogApi getInstance(){
        if (instance == null)
            instance = new CatalogApi();
        return instance;
    }

    public CatalogApi()
    {}

    // Getter and Setter's
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
