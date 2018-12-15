package com.ronixtech.ronixhome.entities;

public class Backup {
    long id;
    String name;
    long timestamp;

    public Backup(){
        this.id = 0;
        this.name = "";
        this.timestamp = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
