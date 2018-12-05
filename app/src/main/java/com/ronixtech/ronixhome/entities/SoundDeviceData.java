package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SoundDeviceData {
    public static final int MODE_LINE_IN = 0;
    public static final int MODE_LINE_IN_2 = 1;
    public static final int MODE_UPNP = 2;
    public static final int MODE_USB = 3;

    public static final int MAX_NUMBER_OF_SPEAKERS = 8;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "device_id")
    long deviceID;
    @ColumnInfo(name = "mode")
    int mode;
    @Ignore
    List<Speaker> speakers;

    public SoundDeviceData(){
        this.id = -1;
        this.deviceID = -1;
        this.mode = SoundDeviceData.MODE_LINE_IN;
        this.speakers = new ArrayList<>();
    }

    public SoundDeviceData(SoundDeviceData soundDeviceData){
        this.id = soundDeviceData.getId();
        this.deviceID = soundDeviceData.getDeviceID();
        this.mode = soundDeviceData.getMode();
        for (Speaker speaker:soundDeviceData.getSpeakers()) {
            Speaker newSpeaker = new Speaker(speaker);
            this.speakers.add(newSpeaker);
        }
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

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }
}
