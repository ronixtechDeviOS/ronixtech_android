package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

@Dao
public abstract class RoomDAO {
    @Query("SELECT * FROM room")
    public abstract List<Room> getAll();

    @Query("SELECT * FROM room WHERE floor_id=:floorID")
    public abstract List<Room> getFloorRooms(long floorID);

    @Query("SELECT * FROM room WHERE id =:id")
    public abstract Room getRoom(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRoom(Room room);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDevices(List<Device> devices);

    @Query("SELECT * FROM device WHERE room_id =:roomID")
    public abstract List<Device> getDeviceList(long roomID);

    public void insertRoomWithDevices(Room room) {
        List<Device> devices = room.getDevices();
        for (int i = 0; i < devices.size(); i++) {
            devices.get(i).setRoomID(room.getId());
        }
        insertDevices(devices);
        insertRoom(room);
    }

    @Query("DELETE from room WHERE id=:roomID")
    public abstract void removeRoom(long roomID);

    public Room getRoomWithDevices(long id) {
        Room room = getRoom(id);
        List<Device> devices = getDeviceList(id);
        if(room != null){
            room.setDevices(devices);
        }
        return room;
    }

    public void removeRoomsWithDevices(Room room){
        List<Device> roomDevices = MySettings.getRoomDevices(room.getId());
        if(roomDevices != null && roomDevices.size() >= 1){
            for (Device device:roomDevices) {
                MySettings.removeDevice(device);
            }
        }
        removeRoom(room.getId());
    }
}