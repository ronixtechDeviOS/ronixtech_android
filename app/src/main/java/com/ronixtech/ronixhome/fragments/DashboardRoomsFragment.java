package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
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

    GridView roomsGridView;
    RoomsGridAdapter adapter;
    List<Room> rooms;
    TextView roomsGridViewLongPressHint;

    private Place place;
    private Floor floor;

    private boolean showPlaceArrow = false;

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
            if(floor != null){
                MainActivity.setActionBarTitle(place.getName() + " - " + floor.getName(), getResources().getColor(R.color.whiteColor));
            }else{
                MainActivity.setActionBarTitle(place.getName() + " - " + getActivity().getResources().getString(R.string.all_rooms), getResources().getColor(R.color.whiteColor));
            }
            showPlaceArrow = true;
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.dashboard), getResources().getColor(R.color.whiteColor));
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
        adapter = new RoomsGridAdapter(getActivity(), rooms, getFragmentManager(), new RoomsGridAdapter.RoomsListener() {
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
                adapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
            @Override
            public void onRoomNameChanged() {

            }
        });
        roomsGridView.setAdapter(adapter);

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
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
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

        checkWifiConnection();
        checkCellularConnection();

        return view;
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
        });
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
            roomsGridView.setVisibility(View.GONE);
            roomsGridViewLongPressHint.setVisibility(View.GONE);
        }else{
            addFabMenu.setVisibility(View.VISIBLE);
            roomsGridView.setVisibility(View.VISIBLE);
            //roomsGridViewLongPressHint.setVisibility(View.VISIBLE);
            roomsGridViewLongPressHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlaceSelected(Place place){
        setLayoutVisibility();
        MySettings.setCurrentFloor(null);
        if(place != null){
            MainActivity.setActionBarTitle(place.getName() + " - " + getActivity().getResources().getString(R.string.all_rooms), getResources().getColor(R.color.whiteColor));

            rooms.clear();
            if(MySettings.getAllRooms() != null && MySettings.getAllRooms().size() >= 1){
                rooms.addAll(MySettings.getAllRooms());
            }
            adapter.notifyDataSetChanged();
        }

        checkWifiConnection();
        checkCellularConnection();
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
                                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_place_first), Toast.LENGTH_SHORT).show();
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
