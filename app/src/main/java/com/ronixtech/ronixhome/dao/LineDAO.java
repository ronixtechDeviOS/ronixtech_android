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


    @Query("UPDATE line SET name =:name WHERE id =:lineID")
    public abstract void updateLineName(long lineID, String name);

    @Query("UPDATE line SET power_usage =:powerUsage WHERE id =:lineID")
    public abstract void updateLinePowerUsage(long lineID, double powerUsage);

    @Query("UPDATE line SET type_id =:typeID WHERE id =:lineID")
    public abstract void updateLineTypeID(long lineID, long typeID);

    @Query("UPDATE line SET type_name =:typeName WHERE id =:lineID")
    public abstract void updateLineTypeName(long lineID, String typeName);

    @Query("UPDATE line SET mode =:mode WHERE id =:lineID")
    public abstract void updateLineMode(long lineID, int mode);

    @Query("UPDATE line SET primary_device_chip_id =:primaryDeviceChipID WHERE id =:lineID")
    public abstract void updateLinePrimaryDeviceChipID(long lineID, String primaryDeviceChipID);

    @Query("UPDATE line SET primary_line_position =:primaryLinePosition WHERE id =:lineID")
    public abstract void updateLinePrimaryLinePosition(long lineID, int primaryLinePosition);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLine(Line line);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLines(List<Line> lines);


    @Query("SELECT * FROM line WHERE primary_device_chip_id =:mainDeviceChipID")
    public abstract List<Line> getSecondaryLine(String mainDeviceChipID);
}

