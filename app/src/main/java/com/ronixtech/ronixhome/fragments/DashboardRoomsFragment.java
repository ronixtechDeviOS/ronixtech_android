package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsDashboardListAdapter;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.WifiNetwork;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardRoomsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardRoomsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DashboardRoomsFragment extends Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener{
    private static final String TAG = DashboardRoomsFragment.class.getSimpleName();

    FloatingActionMenu addFabMenu;
    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;
    RelativeLayout addPlaceLayout, addRoomLayout, addDeviceLayout;

    private static final int LIST_VIEW = 0;
    private static final int GRID_VIEW = 2;
    private static int lastSelectedView = GRID_VIEW;

    GridView roomsGridView;
    RoomsGridAdapter roomsGridAdapter;
    List<Room> rooms;
    TextView roomsGridViewLongPressHint;

    ListView roomsListView;
    RoomsDashboardListAdapter roomsDashboardListAdapter;

    RelativeLayout roomsHeaderLayout;
    ImageView showAsGrid, showAsList, sortImageView;

    private Place place;
    private Floor floor;

    private boolean showPlaceArrow = false;

    private OnFragmentInteractionListener mListener;

    //Stuff for remote/MQTT mode
    MqttAndroidClient mqttAndroidClient;

    List<Device> placeDevices;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardRoomsFragment.
     */
    public static DashboardRoomsFragment newInstance(String param1, String param2) {
        DashboardRoomsFragment fragment = new DashboardRoomsFragment();
        return fragment;
    }
    public DashboardRoomsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_rooms, container, false);
        place = MySettings.getCurrentPlace();
        floor = MySettings.getCurrentFloor();
        if(place != null){
            if(MySettings.getPlaceFloors(place.getId()).size() == 1) {
                MainActivity.setActionBarTitle(place.getName(), getResources().getColor(R.color.whiteColor));
            }else{
                if(floor != null){
                    MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName(), getResources().getColor(R.color.whiteColor));
                }else{
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms), getResources().getColor(R.color.whiteColor));
                }
            }
            showPlaceArrow = true;
        }else{
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.dashboard), getResources().getColor(R.color.whiteColor));
            showPlaceArrow = false;
        }
        setHasOptionsMenu(true);

        addPlaceLayout = view.findViewById(R.id.add_new_place_layout);
        addRoomLayout = view.findViewById(R.id.add_new_room_layout);
        addDeviceLayout = view.findViewById(R.id.add_new_device_layout);

        addFabMenu = view.findViewById(R.id.add_fab_menu);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        roomsGridViewLongPressHint = view.findViewById(R.id.rooms_gridview_long_press_hint_textview);

        if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
            List<Device> devices = MySettings.getAllDevices();
            if(devices != null && devices.size() >= 1){
                for (Device device:devices) {
                    if(device.getRoomID() == -1){
                        MySettings.removeDevice(device);
                    }
                }
            }
        }

        roomsHeaderLayout = view.findViewById(R.id.rooms_header_layout);
        showAsGrid = view.findViewById(R.id.gridview_imageview);
        showAsList = view.findViewById(R.id.listview_imageview);
        sortImageView = view.findViewById(R.id.sort_imageview);

        rooms = new ArrayList<>();
        if(place != null){
            if(floor != null){
                if(MySettings.getFloorRooms(floor.getId()) != null && MySettings.getFloorRooms(floor.getId()).size() >= 1) {
                    rooms.addAll(MySettings.getFloorRooms(floor.getId()));
                }
            }else {
                if(MySettings.getPlaceRooms(place) != null && MySettings.getPlaceRooms(place).size() >= 1){
                    rooms.addAll(MySettings.getPlaceRooms(place));
                }
            }
        }
        roomsGridView = view.findViewById(R.id.rooms_gridview);
        roomsGridAdapter = new RoomsGridAdapter(getActivity(), rooms, getFragmentManager(), new RoomsGridAdapter.RoomsListener() {
            @Override
            public void onRoomDeleted() {
                MySettings.setCurrentRoom(null);
                rooms.clear();
                if(place != null){
                    if(floor != null && MySettings.getFloorRooms(floor.getId()) != null && MySettings.getFloorRooms(floor.getId()).size() >= 1){
                        rooms.addAll(MySettings.getFloorRooms(floor.getId()));
                    }else{
                        if(MySettings.getPlaceRooms(place) != null && MySettings.getPlaceRooms(place).size() >= 1){
                            rooms.addAll(MySettings.getPlaceRooms(place));
                        }
                    }
                }
                roomsGridAdapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
            @Override
            public void onRoomNameChanged() {

            }
            @Override
            public void onRoomDevicesToggled(Room room, int newState) {
                if(MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1){
                    List<Device> roomDevices = new ArrayList<>();
                    roomDevices.addAll(MySettings.getRoomDevices(room.getId()));
                    for (Device device : roomDevices){
                        toggleRoomDevice(device, newState);
                    }
                }
            }
        });
        roomsGridView.setAdapter(roomsGridAdapter);

        roomsListView = view.findViewById(R.id.rooms_listview);
        roomsDashboardListAdapter = new RoomsDashboardListAdapter(getActivity(), rooms, getFragmentManager(), new RoomsDashboardListAdapter.RoomsListener() {
            @Override
            public void onRoomDeleted() {
                MySettings.setCurrentRoom(null);
                rooms.clear();
                if(place != null){
                    if(floor != null && MySettings.getFloorRooms(floor.getId()) != null && MySettings.getFloorRooms(floor.getId()).size() >= 1){
                        rooms.addAll(MySettings.getFloorRooms(floor.getId()));
                    }else{
                        if(MySettings.getPlaceRooms(place) != null && MySettings.getPlaceRooms(place).size() >= 1){
                            rooms.addAll(MySettings.getPlaceRooms(place));
                        }
                    }
                }
                roomsDashboardListAdapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
            @Override
            public void onRoomNameChanged() {

            }
            @Override
            public void onRoomDevicesToggled(Room room, int newState) {
                if(MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1){
                    List<Device> roomDevices = new ArrayList<>();
                    roomDevices.addAll(MySettings.getRoomDevices(room.getId()));
                    for (Device device : roomDevices){
                        toggleRoomDevice(device, newState);
                    }
                }
            }
        });
        roomsListView.setAdapter(roomsDashboardListAdapter);

        setLayoutVisibility();

        placeDevices = new ArrayList<>();
        if(MySettings.getPlaceDevices(MySettings.getCurrentPlace()) != null && MySettings.getPlaceDevices(MySettings.getCurrentPlace()).size() >= 1){
            placeDevices.addAll(MySettings.getPlaceDevices(MySettings.getCurrentPlace()));
        }

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
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddRoomFragment addRoomFragment = new AddRoomFragment();
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                }else {
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

        addPlaceLayout.setOnClickListener(new View.OnClickListener() {
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
        addRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_place_first), true);
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
        addDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
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

        showAsGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsGridView.setVisibility(View.VISIBLE);
                roomsListView.setVisibility(View.INVISIBLE);
                lastSelectedView = GRID_VIEW;
            }
        });
        showAsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsGridView.setVisibility(View.INVISIBLE);
                roomsListView.setVisibility(View.VISIBLE);
                lastSelectedView = LIST_VIEW;
            }
        });

        checkWifiConnection();
        checkCellularConnection();

        return view;
    }

    private void toggleRoomDevice(Device device, int newState){
        if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
            DeviceToggler deviceToggler = new DeviceToggler(device, newState);
            deviceToggler.execute();
        }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
            //send command usint MQTT
            if(mqttAndroidClient != null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    for (Line line : device.getLines()){
                        int position = line.getPosition();
                        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                            if(newState == Line.LINE_STATE_ON){
                                switch(position){
                                    case 0:
                                        jsonObject.put("L_0_DIM", ":");
                                        break;
                                    case 1:
                                        jsonObject.put("L_1_DIM", ":");
                                        break;
                                    case 2:
                                        jsonObject.put("L_2_DIM", ":");
                                        break;
                                }
                            }else if(newState == Line.LINE_STATE_OFF){
                                switch (position){
                                    case 0:
                                        jsonObject.put("L_0_DIM", "0");
                                        break;
                                    case 1:
                                        jsonObject.put("L_1_DIM", "0");
                                        break;
                                    case 2:
                                        jsonObject.put("L_2_DIM", "0");
                                        break;
                                }
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                            if(newState == Line.LINE_STATE_ON){
                                switch(position){
                                    case 0:
                                        jsonObject.put("L_0_STT", 1);
                                        break;
                                    case 1:
                                        jsonObject.put("L_1_STT", 1);
                                        break;
                                    case 2:
                                        jsonObject.put("L_2_STT", 1);
                                        break;
                                }
                            }else if(newState == Line.LINE_STATE_OFF){
                                switch(position){
                                    case 0:
                                        jsonObject.put("L_0_STT", 0);
                                        break;
                                    case 1:
                                        jsonObject.put("L_1_STT", 0);
                                        break;
                                    case 2:
                                        jsonObject.put("L_2_STT", 0);
                                        break;
                                }
                            }
                        }
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
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
            }else{
                Log.d(TAG, "mqttAndroidClient is null");
            }
            MySettings.setControlState(false);
        }
    }

    private void checkWifiConnection(){
        List<Place> allPlaces = MySettings.getAllPlaces();
        for (Place place : allPlaces) {
            place.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.updatePlaceMode(place, Place.PLACE_MODE_REMOTE);
        }
        if(MySettings.getCurrentPlace() != null ){
            Place currentPlace = MySettings.getCurrentPlace();
            currentPlace.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.setCurrentPlace(currentPlace);
        }
        WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(mWifiManager != null){
            //Wifi is available
            if(mWifiManager.isWifiEnabled()){
                //Wifi is ON, check which SSID is currently associated with this device
                Log.d(TAG, "Wifi is ON, check which SSID is currently associated with this device");
                WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
                if(mWifiInfo != null){
                    //Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode
                    Log.d(TAG, "Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode");
                    String ssid = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
                    Log.d(TAG, "Currently connected to: " + ssid);
                    WifiNetwork wifiNetwork = MySettings.getWifiNetworkBySSID(ssid);
                    if(wifiNetwork != null){
                        Log.d(TAG, "wifinetwork DB id: " + wifiNetwork.getId());
                        long placeID = wifiNetwork.getPlaceID();
                        Log.d(TAG, "wifinetwork placeID: " + placeID);
                        if(placeID != -1){
                            Place localPlace = MySettings.getPlace(placeID);
                            if(localPlace != null){
                                Log.d(TAG, "wifinetwork DB placeName: " + localPlace.getName());
                                localPlace.setMode(Place.PLACE_MODE_LOCAL);
                                MySettings.updatePlaceMode(localPlace, Place.PLACE_MODE_LOCAL);
                                if(MySettings.getCurrentPlace() != null && MySettings.getCurrentPlace().getId() == localPlace.getId()){
                                    MySettings.setCurrentPlace(localPlace);
                                }
                            }
                        }
                    }else{
                        //Wifi network is NOT associated with any Place
                        Log.d(TAG, "Wifi network is NOT associated with any Place");
                    }
                }else{
                    //Wifi is ON but not connected to any ssid
                    Log.d(TAG, "Wifi is ON but not connected to any ssid");
                }
            }else{
                //Wifi is OFF
                Log.d(TAG, "Wifi is OFF");
            }
        }else {
            //Wifi is not available
        }
    }

    private void checkCellularConnection(){
        new Utils.InternetChecker(MainActivity.getInstance(), new Utils.InternetChecker.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                MySettings.setInternetConnectivityState(true);
            }

            @Override
            public void onConnectionFail(String errorMsg) {
                MySettings.setInternetConnectivityState(false);
            }
        }).execute();
    }

    private void setLayoutVisibility(){
        boolean showAddPlaceLayout = false;
        boolean showAddRoomLayout = false;
        boolean showAddDeviceLayout = false;
        if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
            showAddPlaceLayout = true;
        }

        if(place != null){
            if(floor != null){
                if(MySettings.getFloorRooms(floor.getId()) == null || MySettings.getFloorRooms(floor.getId()).size() < 1){
                    showAddRoomLayout = true;
                }
            }else{
                if(MySettings.getPlaceRooms(place) == null || MySettings.getPlaceRooms(place).size() < 1){
                    showAddRoomLayout = true;
                }
            }
        }else{
            showAddRoomLayout = true;
        }

        /*if(place != null){
            if(MySettings.getPlaceDevices(place) == null || MySettings.getPlaceDevices(place).size() < 1){
                showAddDeviceLayout = true;
            }
        }else{
            if(MySettings.getAllDevices() == null || MySettings.getAllDevices().size() < 1){
                showAddDeviceLayout = true;
            }
        }*/

        if(showAddPlaceLayout){
            addPlaceLayout.setVisibility(View.VISIBLE);
        }else{
            addPlaceLayout.setVisibility(View.GONE);
        }
        if(showAddRoomLayout){
            addRoomLayout.setVisibility(View.VISIBLE);
            addDeviceLayout.setVisibility(View.VISIBLE);
        }else{
            addRoomLayout.setVisibility(View.GONE);
            addDeviceLayout.setVisibility(View.GONE);
        }
        /*if(showAddDeviceLayout){
            addRoomLayout.setVisibility(View.VISIBLE);
        }else{
            addDeviceLayout.setVisibility(View.GONE);

        }*/

        if(showAddPlaceLayout || showAddRoomLayout){
            addFabMenu.setVisibility(View.GONE);
            roomsGridViewLongPressHint.setVisibility(View.GONE);

            roomsGridView.setVisibility(View.GONE);
            roomsListView.setVisibility(View.GONE);
            roomsHeaderLayout.setVisibility(View.GONE);
        }else{
            addFabMenu.setVisibility(View.VISIBLE);
            //roomsGridViewLongPressHint.setVisibility(View.VISIBLE);
            roomsGridViewLongPressHint.setVisibility(View.GONE);

            roomsHeaderLayout.setVisibility(View.VISIBLE);
            if(lastSelectedView == GRID_VIEW){
                roomsGridView.setVisibility(View.VISIBLE);
                roomsListView.setVisibility(View.INVISIBLE);
            }else if(lastSelectedView == LIST_VIEW){
                roomsGridView.setVisibility(View.INVISIBLE);
                roomsListView.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onPlaceSelected(Place selectedPlace){
        if(selectedPlace != null){
            floor = null;
            MySettings.setCurrentFloor(null);
            this.place = selectedPlace;
            MySettings.setCurrentPlace(place);
            if(MySettings.getPlaceFloors(place.getId()).size() == 1) {
                MainActivity.setActionBarTitle(place.getName(), getResources().getColor(R.color.whiteColor));
            }else{
                MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms), getResources().getColor(R.color.whiteColor));
            }

            rooms.clear();
            if(MySettings.getPlaceRooms(place) != null && MySettings.getPlaceRooms(place).size() >= 1){
                rooms.addAll(MySettings.getPlaceRooms(place));
            }
            roomsGridAdapter.notifyDataSetChanged();
            roomsDashboardListAdapter.notifyDataSetChanged();

            setLayoutVisibility();

            checkWifiConnection();
            checkCellularConnection();
        }
    }

    public MqttAndroidClient getMqttAndroidClient(){
        if(mqttAndroidClient != null){
            return mqttAndroidClient;
        }else{
            return null;
        }
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
                    for (Device device:placeDevices) {
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
                                for (Device device:placeDevices) {
                                    subscribe(mqttAndroidClient, device, 1);
                                }
                            }catch (MqttException e){
                                Log.d(TAG, "Exception " + e.getMessage());
                                for (Device device:placeDevices) {
                                    device.setDeviceMQTTReachable(false);
                                }
                                MainActivity.getInstance().refreshDevicesListFromMemory();
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d(TAG, "MQTT connect onFailure: " + exception.toString());
                            for (Device device:placeDevices) {
                                device.setDeviceMQTTReachable(false);
                            }
                            MainActivity.getInstance().refreshDevicesListFromMemory();
                        }
                    });
                }
            } catch (MqttException e) {
                e.printStackTrace();
                for (Device device:placeDevices) {
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

    @Override
    public void onPause(){
        super.onPause();
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            Toolbar toolbar = (Toolbar) MainActivity.getInstance().findViewById(R.id.toolbar);
            if(toolbar != null){
                ImageView arrowImageView = toolbar.findViewById(R.id.toolbar_change_home_imageview);
                arrowImageView.setVisibility(View.GONE);
                toolbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (MainActivity.getInstance() != null && MainActivity.isResumed) {
            Toolbar toolbar = (Toolbar) MainActivity.getInstance().findViewById(R.id.toolbar);
            if(toolbar != null){
                if(showPlaceArrow) {
                    ImageView arrowImageView = toolbar.findViewById(R.id.toolbar_change_home_imageview);
                    arrowImageView.setVisibility(View.VISIBLE);
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1) {
                                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_place_first), true);
                            } else {
                                // DialogFragment.show() will take care of adding the fragment
                                // in a transaction.  We also want to remove any currently showing
                                // dialog, so make our own transaction and take care of that here.
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickPlaceDialogFragment");
                                if (prev != null) {
                                    ft.remove(prev);
                                }
                                ft.addToBackStack(null);

                                // Create and show the dialog.
                                PickPlaceDialogFragment fragment = PickPlaceDialogFragment.newInstance();
                                fragment.setTargetFragment(DashboardRoomsFragment.this, 0);
                                fragment.show(ft, "pickPlaceDialogFragment");
                            }
                        }
                    });
                }else{
                    ImageView arrowImageView = toolbar.findViewById(R.id.toolbar_change_home_imageview);
                    arrowImageView.setVisibility(View.GONE);
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }
            }
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
        for (Device device:placeDevices) {
            device.setDeviceMQTTReachable(false);
        }
        for (Device device:placeDevices) {
            MySettings.addDevice(device);
        }
        super.onDestroy();
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

    public static class DeviceToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = DashboardDevicesFragment.DeviceSyncer.class.getSimpleName();

        Device device;
        int newState;

        int statusCode;
        boolean ronixUnit = true;

        public DeviceToggler(Device device, int state) {
            this.device = device;
            this.newState = state;
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

            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                Log.d(TAG,  "deviceToggler URL: " + urlString);

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
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                    for (Line line : device.getLines()) {
                        switch (line.getPosition()){
                            case 0:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_0_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_0_DIM", "0");
                                }
                                break;
                            case 1:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_1_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_1_DIM", "0");
                                }
                                break;
                            case 2:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_2_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_2_DIM", "0");
                                }
                                break;
                        }
                    }
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                    for (Line line : device.getLines()) {
                        switch (line.getPosition()){
                            case 0:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_0_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_0_STT", "0");
                                }
                                break;
                            case 1:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_1_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_1_STT", "0");
                                }
                                break;
                            case 2:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_2_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_2_STT", "0");
                                }
                                break;
                        }
                    }
                }

                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                Log.d(TAG,  "deviceToggler POST data: " + jObject.toString());


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
                Log.d(TAG,  "deviceToggler response: " + result.toString());
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

                                if(wifiStatus.has("U_W_HWV")){
                                    String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                    if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                        int wifiVersion = Integer.parseInt(wifiVersionString);
                                        device.setWifiVersion(""+wifiVersion);
                                    }
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

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }

                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                }


                                String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                if(hardwareStatus.has("L_0_DIM")){
                                    line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                    if(line0DimmingValueString.equals(":")){
                                        line0DimmingValue = 10;
                                    }else{
                                        line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                    }
                                }
                                if(hardwareStatus.has("L_1_DIM")){
                                    line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                    if(line1DimmingValueString.equals(":")){
                                        line1DimmingValue = 10;
                                    }else{
                                        line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                    }
                                }
                                if(hardwareStatus.has("L_2_DIM")){
                                    line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                    if(line2DimmingValueString.equals(":")){
                                        line2DimmingValue = 10;
                                    }else{
                                        line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                    }
                                }


                                String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                if(hardwareStatus.has("L_0_D_S")){
                                    line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                    line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                }
                                if(hardwareStatus.has("L_1_D_S")){
                                    line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                    line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                }
                                if(hardwareStatus.has("L_2_D_S")){
                                    line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                    line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                }

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
                                if(hardwareStatus.has("U_H_TMP")){
                                    temperatureString = hardwareStatus.getString("U_H_TMP");
                                    temperatureValue = Integer.parseInt(temperatureString);
                                    device.setTemperature(temperatureValue);
                                }
                                if(hardwareStatus.has("U_BEEP_")){
                                    beepString = hardwareStatus.getString("U_BEEP_");
                                    if(beepString != null && beepString.length() >= 1){
                                        if(Integer.parseInt(beepString) == 1){
                                            beep = true;
                                            device.setBeep(beep);
                                        }else{
                                            beep = false;
                                            device.setBeep(beep);
                                        }
                                    }
                                }
                                if(hardwareStatus.has("U_H_LCK")){
                                    hwLockString = hardwareStatus.getString("U_H_LCK");
                                    if(hwLockString != null && hwLockString.length() >= 1){
                                        if(Integer.parseInt(hwLockString) == 1){
                                            hwLock = true;
                                            device.setHwLock(hwLock);
                                        }else{
                                            hwLock = false;
                                            device.setHwLock(hwLock);
                                        }
                                    }
                                }

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
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

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                }

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
                                if(hardwareStatus.has("U_H_TMP")){
                                    temperatureString = hardwareStatus.getString("U_H_TMP");
                                    temperatureValue = Integer.parseInt(temperatureString);
                                    device.setTemperature(temperatureValue);
                                }
                                if(hardwareStatus.has("U_BEEP_")){
                                    beepString = hardwareStatus.getString("U_BEEP_");
                                    if(beepString != null && beepString.length() >= 1){
                                        if(Integer.parseInt(beepString) == 1){
                                            beep = true;
                                            device.setBeep(beep);
                                        }else{
                                            beep = false;
                                            device.setBeep(beep);
                                        }
                                    }
                                }
                                if(hardwareStatus.has("U_H_LCK")){
                                    hwLockString = hardwareStatus.getString("U_H_LCK");
                                    if(hwLockString != null && hwLockString.length() >= 1){
                                        if(Integer.parseInt(hwLockString) == 1){
                                            hwLock = true;
                                            device.setHwLock(hwLock);
                                        }else{
                                            hwLock = false;
                                            device.setHwLock(hwLock);
                                        }
                                    }
                                }

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
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

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                String pirStateString;
                                int pirState = 0;
                                if(hardwareStatus.has("L_0_STT")){
                                    pirStateString = hardwareStatus.getString("L_0_STT");
                                    pirState = Integer.valueOf(pirStateString);
                                }

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
}
