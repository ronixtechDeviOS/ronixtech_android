package com.ronixtech.ronixhome.alexa;

public abstract class AvsItem {
    String token;
    public AvsItem(String token){
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}