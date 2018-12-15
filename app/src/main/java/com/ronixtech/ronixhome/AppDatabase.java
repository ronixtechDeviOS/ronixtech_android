package com.ronixtech.ronixhome;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ronixtech.ronixhome.dao.DeviceDAO;
import com.ronixtech.ronixhome.dao.FloorDAO;
import com.ronixtech.ronixhome.dao.LineDAO;
import com.ronixtech.ronixhome.dao.PlaceDAO;
import com.ronixtech.ronixhome.dao.RoomDAO;
import com.ronixtech.ronixhome.dao.SoundDeviceDataDAO;
import com.ronixtech.ronixhome.dao.SpeakerDAO;
import com.ronixtech.ronixhome.dao.TypeDAO;
import com.ronixtech.ronixhome.dao.UserDAO;
import com.ronixtech.ronixhome.dao.WifiNetworkDao;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.entities.Speaker;
import com.ronixtech.ronixhome.entities.Type;
import com.ronixtech.ronixhome.entities.User;
import com.ronixtech.ronixhome.entities.WifiNetwork;

@Database(entities = {Type.class, WifiNetwork.class, Speaker.class, Line.class, SoundDeviceData.class, PIRData.class, Device.class, Room.class, Floor.class, Place.class, User.class}, version = 26, exportSchema =  false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TypeDAO typeDAO();
    public abstract SpeakerDAO speakerDAO();
    public abstract LineDAO lineDAO();
    public abstract SoundDeviceDataDAO soundDeviceDataDAO();
    public abstract DeviceDAO deviceDAO();
    public abstract RoomDAO roomDAO();
    public abstract FloorDAO floorDAO();
    public abstract PlaceDAO placeDAO();
    public abstract WifiNetworkDao wifiNetworkDao();
    public abstract UserDAO userDao();
}