package com.ronixtech.ronixhome.entities;

public class TimeUnit {
    public static final int UNIT_INDEFINITE = 0;
    public static final int UNIT_SECONDS = 1;
    public static final int UNIT_MINUTES = 2;
    public static final int UNIT_HOURS = 3;
    public static final int UNIT_DAYS = 4;

    int id;
    String name;

    public TimeUnit(){
        this.id= -1;
        this.name= "";
    }

    public TimeUnit(int unitID, String unitName){
        this.id = unitID;
        this.name = unitName;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id= id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

    @Override
    public boolean equals(Object object){
        TimeUnit timeUnit = (TimeUnit) object;
        if(timeUnit.getID() == this.id){
            return true;
        }else{
            return false;
        }
    }
}
