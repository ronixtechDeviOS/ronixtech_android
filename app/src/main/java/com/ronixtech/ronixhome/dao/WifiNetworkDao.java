package com.ronixtech.ronixhome.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.List;

@Dao
public abstract class WifiNetworkDao {
    @Query("SELECT * FROM wifinetwork")
    public abstract List<WifiNetwork> getAll();

    @Query("SELECT * FROM wifinetwork WHERE place_id =:placeID")
    public abstract List<WifiNetwork> getPlaceWifiNetworks(long placeID);

    @Query("SELECT * FROM wifinetwork WHERE id =:id")
    public abstract WifiNetwork getWifiNetwork(long id);

    @Query("SELECT * FROM wifinetwork WHERE ssid LIKE :ssid")
    public abstract WifiNetwork getWifiNetworkBySSID(String ssid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertWifiNetwork(WifiNetwork wifiNetwork);

    @Query("UPDATE wifinetwork SET place_id =:placeID WHERE id =:wifiNetworkID")
    public abstract void updateWifiNetworkPlaceID(long wifiNetworkID, long placeID);

    @Query("DELETE from wifinetwork WHERE id=:wifiNetworkID")
    public abstract void removeWifiNetwork(long wifiNetworkID);
}