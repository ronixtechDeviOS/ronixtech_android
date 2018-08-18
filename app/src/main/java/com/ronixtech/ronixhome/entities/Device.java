package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

//This is the entity for the RonixTech device
@Entity(indices = {@Index(value = {"mac_address"}, unique = true)})
public class Device {
    public static final int DEVICE_TYPE_nowifi_1line = 100001;
    public static final int DEVICE_TYPE_nowifi_2lines = 100002;
    public static final int DEVICE_TYPE_nowifi_3lines = 100003;
    public static final int DEVICE_TYPE_wifi_1line = 4;
    public static final int DEVICE_TYPE_wifi_2lines = 5;
    public static final int DEVICE_TYPE_wifi_3lines = 6;
    public static final int DEVICE_TYPE_wifi_1line_old = 100004;
    public static final int DEVICE_TYPE_wifi_2lines_old = 100005;
    public static final int DEVICE_TYPE_wifi_3lines_old = 100006;
    public static final int MAX_CONSECUTIVE_ERROR_COUNT = 10000; //testing a high number to disable the re-scan feature when it fails temporarily

    public static final int CONTROL_TIMEOUT = 400;
    public static final int CONTROL_NUMBER_OF_RETRIES = 4;

    public static final int REFRESH_RATE_MS = 500;
    public static final int REFRESH_TIMEOUT = 400;
    public static final int REFRESH_NUMBER_OF_RETRIES = 0;

    public static final int CONFIG_TIMEOUT = 200;
    public static final int CONFIG_NUMBER_OF_RETRIES = 10;

    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "mac_address")
    String macAddress;
    @ColumnInfo(name = "chip_id")
    String chipID;
    @ColumnInfo(name = "device_type_id")
    int deviceTypeID;
    @ColumnInfo(name =  "ip_address")
    String ipAddress;
    @ColumnInfo(name = "room_id")
    long roomID;
    @ColumnInfo(name =  "error_count")
    int errorCount;
    //@Relation(parentColumn = "id", entityColumn = "id")
    @Ignore
    List<Line> lines;

    public Device(){
        this.id = 0;
        this.name = "";
        this.macAddress = "";
        this.chipID = "";
        this.deviceTypeID = Device.DEVICE_TYPE_wifi_3lines;
        this.ipAddress = "";
        this.roomID = -1;
        this.errorCount = 0;
        this.lines = new ArrayList<>();
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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getChipID() {
        return chipID;
    }

    public void setChipID(String chipID) {
        this.chipID = chipID;
    }

    public int getDeviceTypeID() {
        return deviceTypeID;
    }

    public void setDeviceTypeID(int deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getRoomID() {
        return roomID;
    }

    public void setRoomID(long roomID) {
        this.roomID = roomID;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(Object object){
        Device device = (Device) object;
        if(device.getId() == this.id){
            return true;
        }else{
            return false;
        }
    }
}
