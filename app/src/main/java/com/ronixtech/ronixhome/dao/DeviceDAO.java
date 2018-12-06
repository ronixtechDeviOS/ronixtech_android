package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;
import com.ronixtech.ronixhome.entities.SoundDeviceData;

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


    @Query("UPDATE device SET ip_address =:ipAddress WHERE id =:deviceID")
    public abstract void updateDeviceIP(long deviceID, String ipAddress);

    @Query("UPDATE device SET room_id =:roomID WHERE id =:deviceID")
    public abstract void updateDeviceRoom(long deviceID, long roomID);

    @Query("UPDATE device SET error_count =:count WHERE id =:deviceID")
    public abstract void updateDeviceErrorCount(long deviceID, int count);

    @Query("UPDATE device SET device_type_id =:deviceType WHERE id =:deviceID")
    public abstract void updateDeviceTypeID(long deviceID, int deviceType);

    @Query("DELETE from device WHERE id=:deviceID")
    public abstract void removeDevice(long deviceID);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLines(List<Line> lines);

    @Query("SELECT * FROM line WHERE device_id =:deviceID")
    public abstract List<Line> getLinesList(long deviceID);

    @Query("DELETE from line WHERE device_id=:deviceID")
    public abstract void removeDeviceLines(long deviceID);



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPIRData(PIRData pirData);
    @Query("SELECT * FROM pirdata WHERE device_id =:deviceID")
    public abstract PIRData getPIRData(long deviceID);
    @Query("DELETE from pirdata WHERE device_id=:deviceID")
    public abstract void removeDevicePIRData(long deviceID);


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



    public void insertDeviceWithSoundDeviceData(Device device){
        if(device.getSoundDeviceData() != null) {
            SoundDeviceData soundDeviceData = device.getSoundDeviceData();
            soundDeviceData.setDeviceID(device.getId());
            MySettings.insertSoundDeviceData(soundDeviceData);
        }
        insertDevice(device);
    }
    public void updateDeviceSoundDeviceData(long deviceID, SoundDeviceData soundDeviceData){
        if(soundDeviceData != null) {
            soundDeviceData.setDeviceID(deviceID);
            MySettings.insertSoundDeviceData(soundDeviceData);
        }
    }
    public Device getDeviceWithSoundSystemDataByID(long id) {
        Device device = findByID(id);
        if(device != null) {
            SoundDeviceData soundDeviceData = MySettings.getSoundDeviceData(id);
            if (device != null) {
                device.setSoundDeviceData(soundDeviceData);
            }
        }
        return device;
    }
    public Device getDeviceWithSoundSystemDataByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            SoundDeviceData soundDeviceData = MySettings.getSoundDeviceData(device.getId());
            if (device != null) {
                device.setSoundDeviceData(soundDeviceData);
            }
        }
        return device;
    }
    public Device getDeviceWithSoundSystemDataByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null) {
            SoundDeviceData soundDeviceData = MySettings.getSoundDeviceData(device.getId());
            if (device != null) {
                device.setSoundDeviceData(soundDeviceData);
            }
        }
        return device;
    }
    public void removeDeviceWithSoundDeviceData(Device device){
        MySettings.removeSoundDeviceData(device.getId());
        removeDevice(device.getId());
    }



    public void insertDeviceWithPIRData(Device device){
        if(device.getPIRData() != null) {
            PIRData pirData= device.getPIRData();
            pirData.setDeviceID(device.getId());
            insertPIRData(pirData);
        }
        insertDevice(device);
    }
    public Device getDeviceWithPIRDataByID(long id) {
        Device device = findByID(id);
        if(device != null) {
            PIRData pirData = getPIRData(id);
            if (device != null) {
                device.setPIRData(pirData);
            }
        }
        return device;
    }
    public Device getDeviceWithPIRDataByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            PIRData pirData = getPIRData(device.getId());
            if (device != null) {
                device.setPIRData(pirData);
            }
        }
        return device;
    }
    public Device getDeviceWithPIRDataByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null) {
            PIRData pirData = getPIRData(device.getId());
            if (device != null) {
                device.setPIRData(pirData);
            }
        }
        return device;
    }
    public void removeDeviceWithPIRData(Device device){
        removeDevicePIRData(device.getId());
        removeDevice(device.getId());
    }
}