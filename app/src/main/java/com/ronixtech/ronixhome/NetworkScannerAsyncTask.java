package com.ronixtech.ronixhome;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Count;
import com.ronixtech.ronixhome.entities.Device;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NetworkScannerAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = NetworkScannerAsyncTask.class.getSimpleName();

    //Activity activity;
    WifiManager mWifiManager;
    DhcpInfo dhcpInfo;

    int NUMBER_OF_THREADS = 10;
    int CURRENT_THREAD;
    int RANGE;
    int START_IP;
    int END_IP;

    public NetworkScannerAsyncTask() {
        //this.activity = activity;
    }

    @Override
    protected void onPreExecute(){
        //mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        dhcpInfo = mWifiManager.getDhcpInfo();
    }

    @Override
    protected void onProgressUpdate(Void... params){
        /*devicesAdapter.notifyDataSetChanged();
        if(devices != null && devices.size() >= 1){
            mqttNoDevicesTextview.setVisibility(View.GONE);
            devicesListView.setVisibility(View.VISIBLE);
        }else{
            mqttNoDevicesTextview.setVisibility(View.VISIBLE);
            devicesListView.setVisibility(View.GONE);
        }*/
    }

    @Override
    protected void onPostExecute(Void params) {
        /*devicesAdapter.notifyDataSetChanged();
        if(devices != null && devices.size() >= 1){
            mqttNoDevicesTextview.setVisibility(View.GONE);
            devicesListView.setVisibility(View.VISIBLE);
        }else{
            mqttNoDevicesTextview.setVisibility(View.VISIBLE);
            devicesListView.setVisibility(View.GONE);
        }*/
    }

    @Override
    protected Void doInBackground(Void... params) {
        try
        {
            Log.d(TAG, "DHCP: gateway: " + intToIp(dhcpInfo.gateway));
            Log.d(TAG, "DHCP: dns1: " + intToIp(dhcpInfo.dns1));
            Log.d(TAG, "DHCP: dns2: " + intToIp(dhcpInfo.dns2));
            Log.d(TAG, "DHCP: ipAddress: " + intToIp(dhcpInfo.ipAddress));
            Log.d(TAG, "DHCP: netmask: " + intToIp(dhcpInfo.netmask));
            Log.d(TAG, "DHCP: serverAddress: " + intToIp(dhcpInfo.serverAddress));
            InetAddress host = InetAddress.getByName(intToIp(dhcpInfo.gateway));
            byte[] ip = host.getAddress();

            NUMBER_OF_THREADS = Utils.getNumCores();

            RANGE = 254 / NUMBER_OF_THREADS;
            START_IP = 1;
            END_IP = START_IP + RANGE;
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            Log.d(TAG, "Resetting counter");
            Count.reset();

            for(CURRENT_THREAD = 0; CURRENT_THREAD < NUMBER_OF_THREADS; CURRENT_THREAD++){
                int currentThread = CURRENT_THREAD;
                int startIP = START_IP;
                int endIP = END_IP;
                Log.d(TAG, "RANGE #" + currentThread + " START " + START_IP + " END " + END_IP);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = startIP; i <= endIP; i++) {
                                ip[3] = (byte) i;
                                InetAddress address = InetAddress.getByAddress(ip);
                                if(address != null){
                                    if (address.isReachable(150)) {
                                        //Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is turned on and can be pinged");
                                    } /*else if (!address.getHostAddress().equals(address.getHostName())) {
                                        Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is known in a DNS lookup");

                                    }*/ else {
                                        //Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
                                    }
                                }else{
                                    //Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
                                }
                            }
                            readAddresses();
                        }catch(UnknownHostException e1) {
                            e1.printStackTrace();
                        }
                        catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //runnable.run();
                executor.execute(runnable);
                START_IP = END_IP + 1;
                END_IP = START_IP + RANGE;
                // = globalCount + 1;
            }
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        return null;
    }

    private void readAddresses() {
        Log.d(TAG, "Reading addresses from local /proc/net/arp file");
        List<Device> devices = new ArrayList<>();
        devices.addAll(DevicesInMemory.getDevices());
        //List<WifiDevice> wifiDevices = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //Log.d(TAG, "rawfiledata " + line);
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String arpResult = splitted[2];
                    String scannedIP = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        /*WifiDevice wifiDevice = new WifiDevice();
                        wifiDevice.setIpAddress(ip);
                        wifiDevice.setMacAddress(mac);
                        wifiDevices.add(wifiDevice);*/

                        for (Device device : devices) {
                            //Log.d(TAG, "Checking if device " + device.getName() + " has an IP in the local /proc/net/arp file");
                            if(device.getMacAddress().toLowerCase().substring(2).equals(mac.toLowerCase().substring(2))){//skip the first 2 letters as each ESP unit has 2 MAC addresses with 2 different beginnings
                                if(!arpResult.equals("0x0")) {//failed arp request has 0x0 in 3rd entry in the arp file, so it's not an up to date ip address
                                    if(device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(scannedIP)){
                                        Log.d(TAG, "Device " + device.getName() + " updated with IP: " + scannedIP);
                                        Utils.showNotification(device);

                                        device.setIpAddress(scannedIP);
                                        DevicesInMemory.updateDevice(device);
                                        MySettings.updateDeviceIP(device, scannedIP);

                                        if(MainActivity.getInstance() != null) {
                                            MainActivity.getInstance().refreshDevicesListFromMemory();
                                        }
                                    }else{
                                        Log.d(TAG, "Device " + device.getName() + " already has an up-to-date IP: " + scannedIP);
                                    }
                                }
                            }else{
                                //Log.d(TAG, "Device " + device.getName() + " has no corresponding IP in the local /proc/net/arp file");
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally{
            Log.d(TAG, "increment counter");
            Count.increment();
            Log.d(TAG, "count=" + Count.count + " NumberOfThreads=" + NUMBER_OF_THREADS);
            if(Count.count >= NUMBER_OF_THREADS){
                Log.d(TAG, "ALL THREADS FINISHED SCANNING");
                MySettings.setCurrentScanningState(false);
                //Utils.hideUpdatingNotification();
                Count.reset();
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
            }
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
        }
    }


    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}