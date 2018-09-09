package com.ronixtech.ronixhome.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentSearch.HomeConnectedListenerInterface} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentSearch#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentSearch extends Fragment {
    private static final String TAG = AddDeviceFragmentSearch.class.getSimpleName();

    private HomeConnectedListenerInterface mListener;

    private static final int RC_PERMISSION_LOCATION = 1004;
    private static final int RC_PERMISSION_ACCESS_WIFI_STATE = 1005;
    private static final int RC_PERMISSION_CHANGE_WIFI_STATE= 1006;

    WifiManager mWifiManager;
    BroadcastReceiver mWifiScanReceiver;
    BroadcastReceiver mWifiConnectionReceiver;

    GifImageView instructionImageView;
    TextView step1TextView, step2TextView, debugTextView;

    public AddDeviceFragmentSearch() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceFragmentSearch.
     */
    public static AddDeviceFragmentSearch newInstance(String param1, String param2) {
        AddDeviceFragmentSearch fragment = new AddDeviceFragmentSearch();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_device_search, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_device_search), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        instructionImageView = view.findViewById(R.id.instructions_gif_imageview);
        step1TextView = view.findViewById(R.id.instructions_1_textview);
        step2TextView = view.findViewById(R.id.instructions_2_textview);
        debugTextView = view.findViewById(R.id.debug_textview);

        /*if(MySettings.getHomeNetwork() == null) {
            //if home network is not defined, configure it first so when adding the device(s), they're automatically connected to it
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
            fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
            fragmentTransaction.addToBackStack("wifiInfoFragment");
            fragmentTransaction.commit();
        }else{
            //scan for device in the background, when found, confirm it's the correct the device and continue to device configuration screen
            //first check if there are permissions to handle WiFi operations
            checkLocationPermissions();
        }*/

        //scan for device in the background, when found, confirm it's the correct the device and continue to device configuration screen
        //first check if there are permissions to handle WiFi operations
        checkLocationPermissions();

        return view;
    }

    private void checkLocationPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //debugTextView.append("location permissions granted\n");
                checkWifiAccessPermissions();
            }else{
                requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_LOCATION);
            }
        }else{
            //no need to show runtime permission stuff
            //debugTextView.append("location permissions granted from manifest file\n");
            checkWifiAccessPermissions();
        }
    }

    private void checkWifiAccessPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //debugTextView.append("wifi access permissions granted\n");
                checkWifiChangePermissions();
            }else{
                requestPermissions(new String[]{"android.permission.ACCESS_WIFI_STATE"}, RC_PERMISSION_ACCESS_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            //debugTextView.append("wifi access permissions granted from manifest file\n");
            checkWifiChangePermissions();
        }
    }

    private void checkWifiChangePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //debugTextView.append("modify wifi permissions granted\n");
                refreshNetworks();
            }else{
                requestPermissions(new String[]{"android.permission.CHANGE_WIFI_STATE"}, RC_PERMISSION_CHANGE_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            //debugTextView.append("modify wifi permissions granted from manifest file\n");
            refreshNetworks();
        }
    }

    private void turnOnLocationServices(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled, isNetworkProviderEnabled;
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGpsProviderEnabled && !isNetworkProviderEnabled) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Location Services");
            builder.setMessage("The app needs location permissions to scan nearby networks. Please turn them on to continue using the app");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
        }
    }

    private void refreshNetworks(){
        turnOnLocationServices();
        debugTextView.setText("Searching for your RonixTech unit, please wait...\n");
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!mWifiManager.isWifiEnabled()){
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    List<ScanResult> mScanResults = mWifiManager.getScanResults();
                    for (ScanResult result : mScanResults) {
                        //debugTextView.append("found SSID: " + result.SSID + "\n");
                        if(result.SSID.toLowerCase().startsWith(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())){
                            instructionImageView.setVisibility(View.GONE);
                            step1TextView.setVisibility(View.GONE);
                            step2TextView.setVisibility(View.GONE);
                            debugTextView.setText("RonixTech unit found!\n");
                            debugTextView.append(""+result.SSID+"\n");
                            debugTextView.append("Connecting to your RonixTech unit, please wait...\n");
                            //Toast.makeText(getActivity(), "RonixTech device detected, connecting...", Toast.LENGTH_SHORT).show();
                            Device device = new Device();
                            device.setMacAddress(result.BSSID);
                            device.setName(result.SSID);
                            MySettings.setTempDevice(device);
                            Log.d(TAG, "Attempting to connect to " + result.SSID + " with default password");
                            List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                            for(WifiConfiguration i : list) {
                                if(i.SSID != null && i.SSID.toLowerCase().equals(MySettings.getHomeNetwork().getSsid())) {
                                    mWifiManager.removeNetwork(i.networkId);
                                    break;
                                }
                            }
                            connectToWifiNetwork(result.SSID, Constants.DEVICE_DEFAULT_PASSWORD, true);
                            try {
                                if (mWifiScanReceiver != null) {
                                    if (getActivity() != null) {
                                        getActivity().unregisterReceiver(mWifiScanReceiver);
                                    }
                                }
                                break;
                            }catch (Exception e){
                                Log.d(TAG, "Error unregistering mWifiScanReceiver");
                                break;
                            }
                        }else{
                            mWifiManager.startScan();
                        }
                    }
                }
            }
        };

        try {
            if (getActivity() != null) {
                getActivity().registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            }
        }catch (Exception e){
            Log.d(TAG, "Error registering mWifiScanReceiver");
        }

        mWifiManager.startScan();
    }

    private void connectToWifiNetwork(final String ssid, String password, boolean registerCallback){
        if(registerCallback){
            /*Tested (I didn't test with the WPS "Wi-Fi Protected Setup" standard):
            In API15 (ICE_CREAM_SANDWICH) this method is called when the new Wi-Fi network state is:
            DISCONNECTED, OBTAINING_IPADDR, CONNECTED or SCANNING

            In API19 (KITKAT) this method is called when the new Wi-Fi network state is:
            DISCONNECTED (twice), OBTAINING_IPADDR, VERIFYING_POOR_LINK, CAPTIVE_PORTAL_CHECK
            or CONNECTED

            (Those states can be obtained as NetworkInfo.DetailedState objects by calling
            the NetworkInfo object method: "networkInfo.getDetailedState()")*/

            /*
             * NetworkInfo object associated with the Wi-Fi network.
             * It won't be null when "android.net.wifi.STATE_CHANGE" action intent arrives.
             */
            mWifiConnectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        if(getActivity() != null && getActivity().getSystemService(Context.CONNECTIVITY_SERVICE) != null){
                            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                                // Wifi is connected
                                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                                String connectedSSID = wifiInfo.getSSID();

                                Log.d(TAG, "Currently connected to: " + connectedSSID);

                                if(connectedSSID.toLowerCase().contains(ssid.toLowerCase())){
                                    if(getActivity() != null) {
                                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.connected_to)  + " " + connectedSSID, Toast.LENGTH_SHORT).show();
                                    }
                                    debugTextView.setText("Connected to your RonixTech device...\n");

                                    try {
                                        if (getActivity() != null) {
                                            getActivity().unregisterReceiver(mWifiConnectionReceiver);
                                        }
                                    }catch (Exception e){
                                        Log.d(TAG, "Error unregistering mWifiConnectionReceiver");
                                    }

                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    AddDeviceFragmentGetData addDeviceFragmentGetData = new AddDeviceFragmentGetData();
                                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentGetData, "addDeviceFragmentGetData");
                                    fragmentTransaction.addToBackStack("addDeviceFragmentGetData");
                                    fragmentTransaction.commit();

                                    //get device info and save them in the database
                                    //namely device mac address (previously), chipid (get request now), typeid (get request now)
                                    //then send configured SSID/password to the device
                                    //getDeviceType();
                                }else{
                                    List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                                    for(WifiConfiguration i : list) {
                                        if(i.SSID != null && i.SSID.toLowerCase().contains(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())) {
                                            mWifiManager.removeNetwork(i.networkId);
                                            break;
                                        }
                                    }

                                    WifiConfiguration conf = new WifiConfiguration();
                                    /*if(Build.VERSION.SDK_INT >= 23){
                                        conf.SSID = ssid;
                                    }else{
                                        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
                                    }*/
                                    conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
                                    conf.preSharedKey = "\""+ password +"\"";
                                    conf.status = WifiConfiguration.Status.ENABLED;
                                    conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                                    mWifiManager.addNetwork(conf);

                                    mWifiManager.setWifiEnabled(true);

                                    list = mWifiManager.getConfiguredNetworks();
                                    for(WifiConfiguration i : list) {
                                        if(i.SSID != null && i.SSID.toLowerCase().contains("\"" + ssid.toLowerCase() + "\"")) {
                                            Log.d(TAG, "Trying to connect to SSID: " + i.SSID + " with password " + password);
                                            mWifiManager.disconnect();
                                            mWifiManager.enableNetwork(i.networkId, true);
                                            mWifiManager.reconnect();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };

            try {
                if (getActivity() != null) {
                    getActivity().registerReceiver(mWifiConnectionReceiver,
                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                }
            }catch (Exception e){
                Log.d(TAG, "Error registering mWifiConnectionReceiver");
            }
        }

        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.toLowerCase().contains(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())) {
                mWifiManager.removeNetwork(i.networkId);
                break;
            }
        }

        WifiConfiguration conf = new WifiConfiguration();
        /*if(Build.VERSION.SDK_INT >= 23){
            conf.SSID = ssid;
        }else{
            conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        }*/
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\""+ password +"\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        mWifiManager.addNetwork(conf);

        mWifiManager.setWifiEnabled(true);

        list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.toLowerCase().contains("\"" + ssid.toLowerCase() + "\"")) {
                Log.d(TAG, "Trying to connect to SSID: " + i.SSID + " with password: " + password);
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();
                break;
            }
        }
    }

    /*private void getDeviceType(){
        *//*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*//*
        debugTextView.append("Getting device type...\n");
        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.GET_DEVICE_TYPE_URL;

        Log.d(TAG,  "getDeviceType URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDeviceType response: " + response);
                try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){
                        String typeIDString = jsonObject.getString(Constants.PARAMETER_DEVICE_TYPE_ID);
                        int deviceTypeID = Integer.valueOf(typeIDString);
                        Device tempDevice = MySettings.getTempDevice();
                        tempDevice.setDeviceTypeID(deviceTypeID);
                        MySettings.setTempDevice(tempDevice);
                        debugTextView.append("Device type ID: " + deviceTypeID + "\n");
                        getChipID();
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                    if(getActivity() != null) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                    }
                    //trial failed, start over from the beginning
                    debugTextView.setText("Attempt failed, trying again...\n");
                    refreshNetworks();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                debugTextView.setText("Attempt failed, trying again...\n");
                refreshNetworks();
            }
        });
        request.setShouldCache(false);
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void getChipID(){
        *//*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*//*
        debugTextView.append("Getting chip id...\n");
        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.GET_CHIP_ID_URL;

        Log.d(TAG,  "getChipID URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getChipID response: " + response);
                try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_CHIP_ID)){
                        String chipID = jsonObject.getString(Constants.PARAMETER_DEVICE_CHIP_ID);
                        Device tempDevice = MySettings.getTempDevice();
                        tempDevice.setChipID(chipID);
                        MySettings.setTempDevice(tempDevice);
                        debugTextView.append("Chip ID: " + chipID + "\n");
                        sendConfigurationToDevice();
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_chip_id), Toast.LENGTH_SHORT).show();
                    }
                    //trial failed, start over from the beginning
                    debugTextView.setText("Attempt failed, trying again...\n");
                    refreshNetworks();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                if(getActivity() != null){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_chip_id), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                debugTextView.setText("Attempt failed, trying again...\n");
                refreshNetworks();
            }
        });
        request.setShouldCache(false);
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }*/

    /*private void sendConfigurationToDevice(){
        debugTextView.append("Sending home network info to your RonixTech device...\n");

        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.SEND_SSID_PASSWORD_URL;
        //?essid=%SSID%&passwd=%PASS%

        url = url.concat("?").concat(Constants.PARAMETER_SSID).concat("=").concat(MySettings.getHomeNetwork().getSsid())
                .concat("&").concat(Constants.PARAMETER_PASSWORD).concat("=").concat(MySettings.getHomeNetwork().getPassword());

        Log.d(TAG,  "sendConfigurationToDevice URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "sendConfigurationToDevice response: " + response);
                debugTextView.append("Connecting to your home network now...\n");
                //debugTextView.append("delwa2ty el device is configured. @Mina Please tell me if you get to this step\n");
                //debugTextView.append(" go to next step, AddDeviceConfigurationFragment\n");
                Device tempDevice = MySettings.getTempDevice();
                MySettings.addDevice(tempDevice);
                if(mListener != null){
                    mListener.onStartListening();
                }
                connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword(), false);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationFragment addDeviceConfigurationFragment = new AddDeviceConfigurationFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationFragment, "addDeviceConfigurationFragment");
                fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                fragmentTransaction.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(getActivity() != null){
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                debugTextView.setText("Attempt failed, trying again...\n");
                refreshNetworks();
            }
        });
        request.setShouldCache(false);
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    //debugTextView.append("location permissions granted\n");
                    checkWifiAccessPermissions();
                }
                else{
                    //denied
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), "You need to enable location permission", Toast.LENGTH_SHORT).show();
                    }
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Location permission")
                                .setMessage("You need to enable location permissions for the app to detect nearby devices")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_LOCATION);
                                    }
                                })
                                .show();
                    }
                }
            }
            case RC_PERMISSION_ACCESS_WIFI_STATE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    //debugTextView.append("wifi access permissions granted\n");
                    checkWifiChangePermissions();
                }
                else{
                    //denied
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), "You need to enable WiFi permission", Toast.LENGTH_SHORT).show();
                    }
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_WIFI_STATE")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Access WiFi permission")
                                .setMessage("You need to enable WiFi permissions for the app to detect nearby WiFi networks")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.ACCESS_WIFI_STATE"}, RC_PERMISSION_ACCESS_WIFI_STATE);
                                    }
                                })
                                .show();
                    }
                }
            }
            case RC_PERMISSION_CHANGE_WIFI_STATE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    //debugTextView.append("modify wifi permissions granted\n");
                    refreshNetworks();
                }
                else{
                    //denied
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), "You need to enable WiFi permission", Toast.LENGTH_SHORT).show();
                    }
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.CHANGE_WIFI_STATE")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Modify WiFi permission")
                                .setMessage("You need to enable WiFi permissions for the app to configure your RonixTech device")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.CHANGE_WIFI_STATE"}, RC_PERMISSION_CHANGE_WIFI_STATE);
                                    }
                                })
                                .show();
                    }
                }
            }
        }
    }

    @Override
    public void onStop(){
        if(getActivity() != null) {
            try{
                getActivity().unregisterReceiver(mWifiScanReceiver);
                getActivity().unregisterReceiver(mWifiConnectionReceiver);
            }catch (Exception e){
                Log.d(TAG, "Already unregistered - " + e.getMessage());
            }
        }
        super.onStop();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeConnectedListenerInterface) {
            mListener = (HomeConnectedListenerInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeConnectedListenerInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    public interface HomeConnectedListenerInterface{
        void onStartListening();
    }
}
