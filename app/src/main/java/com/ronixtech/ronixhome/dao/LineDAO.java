package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.Line;

import java.util.List;

@Dao
public abstract class LineDAO{
    @Query("SELECT * FROM line")
    public abstract List<Line> getAll();

    @Query("SELECT * FROM line WHERE id =:id")
    public abstract Line getLine(int id);

    @Query("UPDATE line SET power_state =:powerState WHERE id =:lineID")
    public abstract void updateLinePowerState(long lineID, int powerState);

    @Query("UPDATE line SET dimming_state =:dimmingState WHERE id =:lineID")
    public abstract void updateLineDimmingState(long lineID, int dimmingState);

    @Query("UPDATE line SET dimming_value =:dimmingValue WHERE id =:lineID")
    public abstract void updateLineDimmingValue(long lineID, int dimmingValue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLine(Line line);
}