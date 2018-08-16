package com.ronixtech.ronixhome;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.work.Worker;

public class NetworkScanner extends Worker {
    private static final String TAG = NetworkScanner.class.getSimpleName();

    WifiManager mWifiManager;
    DhcpInfo dhcpInfo;

    int NUMBER_OF_THREADS = 10;
    int CURRENT_THREAD;
    int RANGE;
    int START_IP;
    int END_IP;

    @Override
    public Worker.Result doWork(){
        Log.d(TAG, "doWork for NetworkScanner");
        // Do the work here--in this case, compress the stored images.
        // In this example no parameters are passed; the task is
        // assumed to be "compress the whole library."
        //myCompress();
        mWifiManager = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        dhcpInfo = mWifiManager.getDhcpInfo();

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

            Log.d(TAG, "resetting counter");
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
                                    if (address.isReachable(50)) {
                                        Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is turned on and can be pinged");
                                    } /*else if (!address.getHostAddress().equals(address.getHostName())) {
                                        Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is known in a DNS lookup");

                                    }*/ else {
                                        Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
                                    }
                                }else{
                                    Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
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

            /*if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                List<Device> devices = MySettings.getAllDevices();
                boolean scanNeeded = false;
                for (Device device : devices) {
                    String macAddress = device.getMacAddress();
                    String savedIPString = device.getIpAddress();
                    if(savedIPString != null && savedIPString.length() >= 1){
                        InetAddress savedAddress = InetAddress.getByName(savedIPString);
                        if(savedAddress.isReachable(60)){
                            //don't do anything, device is up to date
                            //TODO make sure macAddress of reached device is the same as the macAddress of the current device
                            //TODO Mo2akatan, ALWAYS scan
                            scanNeeded = true;
                            Log.d(TAG, "Device " + device.getName() + " has a reachable IP");
                        }else{
                            //scan for new ip using the macAddress
                            Log.d(TAG, "Device " + device.getName() + " has an unreachable IP");
                            scanNeeded = true;
                        }
                    }else{
                        //scan for new ip using the macAddress
                        Log.d(TAG, "Device " + device.getName() + " doesn't have an IP");
                        scanNeeded = true;
                    }
                }
                if(scanNeeded){
                    globalCount = 0;
                    RANGE = 254 / NUMBER_OF_THREADS;
                    START_IP = 1;
                    END_IP = START_IP + RANGE;
                    ThreadPoolExecutor executor=(ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_THREADS);

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
                                            if (address.isReachable(20)) {
                                                Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is turned on and can be pinged");
                                            } else if (!address.getHostAddress().equals(address.getHostName())) {
                                                Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is known in a DNS lookup");

                                            } else {
                                                Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
                                            }
                                        }else{
                                            Log.d(TAG, "THREAD #" + currentThread + " - ping - " + address + " machine is not reachable");
                                        }
                                    }
                                    readAddresses();
                                }catch(UnknownHostException e1)
                                {
                                    e1.printStackTrace();
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };
                        //runnable.run();
                        executor.execute(runnable);
                        START_IP = END_IP + 1;
                        END_IP = START_IP + RANGE;
                        globalCount = globalCount + 1;
                    }
                }
            }*/
        }catch(UnknownHostException e1)
        {
            Log.d(TAG, "Exception: " + e1.getMessage());
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            Log.d(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }

        // Indicate success or failure with your return value:
        return Result.SUCCESS;

        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private void readAddresses() {
        Log.d(TAG, "Reading addresses from local /proc/net/arp file");
        List<Device> devices = MySettings.getAllDevices();
        //List<WifiDevice> wifiDevices = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "rawfiledata " + line);
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String arpResult = splitted[2];
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        /*WifiDevice wifiDevice = new WifiDevice();
                        wifiDevice.setIpAddress(ip);
                        wifiDevice.setMacAddress(mac);
                        wifiDevices.add(wifiDevice);*/

                        for (Device device : devices) {
                            //Log.d(TAG, "Checking if device " + device.getName() + " has an IP in the local /proc/net/arp file");
                            if(device.getMacAddress().toLowerCase().substring(2).equals(mac.toLowerCase().substring(2))){
                                if(!arpResult.equals("0x0")) {//failed arp request has 0x0 in 3rd entry in the arp file, so it's not an up to date ip address
                                    Log.d(TAG, "Device " + device.getName() + " updated with IP: " + ip);
                                    Utils.showNotification(device);
                                    MySettings.updateDeviceIP(device, ip);
                                    if(MainActivity.getInstance() != null) {
                                        MainActivity.getInstance().updateDevicesList();
                                    }
                                }
                            }else{
                                //Log.d(TAG, "Device " + device.getName() + " has no corresponding IP in the local /proc/net/arp file");
                            }
                        }
                    }
                }
            }
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
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
        }
    }


    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}
