package com.ronixtech.ronixhome.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.ronixtech.ronixhome.BuildConfig;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.fragments.AboutFragment;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentGetData;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentSendData;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.DashboardRoomsFragment;
import com.ronixtech.ronixhome.fragments.PlacesFragment;
import com.ronixtech.ronixhome.fragments.RoomsFragment;
import com.ronixtech.ronixhome.fragments.WifiInfoFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);


        LinearLayout headerLayout = (LinearLayout) navigationView.getHeaderView(0);
        userNameTextView = headerLayout.findViewById(R.id.user_name_textview);
        userEmailTextView = headerLayout.findViewById(R.id.user_email_textview);

        userNameTextView.setText(MySettings.getActiveUser().getFirstName() + " " + MySettings.getActiveUser().getLastName());
        userEmailTextView.setText(MySettings.getActiveUser().getEmail());

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

        //getLatestAppVersion();

        getLatestFirmwareVersion();
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

        Log.d(TAG, "getLatestFirmwareVersions URL: " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getLatestFirmwareVersions response: " + response);
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    for(int x = 0; x < length; x++){
                        JSONObject jsonObject = jsonArray.getJSONObject(x);
                        String deviceTypeString = jsonObject.getString("unit_type_id");
                        String latestVersionString = jsonObject.getString("latest_firmware_version");

                        int deviceType = Integer.valueOf(deviceTypeString);
                        MySettings.setDeviceLatestFirmwareVersion(deviceType, latestVersionString);
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley error: " + error.getMessage());
            }
        });
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.SERVER_TIMEOUT, Constants.SERVER_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(mInstance).addToRequestQueue(stringRequest);
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
    }

    @Override
    public void onPause() {
        isResumed = false;
        super.onPause();
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
            WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
            wifiInfoFragment.setSource(Constants.SOURCE_NAV_DRAWER);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
            fragmentTransaction.addToBackStack("wifiInfoFragment");
            fragmentTransaction.commit();
        } else if(id == R.id.nav_refresh_devices){
            Toast.makeText(mInstance, "Refreshing devices", Toast.LENGTH_SHORT).show();
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
            fragmentTransaction.addToBackStack("placesFragment");
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
            RoomsFragment roomsFragment = new RoomsFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, roomsFragment, "roomsFragment");
            fragmentTransaction.addToBackStack("roomsFragment");
            fragmentTransaction.commit();
        }else if (id == R.id.nav_about) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AboutFragment aboutFragment = new AboutFragment();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, aboutFragment, "aboutFragment");
            fragmentTransaction.addToBackStack("aboutFragment");
            fragmentTransaction.commit();
        } else if( id == R.id.log_out){
            if(MySettings.getActiveUser() != null) {
                MySettings.deleteCurrentUser(MySettings.getActiveUser());
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