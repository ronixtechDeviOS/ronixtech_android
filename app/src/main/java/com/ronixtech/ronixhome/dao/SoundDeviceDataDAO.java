package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.SoundDeviceData;

@Dao
public abstract class SoundDeviceDataDAO {
    @Query("SELECT * FROM sounddevicedata WHERE device_id =:deviceID")
    public abstract SoundDeviceData getSoundDeviceData(int deviceID);

    @Query("UPDATE sounddevicedata SET mode =:mode WHERE id =:soundDeviceID")
    public abstract void updateMode(long soundDeviceID, int mode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSoundDeviceData(SoundDeviceData data);
}
