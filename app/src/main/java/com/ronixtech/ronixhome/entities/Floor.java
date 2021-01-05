package com.ronixtech.ronixhome.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
    @ColumnInfo(name = "type_id")
    public long typeID;
    @ColumnInfo(name = "type_name")
    public String typeName;

    public Floor(){
        this.id = 0;
        this.name = "";
        this.level = 0;
        this.rooms = new ArrayList<>();
        this.placeID = 0;
        this.typeID = -1;
        this.typeName = "";
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

    public long getTypeID(){
        return this.typeID;
    }

    public void setTypeID(long id){
        this.typeID = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Ignore
    private Type cachedType;
    public Type getType(){
        if(cachedType != null){
            return cachedType;
        }else{
            if(typeName != null && typeName.length() >= 1 && MySettings.getTypeByName(typeName) != null){
                cachedType = MySettings.getTypeByName(typeName);
                return cachedType;
            }else if(typeID != -1 && MySettings.getType(typeID) != null){
                cachedType = MySettings.getType(typeID);
                return cachedType;
            }else{
                cachedType = MySettings.getTypeByName("Floor");
                return cachedType;
            }
        }
    }

    public String getPlaceName(){
        if(MySettings.getPlace(this.placeID) != null){
            return MySettings.getPlace(this.placeID).getName();

        }else{
            return "No place assigned";
        }
    }
}
