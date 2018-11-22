package com.ronixtech.ronixhome;

import android.net.nsd.NsdManager;
import android.util.Log;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.ResolveListener;
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
        if(MainActivity.getInstance() != null) {
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
        }

        /*if(MainActivity.getInstance() != null){
            if(mNsdManager == null) {
                try{
                    mNsdManager = (NsdManager) MainActivity.getInstance().getSystemService(Context.NSD_SERVICE);
                    mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                        @Override
                        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                            Log.d(TAG, "onStartDiscoveryFailed failed: Error code:" + errorCode);
                            //mNsdManager.stopServiceDiscovery(this);
                        }

                        @Override
                        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                            Log.d(TAG, "onStopDiscoveryFailed failed: Error code:" + errorCode);
                            //mNsdManager.stopServiceDiscovery(this);
                        }

                        @Override
                        public void onDiscoveryStarted(String serviceType) {
                            Log.d(TAG, "onDiscoveryStarted discovery started");
                        }

                        @Override
                        public void onDiscoveryStopped(String serviceType) {
                            Log.d(TAG, "onDiscoveryStopped stopped: " + serviceType);
                        }

                        @Override
                        public void onServiceFound(NsdServiceInfo serviceInfo) {
                            // A service was found! Do something with it.
                            Log.d(TAG, "onServiceFound discovery success");
                            // The name of the service tells the user what they'd be
                            // connecting to. It could be "Bob's Chat App".
                            Log.d(TAG, "Service Name: " + serviceInfo.getServiceName());
                            // Service type is the string containing the protocol and
                            // transport layer for this service.
                            Log.d(TAG, "Service Type: " + serviceInfo.getServiceType());

                            if(serviceInfo.getServiceName().startsWith(Constants.DEVICE_NAME_IDENTIFIER)){
                                mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                                    @Override
                                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                        // Called when the resolve fails. Use the error code to debug.
                                        Log.d(TAG, "onResolveFailed: " + errorCode);
                                    }

                                    @Override
                                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                        Log.d(TAG, "onServiceResolved: " + serviceInfo);

                                        NsdServiceInfo mService = serviceInfo;
                                        int port = mService.getPort();
                                        InetAddress host = mService.getHost();
                                        Log.d(TAG, "PORT: " + port);
                                        Log.d(TAG, "HOST: " + host);

                                        if (serviceInfo.getServiceName().startsWith(Constants.DEVICE_NAME_IDENTIFIER)) {

                                            int index = serviceInfo.getServiceName().indexOf("_");
                                            if(index != -1){
                                                Device device = DevicesInMemory.getDeviceByChipID(serviceInfo.getServiceName().substring(index+1));
                                                if(device != null){
                                                    if(device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(host.getHostAddress())){
                                                        Log.d(TAG, "Device " + device.getName() + " updated with IP: " + host.getHostAddress());
                                                        Utils.showNotification(device);

                                                        device.setIpAddress(host.getHostAddress());
                                                        DevicesInMemory.updateDevice(device);
                                                        MySettings.updateDeviceIP(device, host.getHostAddress());

                                                        if(MainActivity.getInstance() != null) {
                                                            MainActivity.getInstance().refreshDevicesListFromMemory();
                                                        }
                                                    }else{
                                                        Log.d(TAG, "Device " + device.getName() + " already has an up-to-date IP: " + host.getHostAddress());
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
                            Log.d(TAG, "onServiceLost discovery lost: " + serviceInfo);
                        }
                    });
                }catch (IllegalArgumentException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (Exception e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
        }*/
    }

    private static void startQueryRecords(int flags, int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {
        try {
            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {
                    Log.d(TAG, "queryAnswered fullName " + fullName);
                    Log.d(TAG, "queryAnswered serviceName " + serviceName);

                    try {
                        InetAddress address = InetAddress.getByAddress(rdata);
                        if (address instanceof Inet4Address) {
                            Log.d(TAG, "queryAnswered InetAddress address: " + address.toString().substring(1));
                            int index = serviceName.indexOf("_");
                            if(index != -1){
                                Device device = DevicesInMemory.getDeviceByChipID(serviceName.substring(index+1));
                                if(device != null){
                                    if(device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(address.toString().substring(1))){
                                        Log.d(TAG, "Device " + device.getName() + " updated with IP: " + address.toString().substring(1));
                                        Utils.showNotification(device);

                                        device.setIpAddress(address.toString().substring(1));
                                        DevicesInMemory.updateDevice(device);
                                        MySettings.updateDeviceIP(device, address.toString().substring(1));

                                        if(MainActivity.getInstance() != null) {
                                            MainActivity.getInstance().refreshDevicesListFromMemory();
                                        }
                                    }else{
                                        Log.d(TAG, "Device " + device.getName() + " already has an up-to-date IP: " + address.toString().substring(1));
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
