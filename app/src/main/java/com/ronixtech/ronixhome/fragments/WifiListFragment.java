package com.ronixtech.ronixhome.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.WifiNetworkItemAdapter;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiListFragment extends Fragment {
    private static final String TAG = WifiListFragment.class.getSimpleName();

    private WifiListFragment.OnNetworkSelectedListener callback;
    public interface OnNetworkSelectedListener {
        public void onNetworkSelected(WifiNetwork network);
    }


    private OnFragmentInteractionListener mListener;

    private static final int RC_PERMISSION_LOCATION = 1004;
    private static final int RC_PERMISSION_ACCESS_WIFI_STATE = 1005;
    private static final int RC_PERMISSION_CHANGE_WIFI_STATE= 1006;

    private static final int RC_ACTIVITY_WIFI_TURN_ON = 1007;
    private static final int RC_ACTIVITY_LOCATION_TURN_ON = 1008;

    Button continueButton;

    List<WifiNetwork> networks;
    ListView networksListView;
    WifiNetworkItemAdapter networksAdapter;
    TextView searchStatusTextView;

    private WifiNetwork preferredNetwork;

    WifiManager mWifiManager;
    BroadcastReceiver mWifiScanReceiver;
    BroadcastReceiver mWifiConnectionReceiver;

    public WifiListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiListFragment.
     */
    public static WifiListFragment newInstance(String param1, String param2) {
        WifiListFragment fragment = new WifiListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*try {
            callback = (WifiListFragment.OnNetworkSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnNetworkSelectedListener");
        }*/
    }

    public void setNetworkSelectedListener(WifiListFragment.OnNetworkSelectedListener listener){
        this.callback = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wifi_list, container, false);
        //MainActivity.setActionBarTitle(getActivity().getResources().getStringExtraInt(R.string.add_device), getResources().getColor(R.color.whiteColor));
        //setHasOptionsMenu(true);

        searchStatusTextView = view.findViewById(R.id.search_status_textview);
        networksListView = view.findViewById(R.id.networks_listview);
        networks = new ArrayList<>();
        networksAdapter = new WifiNetworkItemAdapter(getActivity(), networks, Constants.WIFI_NETWORK_SEARCH);
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_wifi_network_footer, null, false);
        networksListView.addFooterView(footerView);
        networksListView.setAdapter(networksAdapter);

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualNetworkDialog();
            }
        });

        continueButton = view.findViewById(R.id.continue_button);

        networksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                WifiNetwork clickedNetwork = (WifiNetwork) networksAdapter.getItem(i);
                showPasswordDialog(clickedNetwork);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                WifiDevicesFragment wifiDevicesFragment = new WifiDevicesFragment();
                fragmentTransaction.replace(R.id.fragment_view, wifiDevicesFragment);
                fragmentTransaction.addToBackStack("wifiDevicesFragment");
                fragmentTransaction.commit();*/
            }
        });

        checkLocationPermissions();

        return view;
    }

    private void showManualNetworkDialog(){
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity()).create();
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r = getActivity().getResources();
        float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        TextView ssidTextView = new TextView(getActivity());
        ssidTextView.setText(Utils.getString(getActivity(), R.string.ssid_colon));
        ssidTextView.setTextSize(20);
        ssidTextView.setGravity(Gravity.CENTER);
        ssidTextView.setLayoutParams(layoutParams);

        TextView passwordTextView = new TextView(getActivity());
        passwordTextView.setText(Utils.getString(getActivity(), R.string.password_colon));
        passwordTextView.setTextSize(20);
        passwordTextView.setGravity(Gravity.CENTER);
        passwordTextView.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r2 = getActivity().getResources();
        float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
        layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
        layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

        final EditText ssidEditText = new EditText(getActivity());
        ssidEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ssidEditText.setHint(Utils.getString(getActivity(), R.string.ssid_hint));
        ssidEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        ssidEditText.setLayoutParams(layoutParams2);

        final EditText passwordEditText = new EditText(getActivity());
        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEditText.setHint(Utils.getString(getActivity(), R.string.password_hint));
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        passwordEditText.setLayoutParams(layoutParams2);

        Button submitButton = new Button(getActivity());
        submitButton.setText(Utils.getString(getActivity(), R.string.done));
        submitButton.setTextColor(getActivity().getResources().getColor(R.color.whiteColor));
        submitButton.setBackgroundColor(getActivity().getResources().getColor(R.color.blueColor));
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ssidEditText.getText().toString() != null && ssidEditText.getText().toString().length() >= 1){
                    if(passwordEditText.getText().toString() != null && passwordEditText.getText().toString().length() >= 4) {
                        WifiNetwork network = new WifiNetwork();
                        network.setSsid(ssidEditText.getText().toString());
                        network.setPassword(passwordEditText.getText().toString());
                        if(callback != null) {
                            callback.onNetworkSelected(network);
                        }
                        dialog.dismiss();
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(passwordEditText);
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(ssidEditText);
                }
            }
        });

        layout.addView(ssidTextView);
        layout.addView(ssidEditText);
        layout.addView(passwordTextView);
        layout.addView(passwordEditText);
        layout.addView(submitButton);

        dialog.setView(layout);

        dialog.show();
    }

    private void showPasswordDialog(WifiNetwork network){
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity()).create();
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r = getActivity().getResources();
        float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        TextView passwordTextView = new TextView(getActivity());
        passwordTextView.setText(Utils.getString(getActivity(), R.string.password_colon));
        passwordTextView.setTextSize(20);
        passwordTextView.setGravity(Gravity.CENTER);
        passwordTextView.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r2 = getActivity().getResources();
        float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
        layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
        layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

        final EditText passwordEditText = new EditText(getActivity());
        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEditText.setHint(Utils.getString(getActivity(), R.string.password_hint));
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        passwordEditText.setLayoutParams(layoutParams2);

        Button submitButton = new Button(getActivity());
        submitButton.setText(Utils.getString(getActivity(), R.string.done));
        submitButton.setTextColor(getActivity().getResources().getColor(R.color.whiteColor));
        submitButton.setBackgroundColor(getActivity().getResources().getColor(R.color.blueColor));
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordEditText.getText().toString() != null && passwordEditText.getText().toString().length() >= 4) {
                    network.setPassword(passwordEditText.getText().toString());
                    if(callback != null) {
                        callback.onNetworkSelected(network);
                    }
                    dialog.dismiss();
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(passwordEditText);
                }
            }
        });

        layout.addView(passwordTextView);
        layout.addView(passwordEditText);
        layout.addView(submitButton);

        dialog.setView(layout);

        dialog.show();
    }

    private void checkLocationPermissions(){
        Utils.log(TAG, "location - chechLocationPermissions", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Utils.log(TAG, "location - checkLocationPermissions greater than M", true);
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Utils.log(TAG, "location - checkLocationPermissions checkSelfPermission TRUE", true);
                checkWifiAccessPermissions();
            }else{
                Utils.log(TAG, "location - checkLocationPermissions checkSelfPermission FALSE", true);
                requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_LOCATION);
            }
        }else{
            //no need to show runtime permission stuff
            Utils.log(TAG, "location - checkLocationPermissions older than M", true);
            checkWifiAccessPermissions();
        }
    }

    private void checkWifiAccessPermissions(){
        Utils.log(TAG, "wifiaccess - checkWifiAccessPermissions", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Utils.log(TAG, "wifiaccess - checkWifiAccessPermissions greater than M", true);
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Utils.log(TAG, "wifiaccess - checkWifiAccessPermissions checkSelfPermission TRUE", true);
                checkWifiChangePermissions();
            }else{
                Utils.log(TAG, "wifiaccess - checkWifiAccessPermissions checkSelfPermission FALSE", true);
                requestPermissions(new String[]{"android.permission.ACCESS_WIFI_STATE"}, RC_PERMISSION_ACCESS_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            Utils.log(TAG, "wifiaccess - checkWifiAccessPermissions older than M", true);
            checkWifiChangePermissions();
        }
    }

    private void checkWifiChangePermissions(){
        Utils.log(TAG, "wifichange - checkWifiChangePermissions", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Utils.log(TAG, "wifichange - checkWifiChangePermissions greater than M", true);
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Utils.log(TAG, "wifichange - checkWifiChangePermissions checkSelfPermission TRUE", true);
                refreshNetworks();
            }else{
                Utils.log(TAG, "wifichange - checkWifiChangePermissions checkSelfPermission FALSE", true);
                requestPermissions(new String[]{"android.permission.CHANGE_WIFI_STATE"}, RC_PERMISSION_CHANGE_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            Utils.log(TAG, "wifichange - checkWifiChangePermissions older than M", true);
            refreshNetworks();
        }
    }

    private boolean checkLocationServices(){
        boolean actionNeeded = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getActivity() != null && getActivity().getSystemService(Context.LOCATION_SERVICE) != null){
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsProviderEnabled, isNetworkProviderEnabled;
                isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                    actionNeeded = true;
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
                            callback.onNetworkSelected(null);
                        }
                    });
                    builder.show();
                }
            }
        }
        return actionNeeded;
    }

    public void setWifiNetwork(WifiNetwork network){
        this.preferredNetwork = network;
    }

    private void refreshNetworks(){
        if(checkLocationServices()){
            return;
        }
        if(getActivity() != null && getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE) != null){
            mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(!mWifiManager.isWifiEnabled()){
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
                                callback.onNetworkSelected(null);
                            }
                        })
                        .show();
            }else{
                mWifiScanReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent intent) {
                        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                            //networks.clear();
                            List<ScanResult> mScanResults = mWifiManager.getScanResults();
                            if(mScanResults != null){
                                for (ScanResult result : mScanResults) {
                                    WifiNetwork network = new WifiNetwork();
                                    network.setSsid(result.SSID);
                                    network.setSignal(""+result.level);
                                    if(!networks.contains(network)){
                                        networks.add(network);
                                    }
                                    /*if(result.SSID.toLowerCase().contains(preferredNetwork.getSsid())){
                                        Toast.makeText(getActivity(), "WiFi network within range, connecting...", Toast.LENGTH_SHORT).show();
                                        connectToWifiNetwork(result.SSID, preferredNetwork.getPassword());
                                    }*/
                                }
                            }
                            networksAdapter.notifyDataSetChanged();
                            if(networks.size() >= 1){
                                searchStatusTextView.setVisibility(View.GONE);
                            }else{
                                searchStatusTextView.setVisibility(View.VISIBLE);
                                if(getActivity() != null) {
                                    searchStatusTextView.setText(Utils.getString(getActivity(), R.string.no_networks_in_range));
                                }
                            }
                            mWifiManager.startScan();
                        }
                    }
                };

                try {
                    if (getActivity() != null) {
                        getActivity().registerReceiver(mWifiScanReceiver,
                                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    }
                }catch (IllegalArgumentException e){
                    Utils.log(TAG, "Error registering mWifiScanReceiver", true);
                }

                mWifiManager.startScan();
            }
        }

    }

    private void connectToWifiNetwork(final String ssid, String password){
        try {
            if (mWifiScanReceiver != null) {
                getActivity().unregisterReceiver(mWifiScanReceiver);
            }
        }catch (IllegalArgumentException e){
            Utils.log(TAG, "Error unregistering mWifiScanReceiver", true);
        }

        continueButton.setText(Utils.getString(getActivity(), R.string.connecting));

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

                    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                            networkInfo.isConnected()) {
                        // Wifi is connected
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        String connectedSSID = wifiInfo.getSSID();

                        Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.connected_to) + " " + connectedSSID, true);
                        Utils.setButtonEnabled(continueButton, true);

                    }

                }
            }
        };

        try {
            if (getActivity() != null) {
                getActivity().registerReceiver(mWifiConnectionReceiver,
                        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
        }catch (IllegalArgumentException e){
            Utils.log(TAG, "Error registering mWifiConnectionReceiver", true);
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

        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.contains("\"" + ssid + "\"")) {
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_LOCATION: {
                Utils.log(TAG, "location - onRequestPermissionsResult", true);
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    Utils.log(TAG, "location - onRequestPermissionsResult allowed", true);
                    checkWifiAccessPermissions();
                }
                else{
                    //denied
                    Utils.log(TAG, "location - onRequestPermissionsResult denied", true);
                    Utils.showToast(getActivity(), "You need to enable location permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")) {
                        Utils.log(TAG, "location - onRequestPermissionsResult shouldShowRequestPermissionRationale ", true);
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
                Utils.log(TAG, "wifiaccess - onRequestPermissionsResult", true);
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    Utils.log(TAG, "wifiaccess - onRequestPermissionsResult allowed", true);
                    checkWifiChangePermissions();
                }
                else{
                    //denied
                    Utils.log(TAG, "wifiaccess - onRequestPermissionsResult denied", true);
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_WIFI_STATE")) {
                        Utils.log(TAG, "wifiaccess - onRequestPermissionsResult shouldShowRequestPermissionRationale ", true);
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
                Utils.log(TAG, "wifichange - onRequestPermissionsResult", true);
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    Utils.log(TAG, "wifichange - onRequestPermissionsResult allowed", true);
                    refreshNetworks();
                }
                else{
                    //denied
                    Utils.log(TAG, "wifichange - onRequestPermissionsResult denied", true);
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.CHANGE_WIFI_STATE")) {
                        Utils.log(TAG, "wifichange - onRequestPermissionsResult shouldShowRequestPermissionRationale ", true);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == RC_ACTIVITY_WIFI_TURN_ON ) {
            /*if(mWifiManager != null && mWifiManager.isWifiEnabled()){
                refreshNetworks();
            }else{
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(getActivity().getResources().getStringExtraInt(R.string.wifi_required_title))
                        //set message
                        .setMessage(getActivity().getResources().getStringExtraInt(R.string.wifi_required_message))
                        //set positive button
                        .setPositiveButton(getActivity().getResources().getStringExtraInt(R.string.go_to_wifi_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                refreshNetworks();
                            }
                        })
                        //set negative button
                        .setNegativeButton(getActivity().getResources().getStringExtraInt(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                                callback.onNetworkSelected(null);
                            }
                        })
                        .show();
            }*/
            refreshNetworks();
        }else if(requestCode == RC_ACTIVITY_LOCATION_TURN_ON){
            /*if(true){

            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getStringExtraInt(R.string.location_required_title));
                builder.setMessage(getActivity().getResources().getStringExtraInt(R.string.location_required_message));
                builder.setPositiveButton(getActivity().getResources().getStringExtraInt(R.string.go_to_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refreshNetworks();
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getStringExtraInt(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onNetworkSelected(null);
                    }
                });
                builder.show();
            }*/
            refreshNetworks();
        }
    }

    @Override
    public void onStop(){
        if(getActivity() != null) {
            try{
                getActivity().unregisterReceiver(mWifiScanReceiver);
                getActivity().unregisterReceiver(mWifiConnectionReceiver);
            }catch (Exception e){
                Utils.log(TAG, "Already unregistered - " + e.getMessage(), true);
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
