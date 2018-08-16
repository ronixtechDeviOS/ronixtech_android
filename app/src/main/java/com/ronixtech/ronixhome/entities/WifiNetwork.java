package com.ronixtech.ronixhome.entities;

//This is an entity that represents a WiFi network, with an SSID, password, and signal strength
public class WifiNetwork {
    String ssid;
    String signal;
    String password;

    public WifiNetwork(){
        this.ssid = "";
        this.signal = "";
        this.password = "";
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o){
        WifiNetwork secondNetwork = (WifiNetwork) o;
        if(this.ssid.equals(secondNetwork.getSsid())){
            return true;
        }else{
            return false;
        }
    }
}
