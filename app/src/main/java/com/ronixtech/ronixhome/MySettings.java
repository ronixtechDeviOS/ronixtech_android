package com.ronixtech.ronixhome;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
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
    public static final String PREF_CURRENT_DEVICE_CONFIG_MAC_ADDRESS = "current_device_mac_address";
    public static final String PREF_TEMP_DEVICE = "temp_device";
    public static final String PREF_SCANNING_ACTIVE = "scanning_active";
    public static final String PREF_CURRENT_PLACE = "pref_current_place";
    public static final String PREF_CURRENT_FLOOR = "pref_current_floor";
    public static final String PREF_CONTROL_ACTIVE = "control_active";


    private static User loggedInUser;
    private static User userBeingChecked;
    private static String loggedInUserEmail;
    private static SharedPreferences sharedPref;
    private static boolean initialStartup;
    private static boolean scanningActive;
    private static boolean controlActive;
    private static WifiNetwork homeNetwork;
    private static String currentDeviceBeingConfiguredMACAddress;
    private static Device tempDevice;

    private static Place currentPlace;
    private static Floor currentFloor;

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

    public static void setCurrentDeviceConfigMacAddress(String macAddress) {
        MySettings.currentDeviceBeingConfiguredMACAddress = macAddress;

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(PREF_CURRENT_DEVICE_CONFIG_MAC_ADDRESS, currentDeviceBeingConfiguredMACAddress);
        editor.apply();
    }
    public static String getCurrentDeviceConfigMacAddress() {
        if (currentDeviceBeingConfiguredMACAddress != null && currentDeviceBeingConfiguredMACAddress.length() >= 1) {
            return currentDeviceBeingConfiguredMACAddress;
        } else {
            SharedPreferences prefs = getSettings();
            currentDeviceBeingConfiguredMACAddress = prefs.getString(PREF_CURRENT_DEVICE_CONFIG_MAC_ADDRESS, "");
            return currentDeviceBeingConfiguredMACAddress;
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
                return tempDevice;
            }
        }
    }

    public static void addDevice(Device device){
        //save device into DB
        MySettings.initDB().deviceDAO().insertDeviceWithLines(device);
    }
    public static void updateDeviceIP(Device device, String ipAddress){
        MySettings.initDB().deviceDAO().updateDeviceIP(device.getId(), ipAddress);
    }
    public static void updateDeviceErrorCount(Device device, int count){
        MySettings.initDB().deviceDAO().updateDeviceErrorCount(device.getId(), count);
    }
    public static Device getDeviceByID(long deviceID) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByID(deviceID);
    }
    public static Device getDeviceByMAC(String macAddress) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByMacAddress(macAddress);
    }
    public static Device getDeviceByChipID(String chipID) {
        return MySettings.initDB().deviceDAO().getDeviceWithLinesByChipID(chipID);
    }
    public static List<Device> getAllDevices(){
        List<Device> devicesWithLines = new ArrayList<>();
        List<Device> devices = MySettings.initDB().deviceDAO().getAll();
        if (devices != null && devices.size() >= 1) {
            for (Device dev : devices) {
                Device tempDevice = MySettings.getDeviceByMAC(dev.getMacAddress());
                devicesWithLines.add(tempDevice);
            }
        }
        return devicesWithLines;
    }
    public static List<Device> getRoomDevices(long roomID){
        List<Device> devicesWithLines = new ArrayList<>();
        List<Device> devices = MySettings.initDB().deviceDAO().getRoomDevices(roomID);
        if (devices != null && devices.size() >= 1) {
            for (Device dev : devices) {
                Device tempDevice = MySettings.getDeviceByMAC(dev.getMacAddress());
                devicesWithLines.add(tempDevice);
            }
        }
        return devicesWithLines;
    }
    public static void removeDevice(Device device){
        //remove device from DB
        MySettings.initDB().deviceDAO().removeDeviceWithLines(device);
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

    public static void addPlace(Place place){
        //save floor into DB
        MySettings.initDB().placeDAO().insertPlaceWithFloors(place);
    }
    public static Place getPlace(long placeID) {
        return MySettings.initDB().placeDAO().getPlaceWIthFloors(placeID);
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
        return controlActive;
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
            //save user into DB
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
            database = Room.databaseBuilder(MyApp.getInstance(), AppDatabase.class, Constants.DB_NAME)
                            .allowMainThreadQueries().
                            fallbackToDestructiveMigration().
                            build();
            return database;
        }
    }

    public static SharedPreferences getSettings() {
        if(sharedPref == null){
            sharedPref = MyApp.getShardPrefs();
        }

        return sharedPref;
    }
}