package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceInfoFragment extends android.support.v4.app.Fragment {
    private static final String TAG = DeviceInfoFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    TextView nameTextView, macAddressTextView, typeTextView, lastSeenTextView, statusTextVuew, ipAddressStaticTextView, ipAddressTextView, firmwareVersionTextView, onlineFirmwareVersionTextView, firmwareMessageTextView, temperatureTextView, beepStatusTextView, hwLockStatusTextView, accessTokenTextView, locaionTextView, linesTextView;

    private Device device;
    private int placeMode;

    public DeviceInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceInfoFragment.
     */
    public static DeviceInfoFragment newInstance(String param1, String param2) {
        DeviceInfoFragment fragment = new DeviceInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);
        if(device != null){
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.device_info), getResources().getColor(R.color.whiteColor));
            //MainActivity.setActionBarTitle(device.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.device_info), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        nameTextView = view.findViewById(R.id.device_name_textview);
        macAddressTextView = view.findViewById(R.id.device_mac_address_textview);
        typeTextView = view.findViewById(R.id.device_type_textview);
        lastSeenTextView = view.findViewById(R.id.device_last_seen_textview);
        statusTextVuew = view.findViewById(R.id.device_status_textview);
        ipAddressStaticTextView = view.findViewById(R.id.device_ip_address_static_textview);
        ipAddressTextView = view.findViewById(R.id.device_ip_address_textview);
        firmwareVersionTextView = view.findViewById(R.id.device_firmware_version_textview);
        onlineFirmwareVersionTextView = view.findViewById(R.id.device_firmware_online_version_textview);
        firmwareMessageTextView = view.findViewById(R.id.device_firmware_message_textview);
        temperatureTextView = view.findViewById(R.id.device_temperature_textview);
        beepStatusTextView = view.findViewById(R.id.device_beep_status_textview);
        hwLockStatusTextView = view.findViewById(R.id.device_hw_lock_status_textview);
        accessTokenTextView = view.findViewById(R.id.device_access_token_textview);
        locaionTextView = view.findViewById(R.id.device_location_textview);
        linesTextView = view.findViewById(R.id.device_lines_textview);

        if(device != null){
            nameTextView.setText(""+device.getName());
            macAddressTextView.setText(""+device.getMacAddress());
            lastSeenTextView.setText(Utils.getTimeStringHoursMinutesSeconds(device.getLastSeenTimestamp()));
            typeTextView.setText(device.getDeviceTypeString());
            if(device.isDeviceMQTTReachable()){
                statusTextVuew.setText(getActivity().getResources().getString(R.string.device_mqtt_reachable));
                statusTextVuew.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
                ipAddressTextView.setVisibility(View.GONE);
                ipAddressStaticTextView.setVisibility(View.GONE);
            }else{
                statusTextVuew.setText(getActivity().getResources().getString(R.string.device_mqtt_unreachable));
                statusTextVuew.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                ipAddressTextView.setVisibility(View.VISIBLE);
                ipAddressStaticTextView.setVisibility(View.VISIBLE);
                if(device.getIpAddress().length() >= 1) {
                    ipAddressTextView.setText("" + device.getIpAddress());
                }else{
                    ipAddressTextView.setText("-");
                }
            }

            int currentVersion = 0, onlineVersion = 0;
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                currentVersion = Integer.parseInt(device.getFirmwareVersion());
                firmwareVersionTextView.setText(""+currentVersion);
            }

            if(MySettings.getDeviceLatestFirmwareVersion(device.getDeviceTypeID()) != null && MySettings.getDeviceLatestFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                onlineVersion = Integer.parseInt(MySettings.getDeviceLatestFirmwareVersion(device.getDeviceTypeID()));
                onlineFirmwareVersionTextView.setText("" + onlineVersion);
            }else{
                onlineFirmwareVersionTextView.setText(getActivity().getResources().getString(R.string.unable_to_obtain_online_firmware_version));
                onlineFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
            }

            if(currentVersion == onlineVersion && currentVersion != 0){
                firmwareMessageTextView.setText(getActivity().getResources().getString(R.string.firmware_up_to_date));

                firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                onlineFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
            }else{
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                if(currentVersion  <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                    firmwareMessageTextView.setText(getActivity().getResources().getString(R.string.firmware_update_required));
                }else{
                    firmwareMessageTextView.setText(getActivity().getResources().getString(R.string.firmware_available));
                }
            }

            if(device.getTemperature() != 0){
                temperatureTextView.setText(""+ device.getTemperature() + " \u2103");
            }else{
                temperatureTextView.setText("-");
            }

            if(device.isBeep()){
                beepStatusTextView.setText(getActivity().getResources().getString(R.string.on));
            }else{
                beepStatusTextView.setText(getActivity().getResources().getString(R.string.off));
            }

            if(device.isHwLock()){
                hwLockStatusTextView.setText(getActivity().getResources().getString(R.string.on));
            }else{
                hwLockStatusTextView.setText(getActivity().getResources().getString(R.string.off));
            }

            accessTokenTextView.setText(""+device.getAccessToken());

            Room room = MySettings.getRoom(device.getRoomID());
            Floor floor = MySettings.getFloor(room.getFloorID());
            locaionTextView.setText("" + floor.getPlaceName() + ":" /*+ floor.getName() + ":"*/ + room.getName());

            if(device.getLines() != null) {
                for (Line line : device.getLines()) {
                    linesTextView.append("Device: " + line.getName() + "\n");
                }
            }

            firmwareMessageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                        MySettings.setTempDevice(device);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                        fragmentTransaction.commit();
                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        return view;
    }

    public void setDevice(Device device){
        this.device = device;
    }

    public void setPlaceMode(int placeMode){
        this.placeMode = placeMode;
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
