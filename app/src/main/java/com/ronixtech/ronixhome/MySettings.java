package com.ronixtech.ronixhome;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
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
    public static final String PREF_HOME_NETOWORK = "pref_home_network";
    public static final String PREF_TEMP_DEVICE_MAC_ADDRESS = "temp_device_mac_address";
    public static final String PREF_TEMP_DEVICE_TYPE_ID = "temp_device_type_id";
    public static final String PREF_TEMP_DEVICE = "temp_device";
    public static final String PREF_SCANNING_ACTIVE = "scanning_active";
    public static final String PREF_CURRENT_PLACE = "pref_current_place";
    public static final String PREF_CURRENT_FLOOR = "pref_current_floor";
    public static final String PREF_CURRENT_ROOM = "pref_current_room";
    public static final String PREF_CONTROL_ACTIVE = "control_active";
    public static final String PREF_GETSTATUS_ACTIVE = "get_status_active";
    public static final String PREF_APP_FIRST_START = "app_first_start";
    public static final String PREF_DEVICES_LATEST_VERSIONS = "pref_devices_latest_firmware_versions";


    private static User loggedInUser;
    private static User userBeingChecked;
    private static String loggedInUserEmail;
    private static SharedPreferences sharedPref;
    private static boolean initialStartup;
    private static boolean scanningActive;
    private static boolean controlActive;
    private static boolean gettingStatusActive;
    private static WifiNetwork homeNetwork;
    private static String tempDeviceMACAddress;
    private static int tempDeviceTypeID;
    private static Device tempDevice;
    private static boolean appFirstStart;

    private static Place currentPlace;
    private static Floor currentFloor;
    private static com.ronixtech.ronixhome.entities.Room currentRoom;

    private static SparseArray<String> devicesLatestVersions;

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

    public static void addDevice(Device device){
        //save device into DB
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround) {
            MySettings.initDB().deviceDAO().insertDeviceWithLines(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().insertDeviceWithSoundDeviceData(device);
        }
    }
    public static void updateDeviceIP(Device device, String ipAddress){
        MySettings.initDB().deviceDAO().updateDeviceIP(device.getId(), ipAddress);
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
    public static Device getDeviceByID(long deviceID, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
            MySettings.initDB().deviceDAO().getDeviceWithLinesByID(deviceID);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByID(deviceID);
        }
        return null;
    }
    public static Device getDeviceByMAC(String macAddress, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
            return MySettings.initDB().deviceDAO().getDeviceWithLinesByMacAddress(macAddress);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByMacAddress(macAddress);
        }
        return null;
    }
    public static Device getDeviceByChipID(String chipID, int deviceType) {
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
            return MySettings.initDB().deviceDAO().getDeviceWithLinesByChipID(chipID);
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().getDeviceWithSoundSystemDataByChipID(chipID);
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
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround) {
            MySettings.initDB().deviceDAO().removeDeviceWithLines(device);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            MySettings.initDB().deviceDAO().removeDeviceWithSoundDeviceData(device);
        }

    }
    public static Device getDeviceByChipID2(String chipID) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByChipID(chipID);
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
    public static void removeRoom(com.ronixtech.ronixhome.entities.Room room){
        //remove room from DB
        MySettings.initDB().roomDAO().removeRoomsWithDevices(room);
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
        return MySettings.initDB().typeDAO().getTypes(category);
    }

    public static void scanNetwork(){
        if(!MySettings.getCurrentScanningState()){
            MySettings.setCurrentScanningState(true);
            Utils.showUpdatingNotification();
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
            NetworkScannerAsyncTask networkScannerAsyncTask = new NetworkScannerAsyncTask();
            networkScannerAsyncTask.execute();
        }
    }
    public static void scanDevices(){
        if(!MySettings.getCurrentScanningState()){
            Log.d(TAG, "scanningNetwork starting");
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
            Log.d(TAG, "scanningNetwork already started");
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
        return false;
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

    public static void setDeviceLatestFirmwareVersion(int deviceType, String latestVersion){
        if(devicesLatestVersions != null){
            devicesLatestVersions.put(deviceType, latestVersion);
        }else{
            devicesLatestVersions = new SparseArray<>();
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {

            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestVersions = gson.fromJson(json, SparseArray.class);
                devicesLatestVersions.put(deviceType, latestVersion);
            }
        }

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_DEVICES_LATEST_VERSIONS, gson.toJson(devicesLatestVersions));
        editor.apply();
    }
    public static String getDeviceLatestFirmwareVersion(int deviceType){
        if(devicesLatestVersions != null){
            if(devicesLatestVersions.get(deviceType) != null && devicesLatestVersions.get(deviceType).length() >= 1){
                return devicesLatestVersions.get(deviceType);
            }
        }else{
            devicesLatestVersions = new SparseArray<>();
            SharedPreferences prefs = getSettings();
            String json = prefs.getString(PREF_DEVICES_LATEST_VERSIONS, "");
            if (json.isEmpty() || json.equals("null")) {
                return Constants.DEVICE_DEFAULT_FIRMWARE_VERSION;
            } else {
                if(gson == null){
                    gson = new Gson();
                }
                devicesLatestVersions = gson.fromJson(json, SparseArray.class);
                if(devicesLatestVersions.get(deviceType) != null && devicesLatestVersions.get(deviceType).length() >= 1){
                    return devicesLatestVersions.get(deviceType);
                }
            }
        }
        return Constants.DEVICE_DEFAULT_FIRMWARE_VERSION;
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

    public static void setCurrentUser(User user){
        if(user != null) {
            //save user into DB
            loggedInUser = user;
            MySettings.initDB().userDao().insertAll(user);
            MySettings.setCurrentUserEmail(user.getEmail());
        }
    }
    public static void deleteCurrentUser(User user){
        if(user != null) {
            //delete user from DB
            loggedInUser = null;
            MySettings.initDB().userDao().delete(user);
            MySettings.setCurrentUserEmail(null);
        }
    }
    public static User getActiveUser() {
        if (loggedInUser != null) {
            return loggedInUser;
        } else {
            //get user from DB
            if(loggedInUserEmail == null || loggedInUserEmail.length() < 1){
                loggedInUserEmail = MySettings.getCurrentUserPhoneNumber();
            }
            loggedInUser = MySettings.initDB().userDao().findByMEmail(loggedInUserEmail);
            return loggedInUser;
        }
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

            database = Room.databaseBuilder(MyApp.getInstance(), AppDatabase.class, Constants.DB_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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