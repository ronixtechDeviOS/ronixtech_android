package com.ronixtech.ronixhome.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MyApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.ViewPagerAdapter;
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

import static android.widget.Toast.LENGTH_SHORT;


/**

 * create an instance of this fragment.
 */
public class AddDeviceLocal extends Fragment {

    private static final String TAG = AddDeviceLocal.class.getSimpleName();

    Device device;
    AddDeviceLocal addDeviceLocal;
    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;

    Button continueButton;
    public AddDeviceLocal() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment AddDeviceLocal.
     */
    // TODO: Rename and change types and number of parameters
    public AddDeviceLocal Instance() {

       return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_add_device_local, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_device), getResources().getColor(R.color.whiteColor));

        device= MySettings.getTempDevice();
        addDeviceLocal=Instance();

        viewPager = (ViewPager) view.findViewById(R.id.devices_hints_pager);
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        DeviceConfigurationHintFragment deviceConfigurationHintFragment = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment.setDeviceType(Device.DEVICE_TYPE_wifi_3lines);
        pagerAdapter.addFrag(deviceConfigurationHintFragment, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_wifi_3lines));
        pagerAdapter.notifyDataSetChanged();


        continueButton=view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG,"Connected");
         //       Toast.makeText(MainActivity.getInstance(),"Connected to Controller", LENGTH_SHORT).show();
          /*      StatusGetter statusGetter=new StatusGetter(device,addDeviceLocal);
                statusGetter.execute();
*/

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceStoreIP addDeviceStoreIP = new AddDeviceStoreIP();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceStoreIP, "storeIPFragment");
                //fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if(wifiInfo != null) {
                if (wifiInfo.getNetworkId() == -1) {
                    return false; // Not connected to an access point
                }
                else {
                    if(wifiInfo.getSSID().contains(device.getChipID())) {
                        Log.v(TAG,"Found: "+device.getChipID());
                        return true; // Connected to an access point
                    }
                }
            }
            Log.v(TAG,"Not Found: "+wifiInfo.getSSID());
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
        return false;
    }


}