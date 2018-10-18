package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.ronixtech.ronixhome.entities.SoundDeviceData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentSendData.HomeConnectedListenerInterface} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentSendData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentSendData extends Fragment {
    private static final String TAG = AddDeviceFragmentSendData.class.getSimpleName();

    private HomeConnectedListenerInterface mListener;

    public AddDeviceFragmentSendData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceFragmentSendData.
     */
    public static AddDeviceFragmentSendData newInstance(String param1, String param2) {
        AddDeviceFragmentSendData fragment = new AddDeviceFragmentSendData();
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
        View view = inflater.inflate(R.layout.fragment_add_device_send_data, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_device_send_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        sendConfigurationToDevice();

        return view;
    }

    private void sendConfigurationToDevice(){
        //debugTextView.append("Sending home network info to your RonixTech device...\n");

        DataSender dataSender = new DataSender(getActivity(), this);
        dataSender.execute();

        //volley request to device to send ssid/password and then get device info for next steps
/*        String url = Constants.DEVICE_URL + Constants.SEND_SSID_PASSWORD_URL;
        //?essid=%SSID%&passwd=%PASS%

        url = url.concat("?").concat(Constants.PARAMETER_SSID).concat("=").concat(MySettings.getHomeNetwork().getSsid())
                .concat("&").concat(Constants.PARAMETER_PASSWORD).concat("=").concat(MySettings.getHomeNetwork().getPassword());

        Log.d(TAG,  "sendConfigurationToDevice URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "sendConfigurationToDevice response: " + response);
                //debugTextView.append("Connecting to your home network now...\n");
                //debugTextView.append("delwa2ty el device is configured. @Mina Please tell me if you get to this step\n");
                //debugTextView.append(" go to next step, AddDeviceConfigurationFragment\n");
                Device tempDevice = MySettings.getTempDevice();
                //MySettings.addDevice(tempDevice);
                if(mListener != null){
                    mListener.onStartListening();
                }
                connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

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
                //debugTextView.setText("Attempt failed, trying again...\n");
                goToSearchFragment();
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONFIG_TIMEOUT, Device.CONFIG_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);*/
    }

    public void goToSearchFragment(){
        if(getFragmentManager() != null) {
            getFragmentManager().popBackStack("addDeviceFragmentIntro", 0);
        }
    }

    public void connectToWifiNetwork(final String ssid, String password){
        if(getActivity() != null && getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE) != null){
            WifiManager mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
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

    public static class DataSender extends AsyncTask<Void, Void, Void> {
        private final String TAG = AddDeviceFragmentSendData.DataSender.class.getSimpleName();

        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public DataSender(Activity activity, AddDeviceFragmentSendData fragment) {
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
                if(fragment.mListener != null){
                    fragment.mListener.onStartListening();
                }
                fragment.connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

                Device device = MySettings.getTempDevice();
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                    FragmentManager fragmentManager = fragment.getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceConfigurationFragment addDeviceConfigurationFragment = new AddDeviceConfigurationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationFragment, "addDeviceConfigurationFragment");
                    fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                    fragmentTransaction.commitAllowingStateLoss();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    //quickly initialize the souddevicedata configuration for the device
                    MySettings.addDevice(device);
                    device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                    SoundDeviceData soundDeviceData = new SoundDeviceData();
                    soundDeviceData.setDeviceID(device.getId());
                    device.setSoundDeviceData(soundDeviceData);
                    MySettings.setTempDevice(device);
                    FragmentManager fragmentManager = fragment.getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocationFragment");
                    fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                    fragmentTransaction.commit();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    //go to PIR motion sensor config. fragment
                    FragmentManager fragmentManager = fragment.getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceConfigurationPIRFragment addDeviceConfigurationPIRFragment = new AddDeviceConfigurationPIRFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationPIRFragment, "addDeviceConfigurationPIRFragment");
                    fragmentTransaction.addToBackStack("addDeviceConfigurationPIRFragment");
                    fragmentTransaction.commitAllowingStateLoss();
                }

            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
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
                    String urlString = Constants.DEVICE_URL + Constants.DEVICE_STATUS_CONTROL_URL;
                    //?essid=%SSID%&passwd=%PASS%

                    /*urlString = urlString.concat("?").concat(Constants.PARAMETER_SSID).concat("=").concat(MySettings.getHomeNetwork().getSsid())
                            .concat("&").concat(Constants.PARAMETER_PASSWORD).concat("=").concat(MySettings.getHomeNetwork().getPassword());*/
                    URL url = new URL(urlString);
                    Log.d(TAG,  "sendConfigurationToDevice URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.PARAMETER_SSID, MySettings.getHomeNetwork().getSsid());
                    jsonObject.put(Constants.PARAMETER_PASSWORD, MySettings.getHomeNetwork().getPassword());
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Log.d(TAG,  "sendConfigurationToDevice POST data: " + jsonObject.toString());

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Log.d(TAG,  "sendConfigurationToDevice response: " + result.toString());
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
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
