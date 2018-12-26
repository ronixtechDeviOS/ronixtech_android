package com.ronixtech.ronixhome.entities;

public class Backup implements Comparable<Backup>{
    long id;
    String name;
    long timestamp;
    long dbVersion;

    public Backup(){
        this.id = 0;
        this.name = "";
        this.timestamp = 0;
        this.dbVersion = 0;
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

    public long getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(long dbVersion) {
        this.dbVersion = dbVersion;
    }

    @Override
    public int compareTo(Backup other) {

        if(this.timestamp > other.getTimestamp()){
            return -1;
        }else if(this.timestamp <  other.getTimestamp()){
            return 1;
        }else{
            return 0;
        }
    }
}
