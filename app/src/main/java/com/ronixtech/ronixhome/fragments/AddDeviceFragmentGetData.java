package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import com.ronixtech.ronixhome.Constants;
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

    public void getDeviceType(){
        /*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*/
        //debugTextView.append("Getting device type...\n");
        DeviceTypeGetter deviceTypeGetter = new DeviceTypeGetter(getActivity(), this);
        deviceTypeGetter.execute();

        //volley request to device to send ssid/password and then get device info for next steps
/*        String url = Constants.DEVICE_URL + Constants.GET_DEVICE_TYPE_URL;

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
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);*/
    }

    public void getChipID(){
        /*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*/
        //debugTextView.append("Getting chip id...\n");
        ChipIDGetter chipIDGetter = new ChipIDGetter(getActivity(), this);
        chipIDGetter.execute();

        //volley request to device to send ssid/password and then get device info for next steps
/*        String url = Constants.DEVICE_URL + Constants.GET_CHIP_ID_URL;

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
                            //remove device and re-add it again, or just go back?
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                    //set icon
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    //set title
                                    .setTitle(getActivity().getResources().getString(R.string.duplicate_unit_title))
                                    //set message
                                    .setMessage(getActivity().getResources().getString(R.string.duplicate_unit_message))
                                    //set positive button
                                    .setPositiveButton(getActivity().getResources().getString(R.string.remove_duplicate_smart_controller), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what would happen when positive button is clicked
                                            MySettings.removeDevice(MySettings.getDeviceByChipID(chipID));
                                            goToSendDataFragment();
                                        }
                                    })
                                    //set negative button
                                    .setNegativeButton(getActivity().getResources().getString(R.string.keep_smart_controller), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what should happen when negative button is clicked
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
                                        }
                                    })
                                    .show();
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
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);*/
    }

    public void goToSendDataFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
        AddDeviceFragmentSendData addDeviceFragmentSendData = new AddDeviceFragmentSendData();
        fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentSendData, "addDeviceFragmentSendData");
        fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
        fragmentTransaction.commit();
    }

    public void goToSearchFragment(){
        getFragmentManager().popBackStack("addDeviceFragmentIntro", 0);
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

    public static class DeviceTypeGetter extends AsyncTask<Void, Void, Void> {
        private final String TAG = AddDeviceFragmentGetData.DeviceTypeGetter.class.getSimpleName();

        int statusCode;

        Activity activity;
        AddDeviceFragmentGetData fragment;

        public DeviceTypeGetter(Activity activity, AddDeviceFragmentGetData fragment) {
            this.activity = activity;
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200){
                fragment.getChipID();
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                fragment.goToSearchFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    URL url = new URL(Constants.DEVICE_URL + Constants.GET_DEVICE_TYPE_URL);
                    Log.d(TAG,  "getDeviceType URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Log.d(TAG,  "getDeviceType response: " + result.toString());
                    if(result.length() >= 3){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null&& jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){
                            String typeIDString = jsonObject.getString(Constants.PARAMETER_DEVICE_TYPE_ID);
                            int deviceTypeID = Integer.valueOf(typeIDString);
                            Device tempDevice = MySettings.getTempDevice();
                            tempDevice.setDeviceTypeID(deviceTypeID);
                            MySettings.setTempDevice(tempDevice);
                        }
                    }
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    urlConnection.disconnect();
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class ChipIDGetter extends AsyncTask<Void, Void, Void> {
        private final String TAG = AddDeviceFragmentGetData.ChipIDGetter.class.getSimpleName();

        int statusCode;
        String mChipID;

        Activity activity;
        AddDeviceFragmentGetData fragment;

        public ChipIDGetter(Activity activity, AddDeviceFragmentGetData fragment) {
            this.activity = activity;
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200){
                if(MySettings.getDeviceByChipID(mChipID) != null){
                    //remove device and re-add it again, or just go back?
                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                            //set icon
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            //set title
                            .setTitle(activity.getResources().getString(R.string.duplicate_unit_title))
                            //set message
                            .setMessage(activity.getResources().getString(R.string.duplicate_unit_message))
                            //set positive button
                            .setPositiveButton(activity.getResources().getString(R.string.remove_duplicate_smart_controller), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //set what would happen when positive button is clicked
                                    MySettings.removeDevice(MySettings.getDeviceByChipID(mChipID));
                                    fragment.goToSendDataFragment();
                                }
                            })
                            //set negative button
                            .setNegativeButton(activity.getResources().getString(R.string.keep_smart_controller), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //set what should happen when negative button is clicked
                                    if(fragment.mListener != null){
                                        fragment.mListener.onStartListening();
                                    }
                                    fragment.connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

                                    FragmentManager fragmentManager = fragment.getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    fragmentTransaction.commit();
                                }
                            })
                            .show();
                }else {
                    //debugTextView.append("Chip ID: " + chipID + "\n");
                    fragment.goToSendDataFragment();
                }
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.unable_to_get_device_chip_id), Toast.LENGTH_SHORT).show();
                fragment.goToSearchFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    URL url = new URL(Constants.DEVICE_URL + Constants.GET_CHIP_ID_URL);
                    Log.d(TAG,  "getChipID URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Log.d(TAG,  "getChipID response: " + result.toString());
                    if(result.length() >= 3){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_CHIP_ID)){
                            String chipID = jsonObject.getString(Constants.PARAMETER_DEVICE_CHIP_ID);
                            Device tempDevice = MySettings.getTempDevice();
                            tempDevice.setChipID(chipID);
                            MySettings.setTempDevice(tempDevice);
                            mChipID = chipID;
                        }
                    }
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    urlConnection.disconnect();
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

}
