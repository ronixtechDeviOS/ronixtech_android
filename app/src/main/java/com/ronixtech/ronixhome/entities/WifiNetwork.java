package com.ronixtech.ronixhome.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//This is an entity that represents a WiFi network, with an SSID, password, and signal strength, along with a placeID
@Entity()
public class WifiNetwork {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "ssid")
    String ssid;
    @ColumnInfo(name = "mac_address")
    String macAddress;
    @ColumnInfo(name = "signal_strength")
    String signal;
    @ColumnInfo(name = "password")
    String password;
    @ColumnInfo(name = "place_id")
    long placeID;

    public WifiNetwork(){
        this.id = 0;
        this.ssid = "";
        this.macAddress = "";
        this.signal = "";
        this.password = "";
        this.placeID = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getPlaceID() {
        return placeID;
    }

    public void setPlaceID(long placeID) {
        this.placeID = placeID;
    }

    @Override
    public boolean equals(Object o){
        WifiNetwork secondNetwork = (WifiNetwork) o;
        if(this.ssid.equals(secondNetwork.getSsid())){
            return true;
        }else{
            return false;
        }
    }
}
