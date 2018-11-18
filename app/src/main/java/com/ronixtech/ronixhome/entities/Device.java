package com.ronixtech.ronixhome.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.ronixtech.ronixhome.Constants;

import java.util.ArrayList;
import java.util.List;

//This is the entity for the RonixTech device
@Entity(indices = {@Index(value = {"mac_address"}, unique = true)})
public class Device implements Comparable {
    public static final int DEVICE_TYPE_nowifi_1line = 100001;
    public static final int DEVICE_TYPE_nowifi_2lines = 100002;
    public static final int DEVICE_TYPE_nowifi_3lines = 100003;
    public static final int DEVICE_TYPE_wifi_1line = 4;
    public static final int DEVICE_TYPE_wifi_2lines = 5;
    public static final int DEVICE_TYPE_wifi_3lines = 6;
    public static final int DEVICE_TYPE_wifi_1line_old = 100004;
    public static final int DEVICE_TYPE_wifi_2lines_old = 100005;
    public static final int DEVICE_TYPE_wifi_3lines_old = 100006;
    public static final int DEVICE_TYPE_wifi_3lines_workaround = 100050;
    public static final int DEVICE_TYPE_PIR_MOTION_SENSOR = 100051;
    public static final int DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER = 100070;
    public static final int DEVICE_TYPE_PLUG_1lines = 100031;
    public static final int DEVICE_TYPE_PLUG_2lines = 100032;
    public static final int DEVICE_TYPE_PLUG_3lines = 100033;
    public static final int DEVICE_NUMBER_OF_TYPES = 3; //lines, pir sensor, speaker controller
    public static final int MAX_CONSECUTIVE_ERROR_COUNT = 30;

    public static final int CONTROL_TIMEOUT = 350;
    public static final int CONTROL_NUMBER_OF_RETRIES = 1;

    public static final int REFRESH_RATE_MS = 500;
    public static final int REFRESH_TIMEOUT = 250;
    public static final int REFRESH_NUMBER_OF_RETRIES = 0;

    public static final int CONFIG_TIMEOUT = 1000;
    public static final int CONFIG_NUMBER_OF_RETRIES = 10;

    public static final int MODE_PRIMARY = 0;
    public static final int MODE_SECONDARY = 1;

    public static final String DEVICE_BASE_FIRMWARE = "101400";
    public static final int SYNC_CONTROLS_STATUS_FIRMWARE_VERSION = 101405;
    public static final int DEVICE_FIRMWARE_VERSION_AUTO_UPDATE_METHOD = 101500;

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
    @Ignore
    SoundDeviceData soundDeviceData;
    @ColumnInfo(name = "access_token")
    String accessToken;
    @ColumnInfo(name = "last_seen_timestamp")
    long lastSeenTimestamp;
    @Ignore
    PIRData pirData;
    @ColumnInfo(name = "firmware_version")
    String firmwareVersion;
    @ColumnInfo(name = "firmware_update_available")
    boolean firmwareUpdateAvailable;
    @ColumnInfo(name = "mqtt_reachable")
    boolean isDeviceMQTTReachable;

    public Device(){
        this.id = 0;
        this.name = "";
        this.macAddress = "";
        this.chipID = "";
        this.deviceTypeID = -1;
        this.ipAddress = "";
        this.roomID = -1;
        this.errorCount = 0;
        this.lines = new ArrayList<>();
        this.soundDeviceData = new SoundDeviceData();
        this.accessToken = Constants.DEVICE_DEFAULT_ACCESS_TOKEN;
        lastSeenTimestamp = 0;
        this.pirData = new PIRData();
        this.firmwareUpdateAvailable = false;
        this.firmwareVersion = Device.DEVICE_BASE_FIRMWARE;
        this.isDeviceMQTTReachable = false;
    }

    public Device(Device device){
        this.id = device.getId();
        this.name = device.getName();
        this.macAddress = device.getMacAddress();
        this.chipID = device.getChipID();
        this.deviceTypeID = device.getDeviceTypeID();
        this.ipAddress = device.getIpAddress();
        this.roomID = device.getRoomID();
        this.errorCount = device.getErrorCount();
        this.lines = new ArrayList<>();
        for (Line line:device.getLines()) {
            Line newLine = new Line(line);
            this.lines.add(newLine);
        }
        this.soundDeviceData = new SoundDeviceData(device.getSoundDeviceData());
        this.accessToken = device.getAccessToken();
        this.lastSeenTimestamp = device.getLastSeenTimestamp();
        this.pirData = new PIRData(device.getPIRData());
        this.firmwareUpdateAvailable = device.isFirmwareUpdateAvailable();
        this.firmwareVersion = device.getFirmwareVersion();
        this.isDeviceMQTTReachable = device.isDeviceMQTTReachable();
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

    public SoundDeviceData getSoundDeviceData(){
        return this.soundDeviceData;
    }

    public void setSoundDeviceData(SoundDeviceData soundSystemDevice){
        this.soundDeviceData = soundSystemDevice;
    }

    public PIRData getPIRData(){
        return this.pirData;
    }

    public void setPIRData(PIRData pirData){
        this.pirData = pirData;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(long lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    public boolean isFirmwareUpdateAvailable() {
        return firmwareUpdateAvailable;
    }

    public void setFirmwareUpdateAvailable(boolean firmwareUpdateAvailable) {
        this.firmwareUpdateAvailable = firmwareUpdateAvailable;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public boolean isDeviceMQTTReachable() {
        return isDeviceMQTTReachable;
    }

    public void setDeviceMQTTReachable(boolean deviceMQTTReachable) {
        isDeviceMQTTReachable = deviceMQTTReachable;
    }

    @Override
    public boolean equals(Object object){
        Device device = (Device) object;
        if(device != null) {
            if (device.getId() == this.id) {
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(Object object){
        Device device = (Device) object;
        if(device != null){
            if(this.id == device.getId()){
                return 0;
            }else if (this.id > device.getId()){
                return 1;
            }else if(this.id < device.getId()){
                return -1;
            }else{
                return 0;
            }
        }else{
            return 0;
        }
    }
}
