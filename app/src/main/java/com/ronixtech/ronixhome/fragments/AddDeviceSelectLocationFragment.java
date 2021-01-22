package com.ronixtech.ronixhome.fragments;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceSelectLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceSelectLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceSelectLocationFragment extends Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener,
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
    EditText deviceNameText;

    Button doneButton;

    private Place selectedPlace;
    private Floor selectedFloor;
    private int selectedFloorIndex = 0;
    private Room selectedRoom;
    private WifiNetwork selectedWifiNetwork;

    private boolean ipAddressStatic = false;

    public AddDeviceSelectLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceSelectLocationFragment.
     */
    public static AddDeviceSelectLocationFragment newInstance(String param1, String param2) {
        AddDeviceSelectLocationFragment fragment = new AddDeviceSelectLocationFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_select_location, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.locate_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

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
        deviceNameText = view.findViewById(R.id.deviceName);

        doneButton = view.findViewById(R.id.done_button);

        ColorStateList colorStateList = ContextCompat.getColorStateList(getContext(), R.color.checkbox_states);
        CompoundButtonCompat.setButtonTintList(staticIPAddressCheckBox, colorStateList);

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

            selectedRoom=MySettings.getCurrentRoom();

            MySettings.setCurrentFloor(selectedPlace.getFloors().get(Integer.parseInt(selectedRoom.getFloorLevel())-1));

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
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_place_first), true);
                }else{
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
                    fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                    fragment.setParentFragment(AddDeviceSelectLocationFragment.this);
                    fragment.show(ft, "pickPlaceDialogFragment");
                }
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
                        /*if(MySettings.getFloorRooms(selectedFloor.getId()) == null || MySettings.getFloorRooms(selectedFloor.getId()).size() < 1){
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
                            fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                            fragment.setParentFragment(AddDeviceSelectLocationFragment.this);
                            fragment.show(ft, "pickRoomDialogFragment");
                        }*/
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment prev = getFragmentManager().findFragmentByTag("pickRoomDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickRoomDialogFragment fragment = PickRoomDialogFragment.newInstance();
                        fragment.setFloorID(selectedFloor.getId());
                        fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                        fragment.setParentFragment(AddDeviceSelectLocationFragment.this);
                        fragment.show(ft, "pickRoomDialogFragment");
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
                if(selectedPlace != null){
                    if(MySettings.getPlaceWifiNetworks(selectedPlace.getId()) != null && MySettings.getPlaceWifiNetworks(selectedPlace.getId()).size() >= 1){
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment prev = getFragmentManager().findFragmentByTag("wifiNetworkPickerDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickWifiNetworkDialogFragment fragment = PickWifiNetworkDialogFragment.newInstance();
                        fragment.setPlaceID(selectedPlace.getId());
                        fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                        fragment.setParentFragment(AddDeviceSelectLocationFragment.this);
                        fragment.show(ft, "wifiNetworkPickerDialogFragment");
                    }else{
                        //go to add wifi network sequence and then come back here
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
                        wifiInfoFragment.setSource(Constants.SOURCE_NEW_PLACE);
                        wifiInfoFragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
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
            }
        });

        /*if(validateInputs()){
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
        }*/

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    MySettings.updateWifiNetworkPlace(selectedWifiNetwork, selectedPlace.getId());

                    Device device = new Device();
                 //   device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                    device.setRoomID(selectedRoom.getId());
                    device.setStaticIPAddress(ipAddressStatic);
                    device.setName(deviceNameText.getText().toString());
                 //   MySettings.updateDeviceRoom(device, selectedRoom.getId());
                 //   MySettings.updateDeviceName(device,deviceNameText.getText().toString());
                    MySettings.setHomeNetwork(selectedWifiNetwork);
                    MySettings.setTempDevice(device);

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.commit();
/*
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentSendData addDeviceFragmentSendData = new AddDeviceFragmentSendData();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentSendData, "addDeviceFragmentSendData");
                    //fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                    fragmentTransaction.commitAllowingStateLoss(); */
                }
            }
        });

        return view;
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
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
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
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
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
        if(selectedWifiNetwork == null){
            inputsValid = false;
        }
        if(deviceNameText.getText().toString().matches(""))
        {
            inputsValid = false;
        }
        return inputsValid;
    }

    @Override
    public void onWifiNetworkSelected(WifiNetwork wifiNetwork){
        Utils.log(TAG, "AddDeviceSelectLocationFragment onWifiNetworkSelected - ID: " + wifiNetwork.getId() + " - SSID: " + wifiNetwork.getSsid(), true);
        if(wifiNetwork != null){
            selectedWifiNetwork = wifiNetwork;
            wifiNetworkNameTextView.setText(""+selectedWifiNetwork.getSsid());
            /*if(validateInputs()){
                Utils.setButtonEnabled(doneButton, true);
            }else{
                Utils.setButtonEnabled(doneButton, false);
            }*/
        }
    }

    @Override
    public void onNetworkAdded(WifiNetwork wifiNetwork){
        Utils.log(TAG, "AddDeviceSelectLocationFragment onNetworkAdded - ID: " + wifiNetwork.getId() + " - SSID: " + wifiNetwork.getSsid(), true);
        if(wifiNetwork != null){
            selectedWifiNetwork = wifiNetwork;
            wifiNetworkNameTextView.setText(""+selectedWifiNetwork.getSsid());
            /*if(validateInputs()){
                Utils.setButtonEnabled(doneButton, true);
            }else{
                Utils.setButtonEnabled(doneButton, false);
            }*/
        }
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
