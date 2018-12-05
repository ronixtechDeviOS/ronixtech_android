package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

//This is an entity that represents a specific speaker
@Entity
public class Speaker {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "volume")
    int volume;
    @ColumnInfo(name = "sound_device_id")
    long soundDeviceID;

    public Speaker(){
        this.id = 0;
        this.name = "";
        this.volume = 0;
        this.soundDeviceID = -1;
    }

    public Speaker(Speaker speaker){
        this.id = speaker.getId();
        this.name = speaker.getName();
        this.volume = speaker.getVolume();
        this.soundDeviceID = speaker.getSoundDeviceID();
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

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public long getSoundDeviceID() {
        return soundDeviceID;
    }

    public void setSoundDeviceID(long soundDeviceID) {
        this.soundDeviceID = soundDeviceID;
    }
}
