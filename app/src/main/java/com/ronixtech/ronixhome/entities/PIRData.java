package com.ronixtech.ronixhome.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PIRData {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "device_id")
    long deviceID;
    @ColumnInfo(name = "state")
    int state;

    public PIRData(){
        this.id = 0;
        this.deviceID = -1;
        this.state = Line.LINE_STATE_OFF;
    }

    public PIRData(PIRData pirData){
        this.id = pirData.getId();
        this.deviceID = pirData.getDeviceID();
        this.state = pirData.getState();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(long deviceID) {
        this.deviceID = deviceID;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
