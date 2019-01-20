package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.MySettings;

import java.util.ArrayList;
import java.util.List;

//This is an entity that represents a room. A room has at least 1 RonixTech device, and is contained in only 1 floor
@Entity
public class Room {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    //@Relation(parentColumn = "id", entityColumn = "id")
    @Ignore
    List<Device> devices;
    @ColumnInfo(name = "floor_id")
    long floorID;
    /*@ColumnInfo(name = "floor")
    Floor floor;*/
    @ColumnInfo(name = "type_id")
    public long typeID;
    @ColumnInfo(name = "type_name")
    public String typeName;

    public Room(){
        this.id = 0;
        this.name = "";
        this.devices = new ArrayList<>();
        this.floorID = -1;
        //this.floor = new Floor();
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

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public long getFloorID() {
        return floorID;
    }

    public void setFloorID(long floorID) {
        this.floorID = floorID;
    }

    public String getFloorName(){
        if(MySettings.getFloor(this.floorID) != null){
            return MySettings.getFloor(this.floorID).getName();

        }else{
            return "No floor assigned";
        }
    }

    public String getFloorLevel(){
        if(MySettings.getFloor(this.floorID) != null){
            return ""+MySettings.getFloor(this.floorID).getLevel();

        }else{
            return "No floor assigned";
        }
    }

    /*public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }*/

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
                cachedType = MySettings.getTypeByName("Living Room");
                return cachedType;
            }
        }
    }
}
