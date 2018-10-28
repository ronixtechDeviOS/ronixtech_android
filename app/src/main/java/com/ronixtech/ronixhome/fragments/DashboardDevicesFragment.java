package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.clans.fab.FloatingActionButton;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.DeviceAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;
    Button addDeviceButton;

    static ListView devicesListView;
    static DeviceAdapter deviceAdapter;
    static List<Device> devices;
    TextView emptyTextView;

    Handler listHandler;

    Timer timer;
    TimerTask doAsynchronousTask;
    Handler handler;

    private Room room;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_devices, container, false);
        if(room != null){
            MainActivity.setActionBarTitle(room.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.home), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        listHandler = new Handler();

        emptyTextView = view.findViewById(R.id.empty_textview);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);
        addDeviceButton = view.findViewById(R.id.add_device_button);

        devicesListView = view.findViewById(R.id.devices_listview);
        devices = DevicesInMemory.getDevices();
        deviceAdapter = new DeviceAdapter(getActivity(), devices, getFragmentManager());
        devicesListView.setAdapter(deviceAdapter);

        loadDevicesFromDatabase();

        MySettings.setControlState(false);

        //startTimer();

        final TextView debugTextView = view.findViewById(R.id.debug_textview);
        Button testButton = view.findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //volley request to device to send ssid/password and then get device info for next steps
                String url = "http://192.168.4.1/ronix/json_test/post";
                //?essid=%SSID%&passwd=%PASS%
                debugTextView.append("POSTING data on " + url +"\n");

                HashMap<String, String> postData = new HashMap<>();
                postData.put("SSID", MySettings.getHomeNetwork().getSsid());
                postData.put("PASS", MySettings.getHomeNetwork().getPassword());

                debugTextView.append("SSID: " + MySettings.getHomeNetwork().getSsid() +"\n");
                debugTextView.append("Password: " + MySettings.getHomeNetwork().getPassword() +"\n");

                debugTextView.append("Json data:"+"\n");
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("SSID", MySettings.getHomeNetwork().getSsid());
                    jsonObject.put("PASS", MySettings.getHomeNetwork().getPassword());
                    debugTextView.append(jsonObject.toString()+"\n");
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }

                Log.d(TAG,  "sendConfigurationToDevice URL: " + url);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        debugTextView.append("Response:" + response.toString() +"\n");
                        debugTextView.append(response.toString() +"\n");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley Error: " + error.getMessage());
                        debugTextView.append("Response error: " + error.getMessage() +"\n");
                        if(getActivity() != null){
                            Toast.makeText(getActivity(), getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                jsonObjectRequest.setShouldCache(false);
                HttpConnector.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
            }
        });

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

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
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

    private void startTimer(){
        timer = new Timer();
        handler = new Handler();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //MySettings.scanDevices();
                        if(MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1){
                            boolean allDevicesReachable = true;
                            for (Device dev : MySettings.getRoomDevices(room.getId())) {
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
        timer.schedule(doAsynchronousTask, 0, Device.REFRESH_RATE_MS); //execute in every 1000 ms
    }

    private void stopTimer(){
        doAsynchronousTask.cancel();
        timer.cancel();
        timer.purge();
    }

    public void setRoom(Room room){
        this.room = room;
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
                emptyTextView.setVisibility(View.GONE);
                addDeviceButton.setVisibility(View.GONE);
            } else {
                emptyTextView.setText("You don't have any RonixTech smart controllers added yet.\nAdd a smart controller by clicking the button below.");
                emptyTextView.setVisibility(View.VISIBLE);
                addDeviceButton.setVisibility(View.VISIBLE);
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
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
            StatusGetter statusGetter = new StatusGetter(device);
            statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            ModeGetter modeGetter = new ModeGetter(device);
            modeGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){

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
        super.onResume();
        startTimer();
    }

    @Override
    public void onPause(){
        stopTimer();
        super.onPause();
        for (Device device:devices) {
            MySettings.addDevice(device);
        }
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

    public static class StatusGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.StatusGetter.class.getSimpleName();

        Device device;

        int statusCode;

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
            if(statusCode == 200) {
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
            }else{
                MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    MySettings.updateDeviceIP(device, "");
                    MySettings.updateDeviceErrorCount(device, 0);
                    MySettings.scanNetwork();
                }
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
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                /*String chipID = wifiStatus.getString("U_W_UID");
                                if(device.getChipID().length() >= 1) {
                                    if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                        MySettings.updateDeviceIP(device, "");
                                        MySettings.updateDeviceErrorCount(device, 0);
                                        MySettings.scanNetwork();
                                        return null;
                                    }
                                }*/

                        JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");
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
                        device.setLines(lines);
                        if(statusCode == 200) {
                            device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                            DevicesInMemory.updateDevice(device);
                        }
                        //MySettings.addDevice(device);
                    }
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
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
        private final String TAG = DashboardDevicesFragment.StatusGetter.class.getSimpleName();

        Device device;

        int statusCode;

        public ModeGetter(Device device) {
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
            if(statusCode == 200) {
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().refreshDevicesListFromMemory();
                }
            }else{
                MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    MySettings.updateDeviceIP(device, "");
                    MySettings.updateDeviceErrorCount(device, 0);
                    MySettings.scanNetwork();
                }
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
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_UPNP)){
                                mode = SoundDeviceData.MODE_UPNP;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_USB)){
                                mode = SoundDeviceData.MODE_USB;
                            }
                        }

                        device.getSoundDeviceData().setMode(mode);

                        if(statusCode == 200) {
                            device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                            DevicesInMemory.updateDevice(device);
                        }
                        //MySettings.addDevice(device);
                    }
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
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
