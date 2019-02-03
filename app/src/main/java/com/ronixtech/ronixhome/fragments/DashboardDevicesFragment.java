package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.DeviceAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

import java.util.ArrayList;
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
        Utils.log(TAG, "onCreateView", true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_devices, container, false);
        if(room != null){
            if(MySettings.getCurrentPlace() != null){
                if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                    MainActivity.setActionBarTitle(room.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                    MainActivity.setActionBarTitle(room.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                }
            }
        }else{
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.dashboard), getResources().getColor(R.color.whiteColor));
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
        if(MySettings.getCurrentPlace() != null){
            deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), MySettings.getCurrentPlace().getMode());
        }else{
            deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), Place.PLACE_MODE_LOCAL);
        }

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
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }

    public void updateUI(){
        if(isResumed) {
            if (room != null) {
                if(MySettings.getCurrentPlace() != null) {
                    if (MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                        MainActivity.setActionBarTitle(room.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_unreachable), getResources().getColor(R.color.whiteColor));
                    } else if (MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE) {
                        MainActivity.setActionBarTitle(room.getName() + " - " + Utils.getString(getActivity(), R.string.device_mqtt_reachable), getResources().getColor(R.color.whiteColor));
                    }
                }
            } else {
                MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.dashboard), getResources().getColor(R.color.whiteColor));
            }

            if(MySettings.getCurrentPlace() != null) {
                deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), MySettings.getCurrentPlace().getMode());
            }else{
                deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager(), Place.PLACE_MODE_LOCAL);
            }
            devicesListView.setAdapter(deviceAdapter);

            loadDevicesFromMemory();

            if(MySettings.getCurrentPlace() != null){
                if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                    //startTimer
                    Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to OFFLINE mode", true);
                    startTimer();
                    //BroadcastServer.startServer();
                /*if(devices != null) {
                    for (Device device : devices) {
                        device.setDeviceMQTTReachable(false);
                    }
                }*/
                }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                    //stopTimer
                    stopTimer();
                    //BroadcastServer.stopServer();
                    Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to ONLINE mode, using MQTT", true);
                    //refresh MQTT client
                    if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                        MainActivity.getInstance().refreshMqttClient();
                    }
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
                                            Utils.getDeviceInfo(dev);
                                        }else{
                                            Utils.log(TAG, "Controls active, skipping get_status", true);
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
            int refreshRate = Device.REFRESH_RATE_MS;
            if(room != null && MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1){
                //refreshRate = 750 + (250 * MySettings.getRoomDevices(room.getId()).size());
                refreshRate = 2600 + (400 * MySettings.getRoomDevices(room.getId()).size());
            }
            timer.schedule(doAsynchronousTask, 0, refreshRate /** (DevicesInMemory.getDevices().size()>=1 ? DevicesInMemory.getDevices().size() : 1)*/); //execute in every REFRESH_RATE_MS
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

    @Override
    public void onResume(){
        Utils.log(TAG, "onResume", true);
        super.onResume();
        isResumed = true;
        loadDevicesFromDatabase();
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
    public void onPause(){
        Utils.log(TAG, "onPause", true);
        isResumed = false;
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
                stopTimer();
                //BroadcastServer.stopServer();
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                //stop MQTT in onDestroy
            }
        }

        super.onPause();
        /*for (Device device:devices) {
            device.setDeviceMQTTReachable(false);
        }*/
        if(devices != null) {
            for (Device device : devices) {
                MySettings.addDevice(device);
            }
        }
    }

    @Override
    public void onStart(){
        Utils.log(TAG, "onStart", true);
        super.onStart();
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL) {
                //startTimer in onResume
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                Utils.log(TAG, "Current place " + MySettings.getCurrentPlace().getName() + " is set to ONLINE mode, using MQTT", true);
                //start MQTT, when a control is sent from the DeviceAdapter, it will be synced here when the MQTT responds
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
        if(devices != null) {
            /*for (Device device : devices) {
                device.setDeviceMQTTReachable(false);
            }*/
            for (Device device : devices) {
                MySettings.addDevice(device);
            }
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

}