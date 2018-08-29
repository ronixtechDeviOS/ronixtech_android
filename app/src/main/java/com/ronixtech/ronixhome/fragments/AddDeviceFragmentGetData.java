package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentGetData.OnStartEnterTransitionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentGetData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentGetData extends Fragment {
    private static final String TAG = AddDeviceFragmentGetData.class.getSimpleName();

    private HomeConnectedListenerInterface mListener;

    public AddDeviceFragmentGetData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceFragmentGetData.
     */
    public static AddDeviceFragmentGetData newInstance(String param1, String param2) {
        AddDeviceFragmentGetData fragment = new AddDeviceFragmentGetData();
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
        View view = inflater.inflate(R.layout.fragment_add_device_get_data, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_device_get_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        getDeviceType();

        return view;
    }

    private void getDeviceType(){
        /*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*/
        //debugTextView.append("Getting device type...\n");
        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.DEVICE_URL + Constants.GET_DEVICE_TYPE_URL;

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
                        //debugTextView.append("Device type ID: " + deviceTypeID + "\n");
                        getChipID();
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                    if(getActivity() != null) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                    }
                    //trial failed, start over from the beginning
                    //debugTextView.setText("Attempt failed, trying again...\n");
                    goToSearchFragment();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                Log.d(TAG, "Volley Error: " + error.getNetworkTimeMs());
                Log.d(TAG, "Volley Error: " + error.getCause());
                Log.d(TAG, "Volley Error: " + error.getLocalizedMessage());
                try {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e(TAG,"TEST " + new String(htmlBodyBytes), error);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "Volley Error: statusCode: " + error.networkResponse.statusCode);
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                //debugTextView.setText("Attempt failed, trying again...\n");
                goToSearchFragment();
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONFIG_TIMEOUT, Device.CONFIG_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void getChipID(){
        /*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*/
        //debugTextView.append("Getting chip id...\n");
        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.DEVICE_URL + Constants.GET_CHIP_ID_URL;

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
                        if(MySettings.getDeviceByChipID(chipID) != null){
                            Toast.makeText(getActivity(), "Device already added before.", Toast.LENGTH_SHORT).show();
                            if(mListener != null){
                                mListener.onStartListening();
                            }
                            connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commit();
                        }else {
                            //debugTextView.append("Chip ID: " + chipID + "\n");
                            goToSendDataFragment();
                        }
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_chip_id), Toast.LENGTH_SHORT).show();
                    }
                    //trial failed, start over from the beginning
                    //debugTextView.setText("Attempt failed, trying again...\n");
                    goToSearchFragment();
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
                //debugTextView.setText("Attempt failed, trying again...\n");
                goToSearchFragment();
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONFIG_TIMEOUT, Device.CONFIG_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void goToSendDataFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
        AddDeviceFragmentSendData addDeviceFragmentSendData = new AddDeviceFragmentSendData();
        fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentSendData, "addDeviceFragmentSendData");
        fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
        fragmentTransaction.commit();
    }

    private void goToSearchFragment(){
        getFragmentManager().popBackStack();
    }

    private void connectToWifiNetwork(final String ssid, String password){
        WifiManager mWifiManager;
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!mWifiManager.isWifiEnabled()){
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    @Override
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
    }

    public interface HomeConnectedListenerInterface{
        void onStartListening();
    }
}
