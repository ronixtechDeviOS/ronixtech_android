package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.DeviceAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.SoundDeviceData;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardDevicesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardDevicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardDevicesFragment extends Fragment {
    private static final String TAG = DashboardDevicesFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addFabMenu;
    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;
    RelativeLayout addDeviceLayout;

    static ListView devicesListView;
    static DeviceAdapter deviceAdapter;
    static List<Device> devices;
    TextView devicesListViewLongPressHint;

    Handler listHandler;

    //Stuff for local mode
    Timer timer;
    TimerTask doAsynchronousTask;
    Handler handler;

    //Stuff for remote/MQTT mode
    MqttAndroidClient mqttAndroidClient;

    private boolean isResumed;

    private static Room room;
    public DashboardDevicesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardDevicesFragment.
     */
    public static DashboardDevicesFragment newInstance(String param1, String param2) {
        DashboardDevicesFragment fragment = new DashboardDevicesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_devices, container, false);
        if(room != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                MainActivity.setActionBarTitle(room.getName() + " - " + "Local", getResources().getColor(R.color.whiteColor));
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                MainActivity.setActionBarTitle(room.getName() + " - " + "Remote", getResources().getColor(R.color.whiteColor));
            }
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.app_name), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        listHandler = new Handler();

        addDeviceLayout = view.findViewById(R.id.add_new_device_layout);

        addFabMenu = view.findViewById(R.id.add_fab_menu);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        devicesListViewLongPressHint = view.findViewById(R.id.devices_listview_long_press_hint_textview);

        devicesListView = view.findViewById(R.id.devices_listview);
        devices = DevicesInMemory.getDevices();
        deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), MySettings.getCurrentPlace().getMode());
        devicesListView.setAdapter(deviceAdapter);

        loadDevicesFromDatabase();

        MySettings.setControlState(false);

        setLayoutVisibility();

        //startTimer();

        addPlaceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });
        addRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_place_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddRoomFragment addRoomFragment = new AddRoomFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                    fragmentTransaction.addToBackStack("addRoomFragment");
                    fragmentTransaction.commit();
                }
            }
        });
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }
            }
        });

        addDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
    }

    public MqttAndroidClient getMqttAndroidClient(){
        if(mqttAndroidClient != null){
            return mqttAndroidClient;
        }else{
            return null;
        }
    }

    public void updateUI(){
        if(isResumed) {
            if (room != null) {
                if (MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                    MainActivity.setActionBarTitle(room.getName() + " - " + "Local", getResources().getColor(R.color.whiteColor));
                } else if (MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE) {
                    MainActivity.setActionBarTitle(room.getName() + " - " + "Remote", getResources().getColor(R.color.whiteColor));
                }
            } else {
                MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.app_name), getResources().getColor(R.color.whiteColor));
            }

            deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), MySettings.getCurrentPlace().getMode());
            devicesListView.setAdapter(deviceAdapter);

            loadDevicesFromMemory();


            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                //stop MQTT
                if(mqttAndroidClient != null){
                    try {
                        mqttAndroidClient.disconnect();
                        mqttAndroidClient.unregisterResources();
                        mqttAndroidClient.close();
                    }catch (MqttException e){
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }catch (Exception e){
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                //startTimer
                Log.d(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to LOCAL mode");
                startTimer();
                for (Device device:devices) {
                    device.setDeviceMQTTReachable(false);
                }
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                //stopTimer
                stopTimer();
                //start MQTT in onStart
                Log.d(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to REMOTE mode, using MQTT");
                //start MQTT, when a control is sent from the DeviceAdapter, it will be synced here when the MQTT responds
                if(mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
                    String clientId = MqttClient.generateClientId();
                    getMqttClient(getActivity(), Constants.MQTT_URL + ":" + Constants.MQTT_PORT, clientId);
                }else{
                    Log.d(TAG, "MQTT is already connected");
                }
            }
        }
    }

    private void startTimer(){
        if(timer == null){
            timer = new Timer();
            handler = new Handler();
            doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            //MySettings.scanDevices();
                            if(DevicesInMemory.getDevices() != null && DevicesInMemory.getDevices().size() >= 1){
                                boolean allDevicesReachable = true;
                                for (Device dev : DevicesInMemory.getDevices()) {
                                    if(dev.getIpAddress() != null && dev.getIpAddress().length() >= 1) {
                                        if(!MySettings.isControlActive()) {
                                            getDeviceInfo(dev);
                                        }else{
                                            Log.d(TAG, "Controls active, skipping get_status");
                                        }
                                    }else{
                                        MySettings.scanNetwork();
                                        allDevicesReachable = false;
                                    }
                                }
                                if(allDevicesReachable){
                                    Utils.hideUpdatingNotification();
                                }
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsynchronousTask, 0, Device.REFRESH_RATE_MS); //execute in every REFRESH_RATE_MS
        }
    }

    private void stopTimer(){
        if(doAsynchronousTask != null) {
            doAsynchronousTask.cancel();
        }
        if(timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = null;
    }

    public void setRoom(Room room){
        this.room = room;
    }

    private void setLayoutVisibility(){
        boolean showAddDeviceLayout = false;

        if(room != null){
            if(MySettings.getRoomDevices(room.getId()) == null || MySettings.getRoomDevices(room.getId()).size() < 1){
                showAddDeviceLayout = true;
            }
        }

        if(showAddDeviceLayout){
            addDeviceLayout.setVisibility(View.VISIBLE);
        }else{
            addDeviceLayout.setVisibility(View.GONE);
        }

        if(showAddDeviceLayout){
            addFabMenu.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            devicesListViewLongPressHint.setVisibility(View.GONE);
        }else{
            addFabMenu.setVisibility(View.VISIBLE);
            devicesListView.setVisibility(View.VISIBLE);
            //devicesListViewLongPressHint.setVisibility(View.VISIBLE);
            devicesListViewLongPressHint.setVisibility(View.GONE);
        }
    }

    public void loadDevicesFromDatabase(){
        if(room != null){
            if(listHandler != null) {
                listHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Device> tempDevices = new ArrayList<>();
                        if (MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1) {
                            tempDevices.addAll(MySettings.getRoomDevices(room.getId()));
                        }
                        DevicesInMemory.setDevices(tempDevices);
                        DevicesInMemory.setLocalDevices(tempDevices);
                        putDevicesIntoListView();
                    }
                });
            }
        }
    }

    public void loadDevicesFromMemory(){
        if(listHandler != null) {
            listHandler.post(new Runnable() {
                @Override
                public void run() {
                    putDevicesIntoListView();
                }
            });
        }
    }

    private void putDevicesIntoListView(){
        if(devices != null) {
            //devices.clear();
            if (DevicesInMemory.getDevices() != null && DevicesInMemory.getDevices().size() >= 1) {
                /*List<Device> tempDevices = new ArrayList<>();
                tempDevices.addAll(DevicesInMemory.getDevices());
                for (Device device:tempDevices) {
                    if(!devices.contains(device)) {
                        devices.add(device);
                    }
                }*/
                //devices.addAll(DevicesInMemory.getDevices());
                setLayoutVisibility();
            } else {
                setLayoutVisibility();
            }
            /*if(devices.size() >= 1) {
                Collections.sort(devices);
            }*/
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void getDeviceInfo(Device device){
        Log.d(TAG, "Getting device info...");
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
                if(currentFirmwareVersion  <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    StatusGetter statusGetter = new StatusGetter(device);
                    statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    DeviceSyncer deviceSyncer = new DeviceSyncer(device);
                    deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }else{
                StatusGetter statusGetter = new StatusGetter(device);
                statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            ModeGetter modeGetter = new ModeGetter(device);
            modeGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            DeviceSyncer deviceSyncer = new DeviceSyncer(device);
            deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        /*//volley request to device to get its status
        String url = "http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS;

        Log.d(TAG,  "getDeviceStatus URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDeviceStatus response: " + response);

                HttpConnectorDeviceStatus.getInstance(getActivity()).getRequestQueue().cancelAll("getStatusRequest");
                DataParser dataParser = new DataParser(device, response);
                dataParser.execute();

                //device.setErrorCount(0);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());

                HttpConnectorDeviceStatus.getInstance(getActivity()).getRequestQueue().cancelAll("getStatusRequest");

                *//*MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    MySettings.updateDeviceIP(device, "");
                    MySettings.updateDeviceErrorCount(device, 0);
                    MySettings.scanNetwork();
                }*//*
            }
        });
        request.setTag("getStatusRequest");
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.REFRESH_TIMEOUT, Device.REFRESH_NUMBER_OF_RETRIES, 0f));
        HttpConnectorDeviceStatus.getInstance(MainActivity.getInstance()).addToRequestQueue(request);*/
    }

    private void removeDevice(Device device){
        devices.remove(device);
        MySettings.removeDevice(device);
        loadDevicesFromDatabase();
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        super.onResume();
        isResumed = true;
        loadDevicesFromDatabase();
        if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
            Log.d(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to LOCAL mode");
            startTimer();
            for (Device device:devices) {
                device.setDeviceMQTTReachable(false);
            }
        }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
            //start MQTT in onStart
        }
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        isResumed = false;
        if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
            stopTimer();
        }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
            //stop MQTT in onDestroy
        }

        super.onPause();
        /*for (Device device:devices) {
            device.setDeviceMQTTReachable(false);
        }*/
        for (Device device:devices) {
            MySettings.addDevice(device);
        }
    }

    @Override
    public void onStart(){
        Log.d(TAG, "onStart");
        super.onStart();
        if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
            //startTimer in onResume
        }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
            Log.d(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to REMOTE mode, using MQTT");
            //start MQTT, when a control is sent from the DeviceAdapter, it will be synced here when the MQTT responds
            if(mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
                String clientId = MqttClient.generateClientId();
                getMqttClient(getActivity(), Constants.MQTT_URL + ":" + Constants.MQTT_PORT, clientId);
            }else{
                Log.d(TAG, "MQTT is already connected");
            }
        }
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        //stop MQTT
        if(mqttAndroidClient != null){
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
            }catch (MqttException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (Exception e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
        for (Device device:devices) {
            device.setDeviceMQTTReachable(false);
        }
        for (Device device:devices) {
            MySettings.addDevice(device);
        }
        super.onDestroy();
    }

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //FIXED: setUserVisibleHint() called before onCreateView() in Fragment causes NullPointerException
        //super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            startTimer();
        }else{
            stopTimer();
        }
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        /*if (v.getId()==R.id.devices_listview) {
         *//*MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_device, menu);*//*

            menu.add("One");
            menu.add("Two");
            menu.add("Three");
        }*/
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        /*switch(item.getItemId()) {
            case R.id.action_0:
                // add stuff here
                Toast.makeText(getActivity(), "action 0", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_1:
                // add stuff here
                Toast.makeText(getActivity(), "action 1", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_2:
                // add stuff here
                Toast.makeText(getActivity(), "action 2", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }*/
        return super.onContextItemSelected(item);
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

    public void getMqttClient(Context context, String brokerUrl, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        /*mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.d(TAG, "Connection lost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.d(TAG, "Message arrived: " + mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.d(TAG, "Delivery complete");
            }
        });*/
        if(mqttAndroidClient != null){
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    Log.d(TAG, "MQTT connectComplete on " + s);
                }
                @Override
                public void connectionLost(Throwable throwable) {
                    Log.d(TAG, "MQTT connectionLost");
                    for (Device device:devices) {
                        device.setDeviceMQTTReachable(false);
                    }
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    //setMessageNotification(s, new String(mqttMessage.getPayload()));
                    Log.d(TAG, "MQTT messageArrived: 'topic': " + s);
                    Log.d(TAG, "MQTT messageArrived: 'mqttMessage': " + new String(mqttMessage.getPayload()));
                    //make sure it's the 'status' topic, not the 'control' topic
                    if(s.contains("status")){
                        /*if(MySettings.isGetStatusActive()){
                           return;
                        }*/
                        if (MySettings.isControlActive()){
                            Log.d(TAG, "Controls active, do nothing");
                            return;
                        }
                        MySettings.setGetStatusState(true);
                        String response = new String(mqttMessage.getPayload());
                        int index = s.lastIndexOf("/");
                        Device device = DevicesInMemory.getDeviceByChipID(s.substring(index+1));
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
                                                        Log.d(TAG, "MQTT Publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                                                        Log.d(TAG, "MQTT Publish data: " + mqttMessage1);
                                                        mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage1);
                                                        device.setDeviceMQTTReachable(true);
                                                    }catch (JSONException e){
                                                        Log.d(TAG, "Exception: " + e.getMessage());
                                                    }catch (MqttException e){
                                                        Log.d(TAG, "Exception: " + e.getMessage());
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
                            DevicesInMemory.updateDevice(device);
                            if (MainActivity.getInstance() != null) {
                                MainActivity.getInstance().refreshDevicesListFromMemory();
                            }
                        }
                        MySettings.setGetStatusState(false);
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Log.d(TAG, "MQTT deliveryComplete");
                }
            });
            try {
                IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
                if(token != null){
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                            Log.d(TAG, "MQTT connect onSuccess");
                            try {
                                for (Device device:devices) {
                                    subscribe(mqttAndroidClient, device, 1);
                                }
                            }catch (MqttException e){
                                Log.d(TAG, "Exception " + e.getMessage());
                                for (Device device:devices) {
                                    device.setDeviceMQTTReachable(false);
                                }
                                MainActivity.getInstance().refreshDevicesListFromMemory();
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d(TAG, "MQTT connect onFailure: " + exception.toString());
                            for (Device device:devices) {
                                device.setDeviceMQTTReachable(false);
                            }
                            MainActivity.getInstance().refreshDevicesListFromMemory();
                        }
                    });
                }
            } catch (MqttException e) {
                e.printStackTrace();
                for (Device device:devices) {
                    device.setDeviceMQTTReachable(false);
                }
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
        }
    }

    public void subscribe(@NonNull final MqttAndroidClient client, Device device, int qos) throws MqttException {
        final IMqttToken token = client.subscribe(String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()), qos);
        if(token != null){
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "MQTT subscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()));
                    device.setDeviceMQTTReachable(false);
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT subscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_STATUS, device.getChipID()));
                    device.setDeviceMQTTReachable(false);
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
            });
        }

        final IMqttToken token2 = client.subscribe(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), qos);
        if(token2 != null){
            token2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "MQTT subscribe onSuccess: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                        jsonObject.put("R_M_ALV", "1");
                        MqttMessage mqttMessage = new MqttMessage();
                        mqttMessage.setPayload(jsonObject.toString().getBytes());
                        Log.d(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                        Log.d(TAG, "MQTT publish data: " + mqttMessage);
                        mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                    }catch (JSONException e){
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }catch (MqttException e){
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "MQTT subscribe onFailure: on " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
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

    public static class StatusGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.StatusGetter.class.getSimpleName();

        Device device;

        int statusCode;
        boolean ronixUnit = true;

        public StatusGetter(Device device) {
            try{
                this.device = device;
            }catch (Exception e){
                Log.d(TAG, "Json exception " + e.getMessage());
            }
        }

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "Enabling getStatus flag...");
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS);
                Log.d(TAG,  "statusGetter URL: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                statusCode = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }
                urlConnection.disconnect();
                Log.d(TAG,  "statusGetter response: " + result.toString());
                if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                    ronixUnit = true;
                }else{
                    ronixUnit = false;
                }
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                            if(wifiStatus != null) {
                                if(wifiStatus.has("U_W_UID")) {
                                    String chipID = wifiStatus.getString("U_W_UID");
                                    if (device.getChipID().length() >= 1) {
                                        if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                            MySettings.updateDeviceIP(device, "");
                                            MySettings.updateDeviceErrorCount(device, 0);
                                            MySettings.scanNetwork();
                                            MainActivity.getInstance().refreshDeviceListFromDatabase();
                                            return null;
                                        }
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
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

                            if(statusCode == 200) {
                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                device.setErrorCount(0);
                                DevicesInMemory.updateDevice(device);
                            }
                            //MySettings.addDevice(device);
                        }
                    }
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                if(!ronixUnit){
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }else{
                    device.setFirmwareUpdateAvailable(true);
                    DevicesInMemory.updateDevice(device);
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static class DeviceSyncer extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.DeviceSyncer.class.getSimpleName();

        Device device;

        int statusCode;
        boolean ronixUnit = true;

        public DeviceSyncer(Device device) {
            this.device = device;
        }

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "Enabling getStatus flag...");
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                String urlString = "http://" + device.getIpAddress() + "/ronix/json/post";

                //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                Log.d(TAG,  "statusGetter URL: " + urlString);

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");

                JSONObject jObject = new JSONObject();
                Device localDevice = DevicesInMemory.getLocalDevice(device);
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                    for (Line line : device.getLines()) {
                        switch (line.getPosition()){
                            case 0:
                                if(line.getPowerState() != localDevice.getLines().get(0).getPowerState()){
                                    //jObject.put("L_0_STT", ""+localDevice.getLines().get(0).getPowerState());
                                    if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_ON){
                                        jObject.put("L_0_DIM", ":");
                                    }else if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                                        jObject.put("L_0_DIM", "0");
                                    }
                                }
                                if(line.getDimmingState() != localDevice.getLines().get(0).getDimmingState()) {
                                    jObject.put("L_0_D_S", "" + localDevice.getLines().get(0).getDimmingState());
                                }
                                if(line.getDimmingVvalue() != localDevice.getLines().get(0).getDimmingVvalue()){
                                    if(localDevice.getLines().get(0).getDimmingVvalue() == 10){
                                        jObject.put("L_0_DIM", ":");
                                    }else{
                                        jObject.put("L_0_DIM", ""+localDevice.getLines().get(0).getDimmingVvalue());
                                    }
                                }
                                break;
                            case 1:
                                if(line.getPowerState() != localDevice.getLines().get(1).getPowerState()){
                                    //jObject.put("L_1_STT", ""+localDevice.getLines().get(1).getPowerState());
                                    if(localDevice.getLines().get(1).getPowerState() == Line.LINE_STATE_ON){
                                        jObject.put("L_1_DIM", ":");
                                    }else if(localDevice.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF){
                                        jObject.put("L_1_DIM", "0");
                                    }
                                }
                                if(line.getDimmingState() != localDevice.getLines().get(1).getDimmingState()) {
                                    jObject.put("L_1_D_S", "" + localDevice.getLines().get(1).getDimmingState());
                                }
                                if(line.getDimmingVvalue() != localDevice.getLines().get(1).getDimmingVvalue()){
                                    if(localDevice.getLines().get(1).getDimmingVvalue() == 10){
                                        jObject.put("L_1_DIM", ":");
                                    }else{
                                        jObject.put("L_1_DIM", ""+localDevice.getLines().get(1).getDimmingVvalue());
                                    }
                                }
                                break;
                            case 2:
                                if(line.getPowerState() != localDevice.getLines().get(2).getPowerState()){
                                    //jObject.put("L_2_STT", ""+localDevice.getLines().get(2).getPowerState());
                                    if(localDevice.getLines().get(2).getPowerState() == Line.LINE_STATE_ON){
                                        jObject.put("L_2_DIM", ":");
                                    }else if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                                        jObject.put("L_2_DIM", "0");
                                    }
                                }
                                if(line.getDimmingState() != localDevice.getLines().get(2).getDimmingState()) {
                                    jObject.put("L_2_D_S", "" + localDevice.getLines().get(2).getDimmingState());
                                }
                                if(line.getDimmingVvalue() != localDevice.getLines().get(2).getDimmingVvalue()){
                                    if(localDevice.getLines().get(2).getDimmingVvalue() == 10){
                                        jObject.put("L_2_DIM", ":");
                                    }else{
                                        jObject.put("L_2_DIM", ""+localDevice.getLines().get(2).getDimmingVvalue());
                                    }
                                }
                                break;
                        }
                    }
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                    for (Line line : device.getLines()) {
                        switch (line.getPosition()){
                            case 0:
                                if(line.getPowerState() != localDevice.getLines().get(0).getPowerState()){
                                    jObject.put("L_0_STT", ""+localDevice.getLines().get(0).getPowerState());
                                }
                                break;
                            case 1:
                                if(line.getPowerState() != localDevice.getLines().get(1).getPowerState()){
                                    jObject.put("L_1_STT", ""+localDevice.getLines().get(1).getPowerState());
                                }
                                break;
                            case 2:
                                if(line.getPowerState() != localDevice.getLines().get(2).getPowerState()){
                                    jObject.put("L_2_STT", ""+localDevice.getLines().get(2).getPowerState());
                                }
                                break;
                        }
                    }
                }

                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                    int currentFirmwareVersion = Integer.parseInt(device.getFirmwareVersion());
                    if(currentFirmwareVersion >= Device.DEVICE_FIRMWARE_DHCP_FIRMWARE){
                        if(device.isStaticIPAddress() && !device.isStaticIPSyncedState()){
                            WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                            jObject.put("R_W_DHC", "off");
                            jObject.put("R_W_IP_", device.getIpAddress());
                            jObject.put("R_W_GWY", Utils.intToIp(dhcpInfo.gateway));
                            if(Utils.intToIp(dhcpInfo.netmask) == null || Utils.intToIp(dhcpInfo.netmask).length() < 1 || Utils.intToIp(dhcpInfo.netmask).equalsIgnoreCase("0.0.0.0")){
                                try {
                                    InetAddress inetAddress = Utils.intToInet(dhcpInfo.ipAddress);
                                    NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                                    if(networkInterface != null){
                                        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                                            String submask = Utils.prefixToSubmask(address.getNetworkPrefixLength());
                                            Log.d("AAAA", address.toString());
                                            jObject.put("R_W_NMK", submask);
                                        }
                                    }else{
                                        jObject.put("R_W_NMK", "255.255.255.0");
                                    }

                                } catch (IOException e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                    jObject.put("R_W_NMK", "255.255.255.0");
                                }
                            }else{
                                jObject.put("R_W_NMK", Utils.intToIp(dhcpInfo.netmask));
                            }
                        }
                    }
                }

                Log.d(TAG,  "statusGetter POST data: " + jObject.toString());


                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jObject.toString());
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
                Log.d(TAG,  "statusGetter response: " + result.toString());
                if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                    ronixUnit = true;
                }else{
                    ronixUnit = false;
                }
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                            if(wifiStatus != null) {
                                if(wifiStatus.has("U_W_UID")) {
                                    String chipID = wifiStatus.getString("U_W_UID");
                                    if (device.getChipID().length() >= 1) {
                                        if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                            MySettings.updateDeviceIP(device, "");
                                            MySettings.updateDeviceErrorCount(device, 0);
                                            MySettings.scanNetwork();
                                            MainActivity.getInstance().refreshDeviceListFromDatabase();
                                            return null;
                                        }
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
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

                                if(wifiStatus.has("R_W_DHC")){
                                    String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                    if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                        device.setStaticIPSyncedState(true);
                                    }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                        device.setStaticIPSyncedState(true);
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }
                                }else{
                                    device.setStaticIPSyncedState(false);
                                }

                                if(wifiStatus.has("R_W_IP_")){
                                    String ipAddress = wifiStatus.getString("R_W_IP_");
                                    if(ipAddress != null && ipAddress.length() >= 1){
                                        device.setIpAddress(ipAddress);
                                    }
                                }

                                if(wifiStatus.has("R_W_GWY")){
                                    String getway = wifiStatus.getString("R_W_GWY");
                                    if(getway != null && getway.length() >= 1){
                                        device.setGateway(getway);
                                    }
                                }

                                if(wifiStatus.has("R_W_NMK")){
                                    String subnetmask = wifiStatus.getString("R_W_NMK");
                                    if(subnetmask != null && subnetmask.length() >= 1){
                                        device.setSubnetMask(subnetmask);
                                    }
                                }
                            }
                        }else{
                            device.setFirmwareUpdateAvailable(true);
                        }

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
                                List<Line> localLines = localDevice.getLines();
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
                                for (Line line:localLines) {
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
                                boolean beep = true, hwLock = false;
                                temperatureString = hardwareStatus.getString("U_H_TMP");
                                beepString = hardwareStatus.getString("U_BEEP_");
                                hwLockString = hardwareStatus.getString("U_H_LCK");

                                temperatureValue = Integer.parseInt(temperatureString);
                                if(beepString != null && beepString.length() >= 1){
                                    if(Integer.parseInt(beepString) == 1){
                                        beep = true;
                                    }else{
                                        beep = false;
                                    }
                                }
                                if(hwLockString != null && hwLockString.length() >= 1){
                                    if(Integer.parseInt(hwLockString) == 1){
                                        hwLock = true;
                                    }else{
                                        hwLock = false;
                                    }
                                }

                                device.setTemperature(temperatureValue);
                                device.setBeep(beep);
                                device.setHwLock(hwLock);

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                    DevicesInMemory.updateLocalDevice(localDevice);
                                }
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
                                List<Line> localLines = localDevice.getLines();
                                for (Line line:lines) {
                                    if(line.getPosition() == 0){
                                        line.setPowerState(line0PowerState);
                                    }else if(line.getPosition() == 1){
                                        line.setPowerState(line1PowerState);
                                    }else if(line.getPosition() == 2){
                                        line.setPowerState(line2PowerState);
                                    }
                                }
                                for (Line line:localLines) {
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

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                    DevicesInMemory.updateLocalDevice(localDevice);
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
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


                                String pirStateString;
                                int pirState = 0;
                                pirStateString = hardwareStatus.getString("L_0_STT");
                                pirState = Integer.valueOf(pirStateString);

                                PIRData pirData = device.getPIRData();
                                pirData.setState(pirState);

                                device.setPIRData(pirData);

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }
                    }
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                if(!ronixUnit){
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }else {
                    device.setFirmwareUpdateAvailable(true);
                    DevicesInMemory.updateDevice(device);
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static class ModeGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.ModeGetter.class.getSimpleName();

        Device device;

        int statusCode;

        public ModeGetter(Device device) {
            this.device = device;
        }

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "Enabling getStatus flag...");
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.CONTROL_SOUND_DEVICE_CHANGE_MODE_URL);
                Log.d(TAG,  "modeGetter URL: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");

                JSONObject jObject = new JSONObject();
                jObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, "");
                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                if(device.isStaticIPAddress() && !device.isStaticIPSyncedState()){
                    WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                    jObject.put("R_W_DHC", "off");
                    jObject.put("R_W_IP_", device.getIpAddress());
                    jObject.put("R_W_GWY", Utils.intToIp(dhcpInfo.gateway));
                    if(Utils.intToIp(dhcpInfo.netmask) == null || Utils.intToIp(dhcpInfo.netmask).length() < 1 || Utils.intToIp(dhcpInfo.netmask).equalsIgnoreCase("0.0.0.0")){
                        try {
                            InetAddress inetAddress = Utils.intToInet(dhcpInfo.ipAddress);
                            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                            if(networkInterface != null){
                                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                                    String submask = Utils.prefixToSubmask(address.getNetworkPrefixLength());
                                    Log.d("AAAA", address.toString());
                                    jObject.put("R_W_NMK", submask);
                                }
                            }else{
                                jObject.put("R_W_NMK", "255.255.255.0");
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Exception: " + e.getMessage());
                            jObject.put("R_W_NMK", "255.255.255.0");
                        }
                    }else{
                        jObject.put("R_W_NMK", Utils.intToIp(dhcpInfo.netmask));
                    }
                }

                Log.d(TAG,  "modeGetter POST data: " + jObject.toString());

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jObject.toString());
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
                Log.d(TAG,  "modeGetter response: " + result.toString());
                if(result.length() >= 3){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        String modeString = jsonObject.getString("mode");

                        int mode = SoundDeviceData.MODE_LINE_IN;

                        if(modeString != null && modeString.length() >= 1){
                            if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN)){
                                mode = SoundDeviceData.MODE_LINE_IN;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN_2)){
                                mode = SoundDeviceData.MODE_LINE_IN_2;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_UPNP)){
                                mode = SoundDeviceData.MODE_UPNP;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_USB)){
                                mode = SoundDeviceData.MODE_USB;
                            }
                        }

                        device.getSoundDeviceData().setMode(mode);

                        if(jsonObject.has("R_W_DHC")){
                            String dhcpStatus = jsonObject.getString("R_W_DHC");
                            if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                device.setStaticIPSyncedState(true);
                            }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                device.setStaticIPSyncedState(true);
                            }else{
                                device.setStaticIPSyncedState(false);
                            }
                        }else{
                            device.setStaticIPSyncedState(false);
                        }

                        if(jsonObject.has("R_W_IP_")){
                            String ipAddress = jsonObject.getString("R_W_IP_");
                            if(ipAddress != null && ipAddress.length() >= 1){
                                device.setIpAddress(ipAddress);
                            }
                        }

                        if(jsonObject.has("R_W_GWY")){
                            String getway = jsonObject.getString("R_W_GWY");
                            if(getway != null && getway.length() >= 1){
                                device.setGateway(getway);
                            }
                        }

                        if(jsonObject.has("R_W_NMK")){
                            String subnetmask = jsonObject.getString("R_W_NMK");
                            if(subnetmask != null && subnetmask.length() >= 1){
                                device.setSubnetMask(subnetmask);
                            }
                        }

                        if(statusCode == 200) {
                            device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                            device.setErrorCount(0);
                            DevicesInMemory.updateDevice(device);
                        }
                        //MySettings.addDevice(device);
                    }
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

}