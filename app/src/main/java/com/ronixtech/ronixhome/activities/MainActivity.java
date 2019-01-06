package com.ronixtech.ronixhome.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ronixtech.ronixhome.BuildConfig;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.WifiNetwork;
import com.ronixtech.ronixhome.fragments.AboutFragment;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentGetData;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentSendData;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.DashboardRoomsFragment;
import com.ronixtech.ronixhome.fragments.ExportDataFragment;
import com.ronixtech.ronixhome.fragments.HomeNetworksFragment;
import com.ronixtech.ronixhome.fragments.ImportDataFragment;
import com.ronixtech.ronixhome.fragments.LinkedAccountsFragment;
import com.ronixtech.ronixhome.fragments.LogViewerFragment;
import com.ronixtech.ronixhome.fragments.PlacesFragment;
import com.ronixtech.ronixhome.fragments.UserProfileFragment;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AddDeviceFragmentSendData.HomeConnectedListenerInterface,
        AddDeviceFragmentGetData.HomeConnectedListenerInterface{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static MainActivity mInstance;
    public static boolean isResumed;

    NavigationView navigationView;

    FragmentManager fragmentManager;
    DashboardDevicesFragment dashboardDevicesFragment;
    Toolbar toolbar;
    private static TextView mTitle;

    TextView userNameTextView, userEmailTextView;
    ImageView userImageView;

    BroadcastReceiver myWifiReceiver;
    IntentFilter intentFilter;

    int logCounterMAX = 8;
    int logCounterToast = 3;
    int logCounter;

    //Stuff for remote/MQTT mode
    MqttAndroidClient mqttAndroidClient;

    List<Device> allDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getString(R.string.app_name));
        mTitle.setTextColor(getResources().getColor(R.color.whiteColor));

        /*// Create a Constraints that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                // Many other constraints are available, see the
                // Constraints.Builder reference
                .build();

        // ...then create a OneTimeWorkRequest that uses those constraints
        OneTimeWorkRequest scannerWork =
                new OneTimeWorkRequest.Builder(NetworkScanner.class)
                        .setConstraints(myConstraints)
                        .build();
        WorkManager.getInstance().enqueue(scannerWork);*/

        if(MySettings.getActiveUser() == null){
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        //check if MySettings.getAciveUser() is verified or not
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth != null && mAuth.getCurrentUser() != null){
            mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    if(fbUser != null) {
                        if (!fbUser.isEmailVerified()) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            loginIntent.putExtra("action", "verify");
                            startActivity(loginIntent);
                            finish();
                        }
                    }
                }
            });
        }

        if(MySettings.getDefaultPlaceID() != -1){
            Place defaultPlace = MySettings.getPlace(MySettings.getDefaultPlaceID());
            if(defaultPlace != null){
                MySettings.setCurrentPlace(defaultPlace);
                MySettings.setCurrentFloor(null);
            }
        }

        allDevices = new ArrayList<>();

        /*if (savedInstanceState == null) {
            // only create fragment if activity is started for the first time
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if(MySettings.getCurrentPlace() != null) {
                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }else {
                PlacesFragment placesFragment = new PlacesFragment();
                fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        } else {
            // do nothing - fragment is recreated automatically
        }*/

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(MySettings.getCurrentPlace() != null) {
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }else {
            PlacesFragment placesFragment = new PlacesFragment();
            fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);


        LinearLayout headerLayout = (LinearLayout) navigationView.getHeaderView(0);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                UserProfileFragment userProfileFragment = new UserProfileFragment();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.replace(R.id.fragment_view, userProfileFragment, "userProfileFragment");
                fragmentTransaction.addToBackStack("userProfileFragment");
                fragmentTransaction.commit();
                drawer.closeDrawer(Gravity.START);
            }
        });
        userNameTextView = headerLayout.findViewById(R.id.user_name_textview);
        userEmailTextView = headerLayout.findViewById(R.id.user_email_textview);
        userImageView = headerLayout.findViewById(R.id.imageView);

        userNameTextView.setText(MySettings.getActiveUser().getFirstName() + " " + MySettings.getActiveUser().getLastName());
        userEmailTextView.setText(MySettings.getActiveUser().getEmail());

        logCounter = 0;
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCounter++;
                if(logCounter >= logCounterMAX){
                    logCounter = 0;
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    LogViewerFragment logViewerFragment = new LogViewerFragment();
                    fragmentTransaction.replace(R.id.fragment_view, logViewerFragment, "logViewerFragment");
                    fragmentTransaction.addToBackStack("logViewerFragment");
                    fragmentTransaction.commit();
                    drawer.closeDrawer(Gravity.START);
                }else{
                    if(logCounter >= logCounterToast){
                        Utils.showToast(mInstance, Utils.getStringExtraInt(mInstance, R.string.log_viewier_message, (logCounterMAX - logCounter)), false);
                    }
                }
            }
        });

        RelativeLayout currentVersionLayout = (RelativeLayout) navigationView.getMenu().findItem(R.id.nav_current_version).getActionView();
        TextView currentVersionTextView = currentVersionLayout.findViewById(R.id.current_version_textview);
        try {
            PackageInfo pInfo = mInstance.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            currentVersionTextView.setText(BuildConfig.BUILD_TYPE+"-"+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            currentVersionTextView.setText("0.0");
        }

        myWifiReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                    checkWifiConnection();
                    /*ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

                    if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        checkWifiConnection();
                    }else{

                    }*/
                }else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                    checkWifiConnection();
                }
                /*SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                switch(newState){
                    case ASSOCIATED:
                        Log.d("WIFI", "CONNECTED");
                        break;
                    case DISCONNECTED:
                        if(!disconnected){
                            Log.d("WIFI", "DISCONNECTED");
                            disconnected = true;
                        }
                }*/
            }};

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myWifiReceiver, intentFilter);

        //getLatestAppVersion();

        getLatestFirmwareVersion();
    }

    private void checkWifiConnection(){
        List<Place> allPlaces = MySettings.getAllPlaces();
        for (Place place : allPlaces) {
            place.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.updatePlaceMode(place, Place.PLACE_MODE_REMOTE);
        }
        if(MySettings.getCurrentPlace() != null ){
            Place currentPlace = MySettings.getCurrentPlace();
            currentPlace.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.setCurrentPlace(currentPlace);
        }
        WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(mWifiManager != null){
            //Wifi is available
            if(mWifiManager.isWifiEnabled()){
                //Wifi is ON, check which SSID is currently associated with this device
                Utils.log(TAG, "Wifi is ON, check which SSID is currently associated with this device", true);
                WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
                if(mWifiInfo != null){
                    //Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode
                    Utils.log(TAG, "Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode", true);
                    String ssid = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
                    Utils.log(TAG, "Currently connected to: " + ssid, true);
                    WifiNetwork wifiNetwork = MySettings.getWifiNetworkBySSID(ssid);
                    if(wifiNetwork != null){
                        Utils.log(TAG, "WifiNetwork DB id: " + wifiNetwork.getId(), true);
                        long placeID = wifiNetwork.getPlaceID();
                        Utils.log(TAG, "WifiNetwork placeID: " + placeID, true);
                        if(placeID != -1){
                            Place localPlace = MySettings.getPlace(placeID);
                            if(localPlace != null){
                                Utils.log(TAG, "WifiNetwork DB placeName: " + localPlace.getName(), true);
                                localPlace.setMode(Place.PLACE_MODE_LOCAL);
                                MySettings.updatePlaceMode(localPlace, Place.PLACE_MODE_LOCAL);
                                if(MySettings.getCurrentPlace() != null && MySettings.getCurrentPlace().getId() == localPlace.getId()){
                                    MySettings.setCurrentPlace(localPlace);
                                }
                            }
                        }
                    }else{
                        //Wifi network is NOT associated with any Place
                        Utils.log(TAG, "WifiNetwork is NOT associated with any Place", true);
                    }
                }else{
                    //Wifi is ON but not connected to any ssid
                    Utils.log(TAG, "Wifi is ON but not connected to any ssid", true);
                }
            }else{
                //Wifi is OFF
                Utils.log(TAG, "Wifi is OFF", true);
            }
        }else {
            //Wifi is not available
        }
        DashboardDevicesFragment fragment = (DashboardDevicesFragment) getSupportFragmentManager().findFragmentByTag("dashboardDevicesFragment");
        if(fragment != null){
            fragment.updateUI();
        }
    }

    private void checkCellularConnection(){
        new Utils.InternetChecker(MainActivity.getInstance(), new Utils.InternetChecker.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                MySettings.setInternetConnectivityState(true);
                if(MySettings.getAllDevices() != null){
                    allDevices.addAll(MySettings.getAllDevices());
                }
                //start MQTT, when a control is sent from the DeviceAdapter or anywhere else, it will be synced here when the MQTT responds
                if(mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
                    String clientId = MqttClient.generateClientId();
                    initMqttClient(mInstance, Constants.MQTT_URL + ":" + Constants.MQTT_PORT, clientId);
                }else{
                    Utils.log(TAG, "MQTT is already connected", true);
                }
            }

            @Override
            public void onConnectionFail(String errorMsg) {
                MySettings.setInternetConnectivityState(false);
            }
        }).execute();
    }

    private void getLatestAppVersion(){
        new Utils.GooglePlayAppVersion(mInstance.getPackageName(), new Utils.GooglePlayAppVersion.Listener() {
            @Override
            public void result(String version) {
                RelativeLayout latestVersionLayout = (RelativeLayout) navigationView.getMenu().findItem(R.id.nav_latest_version).getActionView();
                TextView latestVersionTextView = latestVersionLayout.findViewById(R.id.latest_version_textview);
                latestVersionTextView.setText("release"+"-"+version);
            }
        }).execute();
    }

    private void getLatestFirmwareVersion(){
        String url = Constants.DEVICE_LATEST_FIRMWARE_VERSIONS_URL;

        Utils.log(TAG, "getLatestFirmwareVersions URL: " + url, true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Utils.log(TAG, "getLatestFirmwareVersions response: " + response, true);
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    for(int x = 0; x < length; x++){
                        JSONObject jsonObject = jsonArray.getJSONObject(x);
                        if(jsonObject.has("unit_type_id")){
                            String deviceTypeString = jsonObject.getString("unit_type_id");
                            int deviceType = Integer.valueOf(deviceTypeString);
                            if(jsonObject.has("latest_firmware_version")){
                                String latestVersionString = jsonObject.getString("latest_firmware_version");
                                MySettings.setDeviceLatestWiFiFirmwareVersion(deviceType, latestVersionString);
                            }
                            if(jsonObject.has("latest_hw_firmware_version")){
                                String latestHWVersionString = jsonObject.getString("latest_hw_firmware_version");
                                MySettings.setDeviceLatestHWFirmwareVersion(deviceType, latestHWVersionString);
                            }
                        }
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Json exception: " + e.getMessage(), true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.log(TAG, "Volley error: \" + error.getMessage()", true);
            }
        });
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.SERVER_TIMEOUT, Constants.SERVER_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(mInstance).addToRequestQueue(stringRequest);
    }

    public void removeDevice(Device device){
        if(allDevices != null){
            allDevices.remove(device);
        }
        if(mqttAndroidClient != null){
            try {
                unsubscribe(mqttAndroidClient, device);
            }catch (MqttException e){
                Utils.log(TAG, "Exception " + e.getMessage(), true);
            }
        }
    }

    public void refreshDevicesListFromMemory(){
        if(fragmentManager != null) {
            if(dashboardDevicesFragment == null) {
                dashboardDevicesFragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
            }

            if(dashboardDevicesFragment != null) {
                dashboardDevicesFragment.loadDevicesFromMemory();
            }
        }
    }

    public void refreshDeviceListFromDatabase(){
        if(fragmentManager != null) {
            if(dashboardDevicesFragment == null) {
                dashboardDevicesFragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
            }

            if (dashboardDevicesFragment != null) {
                dashboardDevicesFragment.loadDevicesFromDatabase();
            }
        }
    }

    public static void setActionBarTitle(String title, int colorID){
        mTitle.setText(title);
        mTitle.setTextColor(colorID);
    }

    public static synchronized MainActivity getInstance() {
        return mInstance;
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
        registerReceiver(myWifiReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        isResumed = false;
        unregisterReceiver(myWifiReceiver);
        super.onPause();
    }

    @Override
    public void onStart(){
        Utils.log(TAG, "onStart", true);
        super.onStart();
        checkCellularConnection();
    }

    @Override
    public void onDestroy(){
        Utils.log(TAG, "onDestroy", true);
        //stop MQTT
        if(mqttAndroidClient != null){
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
            }catch (MqttException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }catch (Exception e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }
        }
        if(allDevices != null) {
            for (Device device : allDevices) {
                device.setDeviceMQTTReachable(false);
                MySettings.addDevice(device);
            }
        }
        if(DevicesInMemory.getDevices() != null){
            for (Device device : DevicesInMemory.getDevices()) {
                device.setDeviceMQTTReachable(false);
                MySettings.addDevice(device);
            }
        }
        super.onDestroy();
    }

    public MqttAndroidClient getMainMqttClient(){
        if(mqttAndroidClient != null){
            return mqttAndroidClient;
        }else{
            return null;
        }
    }

    public void refreshMqttClient(){
        /*//disconnect from MQTT server
        if(mqttAndroidClient != null){
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
            }catch (MqttException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }catch (Exception e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }
        }
        if(allDevices != null) {
            for (Device device : allDevices) {
                device.setDeviceMQTTReachable(false);
                MySettings.addDevice(device);
            }
        }
        if(DevicesInMemory.getDevices() != null){
            for (Device device : DevicesInMemory.getDevices()) {
                device.setDeviceMQTTReachable(false);
                MySettings.addDevice(device);
            }
        }
        //connect to MQTT server
        checkCellularConnection();*/
    }

    public void initMqttClient(Context context, String brokerUrl, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        if(mqttAndroidClient != null){
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    Utils.log(TAG, "MQTT connectComplete on " + s, true);
                }
                @Override
                public void connectionLost(Throwable throwable) {
                    Utils.log(TAG, "MQTT connectionLost", true);
                    if(allDevices != null) {
                        for (Device device : allDevices) {
                            device.setDeviceMQTTReachable(false);
                            MySettings.addDevice(device);
                            DevicesInMemory.updateDevice(device);
                        }
                        MainActivity.getInstance().refreshDevicesListFromMemory();
                    }
                }
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    //setMessageNotification(s, new String(mqttMessage.getPayload()));
                    Utils.log(TAG, "MQTT messageArrived: 'topic': " + s, true);
                    Utils.log(TAG, "MQTT messageArrived: 'mqttMessage': " + new String(mqttMessage.getPayload()), true);
                    //make sure it's the 'status' topic, not the 'control' topic
                    if(s.contains("status")){
                        /*if(MySettings.isGetStatusActive()){
                           return;
                        }*/
                        if (MySettings.isControlActive()){
                            Utils.log(TAG, "Controls active, do nothing", true);
                            return;
                        }
                        MySettings.setGetStatusState(true);
                        String response = new String(mqttMessage.getPayload());
                        int index = s.lastIndexOf("/");
                        Device device = MySettings.getDeviceByChipID2(s.substring(index+1));
                        if(device != null){
                            if(response != null && response.length() >= 1 && response.contains("UNIT_STATUS")){
                                JSONObject jsonObject = new JSONObject(response);
                                if(jsonObject.has("UNIT_STATUS")){
                                    //parse received unit status and update relevant device, which has the received chip_id
                                    JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                                    if(unitStatus != null && unitStatus.has("U_W_STT")){
                                        JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                        if(wifiStatus != null) {
                                            if(wifiStatus.has("U_W_UID")) {
                                                String chipID = wifiStatus.getString("U_W_UID");
                                            }else{
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                            if(wifiStatus.has("R_M_ALV")){
                                                String R_M_ALV_string = wifiStatus.getString("R_M_ALV");
                                                int R_M_ALV = Integer.parseInt(R_M_ALV_string);
                                                if(R_M_ALV == 1){
                                                    try {
                                                        JSONObject jsonObject1 = new JSONObject();
                                                        jsonObject1.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                                                        jsonObject1.put("R_M_ALV", "0");
                                                        MqttMessage mqttMessage1 = new MqttMessage();
                                                        mqttMessage1.setPayload(jsonObject1.toString().getBytes());
                                                        Utils.log(TAG, "MQTT Publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                                                        Utils.log(TAG, "MQTT Publish data: " + mqttMessage1, true);
                                                        mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage1);
                                                        device.setDeviceMQTTReachable(true);
                                                    }catch (JSONException e){
                                                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                                                    }catch (MqttException e){
                                                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                                                    }
                                                }
                                            }
                                            if(wifiStatus.has("U_W_FWV")) {
                                                String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                                if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                                    device.setFirmwareVersion(currentFirmwareVersion);
                                                    if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                        int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                        int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                        if (onlineVersion != currentVersion) {
                                                            device.setFirmwareUpdateAvailable(true);
                                                        }else{
                                                            device.setFirmwareUpdateAvailable(false);
                                                        }
                                                    }
                                                }else{
                                                    device.setFirmwareUpdateAvailable(true);
                                                }
                                            }else{
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }

                                    if(device.isDeviceMQTTReachable()){
                                        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                                if(hardwareStatus.has("U_H_FWV")) {
                                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                            if (onlineHWVersion != currentHWVersion) {
                                                                device.setHwFirmwareUpdateAvailable(true);
                                                            }else{
                                                                device.setHwFirmwareUpdateAvailable(false);
                                                            }
                                                        }
                                                    }else{
                                                        device.setHwFirmwareUpdateAvailable(true);
                                                    }
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }


                                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;
                                                line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                                line0PowerState = Integer.valueOf(line0PowerStateString);
                                                line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                                line1PowerState = Integer.valueOf(line1PowerStateString);
                                                line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                                line2PowerState = Integer.valueOf(line2PowerStateString);

                                                String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                                int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                                line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                                if(line0DimmingValueString.equals(":")){
                                                    line0DimmingValue = 10;
                                                }else{
                                                    line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                                }

                                                line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                                if(line1DimmingValueString.equals(":")){
                                                    line1DimmingValue = 10;
                                                }else{
                                                    line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                                }

                                                line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                                if(line2DimmingValueString.equals(":")){
                                                    line2DimmingValue = 10;
                                                }else{
                                                    line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                                }


                                                String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                                int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                                line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                                line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                                line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                                line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                                line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                                line2DimmingState = Integer.valueOf(line2DimmingStateString);

                                                List<Line> lines = device.getLines();
                                                for (Line line:lines) {
                                                    if(line.getPosition() == 0){
                                                        line.setPowerState(line0PowerState);
                                                        line.setDimmingState(line0DimmingState);
                                                        line.setDimmingVvalue(line0DimmingValue);
                                                    }else if(line.getPosition() == 1){
                                                        line.setPowerState(line1PowerState);
                                                        line.setDimmingState(line1DimmingState);
                                                        line.setDimmingVvalue(line1DimmingValue);
                                                    }else if(line.getPosition() == 2){
                                                        line.setPowerState(line2PowerState);
                                                        line.setDimmingState(line2DimmingState);
                                                        line.setDimmingVvalue(line2DimmingValue);
                                                    }
                                                }

                                                String temperatureString, beepString, hwLockString;
                                                int temperatureValue;
                                                boolean beep, hwLock;
                                                temperatureString = hardwareStatus.getString("U_H_TMP");
                                                beepString = hardwareStatus.getString("U_BEEP_");
                                                hwLockString = hardwareStatus.getString("U_H_LCK");

                                                temperatureValue = Integer.parseInt(temperatureString);
                                                beep = Boolean.parseBoolean(beepString);
                                                hwLock = Boolean.parseBoolean(hwLockString);

                                                device.setTemperature(temperatureValue);
                                                device.setBeep(beep);
                                                device.setHwLock(hwLock);

                                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                            }else{
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                                if(hardwareStatus.has("U_H_FWV")) {
                                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                            if (onlineHWVersion != currentHWVersion) {
                                                                device.setHwFirmwareUpdateAvailable(true);
                                                            }else{
                                                                device.setHwFirmwareUpdateAvailable(false);
                                                            }
                                                        }
                                                    }else{
                                                        device.setHwFirmwareUpdateAvailable(true);
                                                    }
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }


                                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;
                                                line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                                line0PowerState = Integer.valueOf(line0PowerStateString);
                                                line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                                line1PowerState = Integer.valueOf(line1PowerStateString);
                                                line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                                line2PowerState = Integer.valueOf(line2PowerStateString);

                                                List<Line> lines = device.getLines();
                                                for (Line line:lines) {
                                                    if(line.getPosition() == 0){
                                                        line.setPowerState(line0PowerState);
                                                    }else if(line.getPosition() == 1){
                                                        line.setPowerState(line1PowerState);
                                                    }else if(line.getPosition() == 2){
                                                        line.setPowerState(line2PowerState);
                                                    }
                                                }

                                                String temperatureString, beepString, hwLockString;
                                                int temperatureValue;
                                                boolean beep, hwLock;
                                                temperatureString = hardwareStatus.getString("U_H_TMP");
                                                beepString = hardwareStatus.getString("U_BEEP_");
                                                hwLockString = hardwareStatus.getString("U_H_LCK");

                                                temperatureValue = Integer.parseInt(temperatureString);
                                                beep = Boolean.parseBoolean(beepString);
                                                hwLock = Boolean.parseBoolean(hwLockString);

                                                device.setTemperature(temperatureValue);
                                                device.setBeep(beep);
                                                device.setHwLock(hwLock);

                                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                            }else {
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                        }
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }
                            MySettings.addDevice(device);
                            DevicesInMemory.updateDevice(device);
                            MainActivity.getInstance().refreshDevicesListFromMemory();
                        }
                        MySettings.setGetStatusState(false);
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Utils.log(TAG, "MQTT deliveryComplete", true);
                }
            });
            try {
                IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
                if(token != null){
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                            Utils.log(TAG, "MQTT connect onSuccess", true);
                            try {
                                if(allDevices != null) {
                                    for (Device device : allDevices) {
                                        subscribe(mqttAndroidClient, device, 1);
                                    }
                                }
                            }catch (MqttException e){
                                Utils.log(TAG, "Exception " + e.getMessage(), true);
                                if(allDevices != null) {
                                    for (Device device : allDevices) {
                                        device.setDeviceMQTTReachable(false);
                                        MySettings.addDevice(device);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    MainActivity.getInstance().refreshDevicesListFromMemory();
                                }
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Utils.log(TAG, "MQTT connect onFailure: " + exception.toString(), true);
                            if(allDevices != null) {
                                for (Device device : allDevices) {
                                    device.setDeviceMQTTReachable(false);
                                    MySettings.addDevice(device);
                                    DevicesInMemory.updateDevice(device);
                                }
                                MainActivity.getInstance().refreshDevicesListFromMemory();
                            }
                        }
                    });
                }
            } catch (MqttException e) {
                e.printStackTrace();
                if(allDevices != null) {
                    for (Device device : allDevices) {
                        device.setDeviceMQTTReachable(false);
                        MySettings.addDevice(device);
                        DevicesInMemory.updateDevice(device);
                    }
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
            }
        }
    }
    public void subscribe(@NonNull final MqttAndroidClient client, Device device, int qos) throws MqttException {
        final IMqttToken token = client.subscribe(String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()), qos);
        if(token != null){
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Utils.log(TAG, "MQTT subscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()), true);
                    device.setDeviceMQTTReachable(false);
                    MySettings.addDevice(device);
                    DevicesInMemory.updateDevice(device);
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT subscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()));
                    device.setDeviceMQTTReachable(false);
                    MySettings.addDevice(device);
                    DevicesInMemory.updateDevice(device);
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
            });
        }

        final IMqttToken token2 = client.subscribe(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), qos);
        if(token2 != null){
            token2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Utils.log(TAG, "MQTT subscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                        jsonObject.put("R_M_ALV", "1");
                        MqttMessage mqttMessage = new MqttMessage();
                        mqttMessage.setPayload(jsonObject.toString().getBytes());
                        Utils.log(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                        Utils.log(TAG, "MQTT publish data: " + mqttMessage, true);
                        mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                    }catch (JSONException e){
                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    }catch (MqttException e){
                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT subscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                }
            });
        }
    }
    public void unsubscribe(@NonNull final MqttAndroidClient client, Device device) throws MqttException {
        final IMqttToken token = client.unsubscribe(String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()));
        if(token != null){
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Utils.log(TAG, "MQTT unsubscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()), true);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT unsubscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()));
                }
            });
        }

        final IMqttToken token2 = client.unsubscribe(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
        if(token2 != null){
            token2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Utils.log(TAG, "MQTT unsubscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT unsubscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                }
            });
        }
    }
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setWill(Constants.MQTT_URL, "I am going offline".getBytes(), 1, false);
        mqttConnectOptions.setUserName(Constants.MQTT_USERNAME);
        mqttConnectOptions.setPassword(Constants.MQTT_PASSWORD.toCharArray());
        return mqttConnectOptions;
    }
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_dashboard){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentTransaction.commit();
        }
        else if (id == R.id.nav_home_network) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            HomeNetworksFragment homeNetworksFragment = new HomeNetworksFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, homeNetworksFragment, "homeNetworksFragment");
            fragmentTransaction.addToBackStack("homeNetworksFragment");
            fragmentTransaction.commit();
        } else if(id == R.id.nav_refresh_devices){
            Utils.showToast(mInstance, "Refreshing devices", true);
            for (Device device: MySettings.getAllDevices()) {
                device.setIpAddress("");
                MySettings.updateDeviceIP(device, "");
            }
            MySettings.scanNetwork();
        } else if (id == R.id.nav_places) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            PlacesFragment placesFragment = new PlacesFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
            //fragmentTransaction.addToBackStack("placesFragment");
            fragmentTransaction.commit();
        } /*else if (id == R.id.nav_floors) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            FloorsFragment addFloorFragment = new FloorsFragment();
            addFloorFragment.setSource(Constants.SOURCE_NAV_DRAWER);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
            fragmentTransaction.addToBackStack("addFloorFragment");
            fragmentTransaction.commit();
        }*/ else if (id == R.id.nav_rooms) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentTransaction.addToBackStack("dashboardRoomsFragment");
            fragmentTransaction.commit();
        }else if (id == R.id.nav_about) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AboutFragment aboutFragment = new AboutFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, aboutFragment, "aboutFragment");
            fragmentTransaction.addToBackStack("aboutFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_profile) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            UserProfileFragment userProfileFragment = new UserProfileFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, userProfileFragment, "userProfileFragment");
            fragmentTransaction.addToBackStack("userProfileFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_upload_data) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            ExportDataFragment exportDataFragment = new ExportDataFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, exportDataFragment, "exportDataFragment");
            fragmentTransaction.addToBackStack("exportDataFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_download_data) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            ImportDataFragment importDataFragment = new ImportDataFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, importDataFragment, "importDataFragment");
            fragmentTransaction.addToBackStack("importDataFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_linked_accounts) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            LinkedAccountsFragment linkedAccountsFragment = new LinkedAccountsFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, linkedAccountsFragment, "linkedAccountsFragment");
            fragmentTransaction.addToBackStack("linkedAccountsFragment");
            fragmentTransaction.commit();
        } else if( id == R.id.log_out){
            if(MySettings.getActiveUser() != null) {
                MySettings.deleteActiveUser(MySettings.getActiveUser());
                if(FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null){
                    FirebaseAuth.getInstance().signOut();
                }
                Intent loginIntent = new Intent(mInstance, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    WifiManager mWifiManager;
    BroadcastReceiver mWifiConnectionReceiver;
    @Override
    public void onStartListening(){
        Handler mHander = new Handler();
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                                    /*// Create a Constraints that defines when the task should run
                                    Constraints myConstraints = new Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            // Many other constraints are available, see the
                                            // Constraints.Builder reference
                                            .build();

                                    // ...then create a OneTimeWorkRequest that uses those constraints
                                    OneTimeWorkRequest scannerWork =
                                            new OneTimeWorkRequest.Builder(NetworkScanner.class)
                                                    .setConstraints(myConstraints)
                                                    .build();
                                    WorkManager.getInstance().enqueue(scannerWork);*/
                MySettings.scanNetwork();
            }
        }, 5000);
        /*try {
            if (mInstance != null) {
                mInstance.unregisterReceiver(mWifiConnectionReceiver);
            }
        }catch (Exception e){
            Log.d(TAG, "Error unregistering mWifiConnectionReceiver");
        }

        mWifiManager = (WifiManager) mInstance.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        *//*Tested (I didn't test with the WPS "Wi-Fi Protected Setup" standard):
            In API15 (ICE_CREAM_SANDWICH) this method is called when the new Wi-Fi network state is:
            DISCONNECTED, OBTAINING_IPADDR, CONNECTED or SCANNING

            In API19 (KITKAT) this method is called when the new Wi-Fi network state is:
            DISCONNECTED (twice), OBTAINING_IPADDR, VERIFYING_POOR_LINK, CAPTIVE_PORTAL_CHECK
            or CONNECTED

            (Those states can be obtained as NetworkInfo.DetailedState objects by calling
            the NetworkInfo object method: "networkInfo.getDetailedState()")*//*

        *//*
         * NetworkInfo object associated with the Wi-Fi network.
         * It won't be null when "android.net.wifi.STATE_CHANGE" action intent arrives.
         *//*
        mWifiConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                    ConnectivityManager cm = (ConnectivityManager) mInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(cm != null){
                        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                            // Wifi is connected
                            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                            String connectedSSID = wifiInfo.getSSID();

                            *//*if(connectedSSID.equals(MySettings.getHomeNetwork().getSsid())){
                                NetworkScannerAsyncTask networkScannerAsyncTask = new NetworkScannerAsyncTask(mInstance);
                                networkScannerAsyncTask.execute();
                            }*//*

                            Handler mHander = new Handler();
                            mHander.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    *//* Create a Constraints that defines when the task should run
                                    Constraints myConstraints = new Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            // Many other constraints are available, see the
                                            // Constraints.Builder reference
                                            .build();

                                    // ...then create a OneTimeWorkRequest that uses those constraints
                                    OneTimeWorkRequest scannerWork =
                                            new OneTimeWorkRequest.Builder(NetworkScanner.class)
                                                    .setConstraints(myConstraints)
                                                    .build();
                                    WorkManager.getInstance().enqueue(scannerWork);*//*
                                    MySettings.scanNetwork();
                                }
                            }, 10000);


                            try {
                                if (mInstance != null) {
                                    mInstance.unregisterReceiver(mWifiConnectionReceiver);
                                }
                            }catch (Exception e){
                                Log.d(TAG, "Error unregistering mWifiConnectionReceiver");
                            }
                        }
                    }
                }
            }
        };

        mInstance.registerReceiver(mWifiConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));*/

    }
}