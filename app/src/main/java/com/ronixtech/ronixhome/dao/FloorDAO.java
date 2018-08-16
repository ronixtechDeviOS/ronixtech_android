package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

@Dao
public abstract class FloorDAO {
    @Query("SELECT * FROM floor")
    public abstract List<Floor> getAll();

    @Query("SELECT * FROM floor WHERE place_id=:placeID")
    public abstract List<Floor> getPlaceFloors(long placeID);

    @Query("SELECT * FROM floor WHERE id =:id")
    public abstract Floor getFloor(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFloor(Floor floor);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRooms(List<Room> rooms);

    @Query("SELECT * FROM room WHERE floor_id =:floorID")
    public abstract List<Room> getRoomList(long floorID);

    @Query("DELETE from floor WHERE id=:floorID")
    public abstract void removeFloor(long floorID);

    public void insertFloorWithRooms(Floor floor) {
        List<Room> rooms = floor.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).setFloorID(floor.getId());
        }
        insertRooms(rooms);
        insertFloor(floor);
    }

    public Floor getFloorWithRooms(long id) {
        Floor floor = getFloor(id);
        List<Room> rooms = getRoomList(id);
        if(floor != null) {
            floor.setRooms(rooms);
        }
        return floor;
    }

    public void removeFloorWithRooms(Floor floor){
        List<Room> floorRooms = MySettings.getFloorRooms(floor.getId());
        if(floorRooms != null && floorRooms.size() >= 1){
            for (Room room:floorRooms) {
                MySettings.removeRoom(room);
            }
        }
        removeFloor(floor.getId());
    }
}