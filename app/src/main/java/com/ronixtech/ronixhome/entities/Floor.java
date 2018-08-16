package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.MySettings;

import java.util.ArrayList;
import java.util.List;

//This is an entity that represents a floor in a place, which has multiple rooms
//A place has at least 1 floor and 1 room
@Entity
public class Floor {
    public static final int MAX_NUMBER = 30;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "level")
    int level;
    //@Relation(parentColumn = "id", entityColumn = "id")
    @Ignore
    List<Room> rooms;
    @ColumnInfo(name = "place_id")
    long placeID;

    public Floor(){
        this.id = 0;
        this.name = "";
        this.level = 0;
        this.rooms = new ArrayList<>();
        this.placeID = 0;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public long getPlaceID() {
        return placeID;
    }

    public void setPlaceID(long placeID) {
        this.placeID = placeID;
    }

    public String getPlaceName(){
        if(MySettings.getPlace(this.placeID) != null){
            return MySettings.getPlace(this.placeID).getName();

        }else{
            return "No place assigned";
        }
    }
}
