package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.MySettings;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Place {
    public static final int PLACE_MODE_LOCAL = 0;
    public static final int PLACE_MODE_REMOTE = 1;
    public static final int PLACE_MODE_UNDEFINED = 2;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    @Ignore
    List<Floor> floors;
    @ColumnInfo(name = "type_id")
    public long typeID;
    @ColumnInfo(name = "mode")
    public int mode;

    public Place(){
        this.id = 0;
        this.name = "";
        this.floors = new ArrayList<>();
        this.typeID = -1;
        this.mode = Place.PLACE_MODE_REMOTE;
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

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public long getTypeID(){
        return this.typeID;
    }

    public void setTypeID(long id){
        this.typeID = id;
    }

    public Type getType(){
        if(typeID != -1) {
            return MySettings.getType(typeID);
        }else{
            return MySettings.getTypeByName("House");
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
