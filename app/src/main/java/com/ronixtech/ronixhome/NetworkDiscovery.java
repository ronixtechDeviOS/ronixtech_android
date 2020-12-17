package com.ronixtech.ronixhome;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.QueryListener;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class NetworkDiscovery {
    private static final String TAG = NetworkDiscovery.class.getSimpleName();

    private static NsdManager mNsdManager;

    private static DNSSD dnssd;
    private static boolean started = false;

    public static void init(){
        /*if(MainActivity.getInstance() != null) {
            if(!started){
                started = true;
                dnssd = new DNSSDBindable(MainActivity.getInstance());
                try {
                    dnssd.browse("_http._tcp", new BrowseListener() {
                        @Override
                        public void serviceFound(DNSSDService browser, int flags, int ifIndex,
                                                 final String serviceName, String regType, String domain) {
                            Log.d(TAG, "serviceFound " + serviceName);
                            if(serviceName != null && serviceName.toLowerCase().startsWith(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())){
                                try{
                                    dnssd.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                                        @Override
                                        public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                                            Log.d(TAG, "serviceResolved: fullName: " + fullName);
                                            Log.d(TAG, "serviceResolved: hostName: " + hostName);
                                            Log.d(TAG, "serviceResolved: txtRecord :" + txtRecord.toString());
                                            startQueryRecords(flags, ifIndex, serviceName, regType, domain, hostName, port, txtRecord);
                                        }

                                        @Override
                                        public void operationFailed(DNSSDService service, int errorCode) {
                                            Log.d(TAG, "operationFailed: " + errorCode);
                                        }
                                    });
                                }catch (DNSSDException e) {
                                    Log.d(TAG, "error", e);
                                }
                            }
                        }

                        @Override
                        public void serviceLost(DNSSDService browser, int flags, int ifIndex,
                                                String serviceName, String regType, String domain) {
                            Log.d(TAG, "serviceLost " + serviceName);
                        }

                        @Override
                        public void operationFailed(DNSSDService service, int errorCode) {
                            Log.d(TAG, "operationFailed: " + errorCode);
                        }
                    });
                } catch (DNSSDException e) {
                    Log.d(TAG, "error", e);
                }
            }
        }*/

        if(MainActivity.getInstance() != null){
            if(mNsdManager == null) {
                try{
                    mNsdManager = (NsdManager) MainActivity.getInstance().getSystemService(Context.NSD_SERVICE);
                    mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                        @Override
                        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                            Utils.log(TAG, "onStartDiscoveryFailed failed: Error code:" + errorCode, true);
                            //mNsdManager.stopServiceDiscovery(this);
                        }

                        @Override
                        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                            Utils.log(TAG, "onStopDiscoveryFailed failed: Error code:" + errorCode, true);
                            //mNsdManager.stopServiceDiscovery(this);
                        }

                        @Override
                        public void onDiscoveryStarted(String serviceType) {
                            Utils.log(TAG, "onDiscoveryStarted discovery started", true);
                        }

                        @Override
                        public void onDiscoveryStopped(String serviceType) {
                            Utils.log(TAG, "onDiscoveryStopped stopped: " + serviceType, true);
                        }

                        @Override
                        public void onServiceFound(NsdServiceInfo serviceInfo) {
                            // A service was found! Do something with it.
                            Utils.log(TAG, "onServiceFound discovery success", true);
                            // The name of the service tells the user what they'd be
                            // connecting to. It could be "Bob's Chat App".
                            Utils.log(TAG, "Service Name: " + serviceInfo.getServiceName(), true);
                            // Service type is the string containing the protocol and
                            // transport layer for this service.
                            Utils.log(TAG, "Service Type: " + serviceInfo.getServiceType(), true);

                            if(serviceInfo.getServiceName().startsWith(Constants.DEVICE_NAME_IDENTIFIER)){
                                mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                                    @Override
                                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                        // Called when the resolve fails. Use the error code to debug.
                                        Utils.log(TAG, "onResolveFailed: " + errorCode, true);
                                    }

                                    @Override
                                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                        Utils.log(TAG, "onServiceResolved: " + serviceInfo, true);

                                        NsdServiceInfo mService = serviceInfo;
                                        int port = mService.getPort();
                                        InetAddress host = mService.getHost();
                                        Utils.log(TAG, "PORT: " + port, true);
                                        Utils.log(TAG, "HOST: " + host, true);

                                        if (serviceInfo.getServiceName().startsWith(Constants.DEVICE_NAME_IDENTIFIER)) {

                                            int index = serviceInfo.getServiceName().indexOf("_");
                                            if(index != -1){
                                                Device device = DevicesInMemory.getDeviceByChipID(serviceInfo.getServiceName().substring(index+1));
                                                if(device != null){
                                                    if(device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(host.getHostAddress())){
                                                        Utils.log(TAG, "Device " + device.getName() + " updated with IP: " + host.getHostAddress(), true);
                                                        Utils.showNotification(device);
                                                        device.setIpAddress(host.getHostAddress());
                                                        DevicesInMemory.updateDevice(device);
                                                        MySettings.updateDeviceIP(device, host.getHostAddress());

                                                        if(MainActivity.getInstance() != null) {
                                                            MainActivity.getInstance().refreshDevicesListFromMemory();
                                                        }
                                                    }else{
                                                        Utils.log(TAG, "Device " + device.getName() + " already has an up-to-date IP: " + host.getHostAddress(), true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onServiceLost(NsdServiceInfo serviceInfo) {
                            // When the network service is no longer available.
                            // Internal bookkeeping code goes here.
                            Utils.log(TAG, "onServiceLost discovery lost: " + serviceInfo, true);
                        }
                    });
                }catch (IllegalArgumentException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (Exception e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }
            }
        }
    }

    private static void startQueryRecords(int flags, int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {
        try {
            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {
                    Utils.log(TAG, "queryAnswered fullName " + fullName, true);
                    Utils.log(TAG, "queryAnswered serviceName " + serviceName, true);

                    try {
                        InetAddress address = InetAddress.getByAddress(rdata);
                        if (address instanceof Inet4Address) {
                            Utils.log(TAG, "queryAnswered InetAddress address: " + address.toString().substring(1), true);
                            int index = serviceName.indexOf("_");
                            if(index != -1){
                                Device device = DevicesInMemory.getDeviceByChipID(serviceName.substring(index+1));
                                if(device != null){
                                    if(device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(address.toString().substring(1))){
                                        Utils.log(TAG, "Device " + device.getName() + " updated with IP: " + address.toString().substring(1), true);
                                        Utils.showNotification(device);

                                        device.setIpAddress(address.toString().substring(1));
                                        DevicesInMemory.updateDevice(device);
                                        MySettings.updateDeviceIP(device, address.toString().substring(1));

                                        if(MainActivity.getInstance() != null) {
                                            MainActivity.getInstance().refreshDevicesListFromMemory();
                                        }
                                    }else{
                                        Utils.log(TAG, "Device " + device.getName() + " already has an up-to-date IP: " + address.toString().substring(1), true);
                                    }
                                }
                            }
                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {

                }
            };
            dnssd.queryRecord(flags, ifIndex, hostName, 1, 1, listener);
            //dnssd.queryRecord(0, ifIndex, hostName, 28, 1, listener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }
}
