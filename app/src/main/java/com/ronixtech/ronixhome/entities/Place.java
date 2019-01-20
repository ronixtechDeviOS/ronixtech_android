package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.MySettings;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Place {
    public static final int PLACE_MODE_LOCAL = 0;
    public static final int PLACE_MODE_REMOTE = 1;
    public static final int PLACE_MODE_UNDEFINED = 2;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    @Ignore
    List<Floor> floors;
    @ColumnInfo(name = "type_id")
    public long typeID;
    @ColumnInfo(name = "type_name")
    public String typeName;
    @ColumnInfo(name = "mode")
    public int mode;
    @ColumnInfo(name = "latitude")
    double latitude;
    @ColumnInfo(name = "longitude")
    double longitude;
    @ColumnInfo(name = "address")
    String address;
    @ColumnInfo(name = "city")
    String city;
    @ColumnInfo(name = "state")
    String state;
    @ColumnInfo(name = "country")
    String country;
    @ColumnInfo(name = "zip_code")
    String zipCode;
    @Ignore
    boolean defaultPlace;
    @Ignore
    List<WifiNetwork> wifiNetworks;

    public Place(){
        this.id = 0;
        this.name = "";
        this.floors = new ArrayList<>();
        this.typeID = -1;
        this.typeName = "";
        this.mode = Place.PLACE_MODE_REMOTE;
        this.latitude = 0;
        this.longitude = 0;
        this.address = "";
        this.city = "";
        this.state = "";
        this.country = "";
        this.zipCode = "";
        this.defaultPlace = false;
        this.wifiNetworks = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public long getTypeID(){
        return this.typeID;
    }

    public void setTypeID(long id){
        this.typeID = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Ignore
    private Type cachedType;
    public Type getType(){
        if(cachedType != null){
            return cachedType;
        }else{
            if(typeName != null && typeName.length() >= 1 && MySettings.getTypeByName(typeName) != null){
                cachedType = MySettings.getTypeByName(typeName);
                return cachedType;
            }else if(typeID != -1 && MySettings.getType(typeID) != null){
                cachedType = MySettings.getType(typeID);
                return cachedType;
            }else{
                cachedType = MySettings.getTypeByName("House");
                return cachedType;
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public boolean isDefaultPlace() {
        return defaultPlace;
    }

    public void setDefaultPlace(boolean defaultPlace) {
        this.defaultPlace = defaultPlace;
    }

    public List<WifiNetwork> getWifiNetworks() {
        return wifiNetworks;
    }

    public void setWifiNetworks(List<WifiNetwork> wifiNetworks) {
        this.wifiNetworks = wifiNetworks;
    }
}
