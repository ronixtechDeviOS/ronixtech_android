package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

import java.util.Calendar;

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

    TextView nameTextView, macAddressTextView, typeTextView, lastSeenTextView, statusTextVuew, dhcpTextView, dhcpStaticTextView, ipAddressStaticTextView, ipAddressTextView, gatewayTextView, gatewayStaticTextView, subnetmaskTextView, subnetmaskStaticTextView, wifiVersionTextView, firmwareVersionTextView, onlineFirmwareVersionTextView, hwVersionTextView, hwFirmwareVersionTextView, onlineHWFirmwareVersionTextView, firmwareMessageTextView, temperatureTextView, beepStatusTextView, hwLockStatusTextView, accessTokenTextView, locaionTextView, linesTextView;

    private Device device;
    private int placeMode;

    private int logCounterMAX = 5;
    private int logCounter;

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
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.device_info), getResources().getColor(R.color.whiteColor));
            //MainActivity.setActionBarTitle(device.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.device_info), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        logCounter = 0;

        nameTextView = view.findViewById(R.id.device_name_textview);
        macAddressTextView = view.findViewById(R.id.device_mac_address_textview);
        typeTextView = view.findViewById(R.id.device_type_textview);
        lastSeenTextView = view.findViewById(R.id.device_last_seen_textview);
        statusTextVuew = view.findViewById(R.id.device_status_textview);
        dhcpStaticTextView = view.findViewById(R.id.device_dhcp_info_static_textview);
        dhcpTextView = view.findViewById(R.id.device_dhcp_info_textview);
        ipAddressStaticTextView = view.findViewById(R.id.device_ip_address_static_textview);
        ipAddressTextView = view.findViewById(R.id.device_ip_address_textview);
        gatewayStaticTextView = view.findViewById(R.id.device_dhcp_gateway_static_textview);
        gatewayTextView = view.findViewById(R.id.device_dhcp_gateway_textview);
        subnetmaskStaticTextView = view.findViewById(R.id.device_dhcp_subnet_mask_static_textview);
        subnetmaskTextView = view.findViewById(R.id.device_dhcp_subnet_mask_textview);
        wifiVersionTextView = view.findViewById(R.id.device_wifi_version_textview);
        firmwareVersionTextView = view.findViewById(R.id.device_firmware_version_textview);
        onlineFirmwareVersionTextView = view.findViewById(R.id.device_firmware_online_version_textview);
        hwVersionTextView = view.findViewById(R.id.device_hw_version_textview);
        hwFirmwareVersionTextView = view.findViewById(R.id.device_hw_firmware_version_textview);
        onlineHWFirmwareVersionTextView = view.findViewById(R.id.device_hw_firmware_online_version_textview);
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

            //show full date if not same day (if it's a new day)
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            long currentTimestamp = cal2.getTimeInMillis();
            cal1.setTimeInMillis(device.getLastSeenTimestamp());
            cal2.setTimeInMillis(currentTimestamp);
            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            if (!sameDay) {
                lastSeenTextView.setText(Utils.getTimeStringDateHoursMinutes(device.getLastSeenTimestamp()));
            }else{
                lastSeenTextView.setText(Utils.getTimeStringHoursMinutesSeconds(device.getLastSeenTimestamp()));
            }

            typeTextView.setText(Device.getDeviceTypeString(device.getDeviceTypeID()));
            if(device.isDeviceMQTTReachable()){
                statusTextVuew.setText(Utils.getString(getActivity(), R.string.device_mqtt_reachable));
                statusTextVuew.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
                dhcpTextView.setVisibility(View.GONE);
                dhcpStaticTextView.setVisibility(View.GONE);
                ipAddressTextView.setVisibility(View.GONE);
                ipAddressStaticTextView.setVisibility(View.GONE);
                gatewayStaticTextView.setVisibility(View.GONE);
                gatewayTextView.setVisibility(View.GONE);
                subnetmaskStaticTextView.setVisibility(View.GONE);
                subnetmaskTextView.setVisibility(View.GONE);
            }else{
                statusTextVuew.setText(Utils.getString(getActivity(), R.string.device_mqtt_unreachable));
                statusTextVuew.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                dhcpTextView.setVisibility(View.VISIBLE);
                dhcpStaticTextView.setVisibility(View.VISIBLE);
                ipAddressTextView.setVisibility(View.VISIBLE);
                ipAddressStaticTextView.setVisibility(View.VISIBLE);
                gatewayStaticTextView.setVisibility(View.VISIBLE);
                gatewayTextView.setVisibility(View.VISIBLE);
                subnetmaskStaticTextView.setVisibility(View.VISIBLE);
                subnetmaskTextView.setVisibility(View.VISIBLE);
                if(device.isStaticIPAddress()){
                    dhcpTextView.setText(Utils.getString(getActivity(), R.string.off));
                }else{
                    dhcpTextView.setText(Utils.getString(getActivity(), R.string.on));
                }
                if(device.getIpAddress().length() >= 1) {
                    ipAddressTextView.setText("" + device.getIpAddress());
                }else{
                    ipAddressTextView.setText("-");
                }
                if(device.getGateway().length() >= 1) {
                    gatewayTextView.setText("" + device.getGateway());
                }else{
                    gatewayTextView.setText("-");
                }
                if(device.getSubnetMask().length() >= 1) {
                    subnetmaskTextView.setText("" + device.getSubnetMask());
                }else{
                    subnetmaskTextView.setText("-");
                }
            }

            int wifiVersion = 0;
            if(device.getWifiVersion() != null && device.getWifiVersion().length() >= 1){
                wifiVersion = Integer.parseInt(device.getWifiVersion());
                wifiVersionTextView.setText(""+wifiVersion);
            }

            int currentWiFiVersion = 0, onlineWiFiVersion = 0;
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                currentWiFiVersion = Integer.parseInt(device.getFirmwareVersion());
                firmwareVersionTextView.setText(""+currentWiFiVersion);
            }

            if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()) != null && MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                onlineWiFiVersion = Integer.parseInt(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                onlineFirmwareVersionTextView.setText("" + onlineWiFiVersion);
            }else{
                onlineFirmwareVersionTextView.setText(Utils.getString(getActivity(), R.string.unable_to_obtain_online_firmware_version));
                onlineFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
            }

            int hwVersion = 0;
            if(device.getHwVersion() != null && device.getHwVersion().length() >= 1){
                hwVersion = Integer.parseInt(device.getHwVersion());
                hwVersionTextView.setText(""+hwVersion);
            }

            int currentHWVersion = 0, onlineHWVersion = 0;
            if(device.getHwFirmwareVersion() != null && device.getHwFirmwareVersion().length() >= 1){
                currentHWVersion = Integer.parseInt(device.getHwFirmwareVersion());
                hwFirmwareVersionTextView.setText(""+currentHWVersion);
            }

            if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()) != null && MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                onlineHWVersion = Integer.parseInt(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                onlineHWFirmwareVersionTextView.setText("" + onlineHWVersion);
            }else{
                onlineHWFirmwareVersionTextView.setText(Utils.getString(getActivity(), R.string.unable_to_obtain_online_firmware_version));
                onlineHWFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
            }


            //TODO add this later when HW upgrading works as expected
            boolean hwUpdateAvailable = false;
            if(currentHWVersion == onlineHWVersion && currentHWVersion != 0){
                hwUpdateAvailable = false;
                firmwareMessageTextView.setText(Utils.getString(getActivity(), R.string.firmware_up_to_date));

                hwFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                onlineHWFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
            }else{
                hwUpdateAvailable = true;
                firmwareMessageTextView.setText(Utils.getString(getActivity(), R.string.firmware_available));
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
            }


            if(!hwUpdateAvailable){
                if(currentWiFiVersion == onlineWiFiVersion && currentWiFiVersion != 0){
                    firmwareMessageTextView.setText(Utils.getString(getActivity(), R.string.firmware_up_to_date));

                    firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                    onlineFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                    firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
                }else{
                    firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                    if(currentWiFiVersion  <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                        firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                        firmwareMessageTextView.setText(Utils.getString(getActivity(), R.string.firmware_update_required));
                    }else{
                        firmwareMessageTextView.setText(Utils.getString(getActivity(), R.string.firmware_available));
                    }
                }
            }

            /*if(currentWiFiVersion == onlineWiFiVersion && currentWiFiVersion != 0){
                firmwareMessageTextView.setText(getActivity().getResources().getStringExtraInt(R.string.firmware_up_to_date));

                firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                onlineFirmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.blackColor));
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.greenColor));
            }else{
                firmwareMessageTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                if(currentWiFiVersion  <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    firmwareVersionTextView.setTextColor(getActivity().getResources().getColor(R.color.redColor));
                    firmwareMessageTextView.setText(getActivity().getResources().getStringExtraInt(R.string.firmware_update_required));
                }else{
                    firmwareMessageTextView.setText(getActivity().getResources().getStringExtraInt(R.string.firmware_available));
                }
            }*/


            if(device.getTemperature() != 0){
                temperatureTextView.setText(""+ device.getTemperature() + " \u2103");
            }else{
                temperatureTextView.setText("-");
            }

            if(device.isBeep()){
                beepStatusTextView.setText(Utils.getString(getActivity(), R.string.on));
            }else{
                beepStatusTextView.setText(Utils.getString(getActivity(), R.string.off));
            }

            if(device.isHwLock()){
                hwLockStatusTextView.setText(Utils.getString(getActivity(), R.string.on));
            }else{
                hwLockStatusTextView.setText(Utils.getString(getActivity(), R.string.off));
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
                        Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.device_update_disabled_only_local_mode), true);
                    }
                }
            });
        }

        accessTokenTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCounter++;
                if(logCounter >= logCounterMAX){
                    logCounter = 0;
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    LogViewerFragment logViewerFragment = new LogViewerFragment();
                    fragmentTransaction.replace(R.id.fragment_view, logViewerFragment, "logViewerFragment");
                    fragmentTransaction.addToBackStack("logViewerFragment");
                    fragmentTransaction.commit();
                }else{
                    Utils.showToast(getActivity(), Utils.getStringExtraInt(getActivity(), R.string.log_viewier_message, (logCounterMAX - logCounter)), false);
                }
            }
        });

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
