package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class SoundDeviceData {
    public static final int MODE_LINE_IN = 0;
    public static final int MODE_UPNP = 1;
    public static final int MODE_USB = 2;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "device_id")
    long deviceID;
    @ColumnInfo(name = "mode")
    int mode;

    public SoundDeviceData(){
        this.id = -1;
        this.deviceID = -1;
        this.mode = SoundDeviceData.MODE_LINE_IN;
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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
