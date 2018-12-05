package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.Speaker;

import java.util.List;

@Dao
public abstract class SpeakerDAO {
    @Query("SELECT * FROM speaker")
    public abstract List<Speaker> getAll();

    @Query("SELECT * FROM speaker WHERE id =:id")
    public abstract Speaker getSpeaker(long id);


    @Query("UPDATE speaker SET volume =:volume WHERE id =:speakerID")
    public abstract void updateSpeakerVolume(long speakerID, int volume);

    @Query("UPDATE speaker SET name =:name WHERE id =:lineID")
    public abstract void updateSpeakerName(long lineID, String name);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSpeaker(Speaker speaker);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSpeakers(List<Speaker> speakers);


    @Query("SELECT * FROM speaker WHERE sound_device_id =:soundDeviceID")
    public abstract List<Speaker> getSoundDeviceSpeakers(long soundDeviceID);

    @Query("DELETE from speaker WHERE sound_device_id=:soundDeviceID")
    public abstract void removeSpeaker(long soundDeviceID);
}
