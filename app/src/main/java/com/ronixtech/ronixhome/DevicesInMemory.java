package com.ronixtech.ronixhome;

import com.ronixtech.ronixhome.entities.Device;

import java.util.ArrayList;
import java.util.List;

public class DevicesInMemory {
    private static List<Device> devices;
    static {
        devices = new ArrayList<>();
    }

    public static void setDevices(List<Device> newDevices){
        devices.clear();
        devices.addAll(newDevices);
    }

    public static List<Device> getDevices(){
        return devices;
    }

    public static void updateDevice(Device device){
        int index = devices.indexOf(device);
        if(index != -1 && index < devices.size()){
            devices.set(index, device);
        }
    }
}
