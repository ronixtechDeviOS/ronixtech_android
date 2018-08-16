package com.ronixtech.ronixhome.entities;

public class Type {
    int id;
    String name;
    String imageUrl;

    public Type(){
        this.id = 0;
        this.name = "";
        this.imageUrl = "";
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
