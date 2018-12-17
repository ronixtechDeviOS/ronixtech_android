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
import android.os.CountDownTimer;
import android.os.Handler;
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

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentSearch.HomeConnectedListenerInterface} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentSearch#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentSearch extends Fragment implements PickSSIDDialogFragment.OnNetworkSelectedListener{
    private static final String TAG = AddDeviceFragmentSearch.class.getSimpleName();

    private HomeConnectedListenerInterface mListener;

    private static final int RC_PERMISSION_LOCATION = 1004;
    private static final int RC_PERMISSION_ACCESS_WIFI_STATE = 1005;
    private static final int RC_PERMISSION_CHANGE_WIFI_STATE= 1006;

    private static final int RC_ACTIVITY_WIFI_TURN_ON = 1007;
    private static final int RC_ACTIVITY_LOCATION_TURN_ON = 1008;

    WifiManager mWifiManager;
    BroadcastReceiver mWifiScanReceiver;
    BroadcastReceiver mWifiConnectionReceiver;

    CountDownTimer searchingCountDownTimer, connectingCountDownTimer;

    DonutProgress progressCircle;
    TextView progressTextView;

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
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_device_search), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        progressCircle = view.findViewById(R.id.progress_circle);
        progressTextView= view.findViewById(R.id.progress_textview);

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

    private boolean checkLocationServices(){
        boolean enabled = true;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getActivity() != null && getActivity().getSystemService(Context.LOCATION_SERVICE) != null){
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsProviderEnabled, isNetworkProviderEnabled;
                isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                    enabled = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(Utils.getString(getActivity(), R.string.location_required_title));
                    builder.setMessage(Utils.getString(getActivity(), R.string.location_required_message));
                    builder.setPositiveButton(Utils.getString(getActivity(), R.string.go_to_location_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, RC_ACTIVITY_LOCATION_TURN_ON);
                        }
                    });
                    builder.setNegativeButton(Utils.getString(getActivity(), R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FragmentManager fragmentManager = getFragmentManager();
                            if(fragmentManager != null) {
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                fragmentTransaction.commitAllowingStateLoss();
                            }
                        }
                    });
                    builder.show();
                }
            }
        }
        return enabled;
    }

    private boolean checkWifiService(){
        boolean enabled = true;
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(mWifiManager != null){
            if(!mWifiManager.isWifiEnabled()){
                enabled = false;
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Utils.getString(getActivity(), R.string.wifi_required_title))
                        //set message
                        .setMessage(Utils.getString(getActivity(), R.string.wifi_required_message))
                        //set positive button
                        .setPositiveButton(Utils.getString(getActivity(), R.string.go_to_wifi_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), RC_ACTIVITY_WIFI_TURN_ON);
                            }
                        })
                        //set negative button
                        .setNegativeButton(Utils.getString(getActivity(), R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                fragmentTransaction.commitAllowingStateLoss();
                            }
                        })
                        .show();
            }
        }else{
            enabled = false;
            //wifi is not available
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                    //set icon
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    //set title
                    .setTitle(Utils.getString(getActivity(), R.string.wifi_required_title))
                    //set message
                    .setMessage(Utils.getString(getActivity(), R.string.add_device_wifi_not_available))
                    //set positive button
                    .setPositiveButton(Utils.getString(getActivity(), R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    })
                    .show();
        }

        return enabled;
    }

    private void refreshNetworks(){
        if(checkLocationServices() && checkWifiService()){
            progressTextView.setText(Utils.getString(getActivity(), R.string.add_device_searching));
            progressCircle.setDonut_progress("" + 0);
            progressCircle.setText("" + 0 + "%");

            /** CountDownTimer starts with 45 seconds and every onTick is 1 second */
            final int totalMillis = 1 * 45 * 1000; // 45 seconds in milli seconds
            searchingCountDownTimer = new CountDownTimer(totalMillis, 1) {
                public void onTick(long millisUntilFinished) {

                    //forward progress
                    long finishedMillis = totalMillis - millisUntilFinished;
                    int totalProgress = (int) (((float)finishedMillis / (float)totalMillis) * 100.0);

                    long totalSeconds =  Math.round(((double)finishedMillis/(double)totalMillis) * 45.0);

                    if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                        progressCircle.setDonut_progress("" + totalProgress);
                        //progressCircle.setText(getActivity().getResources().getStringExtraInt(R.string.seconds, 45 - (int) totalSeconds));
                        progressCircle.setText("" + totalProgress + "%");
                    }
                }

                public void onFinish() {
                    // DO something when 45 seconds are up
                    if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                        if(getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        }
                    }
                }
            }.start();
            mWifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                        List<ScanResult> mScanResults = mWifiManager.getScanResults();
                        if(mScanResults != null){
                            for (ScanResult result : mScanResults) {
                                //debugTextView.append("found SSID: " + result.SSID + "\n");
                                if(result.SSID.toLowerCase().startsWith(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())){
                                    searchingCountDownTimer.cancel();

                                    //show popup if not already shown, and add scanned network to its list

                                    Log.d(TAG, "Found ssid: " + result.SSID);

                                    WifiNetwork scannedNetwork = new WifiNetwork();
                                    scannedNetwork.setSsid(result.SSID);
                                    scannedNetwork.setMacAddress(result.BSSID);
                                    scannedNetwork.setPassword(Constants.DEVICE_DEFAULT_PASSWORD);
                                    scannedNetwork.setSignal(""+result.level);

                                    // DialogFragment.show() will take care of adding the fragment
                                    // in a transaction.  We also want to remove any currently showing
                                    // dialog, so make our own transaction and take care of that here.
                                    PickSSIDDialogFragment ssidFragment = (PickSSIDDialogFragment) getFragmentManager().findFragmentByTag("ssidPickerDialogFragment");
                                    if (ssidFragment != null) {
                                        Log.d(TAG, "Fragment is showing");
                                        ssidFragment.addNetworkToList(scannedNetwork);
                                    }else{
                                        Log.d(TAG, "Fragment is not showing");
                                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                        // Create and show the dialog.
                                        PickSSIDDialogFragment fragment = PickSSIDDialogFragment.newInstance();
                                        fragment.addNetworkToList(scannedNetwork);
                                        fragment.setTargetFragment(AddDeviceFragmentSearch.this, 0);
                                        fragment.show(fragmentTransaction, "ssidPickerDialogFragment");
                                        getFragmentManager().executePendingTransactions();
                                    }

                                }else{
                                    mWifiManager.startScan();
                                }
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
                                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.connected_to) + " " + connectedSSID, true);
                                    connectingCountDownTimer.cancel();
                                    progressTextView.append(Utils.getStringExtraText(getActivity(), R.string.add_device_connected, connectedSSID) + "\n");

                                    try {
                                        if (getActivity() != null) {
                                            getActivity().unregisterReceiver(mWifiConnectionReceiver);
                                        }
                                    }catch (Exception e){
                                        Log.d(TAG, "Error unregistering mWifiConnectionReceiver");
                                    }

                                    if(MainActivity.getInstance() != null && MainActivity.isResumed){
                                        if(getFragmentManager() != null){
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                            AddDeviceFragmentGetData addDeviceFragmentGetData = new AddDeviceFragmentGetData();
                                            fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentGetData, "addDeviceFragmentGetData");
                                            //fragmentTransaction.addToBackStack("addDeviceFragmentGetData");
                                            fragmentTransaction.commitAllowingStateLoss();
                                        }
                                    }

                                    //get device info and save them in the database
                                    //namely device mac address (previously), chipid (get request now), typeid (get request now)
                                    //then send configured SSID/password to the device
                                    //getDeviceType();
                                }else{
                                    connectToWifiNetwork(ssid, password, true);
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
            if(i != null && i.SSID.toLowerCase().startsWith(Constants.DEVICE_NAME_IDENTIFIER)){
                Log.d(TAG, "Removing network '" + i.SSID + "' from saved networks");
                if(!mWifiManager.removeNetwork(i.networkId)){
                    Log.d(TAG, "Failed to remove network " + i.SSID + ", disabling it");
                    mWifiManager.disableNetwork(i.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();

        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }

        mWifiManager.setWifiEnabled(true);

        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

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

        //WPA/WPA2 Security
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);


        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        int networkID = mWifiManager.addNetwork(conf);
        mWifiManager.saveConfiguration();

        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }

        if (networkID == -1) {
            // Get existed network id if it is already added to WiFi network
            networkID = getExistingNetworkId(ssid);
        }

        Log.d(TAG, "Added network, new ID:" + networkID);

        if(networkID != -1) {
            Log.d(TAG, "Connecting to SSID: " + conf.SSID + " with password: " + password + " and networkID: " + networkID);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(networkID, true);
            mWifiManager.reconnect();
        }else{
            list = mWifiManager.getConfiguredNetworks();
            for(WifiConfiguration i : list) {
                if(i.SSID != null && i.SSID.toLowerCase().contains(ssid.toLowerCase()/*"\"" + ssid.toLowerCase() + "\""*/)) {
                    Log.d(TAG, "Connecting to SSID: " + conf.SSID + " with password: " + password + " and networkID: " + conf.networkId);
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    mWifiManager.reconnect();
                    break;
                }
            }
        }

        /*list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.toLowerCase().contains("\"" + ssid.toLowerCase() + "\"")) {
                Log.d(TAG, "Connecting to SSID: " + i.SSID + " with password: " + password);
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();
                break;
            }
        }*/
    }

    private int getExistingNetworkId(String SSID) {
        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (SSID.equalsIgnoreCase(existingConfig.SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    @Override
    public void onWifiNetworkSelected(WifiNetwork network){
        progressTextView.setText(Utils.getString(getActivity(), R.string.add_device_found_device) + "\n");
        progressTextView.append(Utils.getStringExtraText(getActivity(), R.string.add_device_connecting, network.getSsid()) + "\n");

        progressCircle.setDonut_progress("" + 1);
        progressCircle.setText("" + 1 + "%");

        /** CountDownTimer starts with 45 seconds and every onTick is 1 second */
        final int totalMillis = 1 * 45 * 1000; // 45 seconds in milli seconds
        connectingCountDownTimer = new CountDownTimer(totalMillis, 1) {
            public void onTick(long millisUntilFinished) {

                //forward progress
                long finishedMillis = totalMillis - millisUntilFinished;
                int totalProgress = (int) (((float)finishedMillis / (float)totalMillis) * 100.0);

                long totalSeconds =  Math.round(((double)finishedMillis/(double)totalMillis) * 45.0);

                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    progressCircle.setDonut_progress("" + totalProgress);
                    //progressCircle.setText(getActivity().getResources().getStringExtraInt(R.string.seconds, 45 - (int) totalSeconds));
                    progressCircle.setText("" + totalProgress + "%");
                }
            }

            public void onFinish() {
                // DO something when 45 seconds are up
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    if(getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                }
            }
        }.start();

        Log.d(TAG, "User chose ssid " + network.getSsid());
        Device device = new Device();
        device.setMacAddress(network.getMacAddress());
        device.setName(network.getSsid());
        MySettings.setTempDevice(device);
        Log.d(TAG, "Connecting to " + network.getSsid() + " with default password");
        try {
            if (mWifiScanReceiver != null) {
                if (getActivity() != null) {
                    getActivity().unregisterReceiver(mWifiScanReceiver);
                }
            }
        }catch (Exception e){
            Log.d(TAG, "Error unregistering mWifiScanReceiver");
        }
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                connectToWifiNetwork(network.getSsid(), network.getPassword(), true);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == RC_ACTIVITY_WIFI_TURN_ON ) {
            refreshNetworks();
        }else if(requestCode == RC_ACTIVITY_LOCATION_TURN_ON){
            refreshNetworks();
        }
    }

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
                    Utils.showToast(getActivity(), "You need to enable location permission", true);
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
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
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
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
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
        Log.d(TAG, "onStop");
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
