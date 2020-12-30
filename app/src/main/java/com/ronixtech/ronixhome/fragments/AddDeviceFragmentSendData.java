package com.ronixtech.ronixhome.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
    Handler myHandler = new Handler();
    Boolean check=false;
    AlertDialog alertDialog;
    AlertDialog.Builder dialogBuilder;
    android.support.v7.app.AlertDialog exitalertDialog;
    MqttAndroidClient mqttAndroidClient;
    Device device;
    TextView textView;
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
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_device_send_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);
         device = MySettings.getTempDevice();
         textView=view.findViewById(R.id.progress_textview);
        sendConfigurationToDevice();
        dialogInit();
        alertDialog=dialogBuilder.create();
        return view;
    }

    private void dialogInit() {
        dialogBuilder=new AlertDialog.Builder(MainActivity.getInstance())
                .setTitle("RonixTech")
                .setMessage("Taking longer than expected. Please select an option to continue")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        check = true;
                        mqttConnect();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        check = true;
                        showExitAlert();
                    }
                })
                .setNeutralButton("Connect Locally", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (MainActivity.getInstance() != null && MainActivity.isResumed) {
                            if (getFragmentManager() != null) {
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                AddDeviceLocal addDeviceLocal = new AddDeviceLocal();
                                fragmentTransaction.replace(R.id.fragment_view, addDeviceLocal, "AddDeviceLocal");
                                // fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                                fragmentTransaction.commitAllowingStateLoss();
                            }
                        }

                    }
                });
    }

    private void sendConfigurationToDevice(){
        //debugTextView.append("Sending home network info to your RonixTech device...\n");

      //  showAlert();
        textView.setText("Configuring device");
        if(device != null){
            DHCPToggler dhcpToggler = new DHCPToggler(getActivity(), this);
            dhcpToggler.execute();
        }

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
                    Toast.makeText(getActivity(), getStringExtraInt(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPause() {
        super.onPause();

    }



    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG,"onStop");
        myHandler.removeCallbacksAndMessages(null);
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

        if(alertDialog!=null && alertDialog.isShowing())
        {
            alertDialog.dismiss();
        }

        if(exitalertDialog!=null && exitalertDialog.isShowing())
        {
            exitalertDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void showAlert() {
     /*   myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //disconnecting mqtt
                alertDialog=new android.support.v7.app.AlertDialog.Builder(MainActivity.getInstance())
                        .setTitle("RonixTech")
                        .setMessage("Taking longer than expected. Please select an option to continue")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                check=true;
                                mqttConnect();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                check=true;
                                showExitAlert();
                            }
                        })
                        .setNeutralButton("Connect Locally", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(MainActivity.getInstance() != null && MainActivity.isResumed){
                                    if(getFragmentManager() != null){
                                        FragmentManager fragmentManager = getFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        AddDeviceLocal addDeviceLocal = new AddDeviceLocal();
                                        fragmentTransaction.replace(R.id.fragment_view, addDeviceLocal, "AddDeviceLocal");
                                       // fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                                        fragmentTransaction.commitAllowingStateLoss();
                                    }
                                }

                            }
                        }).show();
            }
        },15000); // Show after 15 Seconds only for the first time
*/
    if(!alertDialog.isShowing()) {
        alertDialog.show();
        }
    }

    public void showExitAlert()
    {
        exitalertDialog =new android.support.v7.app.AlertDialog.Builder(MainActivity.getInstance())
                .setTitle("RonixTech")
                .setMessage("This will terminate the process of adding the smart controller")
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       // showStaticAlert();
                        alertDialog.show();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteDevice();
                        gotoRoomsDashboard();
                    }
                }).show();
    }

    private void deleteDevice() {
        MySettings.removeDevice(device);
        DevicesInMemory.removeDevice(device);
        MainActivity.getInstance().removeDevice(device);
        MainActivity.getInstance().refreshDevicesListFromMemory();
    }

    private void gotoRoomsDashboard() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }


    public void goToSearchFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if (getFragmentManager() != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro =new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }
        }
    }

    public void goToSuccessFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){

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
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void connectToWifiNetwork2(String ssid, String pass) {
        final NetworkSpecifier specifier =
                new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .build();
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(specifier)
                        .build();
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                ((ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .bindProcessToNetwork(network);
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getStatusMQTT();
                    }
                });

            }
            @Override
            public void onLost(@NonNull Network network) {
                ((ConnectivityManager) MainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .bindProcessToNetwork(null);
            }
        };
        connectivityManager.requestNetwork(request, networkCallback);

        /*final WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build();

        final List<WifiNetworkSuggestion> suggestionsList = new ArrayList<WifiNetworkSuggestion>()
        {{ add(suggestion1);
        }};

        final WifiManager wifiManager = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final int status = wifiManager.addNetworkSuggestions(suggestionsList);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // do error handling here…
              }

    // Optional (Wait for post connection broadcast to one of your suggestions)
        final IntentFilter intentFilter =
                new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(
                        WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    return;
                }
                getStatusMQTT();
                // do post connect processing here...
            }
        };
        MainActivity.getInstance().registerReceiver(broadcastReceiver, intentFilter);
*/

    }

    @SuppressLint("MissingPermission")
    public void connectToWifiNetwork(final String ssid, String password){
        if(getActivity() != null && getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE) != null){
            WifiManager mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if(!mWifiManager.isWifiEnabled()){
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }

           List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            if(list!=null) {
                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.toLowerCase().contains(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())) {
                        mWifiManager.removeNetwork(i.networkId);
                        break;
                    }
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
            getStatusMQTT();
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

    private void getStatusMQTT() {
        textView.setText("Trying to connect to smart controller online");

        try {
            String clientid = MqttClient.generateClientId();
            initMqttClient(MainActivity.getInstance(), Constants.MQTT_URL + ":" + Constants.MQTT_PORT, clientid);
        }
        catch(Exception e)
        {
            Utils.log(TAG,"Mqtt initialization error: "+e.getMessage(),true);
        }
        mqttConnect();

    }

    public interface HomeConnectedListenerInterface{
        void onStartListening();
    }

    public static class DHCPToggler extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public DHCPToggler(Activity activity, AddDeviceFragmentSendData fragment) {
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
            Device device = MySettings.getTempDevice();
            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                /*ControllerAddPairings controllerAddPairings = new ControllerAddPairings(getActivity(), this);
                controllerAddPairings.execute();*/
                DimmingControlsSenderPost dimmingControlsSenderPost = new DimmingControlsSenderPost(activity, fragment);
                dimmingControlsSenderPost.execute();
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(activity, fragment);
                wiFiDataSenderGet.execute();
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                PIRResetPairings pirResetPairings = new PIRResetPairings(activity, fragment);
                pirResetPairings.execute();
            }else{
                WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(activity, fragment);
                wiFiDataSenderGet.execute();
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

                    URL url = new URL(urlString);
                    Utils.log(TAG, "DHCPToggler URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);
                    jsonObject.put("R_W_DHC", "on");

                    Utils.log(TAG, "DHCPToggler POST data: " + jsonObject.toString(), true);

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
                    Utils.log(TAG, "DHCPToggler response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class ControllerAddPairings extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public ControllerAddPairings(Activity activity, AddDeviceFragmentSendData fragment) {
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
            if(statusCode == 200) {
                DimmingControlsSenderPost dimmingControlsSenderPost = new DimmingControlsSenderPost(activity, fragment);
                dimmingControlsSenderPost.execute();
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
                fragment.goToSearchFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            for (Line line:MySettings.getTempDevice().getLines()) {
                if(line.getMode() == Line.MODE_SECONDARY){
                    statusCode = 0;
                    int numberOfRetries = 0;
                    while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                        try{
                            String urlString = Constants.DEVICE_URL + Constants.DEVICE_ADD_LINE_PAIRINGS_URL;

                            urlString = urlString.concat("?").concat("line_position").concat("=").concat(""+line.getPosition());
                            urlString = urlString.concat("&").concat("primary_chip_id").concat("=").concat(line.getPrimaryDeviceChipID());
                            urlString = urlString.concat("&").concat("primary_line_position").concat("=").concat(""+line.getPrimaryLinePosition());

                            URL url = new URL(urlString);
                            Utils.log(TAG, "addPairing URL: " + url, true);

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
                            Utils.log(TAG, "addPairing response: " + result.toString(), true);
                        }catch (MalformedURLException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }catch (IOException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }finally {
                            if(urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            numberOfRetries++;
                        }
                    }
                }
            }

            return null;
        }
    }

    public static class DimmingControlsSenderPost extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public DimmingControlsSenderPost(Activity activity, AddDeviceFragmentSendData fragment) {
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
                WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(activity, fragment);
                wiFiDataSenderGet.execute();
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
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

                    URL url = new URL(urlString);
                    Utils.log(TAG, "sendDimmingControls URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    Device device = MySettings.getTempDevice();
                    if(device.getLines() != null){
                        for (Line line : device.getLines()) {
                            if(line.getPosition() == 0){
                                jsonObject.put("L_0_D_S", "" + line.getDimmingState());
                            }else if(line.getPosition() == 1){
                                jsonObject.put("L_1_D_S", "" + line.getDimmingState());
                            }else if(line.getPosition() == 2){
                                jsonObject.put("L_2_D_S", "" + line.getDimmingState());
                            }
                        }
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "sendDimmingControls POST data: " + jsonObject.toString(), true);

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
                    Utils.log(TAG, "sendDimmingControls response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class PIRResetPairings extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public PIRResetPairings(Activity activity, AddDeviceFragmentSendData fragment) {
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
            if(statusCode == 200) {
                if(MySettings.getTempDevice() != null && MySettings.getTempDevice().getLines() != null && MySettings.getTempDevice().getLines().size() >= 1){
                    PIRAddPairings pirAddPairings = new PIRAddPairings(activity, fragment);
                    pirAddPairings.execute();
                }else{
                    WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(activity, fragment);
                    wiFiDataSenderGet.execute();
                }
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
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

                    URL url = new URL(urlString);
                    Utils.log(TAG, "resetPairings URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("U_P_STT", "0");
                    jsonObject.put("U_P_CID", "0");

                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "resetPairings POST data: " + jsonObject.toString(), true);

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
                    Utils.log(TAG, "resetPairings response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class PIRAddPairings extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public PIRAddPairings(Activity activity, AddDeviceFragmentSendData fragment) {
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
            if(statusCode == 200) {
                WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(activity, fragment);
                wiFiDataSenderGet.execute();
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
                fragment.goToSearchFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            if(MySettings.getTempDevice() != null && MySettings.getTempDevice().getLines() != null && MySettings.getTempDevice().getLines().size() >= 1){
                for (Line line:MySettings.getTempDevice().getLines()) {
                    statusCode = 0;
                    int numberOfRetries = 0;
                    while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                        try{
                            String urlString = Constants.DEVICE_URL + Constants.DEVICE_STATUS_CONTROL_URL;

                            URL url = new URL(urlString);
                            Utils.log(TAG, "addPairing URL: " + url, true);

                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                            urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                            urlConnection.setDoOutput(true);
                            urlConnection.setDoInput(true);
                            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            urlConnection.setRequestProperty("Accept", "application/json");
                            urlConnection.setRequestMethod("POST");

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("U_P_STT", "1");
                            /*jsonObject.put("U_P_CID", MySettings.getDeviceByID2(line.getDeviceID()).getChipID());
                            jsonObject.put("U_P_CIP", MySettings.getDeviceByID2(line.getDeviceID()).getIpAddress());*/
                            jsonObject.put("U_P_CID", line.getPrimaryDeviceChipID());
                            jsonObject.put("U_P_CIP", MySettings.getDeviceByChipID2(line.getPrimaryDeviceChipID()).getIpAddress());
                            jsonObject.put("U_P_LNO", ""+line.getPosition());
                            jsonObject.put("U_P_TYP", ""+MySettings.getDeviceByChipID2(line.getPrimaryDeviceChipID()).getDeviceTypeID());
                            if(line.getPirPowerState() == Line.LINE_STATE_ON){
                                if(line.getPirDimmingValue() == 10){
                                    jsonObject.put("U_P_LVN", ":");
                                }else{
                                    jsonObject.put("U_P_LVN", ""+line.getPirDimmingValue());
                                }
                                jsonObject.put("U_P_LVF", "0");
                            }else if(line.getPirPowerState() == Line.LINE_STATE_OFF){
                                jsonObject.put("U_P_LVN", "0");
                                jsonObject.put("U_P_LVF", ":");
                            }
                            //TODO send U_P_DUR in SECONDS not MILLISECONDS
                            jsonObject.put("U_P_DUR", "" +  Utils.getTimeUnitMilliseconds(line.getPirTriggerActionDurationTimeUnit(), line.getPirTriggerActionDuration()));

                            jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                            Utils.log(TAG, "addPairing POST data: " + jsonObject.toString(), true);

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
                            Utils.log(TAG, "addPairing response: " + result.toString(), true);
                        }catch (MalformedURLException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }catch (IOException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }catch (JSONException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }finally {
                            if(urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            numberOfRetries++;
                        }
                    }
                }
            }else{
                statusCode = 200;
            }

            return null;
        }
    }

    public static class WiFiDataSenderGet extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public WiFiDataSenderGet(Activity activity, AddDeviceFragmentSendData fragment) {
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
                Device device = MySettings.getTempDevice();
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.textView.setText("Rebooting Device");
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterPost deviceRebooterPost = new DeviceRebooterPost(activity, fragment);
                            deviceRebooterPost.execute();
                        }
                    }, 1000);
                }else{
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }
            }else{
                WiFiDataSenderPost wiFiDataSenderPost = new WiFiDataSenderPost(activity, fragment);
                wiFiDataSenderPost.execute();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    String urlString = Constants.DEVICE_URL + Constants.SEND_SSID_PASSWORD_URL;
                    //?essid=%SSID%&passwd=%PASS%

                    if(MySettings.getHomeNetwork() != null) {
                        urlString = urlString.concat("?").concat(Constants.PARAMETER_SSID_GET_METHOD).concat("=").concat(MySettings.getHomeNetwork().getSsid())
                                .concat("&").concat(Constants.PARAMETER_PASSWORD_GET_METHOD).concat("=").concat(MySettings.getHomeNetwork().getPassword());
                    }

                    URL url = new URL(urlString);
                    Utils.log(TAG, "sendConfigurationToDevice URL: " + url, true);

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
                    Utils.log(TAG, "sendConfigurationToDevice response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class WiFiDataSenderPost extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public WiFiDataSenderPost(Activity activity, AddDeviceFragmentSendData fragment) {
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
                Device device = MySettings.getTempDevice();
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterPost deviceRebooterPost = new DeviceRebooterPost(activity, fragment);
                            deviceRebooterPost.execute();
                        }
                    }, 1000);
                }else{
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
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

                    URL url = new URL(urlString);
                    Utils.log(TAG, "sendConfigurationToDevice URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    if(MySettings.getHomeNetwork() != null){
                        jsonObject.put(Constants.PARAMETER_SSID, MySettings.getHomeNetwork().getSsid());
                        jsonObject.put(Constants.PARAMETER_PASSWORD, MySettings.getHomeNetwork().getPassword());
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "sendConfigurationToDevice POST data: " + jsonObject.toString(), true);

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
                    Utils.log(TAG, "sendConfigurationToDevice response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
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

    public void initMqttClient(Context context, String brokerUrl, String clientId) {
         mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        Log.v(TAG,"Started");
        if(mqttAndroidClient != null){
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.v(TAG,"COMPLETE");
                try {
                    subscribe(mqttAndroidClient,MySettings.getTempDevice(),2);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.v(TAG,"LOST");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                String response = new String(mqttMessage.getPayload());
                int index = s.lastIndexOf("/");
                //Device device = MySettings.getDeviceByChipID2(s.substring(index + 1));
                if (device != null) {
                    if (response != null && response.length() >= 1 && response.contains("UNIT_STATUS")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("UNIT_STATUS")) {
                            //parse received unit status and update relevant device, which has the received chip_id
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if(unitStatus != null && unitStatus.has("U_W_STT")){
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if(wifiStatus != null) {
                                    device.setIpAddress(wifiStatus.getString("R_W_IP_"));
                                   // MySettings.updateDeviceIP(device,wifiStatus.getString("R_W_IP_") );
                                    MySettings.setTempDevice(device);
                                    Log.v(TAG,"Device: "+device.toString()+" IP: "+wifiStatus.getString("R_W_IP_"));
                                    myHandler.removeCallbacksAndMessages(null);
                                    goToSuccessFragment();
                                }

                            }

                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.v(TAG,"DEV COMP");
            }
        });
        }

     /*   try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            Log.v(TAG,"Started 2");
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.v(TAG,"ONSuccess");

                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.v(TAG,"Connection Failure");
                    if(check) {
                        showStaticAlert();
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

    }

    public void mqttConnect()
    {
        try {
            if (mqttAndroidClient != null) {
                IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
                Log.v(TAG, "Started 2");

                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        Log.v(TAG, "ONSuccess");

                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        Log.v(TAG, "Connection Failure");
                            // showStaticAlert();
                           // alertDialog.show();
                            showAlert();

                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(false);
        //mqttConnectOptions.setWill(Constants.MQTT_URL, "I am going offline".getBytes(), 1, false);
        mqttConnectOptions.setUserName(Constants.MQTT_USERNAME);
        mqttConnectOptions.setPassword(Constants.MQTT_PASSWORD.toCharArray());
        return mqttConnectOptions;
    }

    public void subscribe(@NonNull final MqttAndroidClient client, Device device, int qos) throws MqttException {
        final IMqttToken token = client.subscribe(String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()), qos);
        if (token != null) {
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                   Log.v(TAG,"Subscribed Success");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.v(TAG,"Subscribed Failure");
                }
            });
        }
    }



    public static class DeviceRebooterGet extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public DeviceRebooterGet(Activity activity, AddDeviceFragmentSendData fragment) {
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
            if(statusCode==200) {
                if (MainActivity.getInstance() != null) {
                    if (fragment.mListener != null) {
                        fragment.mListener.onStartListening();
                    }
                    if (MySettings.getHomeNetwork() != null) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            fragment.connectToWifiNetwork2(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());
                        } else {
                            fragment.connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

                        }
                        // fragment.getStatusMQTT();
                    }


                }
            }
            else
            {
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
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
                    URL url = new URL(Constants.DEVICE_URL + Constants.DEVICE_REBOOT_URL);
                    Utils.log(TAG, "rebootDevice URL: " + url, true);

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
                    Utils.log(TAG, "rebootDevice response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class DeviceRebooterPost extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        AddDeviceFragmentSendData fragment;

        public DeviceRebooterPost(Activity activity, AddDeviceFragmentSendData fragment) {
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
            if(MainActivity.getInstance() != null){
                if(fragment.mListener != null){
                    fragment.mListener.onStartListening();
                }
                if(MySettings.getHomeNetwork() != null) {
                    fragment.connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());
                }
                fragment.goToSuccessFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    URL url = new URL(Constants.DEVICE_URL + Constants.DEVICE_SOUND_SYSTEM_SHUTDOWN_URL);
                    Utils.log(TAG, "rebootDevice URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_SHUTDOWN_MODE, Constants.PARAMETER_SOUND_CONTROLLER_OPTION_REBOOT);
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "rebootDevice POST data: " + jsonObject.toString(), true);

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "rebootDevice response: " + result.toString(), true);
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }finally {
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