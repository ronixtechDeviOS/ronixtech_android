package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MyApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsDashboardListAdapter;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    LinearLayout addLayout;

    private static final int LIST_VIEW = 0;
    private static final int GRID_VIEW = 2;
    private static int lastSelectedView = LIST_VIEW;

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

    private boolean isResumed;

    Handler listHandler;

    //Stuff for local mode
    Timer timer;
    TimerTask doAsynchronousTask;
    Handler handler;

    ImagePicker imagePicker;

    private OnFragmentInteractionListener mListener;

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
                if(place.getMode() == Place.PLACE_MODE_LOCAL){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                }
            }else{
                if(floor != null){
                    if(place.getMode() == Place.PLACE_MODE_LOCAL){
                        MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                    }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                        MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                    }
                }else{
                    if(place.getMode() == Place.PLACE_MODE_LOCAL){
                        MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                    }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                        MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                    }
                }
            }
            showPlaceArrow = true;
        }else{
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.dashboard), getResources().getColor(R.color.whiteColor));
            showPlaceArrow = false;
        }
        setHasOptionsMenu(true);

        addLayout = view.findViewById(R.id.add_layout);
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

                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines) {
                        if(device.getLines() == null || device.getLines().size() != 1){
                            MySettings.removeDevice(device);
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines){
                        if(device.getLines() == null || device.getLines().size() != 2){
                            MySettings.removeDevice(device);
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines
                            || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        if(device.getLines() == null || device.getLines().size() != 3){
                            Utils.log(TAG,"Deleting, device lines: "+device.getLines().size(),false);
                            MySettings.removeDevice(device);
                            MainActivity.getInstance().removeDevice(device);
                        }
                    }
                }
            }
        }

        roomsHeaderLayout = view.findViewById(R.id.rooms_header_layout);
        showAsGrid = view.findViewById(R.id.gridview_imageview);
        showAsList = view.findViewById(R.id.listview_imageview);
        sortImageView = view.findViewById(R.id.sort_imageview);

        listHandler = new Handler();

        int padddingHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics());
        Space footerView = new Space(getActivity());
        footerView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, padddingHeight));

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
            public void onRoomImageChangeRequested(Room room) {
                imagePicker = new ImagePicker(getActivity(), DashboardRoomsFragment.this, new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {
                        //Utils.showToast(getActivity(), "picked image uri: " + imageUri.toString(), false);
                        Utils.showLoading(getActivity());

                        new Runnable() {
                            public void run() {
                                try{
                                    //creating a new folder for the images to be saved to
                                    File ronixDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/");
                                    if(!ronixDirectory.exists()) {
                                        if(ronixDirectory.mkdir()) {
                                            //directory is created;
                                        }
                                    }
                                    Utils.log(TAG, "Created directory: " + ronixDirectory.getAbsolutePath(), true);
                                    File imagesDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/" + "RoomImages/");
                                    if(!imagesDirectory.exists()) {
                                        if(imagesDirectory.mkdir()) {
                                            //directory is created;
                                        }
                                    }

                                    Bitmap pickedBitmap = BitmapFactory.decodeFile(imagePicker.getImageFile().getAbsolutePath());

                                    File outputFile = new File(getActivity().getFilesDir()  + "/RonixHome/" + "RoomImages/" + "room_" + room.getId() + ".jpg");
                                    FileOutputStream out = new FileOutputStream(outputFile, false);

                                    //Bitmap finalBitmap = Utils.resizeBitmapByScale(pickedBitmap, 0.4f, true);
                                    Bitmap finalBitmap = Utils.resizeBitmapByDimensions(pickedBitmap, 500, 500, true);
                                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);


                                    //Bitmap compressedBitmap = BitmapFactory.decodeStream(new FileInputStream(out.toString()));


                                    /*File imageFile = imagePicker.getImageFile();

                                    FileChannel src = new FileInputStream(imageFile).getChannel();
                                    FileChannel dst = new FileOutputStream(outputFile).getChannel();
                                    dst.transferFrom(src, 0, src.size());
                                    src.close();
                                    dst.close();*/
                                }catch (IOException e){
                                    Utils.showToast(MainActivity.getInstance(), e.toString(), true);
                                }finally {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            roomsDashboardListAdapter.notifyDataSetChanged();
                                            Utils.dismissLoading();
                                        }
                                    });
                                }
                            }
                        }.run();
                        /*UCrop.of(imageUri, imageUri)
                                                    .withAspectRatio(2, 1)
                                                    *//*.withMaxResultSize(500, 400)*//*
                                                    .start(getActivity());*/
                    }
                }).setWithImageCrop(16,9);
                imagePicker.choosePicture(true);
            }
        });
        roomsListView.setAdapter(roomsDashboardListAdapter);
        roomsListView.addFooterView(footerView);

        setLayoutVisibility();

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
                }
                else if(MySettings.getFloorRooms(MySettings.getCurrentFloor().getId()).isEmpty())
                {
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                }
                else
                    {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocation");
                    fragmentTransaction.addToBackStack("addDeviceSelectLocation");
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
                }
                else if(MySettings.getFloorRooms(MySettings.getCurrentFloor().getId()).isEmpty())
                {
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                }
                else
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocation");
                    fragmentTransaction.addToBackStack("addDeviceSelectLocation");
                    fragmentTransaction.commit();
                }
            }
        });

        showAsGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                roomsGridView.setVisibility(View.VISIBLE);
                roomsListView.setVisibility(View.INVISIBLE);
                lastSelectedView = GRID_VIEW;
            }
        });
        showAsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                roomsGridView.setVisibility(View.INVISIBLE);
                roomsListView.setVisibility(View.VISIBLE);
                lastSelectedView = LIST_VIEW;
            }
        });

        loadDevicesFromDatabase();

        checkWifiConnection();
        checkCellularConnection();

        return view;
    }

    private void checkWifiConnection(){
        List<Place> places = MySettings.getAllPlaces();
        for (Place place : places) {
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
                Utils.log(TAG, "Wifi is ON, check which SSID is currently associated with this device", true);
                WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
                if(mWifiInfo != null){
                    //Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode
                    Utils.log(TAG, "Wifi is ON and connected to network, check which Place (if any) is associated with this SSID and set its mode to Local mode", true);
                    String ssid = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
                    Utils.log(TAG, "Currently connected to: " + ssid, true);
                    if(ssid.equalsIgnoreCase("androidwifi") || ssid.equalsIgnoreCase("wiredssid")){
                        Utils.log(TAG, "Current SSID is known to be an emulator SSID, mode will be set to LOCAL for all palces", true);
                        List<Place> allPlaces = MySettings.getAllPlaces();
                        for (Place place : allPlaces) {
                            place.setMode(Place.PLACE_MODE_LOCAL);
                            MySettings.updatePlaceMode(place, Place.PLACE_MODE_LOCAL);
                        }
                        if(MySettings.getCurrentPlace() != null ){
                            Place currentPlace = MySettings.getCurrentPlace();
                            currentPlace.setMode(Place.PLACE_MODE_LOCAL);
                            MySettings.setCurrentPlace(currentPlace);
                        }
                    }else{
                        WifiNetwork wifiNetwork = MySettings.getWifiNetworkBySSID(ssid);
                        if(wifiNetwork != null){
                            Utils.log(TAG, "WifiNetwork DB id: " + wifiNetwork.getId(), true);
                            long placeID = wifiNetwork.getPlaceID();
                            Utils.log(TAG, "WifiNetwork placeID: " + placeID, true);
                            if(placeID != -1){
                                Place localPlace = MySettings.getPlace(placeID);
                                if(localPlace != null){
                                    Utils.log(TAG, "WifiNetwork DB placeName: " + localPlace.getName(), true);
                                    localPlace.setMode(Place.PLACE_MODE_LOCAL);
                                    MySettings.updatePlaceMode(localPlace, Place.PLACE_MODE_LOCAL);
                                    if(MySettings.getCurrentPlace() != null && MySettings.getCurrentPlace().getId() == localPlace.getId()){
                                        Utils.log(TAG, "Updating current place", true);
                                        if(localPlace.getMode() == Place.PLACE_MODE_LOCAL) {
                                            Utils.log(TAG, "Updating current place: Setting mode to LOCAL", true);
                                            MainActivity.getInstance().refreshDeviceListFromDatabase();
                                        }else if(localPlace.getMode() == Place.PLACE_MODE_REMOTE) {
                                            Utils.log(TAG, "Updating current place: Setting mode to REMOTE", true);
                                        }
                                        MySettings.setCurrentPlace(localPlace);
                                        this.place = localPlace;
                                    }
                                }
                            }
                        }else{
                            //Wifi network is NOT associated with any Place
                            Utils.log(TAG, "WifiNetwork is NOT associated with any Place", true);
                        }
                    }
                }else{
                    //Wifi is ON but not connected to any ssid
                    Utils.log(TAG, "Wifi is ON but not connected to any ssid", true);
                }
            }else{
                //Wifi is OFF
                Utils.log(TAG, "Wifi is OFF", true);
            }
        }else {
            //Wifi is not available
        }
    }

    private void checkCellularConnection(){
        /*List<Place> allPlaces = MySettings.getAllPlaces();
        for (Place place : allPlaces) {
            place.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.updatePlaceMode(place, Place.PLACE_MODE_REMOTE);
        }
        if(MySettings.getCurrentPlace() != null ){
            Place currentPlace = MySettings.getCurrentPlace();
            currentPlace.setMode(Place.PLACE_MODE_REMOTE);
            MySettings.setCurrentPlace(currentPlace);
        }*/
        new Utils.InternetChecker(MainActivity.getInstance(), new Utils.InternetChecker.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                MySettings.setInternetConnectivityState(true);
            }

            @Override
            public void onConnectionFail(String errorMsg) {
                MySettings.setInternetConnectivityState(false);
                //checkWifiConnection();
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

            addLayout.setVisibility(View.VISIBLE);
        }else{
            //addFabMenu.setVisibility(View.VISIBLE);
            addFabMenu.setVisibility(View.GONE);
            //roomsGridViewLongPressHint.setVisibility(View.VISIBLE);
            roomsGridViewLongPressHint.setVisibility(View.GONE);

            //roomsHeaderLayout.setVisibility(View.VISIBLE);
            roomsHeaderLayout.setVisibility(View.GONE);
            if(lastSelectedView == GRID_VIEW){
                roomsGridView.setVisibility(View.VISIBLE);
                roomsListView.setVisibility(View.INVISIBLE);
            }else if(lastSelectedView == LIST_VIEW){
                roomsGridView.setVisibility(View.INVISIBLE);
                roomsListView.setVisibility(View.VISIBLE);
            }

            addLayout.setVisibility(View.GONE);
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
                                    if(dev.getIpAddress() != "" && dev.getIpAddress().length() >= 1) {
                                        if(!MySettings.isControlActive()) {
                                            Utils.getDeviceInfo(dev);
                                        }else{
                                            Utils.log(TAG, "Controls active, skipping get_status", true);
                                        }
                                    }else{
                                        //Utils.showToast(MainActivity.getInstance(),"Please connect to internet to get status",true);
                                        //MySettings.scanNetwork();
                                        allDevicesReachable = false;
                                    }
                                }
                                if(allDevicesReachable){
                                    Utils.hideUpdatingNotification();
                                }
                            }else{
                                Utils.log(TAG, "NO DEVICES IN ROOM", true);
                            }
                        }
                    });
                }
            };
            int refreshRate = 2600 + (400 * MySettings.getAllDevices().size());
            timer.schedule(doAsynchronousTask, 0, refreshRate/*Device.REFRESH_RATE_MS * (DevicesInMemory.getDevices().size()>=1 ? DevicesInMemory.getDevices().size() : 1)*/); //execute in every REFRESH_RATE_MS
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

    public void loadDevicesFromDatabase(){
        if(listHandler != null) {
            listHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<Device> tempDevices = new ArrayList<>();
                    if(place != null){
                        if (MySettings.getPlaceDevices(place) != null && MySettings.getPlaceDevices(place).size() >= 1) {
                            tempDevices.addAll(MySettings.getPlaceDevices(place));
                        }
                        DevicesInMemory.setDevices(tempDevices);
                        DevicesInMemory.setLocalDevices(tempDevices);
                        putDevicesIntoListView();
                    }
                }
            });
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
        /*if(devices != null) {
            //devices.clear();
            if (DevicesInMemory.getDevices() != null && DevicesInMemory.getDevices().size() >= 1) {
                *//*List<Device> tempDevices = new ArrayList<>();
                tempDevices.addAll(DevicesInMemory.getDevices());
                for (Device device:tempDevices) {
                    if(!devices.contains(device)) {
                        devices.add(device);
                    }
                }*//*
                //devices.addAll(DevicesInMemory.getDevices());
                setLayoutVisibility();
            } else {
                setLayoutVisibility();
            }
            *//*if(devices.size() >= 1) {
                Collections.sort(devices);
            }*//*
            roomsDashboardListAdapter.notifyDataSetChanged();
        }*/
        roomsDashboardListAdapter.notifyDataSetChanged();
    }

    public void updateUI(){
        if(isResumed) {
            place = MySettings.getCurrentPlace();
            if(place != null){
                if(MySettings.getPlaceFloors(place.getId()).size() == 1) {
                    if(place.getMode() == Place.PLACE_MODE_LOCAL){
                        MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                    }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                        MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                    }
                }else{
                    if(floor != null){
                        if(place.getMode() == Place.PLACE_MODE_LOCAL){
                            MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                        }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                            MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                        }
                    }else{
                        if(place.getMode() == Place.PLACE_MODE_LOCAL){
                            MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                        }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                            MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                        }
                    }
                }
            }else{
                MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.dashboard), getResources().getColor(R.color.whiteColor));
            }


            /*roomsDashboardListAdapter = new RoomsDashboardListAdapter(getActivity(), rooms, getFragmentManager(), MySettings.getCurrentPlace().getMode());
            roomsListView.setAdapter(roomsDashboardListAdapter);

            loadDevicesFromMemory();

            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                //startTimer
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to LOCAL mode", true);
                startTimer();
                if(devices != null) {
                    for (Device device : devices) {
                        device.setDeviceMQTTReachable(false);
                    }
                }
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                //stopTimer
                stopTimer();
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to REMOTE mode, using MQTT", true);
                //refresh MQTT client
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    MainActivity.getInstance().refreshMqttClient();
                }
            }*/
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
                if(place.getMode() == Place.PLACE_MODE_LOCAL){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                }
            }else{
                if(place.getMode() == Place.PLACE_MODE_LOCAL){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                }else if(place.getMode() == Place.PLACE_MODE_REMOTE){
                    MainActivity.setActionBarTitle(place.getName() + " - " + Utils.getString(getActivity(), R.string.all_rooms) + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                }
            }

            rooms.clear();
            if(MySettings.getPlaceRooms(place) != null && MySettings.getPlaceRooms(place).size() >= 1){
                rooms.addAll(MySettings.getPlaceRooms(place));
            }
            loadDevicesFromDatabase();
            roomsGridAdapter.notifyDataSetChanged();
            roomsDashboardListAdapter.notifyDataSetChanged();

            setLayoutVisibility();

            checkWifiConnection();
            checkCellularConnection();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_dashboard_rooms, menu);

        /*MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.test_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add) {
            /*// DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("additionDialogFragment");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            AddDialogFragment fragment = AddDialogFragment.newInstance();
            fragment.show(ft, "additionDialogFragment");*/
            //go to add room fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AddRoomFragment addRoomFragment = new AddRoomFragment();
            fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
            fragmentTransaction.addToBackStack("addRoomFragment");
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause(){
        Utils.log(TAG, "onPause", true);
        super.onPause();
        isResumed = false;
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
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
                stopTimer();
                //BroadcastServer.stopServer();
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                //stop MQTT in onDestroy
            }
        }
    }

    @Override
    public void onResume(){
        Utils.log(TAG, "onResume", true);
        super.onResume();
        isResumed = true;
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
                                Fragment prev = getFragmentManager().findFragmentByTag("pickPlaceDialogFragment");
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
        loadDevicesFromDatabase();
        MainActivity.getInstance().refreshDevicesListFromMemory();
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to LOCAL mode", true);
                startTimer();
                //BroadcastServer.startServer();
            /*if(devices != null) {
                for (Device device : devices) {
                    device.setDeviceMQTTReachable(false);
                }
            }*/
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                //start MQTT in onStart
            }
        }
    }

    @Override
    public void onStart(){
        Utils.log(TAG, "onStart", true);
        super.onStart();
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to LOCAL mode", true);
                //startTimer in onResume
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    MainActivity.getInstance().refreshMqttClient();
                }
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to REMOTE mode, using MQTT", true);
                //refresh MQTT client
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    MainActivity.getInstance().refreshMqttClient();

                }
            }
        }
    }

    @Override
    public void onDestroy(){
        Utils.log(TAG, "onDestroy", true);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(imagePicker != null) {
            imagePicker.handleActivityResult(resultCode, requestCode, data);
        }

        /*if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Utils.showToast(getActivity(), "cropped image uri: " + resultUri.toString(), false);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }*/
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.handlePermission(requestCode, grantResults);
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
