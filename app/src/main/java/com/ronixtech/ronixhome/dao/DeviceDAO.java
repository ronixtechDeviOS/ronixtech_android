package com.ronixtech.ronixhome.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

    @Query("SELECT * FROM device WHERE name =:deviceName")
    public abstract Device findByDeviceName(String deviceName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDevice(Device device);


    @Query("UPDATE device SET ip_address =:ipAddress WHERE id =:deviceID")
    public abstract void updateDeviceIP(long deviceID, String ipAddress);

    @Query("UPDATE device SET name =:DeviceName WHERE id =:deviceID")
    public abstract void updateDeviceName(long deviceID, String DeviceName);

    @Query("UPDATE device SET mqtt_reachable =:mqtt WHERE id =:deviceID")
    public abstract void updateDeviceMQTTreachable(long deviceID,Integer mqtt);

    @Query("UPDATE device SET room_id =:roomID WHERE id =:deviceID")
    public abstract void updateDeviceRoom(long deviceID, long roomID);

    @Query("UPDATE device SET error_count =:count WHERE id =:deviceID")
    public abstract void updateDeviceErrorCount(long deviceID, int count);

    @Query("UPDATE device SET device_type_id =:deviceType WHERE id =:deviceID")
    public abstract void updateDeviceTypeID(long deviceID, int deviceType);

    @Delete
    public abstract void removeDevice(Device device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLines(List<Line> lines);

    @Query("SELECT * FROM line WHERE device_id =:deviceID")
    public abstract List<Line> getLinesList(long deviceID);

    @Delete
    public abstract void removeDeviceLines(Line line);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPIRData(PIRData pirData);
    @Query("SELECT * FROM pirdata WHERE device_id =:deviceID")
    public abstract PIRData getPIRData(long deviceID);
    @Query("DELETE from pirdata WHERE device_id=:deviceID")
    public abstract void removeDevicePIRData(long deviceID);


    public void insertDeviceWithLines(Device device) {
        List<Line> lines = device.getLines();
        if(lines != null && lines.size()<=3) {
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
            device.setLines(lines);
        }
        return device;
    }
    public Device getDeviceWithLinesByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            List<Line> lines = getLinesList(device.getId());
            device.setLines(lines);
        }
        return device;
    }
    public Device getDeviceWithLinesByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null){
            List<Line> lines = getLinesList(device.getId());
            device.setLines(lines);
        }
        return device;
    }
    public void removeDeviceWithLines(Device device){
        for(int i=0;i<device.getLines().size();i++)
        {
            removeDeviceLines(device.getLines().get(i));
        }
        removeDevice(device);
    }

    public Device getDeviceWithDeviceName(String name)
    {
        Device device = findByDeviceName(name);
        if(device != null){
            List<Line> lines = getLinesList(device.getId());
            device.setLines(lines);
        }
        return device;
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
            device.setSoundDeviceData(soundDeviceData);
            return device;
        }
      return null;
    }
    public Device getDeviceWithSoundSystemDataByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            SoundDeviceData soundDeviceData = MySettings.getSoundDeviceData(device.getId());
            device.setSoundDeviceData(soundDeviceData);
        }
        return device;
    }
    public Device getDeviceWithSoundSystemDataByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null) {
            SoundDeviceData soundDeviceData = MySettings.getSoundDeviceData(device.getId());
            device.setSoundDeviceData(soundDeviceData);
        }
        return device;
    }
    public void removeDeviceWithSoundDeviceData(Device device){
        MySettings.removeSoundDeviceData(device.getId());
         removeDevice(device);
    }



    public void insertDeviceWithPIRData(Device device){
        if(device.getPIRData() != null) {
            PIRData pirData= device.getPIRData();
            pirData.setDeviceID(device.getId());
            insertPIRData(pirData);
        }
        List<Line> lines = device.getLines();
        if(lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                lines.get(i).setDeviceID(device.getId());
            }
            insertLines(lines);
        }
        insertDevice(device);
    }
    public Device getDeviceWithPIRDataByID(long id) {
        Device device = findByID(id);
        if(device != null) {
            PIRData pirData = getPIRData(id);
            device.setPIRData(pirData);
            List<Line> lines = getLinesList(id);
            device.setLines(lines);
        }
        return device;
    }
    public Device getDeviceWithPIRDataByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        if(device != null) {
            PIRData pirData = getPIRData(device.getId());
            device.setPIRData(pirData);
            List<Line> lines = getLinesList(device.getId());
            device.setLines(lines);
        }
        return device;
    }
    public Device getDeviceWithPIRDataByChipID(String chipID) {
        Device device = findByChipID(chipID);
        if(device != null) {
            PIRData pirData = getPIRData(device.getId());
            device.setPIRData(pirData);
            List<Line> lines = getLinesList(device.getId());
            device.setLines(lines);
        }
        return device;
    }
    public void removeDeviceWithPIRData(Device device){
        removeDevicePIRData(device.getId());
        for(int i=0;i<device.getLines().size();i++)
        {
            removeDeviceLines(device.getLines().get(i));
        }
        removeDevice(device);
    }


    public void insertShutter(Device device){
        insertDevice(device);
    }
    public Device getShutterByID(long id) {
        Device device = findByID(id);
        return device;
    }
    public Device getShutterByMacAddress(String macAddress) {
        Device device = findByMAC(macAddress);
        return device;
    }
    public Device getShutterByChipID(String chipID) {
        Device device = findByChipID(chipID);
        return device;
    }
    public void removeShutter(Device device){
        removeDevice(device);
    }
}