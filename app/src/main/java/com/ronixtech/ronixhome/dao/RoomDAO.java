package com.ronixtech.ronixhome.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

@Dao
public abstract class RoomDAO {
    @Query("SELECT * FROM room")
    public abstract List<Room> getAll();

    @Query("SELECT * FROM room WHERE name LIKE :roomName")
    public abstract Room getRoomByName(String roomName);

    @Query("SELECT * FROM room WHERE floor_id=:floorID")
    public abstract List<Room> getFloorRooms(long floorID);

    @Query("SELECT * FROM room WHERE id =:id")
    public abstract Room getRoom(long id);


    @Query("SELECT * FROM device WHERE room_id =:roomID")
    public abstract List<Device> getDeviceList(long roomID);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRoom(Room room);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDevices(List<Device> devices);


    @Query("UPDATE room SET name =:newName WHERE id =:roomID")
    public abstract void updateRoomName(long roomID, String newName);

    @Query("UPDATE room SET type_id =:newType WHERE id =:roomID")
    public abstract void updateRoomType(long roomID, long newType);

    @Query("UPDATE room SET type_name =:newType WHERE id =:roomID")
    public abstract void updateRoomType(long roomID, String newType);

    @Query("UPDATE room SET floor_id =:newFloor WHERE id =:roomID")
    public abstract void updateRoomFloor(long roomID, long newFloor);


    @Query("DELETE from room WHERE id=:roomID")
    public abstract void removeRoom(long roomID);


    public void insertRoomWithDevices(Room room) {
        List<Device> devices = room.getDevices();
        for (int i = 0; i < devices.size(); i++) {
            devices.get(i).setRoomID(room.getId());
        }
        insertDevices(devices);
        insertRoom(room);
    }

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