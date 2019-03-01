package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditDeviceLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditDeviceLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDeviceLocationFragment extends android.support.v4.app.Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener,
        PickRoomDialogFragment.OnRoomSelectedListener,
        PickWifiNetworkDialogFragment.OnNetworkSelectedListener,
        WifiInfoFragment.OnNetworkAddedListener{
    private static final String TAG = AddDeviceSelectLocationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout placeSelectionLayout, selectedFloorLayout, roomSelectionLayout, wifiNetworkSelectionLayout;
    TextView placeNameTextView, roomNameTextView, wifiNetworkNameTextView;
    ImageView placeImageView, roomImageView;
    TextView selectedFloorTextView;
    Button incrementFloorButton, decremetnFloorButton;
    CheckBox staticIPAddressCheckBox;

    Button saveButton;

    Device device;

    private Place selectedPlace;
    private Floor selectedFloor;
    private int selectedFloorIndex = 0;
    private Room selectedRoom;
    private WifiNetwork selectedWifiNetwork;

    private boolean ipAddressStatic = true;

    private int placeMode;

    public EditDeviceLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditDeviceLocationFragment.
     */
    public static EditDeviceLocationFragment newInstance(String param1, String param2) {
        EditDeviceLocationFragment fragment = new EditDeviceLocationFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_device_location, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.locate_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        device = MySettings.getTempDevice();

        placeSelectionLayout = view.findViewById(R.id.place_selection_layout);
        placeNameTextView = view.findViewById(R.id.selected_place_name_textview);
        placeImageView = view.findViewById(R.id.selected_place_image_view);
        selectedFloorLayout = view.findViewById(R.id.floor_layout);
        selectedFloorTextView = view.findViewById(R.id.selected_floor_textview);
        roomSelectionLayout = view.findViewById(R.id.room_selection_layout);
        roomNameTextView = view.findViewById(R.id.selected_room_name_textview);
        roomImageView = view.findViewById(R.id.selected_room_image_view);
        wifiNetworkSelectionLayout = view.findViewById(R.id.wifi_network_selection_layout);
        wifiNetworkNameTextView = view.findViewById(R.id.selected_wifi_network_name_textview);
        incrementFloorButton = view.findViewById(R.id.increment_button);
        decremetnFloorButton = view.findViewById(R.id.decrement_button);
        staticIPAddressCheckBox = view.findViewById(R.id.static_ip_address_checkbox);

        saveButton = view.findViewById(R.id.save_button);

        ColorStateList colorStateList = ContextCompat.getColorStateList(getContext(), R.color.checkbox_states);
        CompoundButtonCompat.setButtonTintList(staticIPAddressCheckBox, colorStateList);

        selectedRoom = MySettings.getRoom(device.getRoomID());
        selectedFloor = MySettings.getFloor(selectedRoom.getFloorID());
        selectedPlace = MySettings.getPlace(selectedFloor.getPlaceID());

        if(selectedPlace == null){
            if(MySettings.getCurrentPlace() != null) {
                selectedPlace = MySettings.getPlace(MySettings.getCurrentPlace().getId());
            }
        }
        if(selectedPlace != null){
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1){
                    placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                }
            }

            if(MySettings.getCurrentFloor() != null){
                selectedFloor = MySettings.getFloor(MySettings.getCurrentFloor().getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getName());
            }else{
                selectedFloor = MySettings.getFloor(selectedPlace.getFloors().get(0).getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getName());
            }
        }

        if(selectedRoom == null){
            if(MySettings.getCurrentRoom() != null) {
                selectedRoom = MySettings.getRoom(MySettings.getCurrentRoom().getId());
                if(selectedRoom != null){
                    if(selectedRoom.getFloorID() != selectedFloor.getId()){
                        selectedRoom = null;
                        roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                        roomImageView.setImageResource(R.drawable.room_icon);
                    }
                }
            }
        }
        if(selectedRoom != null){
            roomNameTextView.setText(selectedRoom.getName());
            if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoom.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                        .into(roomImageView);
            }else {
                if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                    roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                }
            }
        }

        if(selectedWifiNetwork != null) {
            wifiNetworkNameTextView.setText("" + selectedWifiNetwork.getSsid());
        }

        staticIPAddressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ipAddressStatic = isChecked;
            }
        });

        placeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.add_place_first), Toast.LENGTH_SHORT).show();
                }else{
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
                    fragment.setTargetFragment(EditDeviceLocationFragment.this, 0);
                    fragment.setParentFragment(EditDeviceLocationFragment.this);
                    fragment.show(ft, "pickPlaceDialogFragment");
                }*/
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.edit_device_place_message), true);
            }
        });

        incrementFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex < selectedPlace.getFloors().size() - 1){
                        selectedFloorIndex++;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                        if(selectedRoom != null){
                            if(selectedRoom.getFloorID() != selectedFloor.getId()){
                                selectedRoom = null;
                                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                                roomImageView.setImageResource(R.drawable.room_icon);
                            }
                        }
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        decremetnFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex >= 1){
                        selectedFloorIndex--;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                        if(selectedRoom != null){
                            if(selectedRoom.getFloorID() != selectedFloor.getId()){
                                selectedRoom = null;
                                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                                roomImageView.setImageResource(R.drawable.room_icon);
                            }
                        }
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        roomSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloor != null){
                        if(MySettings.getFloorRooms(selectedFloor.getId()) == null || MySettings.getFloorRooms(selectedFloor.getId()).size() < 1){
                            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                        }else{
                            // DialogFragment.show() will take care of adding the fragment
                            // in a transaction.  We also want to remove any currently showing
                            // dialog, so make our own transaction and take care of that here.
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickRoomDialogFragment");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);

                            // Create and show the dialog.
                            PickRoomDialogFragment fragment = PickRoomDialogFragment.newInstance();
                            fragment.setFloorID(selectedFloor.getId());
                            fragment.setTargetFragment(EditDeviceLocationFragment.this, 0);
                            fragment.setParentFragment(EditDeviceLocationFragment.this);
                            fragment.show(ft, "pickRoomDialogFragment");
                        }
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(selectedFloorLayout);
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        wifiNetworkSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(placeMode == Place.PLACE_MODE_LOCAL){
                    if(selectedPlace != null){
                        if(MySettings.getPlaceWifiNetworks(selectedPlace.getId()) != null && MySettings.getPlaceWifiNetworks(selectedPlace.getId()).size() >= 1){
                            // DialogFragment.show() will take care of adding the fragment
                            // in a transaction.  We also want to remove any currently showing
                            // dialog, so make our own transaction and take care of that here.
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("wifiNetworkPickerDialogFragment");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);

                            // Create and show the dialog.
                            PickWifiNetworkDialogFragment fragment = PickWifiNetworkDialogFragment.newInstance();
                            fragment.setPlaceID(selectedPlace.getId());
                            fragment.setTargetFragment(EditDeviceLocationFragment.this, 0);
                            fragment.setParentFragment(EditDeviceLocationFragment.this);
                            fragment.show(ft, "wifiNetworkPickerDialogFragment");
                        }else{
                            //go to add wifi network sequence and then come back here
                            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
                            wifiInfoFragment.setSource(Constants.SOURCE_NEW_PLACE);
                            wifiInfoFragment.setTargetFragment(EditDeviceLocationFragment.this, 0);
                            fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
                            fragmentTransaction.addToBackStack("wifiInfoFragment");
                            fragmentTransaction.commit();
                        }
                    }else{
                        Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.select_place_first), true);
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(placeSelectionLayout);
                    }
                }else if(placeMode == Place.PLACE_MODE_REMOTE){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.device_edit_location_ssid_disabled_only_local_mode), true);
                }
            }
        });

        /*if(validateInputs()){
            Utils.setButtonEnabled(saveButton, true);
        }else{
            Utils.setButtonEnabled(saveButton, false);
        }*/

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                    device.setRoomID(selectedRoom.getId());
                    device.setStaticIPAddress(ipAddressStatic);
                    MySettings.updateDeviceRoom(device, selectedRoom.getId());

                    DevicesInMemory.updateDevice(device);

                    if(placeMode == Place.PLACE_MODE_LOCAL && selectedWifiNetwork != null) {
                        Utils.showLoading(getActivity());
                        MySettings.updateWifiNetworkPlace(selectedWifiNetwork, selectedPlace.getId());
                        MySettings.setHomeNetwork(selectedWifiNetwork);
                        WiFiDataSenderGet wiFiDataSenderGet = new WiFiDataSenderGet(getActivity(), EditDeviceLocationFragment.this, device);
                        wiFiDataSenderGet.execute();
                    }else{
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commit();
                    }
                }
            }
        });

        return view;
    }

    public void setPlaceMode(int placeMode){
        this.placeMode = placeMode;
    }

    @Override
    public void onPlaceSelected(Place place){
        if(place != null){
            this.selectedPlace = MySettings.getPlace(place.getId());
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1){
                    placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                }
            }
            selectedFloorIndex = 0;
            selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
            if(selectedFloor != null){
                selectedFloorTextView.setText(""+selectedFloor.getName());
                if(MySettings.getFloorRooms(selectedFloor.getId()) != null && MySettings.getFloorRooms(selectedFloor.getId()).size() >= 1){
                    selectedRoom = MySettings.getRoom(MySettings.getFloorRooms(selectedFloor.getId()).get(0).getId());
                    if(selectedRoom != null){
                        roomNameTextView.setText(selectedRoom.getName());
                        if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                            GlideApp.with(getActivity())
                                    .load(selectedRoom.getType().getImageUrl())
                                    .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                                    .into(roomImageView);
                        }else {
                            if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                                roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                            }else{
                                roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                            }
                        }
                    }else{
                        selectedRoom = null;
                        roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                        roomImageView.setImageResource(R.drawable.room_icon);
                    }
                }else{
                    selectedRoom = null;
                    roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                    roomImageView.setImageResource(R.drawable.room_icon);
                }
            }else{
                selectedRoom = null;
                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                roomImageView.setImageResource(R.drawable.room_icon);
            }
        }

        /*if(validateInputs()){
            Utils.setButtonEnabled(saveButton, true);
        }else{
            Utils.setButtonEnabled(saveButton, false);
        }*/
    }

    @Override
    public void onRoomSelected(Room room){
        if(room != null){
            this.selectedRoom = MySettings.getRoom(room.getId());
            roomNameTextView.setText(selectedRoom.getName());
            if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoom.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                        .into(roomImageView);
            }else {
                if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                    roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                }
            }
        }
        /*if(validateInputs()){
            Utils.setButtonEnabled(saveButton, true);
        }else{
            Utils.setButtonEnabled(saveButton, false);
        }*/
    }

    private boolean validateInputs(){
        boolean inputsValid = true;
        if(selectedPlace == null){
            inputsValid = false;
        }
        if(selectedFloor == null){
            inputsValid = false;
        }
        if(selectedRoom == null){
            inputsValid = false;
        }
        /*if(selectedWifiNetwork == null){
            inputsValid = false;
        }*/

        return inputsValid;
    }

    @Override
    public void onWifiNetworkSelected(WifiNetwork wifiNetwork){
        Utils.log(TAG, "EditDeviceSelectLocationFragment onWifiNetworkSelected - ID: " + wifiNetwork.getId() + " - SSID: " + wifiNetwork.getSsid(), true);
        if(wifiNetwork != null){
            selectedWifiNetwork = wifiNetwork;
            wifiNetworkNameTextView.setText(""+selectedWifiNetwork.getSsid());
            /*if(validateInputs()){
                Utils.setButtonEnabled(saveButton, true);
            }else{
                Utils.setButtonEnabled(saveButton, false);
            }*/
        }
    }

    @Override
    public void onNetworkAdded(WifiNetwork wifiNetwork){
        Utils.log(TAG, "EditDeviceSelectLocationFragment onNetworkAdded - ID: " + wifiNetwork.getId() + " - SSID: " + wifiNetwork.getSsid(), true);
        if(wifiNetwork != null){
            selectedWifiNetwork = wifiNetwork;
            wifiNetworkNameTextView.setText(""+selectedWifiNetwork.getSsid());
            /*if(validateInputs()){
                Utils.setButtonEnabled(saveButton, true);
            }else{
                Utils.setButtonEnabled(saveButton, false);
            }*/
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

   /* @Override
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

    public static class WiFiDataSenderGet extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        EditDeviceLocationFragment fragment;

        Device device;

        public WiFiDataSenderGet(Activity activity, EditDeviceLocationFragment fragment, Device device) {
            this.activity = activity;
            this.fragment = fragment;
            this.device = device;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            Utils.dismissLoading();

            if(statusCode == 200){
                Device device = MySettings.getTempDevice();
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment, device);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    //reboot the device
                    /*final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterPost deviceRebooterPost = new DeviceRebooterPost(activity, fragment);
                            deviceRebooterPost.execute();
                        }
                    }, 1000);*/
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    //reboot the device
                   /* final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);*/
                }else{
                    //reboot the device
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DeviceRebooterGet deviceRebooterGet = new DeviceRebooterGet(activity, fragment, device);
                            deviceRebooterGet.execute();
                        }
                    }, 1000);
                }
            }else{
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.SEND_SSID_PASSWORD_URL;
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

    public static class DeviceRebooterGet extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        EditDeviceLocationFragment fragment;

        Device device;

        public DeviceRebooterGet(Activity activity, EditDeviceLocationFragment fragment, Device device) {
            this.activity = activity;
            this.fragment = fragment;
            this.device = device;
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
                if(MainActivity.isResumed) {
                    FragmentManager fragmentManager = fragment.getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    URL url = new URL("http://" + device.getIpAddress() + Constants.DEVICE_REBOOT_URL);
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
}
