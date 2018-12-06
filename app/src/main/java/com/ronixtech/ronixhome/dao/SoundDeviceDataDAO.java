package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.entities.Speaker;

import java.util.List;

@Dao
public abstract class SoundDeviceDataDAO {
    @Query("SELECT * FROM sounddevicedata WHERE device_id =:deviceID")
    public abstract SoundDeviceData getSoundDeviceData(long deviceID);

    @Query("UPDATE sounddevicedata SET mode =:mode WHERE id =:soundDeviceID")
    public abstract void updateMode(long soundDeviceID, int mode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSoundDeviceData(SoundDeviceData data);

    @Query("DELETE from sounddevicedata WHERE device_id=:deviceID")
    public abstract void removeDeviceSoundDeviceData(long deviceID);


    public void insertSoundDeviceDataWithSpeakers(SoundDeviceData soundDeviceData){
        List<Speaker> speakers = soundDeviceData.getSpeakers();
        if(speakers != null) {
            for (int i = 0; i < speakers.size(); i++) {
                speakers.get(i).setSoundDeviceID(soundDeviceData.getId());
            }
            MySettings.insertSpeakers(speakers);
        }
        insertSoundDeviceData(soundDeviceData);
    }
    public void updateSoundDeviceDataSpeakers(long soundDeviceDataID, List<Speaker> speakers){
        if(speakers != null) {
            for (int i = 0; i < speakers.size(); i++) {
                speakers.get(i).setSoundDeviceID(soundDeviceDataID);
            }
            MySettings.insertSpeakers(speakers);
        }
    }

    public SoundDeviceData getSoundDeviceDataWithSpeakers(long deviceID){
        SoundDeviceData soundDeviceData = getSoundDeviceData(deviceID);
        if(soundDeviceData != null) {
            List<Speaker> speakers = MySettings.getSpeakers(soundDeviceData.getId());
            if (soundDeviceData != null) {
                soundDeviceData.setSpeakers(speakers);
            }
        }
        return soundDeviceData;
    }

    public void removeSoundDeviceDataWithSpeakers(long deviceID){
        SoundDeviceData soundDeviceData = getSoundDeviceData(deviceID);
        if(soundDeviceData != null){
            MySettings.removeSpeaker(soundDeviceData.getId());
        }
        removeDeviceSoundDeviceData(deviceID);
    }
}
