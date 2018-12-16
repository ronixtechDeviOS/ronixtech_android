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
    private static final String DEVICE_MODEL_SWITCH_WIFI_1_LINE = "SmartSwitch - 1 Line - WiFi";
    private static final String DEVICE_MODEL_SWITCH_WIFI_2_LINE = "SmartSwitch - 2 Lines - WiFi";
    private static final String DEVICE_MODEL_SWITCH_WIFI_3_LINE = "SmartSwitch - 3 Lines - WiFi";
    private static final String DEVICE_MODEL_PLUG_1_LINE = "SmartPlug - 1 Line - WiFi";
    private static final String DEVICE_MODEL_PLUG_2_LINE = "SmartPlug - 2 Lines - WiFi";
    private static final String DEVICE_MODEL_PLUG_3_LINE = "SmartPlug - 3 Lines - WiFi";
    private static final String DEVICE_MODEL_PIR_SENSOR = "SmartSensor - MotionSensor";
    private static final String DEVICE_MODEL_SOUND_CONTROLLER = "SmartSound";

    private static final String DEVICE_MODEL_SWITCH = "SmartSwitch";
    private static final String DEVICE_MODEL_PLUG = "SmartPlug";
    private static final String DEVICE_MODEL_SENSOR = "SmartSensor";
    private static final String DEVICE_MODEL_SOUND = "SmartSound";

    private static final String DEVICE_MODEL_UNKNOWN = "Unknown Device Type";

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


    public static final int MAX_CONSECUTIVE_ERROR_COUNT = 10;

    public static final int CONTROL_TIMEOUT = 350;
    public static final int CONTROL_NUMBER_OF_RETRIES = 1;

    public static final int REFRESH_RATE_MS = 1000;
    public static final int REFRESH_TIMEOUT = 250;
    public static final int REFRESH_NUMBER_OF_RETRIES = 0;

    public static final int CONFIG_TIMEOUT = 1000;
    public static final int CONFIG_NUMBER_OF_RETRIES = 10;

    public static final int MODE_PRIMARY = 0;
    public static final int MODE_SECONDARY = 1;

    public static final String DEVICE_BASE_FIRMWARE = "101400";
    public static final int SYNC_CONTROLS_STATUS_FIRMWARE_VERSION = 101405;
    public static final int DEVICE_FIRMWARE_VERSION_AUTO_UPDATE_METHOD = 101500;
    public static final int DEVICE_FIRMWARE_DHCP_FIRMWARE = 101419;

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
    @ColumnInfo(name = "beep_state")
    boolean beep;
    @ColumnInfo(name = "hw_lock_state")
    boolean hwLock;
    @ColumnInfo(name = "temperature")
    int temperature;
    @ColumnInfo(name = "hw_version")
    String hwVersion;
    @ColumnInfo(name = "wifi_version")
    String wifiVersion;
    @ColumnInfo(name = "hw_firmware_version")
    String hwFirmwareVersion;
    @ColumnInfo(name = "hw_firmware_update_available")
    boolean hwFirmwareUpdateAvailable;
    @ColumnInfo(name = "static_ip_address")
    boolean staticIPAddress;
    @ColumnInfo(name = "static_ip_address_sync_state")
    boolean staticIPSyncedState;
    @ColumnInfo(name = "ip_gateway")
    String gateway;
    @ColumnInfo(name = "ip_subnet_mask")
    String subnetMask;

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
        this.lastSeenTimestamp = 0;
        this.pirData = new PIRData();
        this.firmwareUpdateAvailable = false;
        this.firmwareVersion = Device.DEVICE_BASE_FIRMWARE;
        this.isDeviceMQTTReachable = false;
        this.beep = true;
        this.hwLock = false;
        this.temperature = 0;
        this.hwVersion = "";
        this.wifiVersion = "";
        this.hwFirmwareVersion = "";
        this.hwFirmwareUpdateAvailable = false;
        this.staticIPAddress = true;
        this.staticIPSyncedState = false;
        this.gateway = "";
        this.subnetMask = "";
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
        this.hwVersion = device.getHwVersion();
        this.wifiVersion = device.getWifiVersion();
        this.firmwareUpdateAvailable = device.isFirmwareUpdateAvailable();
        this.firmwareVersion = device.getFirmwareVersion();
        this.isDeviceMQTTReachable = device.isDeviceMQTTReachable();
        this.beep = device.isBeep();
        this.hwLock = device.isHwLock();
        this.temperature = device.getTemperature();
        this.hwFirmwareUpdateAvailable = device.isHwFirmwareUpdateAvailable();
        this.hwFirmwareVersion = device.getHwFirmwareVersion();
        this.staticIPAddress = device.isStaticIPAddress();
        this.staticIPSyncedState = device.isStaticIPSyncedState();
        this.gateway = device.getGateway();
        this.subnetMask = device.getSubnetMask();
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

    public boolean isBeep() {
        return beep;
    }

    public void setBeep(boolean beep) {
        this.beep = beep;
    }

    public boolean isHwLock() {
        return hwLock;
    }

    public void setHwLock(boolean hwLock) {
        this.hwLock = hwLock;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    public String getWifiVersion() {
        return wifiVersion;
    }

    public void setWifiVersion(String wifiVersion) {
        this.wifiVersion = wifiVersion;
    }

    public String getHwFirmwareVersion() {
        return hwFirmwareVersion;
    }

    public void setHwFirmwareVersion(String hwFirmwareVersion) {
        this.hwFirmwareVersion = hwFirmwareVersion;
    }

    public boolean isHwFirmwareUpdateAvailable() {
        //TODO add this later when HW upgrading works as expected
        return hwFirmwareUpdateAvailable;
        //return false;
    }

    public void setHwFirmwareUpdateAvailable(boolean hwFirmwareUpdateAvailable) {
        this.hwFirmwareUpdateAvailable = hwFirmwareUpdateAvailable;
    }

    public boolean isStaticIPAddress() {
        return staticIPAddress;
    }

    public void setStaticIPAddress(boolean staticIPAddress) {
        this.staticIPAddress = staticIPAddress;
    }

    public boolean isStaticIPSyncedState() {
        return staticIPSyncedState;
    }

    public void setStaticIPSyncedState(boolean staticIPSyncedState) {
        this.staticIPSyncedState = staticIPSyncedState;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public static String getDeviceTypeString(int deviceTypeID){
        if(deviceTypeID == Device.DEVICE_TYPE_wifi_1line_old || deviceTypeID == Device.DEVICE_TYPE_wifi_1line){
            return DEVICE_MODEL_SWITCH_WIFI_1_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_wifi_2lines_old || deviceTypeID == Device.DEVICE_TYPE_wifi_2lines){
            return DEVICE_MODEL_SWITCH_WIFI_2_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_wifi_3lines_old || deviceTypeID == Device.DEVICE_TYPE_wifi_3lines || deviceTypeID == DEVICE_TYPE_wifi_3lines_workaround){
            return DEVICE_MODEL_SWITCH_WIFI_3_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PLUG_1lines){
            return DEVICE_MODEL_PLUG_1_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PLUG_2lines){
            return DEVICE_MODEL_PLUG_2_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PLUG_3lines){
            return DEVICE_MODEL_PLUG_3_LINE;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            return DEVICE_MODEL_PIR_SENSOR;
        }else if(deviceTypeID == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return DEVICE_MODEL_SOUND_CONTROLLER;
        }else return DEVICE_MODEL_UNKNOWN;
    }

    public static String getDeviceTypeCategoryString(int deviceTypeID){
        if(deviceTypeID == Device.DEVICE_TYPE_wifi_1line_old || deviceTypeID == Device.DEVICE_TYPE_wifi_2lines_old || deviceTypeID == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceTypeID == Device.DEVICE_TYPE_wifi_1line || deviceTypeID == Device.DEVICE_TYPE_wifi_2lines || deviceTypeID == Device.DEVICE_TYPE_wifi_3lines ||
                deviceTypeID == Device.DEVICE_TYPE_wifi_3lines_workaround){
            return DEVICE_MODEL_SWITCH;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PLUG_1lines || deviceTypeID == Device.DEVICE_TYPE_PLUG_2lines || deviceTypeID == Device.DEVICE_TYPE_PLUG_3lines){
            return DEVICE_MODEL_PLUG;
        }else if(deviceTypeID == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            return DEVICE_MODEL_SENSOR;
        }else if(deviceTypeID == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return DEVICE_MODEL_SOUND;
        }else return DEVICE_MODEL_UNKNOWN;
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
