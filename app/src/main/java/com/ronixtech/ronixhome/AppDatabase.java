package com.ronixtech.ronixhome;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ronixtech.ronixhome.dao.PlaceDAO;
import com.ronixtech.ronixhome.dao.RoomDAO;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.dao.DeviceDAO;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.dao.FloorDAO;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.dao.LineDAO;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.User;
import com.ronixtech.ronixhome.dao.UserDAO;

@Database(entities = {Line.class, Device.class, Room.class, Floor.class, Place.class, User.class}, version = 1, exportSchema =  false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LineDAO lineDAO();
    public abstract DeviceDAO deviceDAO();
    public abstract RoomDAO roomDAO();
    public abstract FloorDAO floorDAO();
    public abstract PlaceDAO placeDAO();
    public abstract UserDAO userDao();
}