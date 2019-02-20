package com.ronixtech.ronixhome;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.entities.Speaker;
import com.ronixtech.ronixhome.entities.Type;
import com.ronixtech.ronixhome.entities.User;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MySettings {
    private static final String TAG = MySettings.class.getSimpleName();

    public static final String PREF_ACTIVE_USER_EMAIL = "pref_current_user_email";
    public static final String PREF_ACTIVE_USER = "pref_current_user";
    public static final String PREF_HOME_NETOWORK = "pref_home_network";
    public static final String PREF_TEMP_DEVICE_MAC_ADDRESS = "temp_device_mac_address";
    public static final String PREF_TEMP_DEVICE_TYPE_ID = "temp_device_type_id";
    public static final String PREF_TEMP_DEVICE = "temp_device";
    public static final String PREF_SCANNING_ACTIVE = "scanning_active";
    public static final String PREF_CURRENT_PLACE = "pref_current_place";
    public static final String PREF_TEMP_PLACE = "pref_temp_place";
    public static final String PREF_DEFAULT_PLACE_ID = "pref_default_place_id";
    public static final String PREF_CURRENT_FLOOR = "pref_current_floor";
    public static final String PREF_CURRENT_ROOM = "pref_current_room";
    public static final String PREF_TEMP_ROOM = "pref_temp_room";
    public static final String PREF_CONTROL_ACTIVE = "control_active";
    public static final String PREF_GETSTATUS_ACTIVE = "get_status_active";
    public static final String PREF_APP_FIRST_START = "app_first_start";
    public static final String PREF_DEVICES_LATEST_WIFI_VERSIONS = "pref_devices_latest_wifi_firmware_versions";
    public static final String PREF_DEVICES_LATEST_HW_VERSIONS = "pref_devices_latest_hw_firmware_versions";


    private static User loggedInUser;
    private static User userBeingChecked;
    private static String loggedInUserEmail;
    private static SharedPreferences sharedPref;
    private static boolean initialStartup;
    private static boolean scanningActive;
    private static boolean controlActive;
    private static boolean gettingStatusActive;
    private static boolean internetConnectivityActive;
    private static WifiNetwork homeNetwork;
    private static String tempDeviceMACAddress;
    private static int tempDeviceTypeID;
    private static Device tempDevice;
    private static boolean appFirstStart;

    private static Place currentPlace, tempPlace;
    private static Floor currentFloor;
    private static com.ronixtech.ronixhome.entities.Room currentRoom, tempRoom;

    private static long defaultPlaceID = -1;

    private static SparseArray<String> devicesLatestWiFiVersions;
    private static SparseArray<String> devicesLatestHWVersions;

    private static Gson gson;

    private static AppDatabase database;

    private MySettings(){

    }

    public static void setHomeNetwork(WifiNetwork network) {
        MySettings.homeNetwork = network;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.homeNetwork);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_HOME_NETOWORK, json);
        editor.apply();
    }
    public static WifiNetwork getHomeNetwork() {
        if (homeNetwork != null) {
            return homeNetwork;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_HOME_NETOWORK, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                homeNetwork = gson.fromJson(json, WifiNetwork.class);
                return homeNetwork;
            }
        }
    }

    public static void setCurrentPlace(Place place) {
        MySettings.currentPlace = place;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.currentPlace);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_CURRENT_PLACE, json);
        editor.apply();
    }
    public static Place getCurrentPlace() {
        if (currentPlace != null) {
            return currentPlace;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_CURRENT_PLACE, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                currentPlace = gson.fromJson(json, Place.class);
                return currentPlace;
            }
        }
    }

    public static void setDefaultPlaceID(long placeID) {
        MySettings.defaultPlaceID = placeID;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putLong(PREF_DEFAULT_PLACE_ID, defaultPlaceID);
        editor.apply();
    }
    public static long getDefaultPlaceID() {
        if (defaultPlaceID != -1) {
            return defaultPlaceID;
        } else {
            SharedPreferences prefs = getSettings();
            defaultPlaceID = prefs.getLong(PREF_DEFAULT_PLACE_ID, -1);
            return defaultPlaceID;
        }
    }

    public static void setCurrentFloor(Floor floor) {
        MySettings.currentFloor = floor;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.currentFloor);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_CURRENT_FLOOR, json);
        editor.apply();
    }
    public static Floor getCurrentFloor() {
        if (currentFloor != null) {
            return currentFloor;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_CURRENT_FLOOR, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                currentFloor = gson.fromJson(json, Floor.class);
                return currentFloor;
            }
        }
    }

    public static void setCurrentRoom(com.ronixtech.ronixhome.entities.Room room) {
        MySettings.currentRoom = room;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.currentRoom);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_CURRENT_ROOM, json);
        editor.apply();
    }
    public static com.ronixtech.ronixhome.entities.Room getCurrentRoom() {
        if (currentRoom != null) {
            return currentRoom;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_CURRENT_ROOM, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                currentRoom = gson.fromJson(json, com.ronixtech.ronixhome.entities.Room.class);
                return currentRoom;
            }
        }
    }

    public static void setTempDeviceMacAddress(String macAddress) {
        MySettings.tempDeviceMACAddress = macAddress;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_TEMP_DEVICE_MAC_ADDRESS, tempDeviceMACAddress);
        editor.apply();
    }
    public static String getTempDeviceMacAddress() {
        if (tempDeviceMACAddress != null && tempDeviceMACAddress.length() >= 1) {
            return tempDeviceMACAddress;
        } else {
            SharedPreferences prefs = getSettings();
            tempDeviceMACAddress = prefs.getString(PREF_TEMP_DEVICE_MAC_ADDRESS, "");
            return tempDeviceMACAddress;
        }
    }

    public static void setTempDeviceType(int type) {
        MySettings.tempDeviceTypeID = type;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putInt(PREF_TEMP_DEVICE_TYPE_ID, tempDeviceTypeID);
        editor.apply();
    }
    public static int getTempDeviceType() {
        if (tempDeviceTypeID != -1) {
            return tempDeviceTypeID;
        } else {
            SharedPreferences prefs = getSettings();
            tempDeviceTypeID = prefs.getInt(PREF_TEMP_DEVICE_TYPE_ID, -1);
            return tempDeviceTypeID;
        }
    }

    public static void setTempDevice(Device device) {
        MySettings.tempDevice = device;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.tempDevice);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_TEMP_DEVICE, json);
        editor.apply();
    }
    public static Device getTempDevice() {
        if (tempDevice != null) {
            /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
            }*/
            return tempDevice;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_TEMP_DEVICE, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                tempDevice = gson.fromJson(json, Device.class);
                /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                    tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
                }*/
                return tempDevice;
            }
        }
    }

    public static void setTempPlace(Place place) {
        MySettings.tempPlace = place;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.tempPlace);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_TEMP_PLACE, json);
        editor.apply();
    }
    public static Place getTempPlace() {
        if (tempPlace != null) {
            /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
            }*/
            return tempPlace;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_TEMP_PLACE, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                tempPlace = gson.fromJson(json, Place.class);
                /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                    tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
                }*/
                return tempPlace;
            }
        }
    }

    public static void setTempRoom(com.ronixtech.ronixhome.entities.Room room) {
        MySettings.tempRoom = room;

        if(gson == null){
            gson = new Gson();
        }
        String json = gson.toJson(MySettings.tempRoom);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_TEMP_ROOM, json);
        editor.apply();
    }
    public static com.ronixtech.ronixhome.entities.Room getTempRoom() {
        if (tempRoom != null) {
            /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
            }*/
            return tempRoom;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_TEMP_ROOM, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                tempRoom = gson.fromJson(json, com.ronixtech.ronixhome.entities.Room.class);
                /*if(MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID()) != null) {
                    tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
                }*/
                return tempRoom;
            }
        }
    }

    public static void addDevice(Device device){
        //save device into DB
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
            MySettings.initDB().deviceDAO().insertDeviceWithLines(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().insertDeviceWithSoundDeviceData(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            MySettings.initDB().deviceDAO().insertDeviceWithPIRData(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SHUTTER){
            MySettings.initDB().deviceDAO().insertShutter(device);
        }
    }
    public static void updateDeviceIP(Device device, String ipAddress){
        MySettings.initDB().deviceDAO().updateDeviceIP(device.getId(), ipAddress);
    }
    public static void updateDeviceRoom(Device device, long roomID){
        MySettings.initDB().deviceDAO().updateDeviceRoom(device.getId(), roomID);
    }
    public static void updateDeviceErrorCount(Device device, int count){
        MySettings.initDB().deviceDAO().updateDeviceErrorCount(device.getId(), count);
        for (Device dev:DevicesInMemory.getDevices()) {
            dev.setErrorCount(dev.getErrorCount()+1);
        }
    }
    public static void updateDeviceType(Device device, int deviceType){
        MySettings.initDB().deviceDAO().updateDeviceTypeID(device.getId(), deviceType);
    }
    public static void updateDeviceSoundData(Device device, SoundDeviceData soundDeviceData){
        MySettings.initDB().deviceDAO().updateDeviceSoundDeviceData(device.getId(), soundDeviceData);
    }
    public static Device getDeviceByID(long deviceID, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines || deviceType == Device.DEVICE_TYPE_PLUG_3lines ||
                deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
            return MySettings.initDB().deviceDAO().getDeviceWithLinesByID(deviceID);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByID(deviceID);
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            return MySettings.initDB().deviceDAO().getDeviceWithPIRDataByID(deviceID);
        }else if(deviceType == Device.DEVICE_TYPE_SHUTTER){
            return MySettings.initDB().deviceDAO().getShutterByID(deviceID);
        }
        return null;
    }
    public static Device getDeviceByMAC(String macAddress, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines || deviceType == Device.DEVICE_TYPE_PLUG_3lines||
                deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
            return MySettings.initDB().deviceDAO().getDeviceWithLinesByMacAddress(macAddress);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByMacAddress(macAddress);
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            return MySettings.initDB().deviceDAO().getDeviceWithPIRDataByMacAddress(macAddress);
        }else if(deviceType == Device.DEVICE_TYPE_SHUTTER){
            return MySettings.initDB().deviceDAO().getShutterByMacAddress(macAddress);
        }
        return null;
    }
    public static Device getDeviceByChipID(String chipID, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround || deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines || deviceType == Device.DEVICE_TYPE_PLUG_3lines ||
                deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || deviceType == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
            return MySettings.initDB().deviceDAO().getDeviceWithLinesByChipID(chipID);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByChipID(chipID);
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            MySettings.initDB().deviceDAO().getDeviceWithPIRDataByChipID(chipID);
        }else if(deviceType == Device.DEVICE_TYPE_SHUTTER){
            MySettings.initDB().deviceDAO().getShutterByChipID(chipID);
        }
        return null;
    }
    public static List<Device> getAllDevices(){
        List<Device> devicesWithData = new ArrayList<>();
        List<Device> devices = MySettings.initDB().deviceDAO().getAll();
        if (devices != null && devices.size() >= 1) {
            for (Device dev : devices) {
                Device tempDevice = MySettings.getDeviceByMAC(dev.getMacAddress(), dev.getDeviceTypeID());
                devicesWithData.add(tempDevice);
            }
        }
        return devicesWithData;
    }
    public static List<Device> getRoomDevices(long roomID){
        List<Device> devicesWithData = new ArrayList<>();
        List<Device> devices = MySettings.initDB().deviceDAO().getRoomDevices(roomID);
        if (devices != null && devices.size() >= 1) {
            for (Device dev : devices) {
                Device tempDevice = MySettings.getDeviceByMAC(dev.getMacAddress(), dev.getDeviceTypeID());
                devicesWithData.add(tempDevice);
            }
        }
        return devicesWithData;
    }
    public static void removeDevice(Device device){
        //remove device from DB
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
            MySettings.initDB().deviceDAO().removeDeviceWithLines(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().removeDeviceWithSoundDeviceData(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            MySettings.initDB().deviceDAO().removeDeviceWithPIRData(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SHUTTER){
            MySettings.initDB().deviceDAO().removeShutter(device);
        }

    }
    public static Device getDeviceByID2(long deviceID) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByID(deviceID);
    }
    public static Device getDeviceByChipID2(String chipID) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByChipID(chipID);
    }
    public static List<Line> getSecondaryLines(Device device){
        return MySettings.initDB().lineDAO().getSecondaryLine(device.getChipID());
    }

    public static void insertSoundDeviceData(SoundDeviceData soundDeviceData){
        MySettings.initDB().soundDeviceDataDAO().insertSoundDeviceDataWithSpeakers(soundDeviceData);
    }
    public static void updateSoundDeviceDataSpeakers(SoundDeviceData soundDeviceData, List<Speaker> speakers){
        MySettings.initDB().soundDeviceDataDAO().updateSoundDeviceDataSpeakers(soundDeviceData.getId(), speakers);
    }
    public static SoundDeviceData getSoundDeviceData(long deviceID){
        return MySettings.initDB().soundDeviceDataDAO().getSoundDeviceDataWithSpeakers(deviceID);
    }
    public static void removeSoundDeviceData(long deviceID){
        MySettings.initDB().soundDeviceDataDAO().removeSoundDeviceDataWithSpeakers(deviceID);
    }

    public static void insertSpeaker(Speaker speaker){
        MySettings.initDB().speakerDAO().insertSpeaker(speaker);
    }
    public static void insertSpeakers(List<Speaker> speakers){
        MySettings.initDB().speakerDAO().insertSpeakers(speakers);
    }
    public static Speaker getSpeaker(long soundDeviceID){
        return MySettings.initDB().speakerDAO().getSpeaker(soundDeviceID);
    }
    public static List<Speaker> getSpeakers(long soundDeviceID){
        return MySettings.initDB().speakerDAO().getSoundDeviceSpeakers(soundDeviceID);
    }
    public static void removeSpeaker(long soundDeviceID){
        MySettings.initDB().speakerDAO().removeSpeaker(soundDeviceID);
    }

    public static void updateLineState(Line line, int powerState){
        MySettings.initDB().lineDAO().updateLinePowerState(line.getId(), powerState);
    }
    public static void updateLineDimmingState(Line line, int dimmingState){
        MySettings.initDB().lineDAO().updateLineDimmingState(line.getId(), dimmingState);
    }
    public static void updateLineDimmingValue(Line line, int dimmingValue){
        MySettings.initDB().lineDAO().updateLineDimmingValue(line.getId(), dimmingValue);
    }
    public static void updateLine(long lineID, Line newLine){
        MySettings.initDB().lineDAO().updateLineDimmingState(lineID, newLine.getDimmingState());
        MySettings.initDB().lineDAO().updateLineName(lineID, newLine.getName());
        MySettings.initDB().lineDAO().updateLinePowerUsage(lineID, newLine.getLinePowerUsage());
        MySettings.initDB().lineDAO().updateLineTypeID(lineID, newLine.getTypeID());
        MySettings.initDB().lineDAO().updateLineTypeName(lineID, newLine.getLineTypeString());
        MySettings.initDB().lineDAO().updateLineMode(lineID, newLine.getMode());
        MySettings.initDB().lineDAO().updateLinePrimaryDeviceChipID(lineID, newLine.getPrimaryDeviceChipID());
        MySettings.initDB().lineDAO().updateLinePrimaryLinePosition(lineID, newLine.getPrimaryLinePosition());
    }

    public static void updateSoundMode(SoundDeviceData soundDeviceData, int mode){
        MySettings.initDB().soundDeviceDataDAO().updateMode(soundDeviceData.getId(), mode);
    }

    public static void addPlace(Place place){
        //save floor into DB
        MySettings.initDB().placeDAO().insertPlaceWithFloors(place);
    }
    public static Place getPlace(long placeID) {
        return MySettings.initDB().placeDAO().getPlaceWIthFloors(placeID);
    }
    public static Place getPlaceByName(String placeName) {
        return MySettings.initDB().placeDAO().getPlaceByName(placeName);
    }
    public static List<Place> getAllPlaces(){
        return MySettings.initDB().placeDAO().getAll();
    }
    public static void updatePlaceMode(Place place, int mode){
        MySettings.initDB().placeDAO().updatePlaceMode(place.getId(), mode);
    }
    public static void updatePlaceName(Place place, String newName){
        MySettings.initDB().placeDAO().updatePlaceName(place.getId(), newName);
    }
    public static void updatePlaceType(Place place, Type newType){
        MySettings.initDB().placeDAO().updatePlaceType(place.getId(), newType.getId());
        MySettings.initDB().placeDAO().updatePlaceType(place.getId(), newType.getName());
    }
    public static void updatePlaceLatitude(Place place, double newLatitude){
        MySettings.initDB().placeDAO().updatePlaceLatitude(place.getId(), newLatitude);
    }
    public static void updatePlaceLongitude(Place place, double newLongitude){
        MySettings.initDB().placeDAO().updatePlaceLongitude(place.getId(), newLongitude);
    }
    public static void updatePlaceAddress(Place place, String newAddress){
        MySettings.initDB().placeDAO().updatePlaceAddress(place.getId(), newAddress);
    }
    public static void updatePlaceCity(Place place, String newCity){
        MySettings.initDB().placeDAO().updatePlaceCity(place.getId(), newCity);
    }
    public static void updatePlaceState(Place place, String newState){
        MySettings.initDB().placeDAO().updatePlaceState(place.getId(), newState);
    }
    public static void updatePlaceCountry(Place place, String newCountry){
        MySettings.initDB().placeDAO().updatePlaceCountry(place.getId(), newCountry);
    }
    public static void updatePlaceZipCode(Place place, String newZipCode){
        MySettings.initDB().placeDAO().updatePlaceZipCode(place.getId(), newZipCode);
    }
    public static void removePlace(Place place){
        //remove floor from DB
        MySettings.initDB().placeDAO().removePlaceWithFloors(place);
    }

    public static void addFloor(Floor floor){
        //save floor into DB
        MySettings.initDB().floorDAO().insertFloorWithRooms(floor);
    }
    public static Floor getFloor(long floorID) {
        return MySettings.initDB().floorDAO().getFloorWithRooms(floorID);
    }
    public static List<Floor> getAllFloors(){
        return MySettings.initDB().floorDAO().getAll();
    }
    public static List<com.ronixtech.ronixhome.entities.Floor> getPlaceFloors(long placeID){
        return MySettings.initDB().floorDAO().getPlaceFloors(placeID);
    }
    public static void updateFloorName(Floor floor, String newName){
        MySettings.initDB().floorDAO().updateFloorName(floor.getId(), newName);
    }
    public static void removeFloor(Floor floor){
        //remove floor from DB
        MySettings.initDB().floorDAO().removeFloorWithRooms(floor);
    }

    public static void addRoom(com.ronixtech.ronixhome.entities.Room room){
        //save room into DB
        MySettings.initDB().roomDAO().insertRoomWithDevices(room);
    }
    public static com.ronixtech.ronixhome.entities.Room getRoom(long roomID) {
        return MySettings.initDB().roomDAO().getRoomWithDevices(roomID);
    }
    public static com.ronixtech.ronixhome.entities.Room getRoomByName(String roomName) {
        return MySettings.initDB().roomDAO().getRoomByName(roomName);
    }
    public static List<com.ronixtech.ronixhome.entities.Room> getAllRooms(){
        List<com.ronixtech.ronixhome.entities.Room> roomsWithDevices = new ArrayList<>();
        List<com.ronixtech.ronixhome.entities.Room> rooms = MySettings.initDB().roomDAO().getAll();
        if (rooms != null && rooms.size() >= 1) {
            for (com.ronixtech.ronixhome.entities.Room room : rooms) {
                com.ronixtech.ronixhome.entities.Room tempRoom = MySettings.getRoom(room.getId());
                roomsWithDevices.add(tempRoom);
            }
        }
        return roomsWithDevices;
    }
    public static List<com.ronixtech.ronixhome.entities.Room> getFloorRooms(long floorID){
        return MySettings.initDB().roomDAO().getFloorRooms(floorID);
    }
    public static void updateRoomName(com.ronixtech.ronixhome.entities.Room room, String newName){
        MySettings.initDB().roomDAO().updateRoomName(room.getId(), newName);
    }
    public static void updateRoomType(com.ronixtech.ronixhome.entities.Room room, Type newType){
        MySettings.initDB().roomDAO().updateRoomType(room.getId(), newType.getId());
        MySettings.initDB().roomDAO().updateRoomType(room.getId(), newType.getName());
    }
    public static void updateRoomFloor(com.ronixtech.ronixhome.entities.Room room, long newFloor){
        MySettings.initDB().roomDAO().updateRoomFloor(room.getId(), newFloor);
    }
    public static void removeRoom(com.ronixtech.ronixhome.entities.Room room){
        //remove room from DB
        MySettings.initDB().roomDAO().removeRoomsWithDevices(room);
    }

    public static List<com.ronixtech.ronixhome.entities.Room> getPlaceRooms(Place place){
        List<com.ronixtech.ronixhome.entities.Room> placeRooms = new ArrayList<>();
        if(place != null){
            List<Floor> placeFloors = MySettings.getPlaceFloors(place.getId());
            if(placeFloors != null && placeFloors.size() >= 1){
                for (Floor floor : placeFloors) {
                    List<com.ronixtech.ronixhome.entities.Room> floorRooms = MySettings.getFloorRooms(floor.getId());
                    if(floorRooms != null && floorRooms.size() >= 1) {
                        placeRooms.addAll(floorRooms);
                    }
                }
            }
        }
        return placeRooms;
    }
    public static List<Device> getPlaceDevices(Place place){
        List<Device> placeDevices= new ArrayList<>();
        List<com.ronixtech.ronixhome.entities.Room> placeRooms = MySettings.getPlaceRooms(place);
        if(placeRooms != null && placeRooms.size() >= 1){
            for (com.ronixtech.ronixhome.entities.Room room : placeRooms) {
                List<Device> roomDevices = MySettings.getRoomDevices(room.getId());
                if(roomDevices != null && roomDevices.size() >= 1) {
                    placeDevices.addAll(roomDevices);
                }
            }
        }
        return placeDevices;
    }

    public static Type getType(long typeID){
        return MySettings.initDB().typeDAO().getType(typeID);
    }
    public static Type getTypeByName(String name){
        return MySettings.initDB().typeDAO().getTypeByName(name);
    }
    public static void addType(Type type){
        MySettings.initDB().typeDAO().insertType(type);
    }
    public static List<Type> getTypes(int category){
        List<Type> types = new ArrayList<>();

        if(category == Constants.TYPE_LINE_PLUG){
            types.addAll(MySettings.initDB().typeDAO().getTypes(Constants.TYPE_LINE_PLUG));
            types.addAll(MySettings.initDB().typeDAO().getTypes(Constants.TYPE_LINE));
        }else{
            types.addAll(MySettings.initDB().typeDAO().getTypes(category));
        }

        return types;
    }

    public static void addWifiNetwork(WifiNetwork wifiNetwork){
        //save wifinetwork into DB
        MySettings.initDB().wifiNetworkDao().insertWifiNetwork(wifiNetwork);
    }
    public static List<WifiNetwork> getAllWifiNetworks(){
        return MySettings.initDB().wifiNetworkDao().getAll();
    }
    public static List<WifiNetwork> getPlaceWifiNetworks(long placeID){
        return MySettings.initDB().wifiNetworkDao().getPlaceWifiNetworks(placeID);
    }
    public static WifiNetwork getWifiNetwork(long wifiNetworkID) {
        return MySettings.initDB().wifiNetworkDao().getWifiNetwork(wifiNetworkID);
    }
    public static WifiNetwork getWifiNetworkBySSID(String ssid) {
        return MySettings.initDB().wifiNetworkDao().getWifiNetworkBySSID(ssid);
    }
    public static void updateWifiNetworkPlace(WifiNetwork network, long placeID){
        MySettings.initDB().wifiNetworkDao().updateWifiNetworkPlaceID(network.getId(), placeID);
    }
    public static void updateWifiNetworkPassword(WifiNetwork network, String password){
        MySettings.initDB().wifiNetworkDao().updateWifiNetworkPassword(network.getId(), password);
    }
    public static void removeWifiNetwork(WifiNetwork wifiNetwork){
        //remove floor from DB
        MySettings.initDB().wifiNetworkDao().removeWifiNetwork(wifiNetwork.getId());
    }

    public static void scanNetwork(){
        NetworkDiscovery.init();

        if(!MySettings.getCurrentScanningState()){
            MySettings.setCurrentScanningState(true);
            Utils.showUpdatingNotification();

            NetworkScannerAsyncTask networkScannerAsyncTask = new NetworkScannerAsyncTask();
            networkScannerAsyncTask.execute();
            /*// Create a Constraints that defines when the task should run
            Constraints myConstraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    // Many other constraints are available, see the
                    // Constraints.Builder reference
                    .build();

            // ...then create a PeriodicWorkRequest that uses those constraints
            *//*PeriodicWorkRequest scannerWork =
                    new PeriodicWorkRequest.Builder(NetworkScanner.class, 15, TimeUnit.MINUTES)
                            .setConstraints(myConstraints)
                            .build();

            WorkManager.getInstance().enqueueUniquePeriodicWork("scannerWork", ExistingPeriodicWorkPolicy.KEEP, scannerWork);*//*

            OneTimeWorkRequest scannerWork2 =
                    new OneTimeWorkRequest.Builder(NetworkScanner.class)
                            .setConstraints(myConstraints)
                            .addTag("NetworkScanner")
                            .build();
            WorkManager.getInstance().cancelAllWorkByTag("NetworkScanner");
            WorkManager.getInstance().enqueue(scannerWork2);*/
        }
    }
    public static void scanDevices(){
        if(!MySettings.getCurrentScanningState()){
            Utils.log(TAG, "scanningNetwork starting", true);
            // Create a Constraints that defines when the task should run
            Constraints myConstraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    // Many other constraints are available, see the
                    // Constraints.Builder reference
                    .build();

            // ...then create a OneTimeWorkRequest that uses those constraints
            OneTimeWorkRequest deviceScannerWork =
                    new OneTimeWorkRequest.Builder(DeviceScanner.class)
                            .setConstraints(myConstraints)
                            .addTag("DeviceScanner")
                            .build();
            WorkManager.getInstance().cancelAllWorkByTag("DeviceScanner");
            WorkManager.getInstance().enqueue(deviceScannerWork);
        }else{
            Utils.log(TAG, "scanningNetwork already started", true);
        }
    }

    public static void setCurrentScanningState(boolean state) {
        MySettings.scanningActive = state;

        //SharedPreferences.Editor editor = getSettings().edit();
        //editor.putBoolean(PREF_SCANNING_ACTIVE, scanningActive);
        //editor.apply();
    }
    public static boolean getCurrentScanningState() {
        //SharedPreferences prefs = getSettings();
        //scanningActive = prefs.getBoolean(PREF_SCANNING_ACTIVE, true);
        return scanningActive;
    }

    public static void setControlState(boolean state) {
        MySettings.controlActive = state;

        //SharedPreferences.Editor editor = getSettings().edit();
        //editor.putBoolean(PREF_CONTROL_ACTIVE, controlActive);
        //editor.apply();
    }
    public static boolean isControlActive() {
        //SharedPreferences prefs = getSettings();
        //scanningActive = prefs.getBoolean(PREF_CONTROL_ACTIVE, true);
        return MySettings.controlActive;
    }

    public static void setGetStatusState(boolean state) {
        MySettings.gettingStatusActive = state;

        //SharedPreferences.Editor editor = getSettings().edit();
        //editor.putBoolean(PREF_GETSTATUS_ACTIVE, controlActive);
        //editor.apply();
    }
    public static boolean isGetStatusActive() {
        //SharedPreferences prefs = getSettings();
        //scanningActive = prefs.getBoolean(PREF_GETSTATUS_ACTIVE, true);
        return gettingStatusActive;
    }

    public static void setInternetConnectivityState(boolean state) {
        MySettings.internetConnectivityActive = state;

        //SharedPreferences.Editor editor = getSettings().edit();
        //editor.putBoolean(PREF_GETSTATUS_ACTIVE, controlActive);
        //editor.apply();
    }
    public static boolean isInternetConnectivityActive() {
        //SharedPreferences prefs = getSettings();
        //scanningActive = prefs.getBoolean(PREF_GETSTATUS_ACTIVE, true);
        return internetConnectivityActive;
    }

    public static void setDeviceLatestWiFiFirmwareVersion(int deviceType, String latestVersion){
        if(devicesLatestWiFiVersions != null){
            devicesLatestWiFiVersions.put(deviceType, latestVersion);
        }else{
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_WIFI_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {
                devicesLatestWiFiVersions = new SparseArray<>();
                devicesLatestWiFiVersions.put(deviceType, latestVersion);
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestWiFiVersions = gson.fromJson(json, SparseArray.class);
                if(devicesLatestWiFiVersions == null){
                    devicesLatestWiFiVersions = new SparseArray<>();
                }
                devicesLatestWiFiVersions.put(deviceType, latestVersion);
            }
        }
        if(gson == null){
            gson = new Gson();
        }
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_DEVICES_LATEST_WIFI_VERSIONS, gson.toJson(devicesLatestWiFiVersions));
        editor.apply();
    }
    public static String getDeviceLatestWiFiFirmwareVersion(int deviceType){
        if(devicesLatestWiFiVersions != null){
            if(devicesLatestWiFiVersions.get(deviceType) != null && devicesLatestWiFiVersions.get(deviceType).length() >= 1){
                return devicesLatestWiFiVersions.get(deviceType);
            }
        }else{
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_WIFI_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {
                return Constants.DEVICE_DEFAULT_WIFI_FIRMWARE_VERSION;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestWiFiVersions = gson.fromJson(json, SparseArray.class);
                if(devicesLatestWiFiVersions != null && devicesLatestWiFiVersions.get(deviceType) != null && devicesLatestWiFiVersions.get(deviceType).length() >= 1){
                    return devicesLatestWiFiVersions.get(deviceType);
                }
            }
        }
        return Constants.DEVICE_DEFAULT_WIFI_FIRMWARE_VERSION;
    }

    public static void setDeviceLatestHWFirmwareVersion(int deviceType, String latestVersion){
        if(devicesLatestHWVersions != null){
            devicesLatestHWVersions.put(deviceType, latestVersion);
        }else{
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_HW_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {
                devicesLatestHWVersions = new SparseArray<>();
                devicesLatestHWVersions.put(deviceType, latestVersion);
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestHWVersions = gson.fromJson(json, SparseArray.class);
                if(devicesLatestHWVersions == null){
                    devicesLatestHWVersions = new SparseArray<>();
                }
                devicesLatestHWVersions.put(deviceType, latestVersion);
            }
        }
        if(gson == null){
            gson = new Gson();
        }
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_DEVICES_LATEST_HW_VERSIONS, gson.toJson(devicesLatestHWVersions));
        editor.apply();
    }
    public static String getDeviceLatestHWFirmwareVersion(int deviceType){
        if(devicesLatestHWVersions != null){
            if(devicesLatestHWVersions.get(deviceType) != null && devicesLatestHWVersions.get(deviceType).length() >= 1){
                return devicesLatestHWVersions.get(deviceType);
            }
        }else{
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_HW_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {
                return Constants.DEVICE_DEFAULT_HW_FIRMWARE_VERSION;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestHWVersions = gson.fromJson(json, SparseArray.class);
                if(devicesLatestHWVersions != null && devicesLatestHWVersions.get(deviceType) != null && devicesLatestHWVersions.get(deviceType).length() >= 1){
                    return devicesLatestHWVersions.get(deviceType);
                }
            }
        }
        return Constants.DEVICE_DEFAULT_HW_FIRMWARE_VERSION;
    }

    public static void setAppFirstStart(boolean state) {
        MySettings.appFirstStart = state;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putBoolean(PREF_APP_FIRST_START, appFirstStart);
        editor.apply();
    }
    public static boolean getAppFirstStart() {
        SharedPreferences prefs = getSettings();
        appFirstStart = prefs.getBoolean(PREF_APP_FIRST_START, true);
        return appFirstStart;
    }

    private static void setCurrentUserEmail(String email) {
        MySettings.loggedInUserEmail = email;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_ACTIVE_USER_EMAIL, loggedInUserEmail);
        editor.apply();
    }
    private static String getCurrentUserPhoneNumber() {
        if (loggedInUserEmail != null && loggedInUserEmail.length() >= 1) {
            return loggedInUserEmail;
        } else {
            SharedPreferences prefs = getSettings();
            loggedInUserEmail = prefs.getString(PREF_ACTIVE_USER_EMAIL, "");
            return loggedInUserEmail;
        }
    }

    public static void setActiveUser(User user){
        if(user != null){
            MySettings.loggedInUser = user;

            if(gson == null){
                gson = new Gson();
            }
            String json = gson.toJson(MySettings.loggedInUser);
            SharedPreferences.Editor editor = getSettings().edit();
            editor.putString(PREF_ACTIVE_USER, json);
            editor.apply();
        }
        /*if(user != null) {
            //save user into DB
            loggedInUser = user;
            MySettings.initDB().userDao().insertAll(user);
            MySettings.setCurrentUserEmail(user.getEmail());
        }*/
    }
    public static void deleteActiveUser(User user){
        if(user != null) {
            MySettings.loggedInUser = null;

            if(gson == null){
                gson = new Gson();
            }
            SharedPreferences.Editor editor = getSettings().edit();
            editor.putString(PREF_ACTIVE_USER, "");
            editor.apply();
        }
        /*if(user != null) {
            //delete user from DB
            loggedInUser = null;
            MySettings.initDB().userDao().delete(user);
            MySettings.setCurrentUserEmail(null);
        }*/
    }
    public static User getActiveUser() {
        if (loggedInUser != null) {
            return loggedInUser;
        } else {
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_ACTIVE_USER, "");
            if (json.isEmpty() || json.equals("null")) {
                return null;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                loggedInUser = gson.fromJson(json, User.class);
                return loggedInUser;
            }
        }
         /*if (loggedInUser != null) {
            return loggedInUser;
        } else {
            //get user from DB
            if(loggedInUserEmail == null || loggedInUserEmail.length() < 1){
                loggedInUserEmail = MySettings.getCurrentUserPhoneNumber();
            }
            loggedInUser = MySettings.initDB().userDao().findByMEmail(loggedInUserEmail);
            return loggedInUser;
        }*/
    }
    public static boolean checkUser(User user){
        if(user.getPhoneNumber() != null && user.getPhoneNumber().length() >= 1){
            if(database == null) {
                database = Room.databaseBuilder(MyApp.getInstance(), AppDatabase.class, Constants.DB_NAME).allowMainThreadQueries().build();
            }
            userBeingChecked = MySettings.initDB().userDao().findByMEmail(user.getPhoneNumber());
            if(userBeingChecked != null && userBeingChecked.getPassword().equals(user.getPassword())){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public static List<User> getAllLinkedAccounts(){
        return MySettings.initDB().userDao().getAllLinkedAccounts();
    }
    public static void addUser(User user){
        if(user != null){
            MySettings.initDB().userDao().insert(user);
        }
    }
    public static void removeUser(User user){
        if(user != null){
            MySettings.initDB().userDao().delete(user);
        }
    }

    public static void clearData(){
        for (Place place:MySettings.getAllPlaces()) {
            MySettings.removePlace(place);
        }

        MySettings.setCurrentPlace(null);
        MySettings.setCurrentFloor(null);
        MySettings.setCurrentRoom(null);

        MySettings.setTempPlace(null);
        MySettings.setTempRoom(null);
        MySettings.setTempDevice(null);

        for (WifiNetwork network:MySettings.getAllWifiNetworks()) {
            MySettings.removeWifiNetwork(network);
        }
        for (User user:MySettings.getAllLinkedAccounts()) {
            MySettings.removeUser(user);
        }
        MySettings.deleteActiveUser(MySettings.getActiveUser());
        if(FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseAuth.getInstance().signOut();
        }
    }

    public static void clearNonUserData(){
        for (Place place:MySettings.getAllPlaces()) {
            MySettings.removePlace(place);
        }

        MySettings.setCurrentPlace(null);
        MySettings.setCurrentFloor(null);
        MySettings.setCurrentRoom(null);

        MySettings.setTempPlace(null);
        MySettings.setTempRoom(null);
        MySettings.setTempDevice(null);

        for (WifiNetwork network:MySettings.getAllWifiNetworks()) {
            MySettings.removeWifiNetwork(network);
        }
        for (User user:MySettings.getAllLinkedAccounts()) {
            MySettings.removeUser(user);
        }
    }

    public static AppDatabase initDB(){
        if(database != null){
            return database;
        }else{
            Migration MIGRATION_1_2 = new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("CREATE TABLE `Type` (`id` INTEGER NOT NULL DEFAULT -1, "
                            + "`name` TEXT,"
                            + "`category_id` INTEGER NOT NULL DEFAULT 0,"
                            + "`image_url` TEXT,"
                            + "`image_resource_id` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN type_id INTEGER NOT NULL DEFAULT -1");
                    database.execSQL("ALTER TABLE Floor "
                            + " ADD COLUMN type_id INTEGER NOT NULL DEFAULT -1");
                    database.execSQL("ALTER TABLE Room "
                            + " ADD COLUMN type_id INTEGER NOT NULL DEFAULT -1");
                    /*database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN type_id INTEGER NOT NULL DEFAULT -1");*/
                }
            };

            Migration MIGRATION_2_3 = new Migration(2, 3) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("CREATE TABLE `sounddevicedata` (`id` INTEGER NOT NULL DEFAULT -1, "
                            + "`device_id` INTEGER NOT NULL DEFAULT -1,"
                            + "`mode` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))");
                }
            };

            Migration MIGRATION_3_4 = new Migration(3, 4) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN access_token TEXT DEFAULT 'ronix_token'");
                }
            };

            Migration MIGRATION_4_5 = new Migration(4, 5) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN last_seen_timestamp INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_5_6 = new Migration(5, 6) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("CREATE TABLE `pirdata` (`id` INTEGER NOT NULL DEFAULT -1, "
                            + "`device_id` INTEGER NOT NULL DEFAULT -1,"
                            + "`state` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))");
                }
            };

            Migration MIGRATION_6_7 = new Migration(6, 7) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN mode INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_7_8 = new Migration(7, 8) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN primary_device_chip_id TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN primary_line_position INTEGER NOT NULL DEFAULT -1");

                }
            };

            Migration MIGRATION_8_9 = new Migration(8, 9) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN firmware_update_available INTEGER NOT NULL DEFAULT 0");

                }
            };

            Migration MIGRATION_9_10 = new Migration(9, 10) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN firmware_version TEXT DEFAULT '101400'");

                }
            };

            Migration MIGRATION_10_11 = new Migration(10, 11) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN pir_power_state INTEGER NOT NULL DEFAULT 1");
                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN pir_dimming_state INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN pir_dimming_value INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN pir_trigger_action_duration INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Line "
                            + " ADD COLUMN pir_trigger_action_duration_time_unit INTEGER NOT NULL DEFAULT 0");

                }
            };

            Migration MIGRATION_11_12 = new Migration(11, 12) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("CREATE TABLE `wifinetwork` (`id` INTEGER NOT NULL DEFAULT 0, "
                            + "`ssid` TEXT,"
                            + "`password` TEXT,"
                            + "`signal_strength` TEXT,"
                            + "`place_id` INTEGER NOT NULL DEFAULT -1, PRIMARY KEY(`id`))");
                    database.execSQL("CREATE UNIQUE INDEX index_WifiNetwork_ssid ON wifinetwork (ssid)");
                }
            };

            Migration MIGRATION_12_13 = new Migration(12, 13) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Type "
                            + " ADD COLUMN image_resource_name TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_13_14 = new Migration(13, 14) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN mode INTEGER NOT NULL DEFAULT " + Place.PLACE_MODE_REMOTE);
                }
            };

            Migration MIGRATION_14_15 = new Migration(14, 15) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN mqtt_reachable INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_15_16 = new Migration(15, 16) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN beep_state INTEGER NOT NULL DEFAULT 1");
                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN hw_lock_state INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN temperature INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_16_17 = new Migration(16, 17) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN hw_firmware_update_available INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN hw_firmware_version TEXT DEFAULT '0'");
                }
            };

            Migration MIGRATION_17_18 = new Migration(17, 18) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN static_ip_address INTEGER NOT NULL DEFAULT 1");
                }
            };

            Migration MIGRATION_18_19 = new Migration(18, 19) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN static_ip_address_sync_state INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_19_20 = new Migration(19, 20) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN ip_gateway TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Device "
                            + " ADD COLUMN ip_subnet_mask TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_20_21 = new Migration(20, 21) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN latitude REAL NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN longitude REAL NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN address TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN city TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN state TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN country TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Place "
                            + " ADD COLUMN zip_code TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_21_22 = new Migration(21, 22) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("CREATE TABLE `Speaker` (`id` INTEGER NOT NULL DEFAULT 0, "
                            + "`name` TEXT,"
                            + "`volume` INTEGER NOT NULL DEFAULT 0, "
                            + "`sound_device_id` INTEGER NOT NULL DEFAULT -1, PRIMARY KEY(`id`))");
                }
            };

            Migration MIGRATION_22_23 = new Migration(22, 23) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    //database.execSQL("CREATE UNIQUE INDEX index_WifiNetwork_ssid ON wifinetwork (ssid)");

                    database.execSQL("" +
                            "DROP INDEX index_WifiNetwork_ssid");
                }
            };

            Migration MIGRATION_23_24 = new Migration(23, 24) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE wifinetwork "
                            + " ADD COLUMN mac_address TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_24_25 = new Migration(24, 25) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE user "
                            + " ADD COLUMN linked_account INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_25_26 = new Migration(25, 26) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE user "
                            + " ADD COLUMN linked_timestamp INTEGER NOT NULL DEFAULT 0");
                }
            };

            Migration MIGRATION_26_27 = new Migration(26, 27) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE device "
                            + " ADD COLUMN hw_version TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_27_28 = new Migration(27, 28) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE device "
                            + " ADD COLUMN wifi_version TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_28_29 = new Migration(28, 29) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE type "
                            + " ADD COLUMN color_hex_code TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_29_30 = new Migration(29, 30) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE place "
                            + " ADD COLUMN type_name TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE floor "
                            + " ADD COLUMN type_name TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE room "
                            + " ADD COLUMN type_name TEXT DEFAULT ''");
                }
            };

            Migration MIGRATION_30_31 = new Migration(30, 31) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {

                    //dropAllUserTables(database);

                    database.execSQL("ALTER TABLE Type "
                            + " ADD COLUMN background_image_url TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE Type "
                            + " ADD COLUMN background_image_resource_id INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE Type "
                            + " ADD COLUMN background_image_resource_name TEXT DEFAULT ''");
                }
            };

            database = Room.databaseBuilder(MyApp.getInstance(), AppDatabase.class, Constants.DB_NAME)
                            .addMigrations(MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7,
                                    MIGRATION_7_8,
                                    MIGRATION_8_9,
                                    MIGRATION_9_10,
                                    MIGRATION_10_11,
                                    MIGRATION_11_12,
                                    MIGRATION_12_13,
                                    MIGRATION_13_14,
                                    MIGRATION_14_15,
                                    MIGRATION_15_16,
                                    MIGRATION_16_17,
                                    MIGRATION_17_18,
                                    MIGRATION_18_19,
                                    MIGRATION_19_20,
                                    MIGRATION_20_21,
                                    MIGRATION_21_22,
                                    MIGRATION_22_23,
                                    MIGRATION_23_24,
                                    MIGRATION_24_25,
                                    MIGRATION_25_26,
                                    MIGRATION_26_27,
                                    MIGRATION_27_28,
                                    MIGRATION_28_29,
                                    MIGRATION_29_30,
                                    MIGRATION_30_31)
                            .allowMainThreadQueries().
                            build();
            return database;
        }
    }

    private static void dropAllUserTables(SupportSQLiteDatabase db) {
        Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'", null);
        //noinspection TryFinallyCanBeTryWithResources not available with API < 19
        try {
            List<String> tables = new ArrayList<>(cursor.getCount());

            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }

            for (String table : tables) {
                if (table.startsWith("sqlite_")) {
                    continue;
                }
                db.execSQL("DROP TABLE IF EXISTS " + table);
                Log.v(TAG, "Dropped table " + table);
            }
        } finally {
            cursor.close();
        }
    }

    public static SharedPreferences getSettings() {
        if(sharedPref == null){
            sharedPref = MyApp.getShardPrefs();
        }

        return sharedPref;
    }
}