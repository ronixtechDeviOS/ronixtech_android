package com.ronixtech.ronixhome.entities;

public class WifiDevice {
    private String name;
    private String ipAddress;
    private String macAddress;

    public WifiDevice(){
        this.name = "";
        this.ipAddress = "";
        this.macAddress = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public boolean equals(Object o){
        String otherMac = (String) o;
        if(otherMac.equals(this.macAddress)){
            return true;
        }else{
            return false;
        }
    }
}