package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import java.util.List;

@Dao
public abstract class DeviceDAO {
    @Query("SELECT * FROM device")
    public abstract List<Device> getAll();

    @Query("SELECT * FROM device WHERE room_id=:roomID")
    public abstract List<Device> getRoomDevices(long roomID);

    @Query("SELECT * FROM device WHERE id =:id")
    public abstract Device findByID(long id);

    @Query("SELECT * FROM device WHERE mac_address =:macAddress")
    public abstract Device findByMAC(String macAddress);

    @Query("SELECT * FROM device WHERE chip_id =:chipID")
    public abstract Device findByChipID(String chipID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDevice(Device device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLines(List<Line> lines);

    @Query("DELETE from device WHERE id=:deviceID")
    public abstract void removeDevice(long deviceID);

    @Query("DELETE from line WHERE device_id=:deviceID")
    public abstract void removeDeviceLines(long deviceID);

    @Query("SELECT * FROM line WHERE device_id =:deviceID")
    public abstract List<Line> getLinesList(long deviceID);

    @Query("UPDATE device SET ip_address =:ipAddress WHERE id =:deviceID")
    public abstract void updateDeviceIP(long deviceID, String ipAddress);

    @Query("UPDATE device SET error_count =:count WHERE id =:deviceID")
    public abstract void updateDeviceErrorCount(long deviceID, int count);

    public void insertDeviceWithLines(Device device) {
        List<Line> lines = device.getLines();
        if(lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                lines.get(i).setDeviceID(device.getId());
            }
            insertLines(lines);
        }
        insertDevice(device);
    }

    public Device getDeviceWithLinesByID(long id) {
        Device device = findByID(id);
        if(device != null) {
            List<Line> lines = getLinesList(id);
            if (device != null) {
                device.setLines(lines);
            }
        }
        return device;
    }

    public Device getDeviceWithLinesByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            List<Line> lines = getLinesList(device.getId());
            if (device != null) {
                device.setLines(lines);
            }
        }
        return device;
    }

    public Device getDeviceWithLinesByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null){
            List<Line> lines = getLinesList(device.getId());
            if(device != null){
                device.setLines(lines);
            }
        }
        return device;
    }

    public void removeDeviceWithLines(Device device){
        removeDeviceLines(device.getId());
        removeDevice(device.getId());
    }
}