package com.ronixtech.ronixhome;

import com.ronixtech.ronixhome.entities.Device;

import java.util.ArrayList;
import java.util.List;

public class DevicesInMemory {
    private static List<Device> devices;
    private static List<Device> localDevices;
    static {
        devices = new ArrayList<>();
        localDevices = new ArrayList<>();
    }

    public static void setDevices(List<Device> newDevices){
        devices.clear();
        devices.addAll(newDevices);
    }

    public static void setLocalDevices(List<Device> devices){
        localDevices.clear();
        if(devices != null){
            for (Device device:devices) {
                Device localDevice = new Device(device);
                localDevices.add(localDevice);
            }
        }
    }

    public static List<Device> getDevices(){
        return devices;
    }

    public static List<Device> getLocalDevices(){
        return localDevices;
    }

    public static void updateDevice(Device device){
        int index = devices.indexOf(device);
        if(index != -1 && index < devices.size()){
            devices.set(index, device);
        }
    }

    public static void updateLocalDevice(Device device){
        int index = localDevices.indexOf(device);
        if(index != -1 && index < localDevices.size()){
            localDevices.set(index, device);
        }
    }

    public static Device getDevice(Device device){
        int index = devices.indexOf(device);
        if(index != -1 && index < devices.size()){
            return devices.get(index);
        }
        return null;
    }

    public static Device getLocalDevice(Device device){
        int index = localDevices.indexOf(device);
        if(index != -1 && index < localDevices.size()){
            return localDevices.get(index);
        }
        return null;
    }

    public static Device getDeviceByChipID(String chipID){
        if(devices != null && devices.size() >= 1){
            for (Device device:devices) {
                if(device.getChipID().equals(chipID)){
                    return device;
                }
            }
            return null;
        }
        return null;
    }

    public static Device getDeviceByID(long deviceID){
        if(devices != null && devices.size() >= 1){
            for (Device device:devices) {
                if(device.getId() == deviceID){
                    return device;
                }
            }
            return null;
        }
        return null;
    }

    public static List<Device> getRoomDevices(long roomID){
        List<Device> roomDevices = new ArrayList<>();
        if(devices.size() >= 1){
            for (Device device : devices) {
                if(device.getRoomID() == roomID){
                    if(!roomDevices.contains(device)){
                        roomDevices.add(device);
                    }
                }
            }
        }
        return roomDevices;
    }

    public static void removeDevice(Device device){
        devices.remove(device);
        localDevices.remove(device);
    }
}
