package com.ronixtech.ronixhome.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentGetData;
import com.ronixtech.ronixhome.fragments.AddDeviceFragmentSendData;
import com.ronixtech.ronixhome.fragments.AddFloorFragment;
import com.ronixtech.ronixhome.fragments.AddPlaceFragment;
import com.ronixtech.ronixhome.fragments.AddRoomFragment;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.DashboardRoomsFragment;
import com.ronixtech.ronixhome.fragments.WifiInfoFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AddDeviceFragmentSendData.HomeConnectedListenerInterface,
        AddDeviceFragmentGetData.HomeConnectedListenerInterface{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static MainActivity mInstance;

    FragmentManager fragmentManager;
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayout headerLayout = (LinearLayout) navigationView.getHeaderView(0);
        userNameTextView = headerLayout.findViewById(R.id.user_name_textview);
        userEmailTextView = headerLayout.findViewById(R.id.user_email_textview);

        userNameTextView.setText(MySettings.getActiveUser().getFirstName() + " " + MySettings.getActiveUser().getLastName());
        userEmailTextView.setText(MySettings.getActiveUser().getEmail());
    }

    public void refreshDevicesListFromMemory(){
        if(fragmentManager != null) {
            DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
            if (fragment != null) {
                fragment.loadDevicesFromMemory();
            }
        }
    }

    public void refreshDeviceListFromDatabase(){
        if(fragmentManager != null) {
            DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
            if (fragment != null) {
                fragment.loadDevicesFromDatabase();
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
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
            fragmentTransaction.addToBackStack("wifiInfoFragment");
            fragmentTransaction.commit();
        } else if(id == R.id.nav_refresh_devices){
            Toast.makeText(mInstance, "Refreshing devices", Toast.LENGTH_SHORT).show();
            MySettings.scanNetwork();
        } else if (id == R.id.nav_places) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
            addPlaceFragment.setSource(Constants.SOURCE_NAV_DRAWER);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
            fragmentTransaction.addToBackStack("addPlaceFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_floors) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AddFloorFragment addFloorFragment = new AddFloorFragment();
            addFloorFragment.setSource(Constants.SOURCE_NAV_DRAWER);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
            fragmentTransaction.addToBackStack("addFloorFragment");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_rooms) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AddRoomFragment addRoomFragment = new AddRoomFragment();
            addRoomFragment.setSource(Constants.SOURCE_NAV_DRAWER);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
            fragmentTransaction.addToBackStack("addRoomFragment");
            fragmentTransaction.commit();
        } else if( id == R.id.log_out){
            MySettings.setCurrentUser(null);
            Intent loginIntent = new Intent(mInstance, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    WifiManager mWifiManager;
    BroadcastReceiver mWifiConnectionReceiver;
    @Override
    public void onStartListening(){
        mWifiManager = (WifiManager) mInstance.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

                    ConnectivityManager cm = (ConnectivityManager) mInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                            networkInfo.isConnected()) {
                        // Wifi is connected
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        String connectedSSID = wifiInfo.getSSID();

                        /*if(connectedSSID.equals(MySettings.getHomeNetwork().getSsid())){
                            NetworkScannerAsyncTask networkScannerAsyncTask = new NetworkScannerAsyncTask(mInstance);
                            networkScannerAsyncTask.execute();
                        }*/

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
        };

        registerReceiver(mWifiConnectionReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }
}