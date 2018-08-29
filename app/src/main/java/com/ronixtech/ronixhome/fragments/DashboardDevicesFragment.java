package com.ronixtech.ronixhome.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.ArrayList;
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

    FloatingActionButton addPlaceFab, addFloorFab, addRoomFab, addDeviceFab;
    Button addDeviceButton;

    ListView devicesListView;
    DeviceAdapter deviceAdapter;
    List<Device> devices;
    TextView emptyTextView;

    Handler mHandler;

    Timer timer;
    TimerTask doAsynchronousTask;

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

        mHandler = new Handler();

        emptyTextView = view.findViewById(R.id.empty_textview);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addFloorFab = view.findViewById(R.id.add_floor_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);
        addDeviceButton = view.findViewById(R.id.add_device_button);

        devicesListView = view.findViewById(R.id.devices_listview);
        devices = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(getActivity(), devices);
        devicesListView.setAdapter(deviceAdapter);

        loadDevicesIntoMemory();

        MySettings.setControlState(false);

        devicesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Device clickedDevice = (Device) deviceAdapter.getItem(i);

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(getActivity().getResources().getString(R.string.remove_unit_question))
                        //set message
                        .setMessage(getActivity().getResources().getString(R.string.remove_unit_message))
                        //set positive button
                        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                removeDevice(clickedDevice);
                            }
                        })
                        //set negative button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();

                /*final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity()).create();
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r = getActivity().getResources();
                float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                TextView ipTextView = new TextView(getActivity());
                ipTextView.setText(getActivity().getResources().getString(R.string.ip_address_title));
                ipTextView.setGravity(Gravity.CENTER);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ipTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                ipTextView.setLayoutParams(layoutParams);

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r2 = getActivity().getResources();
                float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
                layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
                layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

                final EditText ipEditText = new EditText(getActivity());
                ipEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                ipEditText.setHint(getActivity().getResources().getString(R.string.ip_address_hint));
                ipEditText.setLayoutParams(layoutParams2);

                Button submitButton = new Button(getActivity());
                submitButton.setText(getActivity().getResources().getString(R.string.done));
                submitButton.setTextColor(getActivity().getResources().getColor(R.color.whiteColor));
                submitButton.setBackgroundColor(getActivity().getResources().getColor(R.color.greenColor));
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ipEditText.getText().toString() != null && ipEditText.getText().toString().length() >= 4) {
                            clickedDevice.setIpAddress(ipEditText.getText().toString());
                            MySettings.updateDeviceIP(clickedDevice, ipEditText.getText().toString());
                            MainActivity.getInstance().updateDevicesList();
                            dialog.dismiss();
                        }else{
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(ipEditText);
                        }
                    }
                });

                layout.addView(ipTextView);
                layout.addView(ipEditText);
                layout.addView(submitButton);

                dialog.setView(layout);

                dialog.show();*/

                return false;
            }
        });

        final Handler handler = new Handler();
        timer = new Timer();
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
                            Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
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
                addPlaceFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });
        addFloorFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddFloorFragment addFloorFragment = new AddFloorFragment();
                addFloorFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
                fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
                fragmentTransaction.addToBackStack("addFloorFragment");
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
                addRoomFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }
        });

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    public void setRoom(Room room){
        this.room = room;
    }

    public void loadDevicesIntoMemory(){
        if(mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(devices != null) {
                        devices.clear();
                        if (MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1) {
                            devices.addAll(MySettings.getRoomDevices(room.getId()));
                            emptyTextView.setVisibility(View.GONE);
                            addDeviceButton.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setText("You don't have any RonixTech smart controllers added yet.\nAdd a unit by clicking the button below.");
                            emptyTextView.setVisibility(View.VISIBLE);
                            addDeviceButton.setVisibility(View.VISIBLE);
                        }
                        deviceAdapter.notifyDataSetChanged();
                        DevicesInMemory.setDevices(devices);
                    }
                }
            });
        }
    }

    public void refreshDevicesFromMemory(){
        if(mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    /*if(devices != null) {
                        devices.clear();
                        if (MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1) {
                            devices.addAll(MySettings.getRoomDevices(room.getId()));
                            emptyTextView.setVisibility(View.GONE);
                            addDeviceButton.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setText("You don't have any RonixTech smart controllers added yet.\nAdd a unit by clicking the button below.");
                            emptyTextView.setVisibility(View.VISIBLE);
                            addDeviceButton.setVisibility(View.VISIBLE);
                        }
                        deviceAdapter.notifyDataSetChanged();
                    }*/
                    devices.clear();
                    devices.addAll(DevicesInMemory.getDevices());
                    deviceAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getDeviceInfo(Device device){
        Log.d(TAG, "Getting device info...");
        StatusGetter statusGetter = new StatusGetter(device);
        statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        loadDevicesIntoMemory();
    }

    @Override
    public void onResume(){
        super.onResume();
        doAsynchronousTask.run();
        //timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 1000 ms
    }

    @Override
    public void onPause(){
        super.onPause();
        //timer.cancel();
        doAsynchronousTask.cancel();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //FIXED: setUserVisibleHint() called before onCreateView() in Fragment causes NullPointerException
        //super.setUserVisibleHint(isVisibleToUser);
        if(!isVisibleToUser){
            //doAsynchronousTask.cancel();
        }else{
            doAsynchronousTask.run();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.devices_listview) {
            /*MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_device, menu);*/

            menu.add("One");
            menu.add("Two");
            menu.add("Three");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
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
        }
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

    public static class DataParser extends AsyncTask<Void, Void, Void> {
        private final String TAG = DashboardDevicesFragment.DataParser.class.getSimpleName();

        Device device;
        JSONObject jsonObject;

        public DataParser(Device device, String data) {
            try{
                jsonObject = new JSONObject(data);
                this.device = device;
            }catch (Exception e){
                Log.d(TAG, "Json exception " + e.getMessage());
            }
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            DevicesInMemory.updateDevice(device);
            //MySettings.addDevice(device);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(jsonObject != null){
                try {
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

                    /*JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");
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


                    /*String deviceStatus = jsonObject.getString(Constants.PARAMETER_DEVICE_STATUS_KEY);
                    String[] status = deviceStatus.split("%");
                    String line1PowerStateString = status[0];
                    String line2PowerStateString = status[1];
                    String line3PowerStateString = status[2];
                    String line1DimmingValueString = status[3];
                    String line2DimmingValueString = status[4];
                    String line3DimmingValueString = status[5];
                    String line1DimmingStateString = status[11];
                    String line2DimmingStateString = status[12];
                    String line3DimmingStateString = status[13];

                    int line1PowerState = Integer.valueOf(line1PowerStateString.substring(line1PowerStateString.indexOf("=")+1));
                    int line2PowerState = Integer.valueOf(line2PowerStateString.substring(line2PowerStateString.indexOf("=")+1));
                    int line3PowerState = Integer.valueOf(line3PowerStateString.substring(line3PowerStateString.indexOf("=")+1));


                    int line1DimmingValue, line2DimmingValue, line3DimmingValue;
                    if(!line1DimmingValueString.substring(line1DimmingValueString.indexOf("=")+1).equals(":")){
                        line1DimmingValue = Integer.valueOf(line1DimmingValueString.substring(line1DimmingValueString.indexOf("=")+1));
                    }else{
                        line1DimmingValue = 10;
                    }

                    if(!line2DimmingValueString.substring(line2DimmingValueString.indexOf("=")+1).equals(":")){
                        line2DimmingValue = Integer.valueOf(line2DimmingValueString.substring(line2DimmingValueString.indexOf("=")+1));
                    }else{
                        line2DimmingValue = 10;
                    }

                    if(!line3DimmingValueString.substring(line3DimmingValueString.indexOf("=")+1).equals(":")){
                        line3DimmingValue = Integer.valueOf(line3DimmingValueString.substring(line3DimmingValueString.indexOf("=")+1));
                    }else{
                        line3DimmingValue = 10;
                    }


                    int line1DimmingState = Integer.valueOf(line1DimmingStateString.substring(line1DimmingStateString.indexOf("=")+1));
                    int line2DimmingState = Integer.valueOf(line2DimmingStateString.substring(line2DimmingStateString.indexOf("=")+1));
                    int line3DimmingState = Integer.valueOf(line3DimmingStateString.substring(line3DimmingStateString.indexOf("=")+1));*/

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
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }
            }
            return null;
        }
    }

    public static class StatusGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.StatusGetter.class.getSimpleName();

        Device device;

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
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
            }
            Log.d(TAG, "Disabling getStatus flag...");
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS);
                Log.d(TAG,  "statusGetter URL: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
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
                        DevicesInMemory.updateDevice(device);
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
                urlConnection.disconnect();
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static class StatusGetterTest extends AsyncTask<Void, Void, Void>{
        private final String TAG = DashboardDevicesFragment.StatusGetterTest.class.getSimpleName();

        Device device;

        public StatusGetterTest(Device device) {
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
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
            }
            Log.d(TAG, "Disabling getStatus flag...");
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                URL url = new URL("http://ronixtech.com/ronix_services/task/srv.php");
                Log.d(TAG,  "statusGetterTest URL: " + url);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "statusGetterTest response: " + result.toString());
                    if(result.length() >= 10){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null){
                            try {
                                JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                String chipID = wifiStatus.getString("U_W_UID");
                                if(device.getChipID().length() >= 1) {
                                    if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                        MySettings.updateDeviceIP(device, "");
                                        MySettings.updateDeviceErrorCount(device, 0);
                                        MySettings.scanNetwork();
                                        return null;
                                    }
                                }

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
                                DevicesInMemory.updateDevice(device);
                                //MySettings.addDevice(device);
                            }catch (JSONException e){
                                Log.d(TAG, "Json exception: " + e.getMessage());
                            }finally {
                                Log.d(TAG, "Disabling getStatus flag...");
                                MySettings.setGetStatusState(false);
                            }
                        }
                    }

                } finally {
                    urlConnection.disconnect();
                    Log.d(TAG, "Disabling getStatus flag...");
                    MySettings.setGetStatusState(false);
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }finally {
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }
}
