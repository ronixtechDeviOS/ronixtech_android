package com.ronixtech.ronixhome.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddDeviceStoreIP#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceStoreIP extends Fragment {
    WifiManager mWifiManager;
    DonutProgress progressCircle;
    TextView progressTextView;
    BroadcastReceiver mWifiConnectionReceiver;
    AddDeviceStoreIP addDeviceStoreIP;
    CountDownTimer searchingCountDownTimer, connectingCountDownTimer;

    public AddDeviceStoreIP() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AddDeviceStoreIP newInstance(String param1, String param2) {
        AddDeviceStoreIP fragment = new AddDeviceStoreIP();
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
        View view = inflater.inflate(R.layout.fragment_add_device_store_i_p, container, false);
        progressCircle = view.findViewById(R.id.progress_circle);
        progressTextView = view.findViewById(R.id.progress_textview);
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        onWifiNetworkSelected(MySettings.getTempSSID());
        addDeviceStoreIP = this;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if(getActivity() != null) {
                        try{
                            getActivity().unregisterReceiver(mWifiConnectionReceiver);
                        }catch (Exception e){
                            Utils.log(TAG, "Already unregistered - " + e.getMessage(), true);
                        }
                    }
                    gotoPrevFragment();

                    return true;
                }
                return false;
            }
        });

    }

    public void onWifiNetworkSelected(String network) {
        progressTextView.append(Utils.getStringExtraText(getActivity(), R.string.add_device_connecting, network) + "\n");

        progressCircle.setDonut_progress("" + 1);
        progressCircle.setText("" + 1 + "%");

        /** CountDownTimer starts with 45 seconds and every onTick is 1 second */
        final int totalMillis = 1 * 30 * 1000; // 30 seconds in milli seconds
        connectingCountDownTimer = new CountDownTimer(totalMillis, 1) {
            public void onTick(long millisUntilFinished) {

                //forward progress
                long finishedMillis = totalMillis - millisUntilFinished;
                int totalProgress = (int) (((float) finishedMillis / (float) totalMillis) * 100.0);

                long totalSeconds = Math.round(((double) finishedMillis / (double) totalMillis) * 45.0);

                if (MainActivity.getInstance() != null && MainActivity.isResumed) {
                    progressCircle.setDonut_progress("" + totalProgress);
                    //progressCircle.setText(getActivity().getResources().getStringExtraInt(R.string.seconds, 45 - (int) totalSeconds));
                    progressCircle.setText("" + totalProgress + "%");
                }
            }

            public void onFinish() {
                gotoPrevFragment();
            }
        }.start();


        Utils.log(TAG, "Connecting to " + network + " with default password", true);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    connectToWifiNetwork2(network, Constants.DEVICE_DEFAULT_PASSWORD);
                } else {
                    connectToWifiNetwork(network, Constants.DEVICE_DEFAULT_PASSWORD, true);
                }
            }
        });
    }

    private void gotoPrevFragment() {
        if (MainActivity.getInstance() != null && MainActivity.isResumed) {
            if (getFragmentManager() != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceLocal addDeviceLocal = new AddDeviceLocal();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceLocal, "addDeviceLocal");
                //fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                fragmentTransaction.commitAllowingStateLoss();
                Utils.showToast(MainActivity.getInstance(), "Please make sure controller wifi is enabled properly and try again.", true);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void connectToWifiNetwork2(String ssid, String pass) {
        // registerReceiver(ssid,pass);
        final NetworkSpecifier specifier =
                new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .build();
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                ((ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .bindProcessToNetwork(network);

                Device device=MySettings.getTempDevice();

                HttpURLConnection urlConnection = null;
                int statusCode = 0;
                int numberOfRetries = 0;
                while (statusCode!=200 && numberOfRetries <= 10) {
                    try {
                        URL url = new URL(Constants.DEVICE_URL + Constants.GET_DEVICE_STATUS);
                        Utils.log(TAG, "statusGetter URL: " + url, true);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                        urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                        statusCode = urlConnection.getResponseCode();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String dataLine;
                        while ((dataLine = bufferedReader.readLine()) != null) {
                            result.append(dataLine);
                            Utils.log(TAG, "Response: " + result.toString(), true);
                        }
                        urlConnection.disconnect();
                        Utils.log(TAG, "statusGetter response: " + result.toString(), true);

                        if (result.length() >= 10) {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject != null) {
                                JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                                if (unitStatus != null && unitStatus.has("U_W_STT")) {
                                    JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                    if (wifiStatus != null) {
                                        if (wifiStatus.has("R_W_IP_") && !wifiStatus.getString("R_W_IP_").matches("")) {
                                            Utils.log(TAG, wifiStatus.getString("U_W_UID"), true);
                                            Utils.log(TAG, wifiStatus.getString("R_W_IP_"), true);
                                            device.setIpAddress(wifiStatus.getString("R_W_IP_"));
                                            DevicesInMemory.updateDevice(device);
                                            MySettings.updateDeviceIP(device, wifiStatus.getString("R_W_IP_"));
                                            MainActivity.getInstance().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gotoSuccessFragment();
                                                }
                                            });

                                        }
                                    }
                                }
                            }
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        numberOfRetries++;
                    }
                }

            }
            @Override
            public void onUnavailable() {
                   if(getFragmentManager()!=null) {
                       FragmentManager fragmentManager = getFragmentManager();
                       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceLocal addDeviceLocalFragment = new AddDeviceLocal();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceLocalFragment, "addDeviceLocalFragment");
                    //fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                    fragmentTransaction.commit();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                ((ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .bindProcessToNetwork(null);
            }
        };
        connectivityManager.requestNetwork(request, networkCallback);

    }


    private void connectToWifiNetwork(final String ssid, String password, boolean registerCallback) {
        if (registerCallback) {
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
                        if (getActivity() != null && getActivity().getSystemService(Context.CONNECTIVITY_SERVICE) != null) {
                            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                                // Wifi is connected
                                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                                String connectedSSID = wifiInfo.getSSID();

                                Utils.log(TAG, "Currently connected to: " + connectedSSID, true);

                                if (connectedSSID.toLowerCase().contains(ssid.toLowerCase())) {
                                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.connected_to) + " " + connectedSSID, true);
                                    connectingCountDownTimer.cancel();
                                    progressTextView.append(Utils.getStringExtraText(getActivity(), R.string.add_device_connected, connectedSSID) + "\n");

                                    try {
                                        if (getActivity() != null) {
                                            getActivity().unregisterReceiver(mWifiConnectionReceiver);
                                        }
                                    } catch (Exception e) {
                                        Utils.log(TAG, "Error unregistering mWifiConnectionReceiver", true);
                                    }

                                    if (MainActivity.getInstance() != null && MainActivity.isResumed) {
                                        if (getFragmentManager() != null) {
                                            StatusGetter statusGetter = new StatusGetter(addDeviceStoreIP);
                                            statusGetter.execute();
                                        }
                                    }

                                    //get device info and save them in the database
                                    //namely device mac address (previously), chipid (get request now), typeid (get request now)
                                    //then send configured SSID/password to the device
                                    //getDeviceType();
                                } else {
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
            } catch (Exception e) {
                Utils.log(TAG, "Error registering mWifiConnectionReceiver", true);
            }
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i != null && i.SSID.toLowerCase().startsWith(Constants.DEVICE_NAME_IDENTIFIER)) {
                Utils.log(TAG, "Removing network '" + i.SSID + "' from saved networks", true);
                if (!mWifiManager.removeNetwork(i.networkId)) {
                    Utils.log(TAG, "Failed to remove network " + i.SSID + ", disabling it", true);
                    mWifiManager.disableNetwork(i.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        mWifiManager.setWifiEnabled(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        WifiConfiguration conf = new WifiConfiguration();
        /*if(Build.VERSION.SDK_INT >= 23){
            conf.SSID = ssid;
        }else{
            conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        }*/
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\"" + password + "\"";
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
        } catch (InterruptedException e) {

        }

        if (networkID == -1) {
            // Get existed network id if it is already added to WiFi network
            networkID = getExistingNetworkId(ssid);
        }

        Utils.log(TAG, "Added network, new ID:" + networkID, true);

        if (networkID != -1) {
            Utils.log(TAG, "Connecting to SSID: " + conf.SSID + " with password: " + password + " and networkID: " + networkID, true);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(networkID, true);
            mWifiManager.reconnect();
        } else {
            list = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.toLowerCase().contains(ssid.toLowerCase()/*"\"" + ssid.toLowerCase() + "\""*/)) {
                    Utils.log(TAG, "Connecting to SSID: " + conf.SSID + " with password: " + password + " and networkID: " + conf.networkId, true);
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    mWifiManager.reconnect();
                    break;
                }
            }
        }


    }

    private void gotoSuccessFragment() {
        Utils.showToast(MainActivity.getInstance(), "Connect to Home Network to use the devices", false);
        if (MySettings.getTempDevice().getIpAddress() != "") {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            SuccessFragment successFragment = new SuccessFragment();
            successFragment.setSuccessSource(Constants.SUCCESS_SOURCE_DEVICE);
            fragmentTransaction.replace(R.id.fragment_view, successFragment, "successFragment");
            //fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private int getExistingNetworkId(String SSID) {
        if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

        }
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

    public static class StatusGetter extends AsyncTask<Void,Void,Void>
    {
        Device device=MySettings.getTempDevice();
        AddDeviceStoreIP fragment;

        public StatusGetter(AddDeviceStoreIP fragment)
        {
            this.fragment=fragment;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            int statusCode = 0;
            int numberOfRetries = 0;
            while (statusCode!=200 && numberOfRetries <= 10) {
                try {
                    URL url = new URL(Constants.DEVICE_URL + Constants.GET_DEVICE_STATUS);
                    Utils.log(TAG, "statusGetter URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while ((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                        Utils.log(TAG, "Response: " + result.toString(), true);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "statusGetter response: " + result.toString(), true);

                    if (result.length() >= 10) {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if (jsonObject != null) {
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if (unitStatus != null && unitStatus.has("U_W_STT")) {
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if (wifiStatus != null) {
                                    if (wifiStatus.has("R_W_IP_") && !wifiStatus.getString("R_W_IP_").matches("")) {
                                        Utils.log(TAG, wifiStatus.getString("U_W_UID"), true);
                                        Utils.log(TAG, wifiStatus.getString("R_W_IP_"), true);
                                        device.setIpAddress(wifiStatus.getString("R_W_IP_"));
                                        DevicesInMemory.updateDevice(device);
                                        MySettings.updateDeviceIP(device, wifiStatus.getString("R_W_IP_"));
                                        MainActivity.getInstance().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                fragment.gotoSuccessFragment();
                                            }
                                        });

                                    }
                                }
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }
            return null;
        }
    }
}