package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Type;

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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationPreparingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationPreparingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationPreparingFragment extends android.support.v4.app.Fragment {
    private static final String TAG = AddDeviceConfigurationPreparingFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    DonutProgress progressCircle;

    Device device;

    CountDownTimer loadingCountDownTimer;

    public AddDeviceConfigurationPreparingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationPreparingFragment.
     */
    public static AddDeviceConfigurationPreparingFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationPreparingFragment fragment = new AddDeviceConfigurationPreparingFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_preparing, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.configure_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        progressCircle = view.findViewById(R.id.progress_circle);

        device = MySettings.getTempDevice();

        if(device != null) {
            //get current device configuration
            getDeviceConfig(device);
        }

        return view;
    }

    private void getDeviceConfig(Device device){
        /** CountDownTimer starts with 60 seconds and every onTick is 1 second */
        final int totalMillis = 1 * 60 * 1000; // 60 seconds in milli seconds
        loadingCountDownTimer = new CountDownTimer(totalMillis, 1) {
            public void onTick(long millisUntilFinished) {

                //forward progress
                long finishedMillis = totalMillis - millisUntilFinished;
                int totalProgress = (int) (((float)finishedMillis / (float)totalMillis) * 100.0);

                long totalSeconds =  Math.round(((double)finishedMillis/(double)totalMillis) * 60);

                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    progressCircle.setDonut_progress("" + totalProgress);
                    //progressCircle.setText(getActivity().getResources().getStringExtraInt(R.string.seconds, 60 - (int) totalSeconds));
                    progressCircle.setText("" + totalProgress + "%");
                }
            }

            public void onFinish() {
                // DO something when 60 seconds are up
                goToSearchFragment();
            }
        }.start();

        DeviceConfigGetter deviceChecker = new DeviceConfigGetter(this, device);
        deviceChecker.execute();
    }

    public void goToSearchFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack("addDeviceFragmentIntro", 0);
            }
        }
    }

    public void goToConfigurationFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationFragment addDeviceConfigurationFragment = new AddDeviceConfigurationFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationFragment, "addDeviceConfigurationFragment");
                //fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                fragmentTransaction.commitAllowingStateLoss();
            }
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

    public class DeviceConfigGetter extends AsyncTask<Void, Void, Void> {
        private final String TAG = AddDeviceConfigurationPreparingFragment.DeviceConfigGetter.class.getSimpleName();

        Device device;
        int statusCode;

        AddDeviceConfigurationPreparingFragment fragment;

        public DeviceConfigGetter(AddDeviceConfigurationPreparingFragment addDeviceConfigurationPreparingFragment, Device device) {
            this.device = device;
            this.fragment = addDeviceConfigurationPreparingFragment;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200){
                loadingCountDownTimer.cancel();
                MySettings.setTempDevice(device);
                /*Log.d(TAG, "onPostExecute - Adding new device...");
                MySettings.addDevice(device);
                Device dbDevice = MySettings.getDeviceByChipID2(device.getChipID());
                Log.d(TAG, "onPostExecute - Added new device. deviceID: " + dbDevice.getId());*/
                fragment.goToConfigurationFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                String urlString = Constants.DEVICE_URL + Constants.DEVICE_STATUS_CONTROL_URL;

                Utils.log(TAG, "deviceConfigGetter URL: " + urlString, true);

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
                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);
                Utils.log(TAG, "deviceConfigGetter POST data: " + jObject.toString(), true);


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
                Utils.log(TAG, "deviceConfigGetter response: " + result.toString(), true);
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                            if(wifiStatus != null) {
                                if(wifiStatus.has("U_W_UID")) {
                                    /*String chipID = wifiStatus.getString("U_W_UID");
                                    if (device.getChipID().length() >= 1) {
                                        if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                            MySettings.updateDeviceIP(device, "");
                                            MySettings.updateDeviceErrorCount(device, 0);
                                            MySettings.scanNetwork();
                                            MainActivity.getInstance().refreshDeviceListFromDatabase();
                                            return null;
                                        }
                                    }*/
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

                                /*if(wifiStatus.has("R_W_DHC")){
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
                                }*/
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

                                boolean firstLinePreset = false, secondLinePresent = false, thirdLinePresent = false;

                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    firstLinePreset = true;
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    secondLinePresent = true;
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    thirdLinePresent = true;
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


                                //init device lines then insert it into DB
                                Type lineType = MySettings.getTypeByName("Fluorescent Lamp");
                                int lineMode = Line.MODE_PRIMARY;

                                //create the lines then device.setLines/line.setDeviceID then MySettings.addDevice()
                                Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                                if(dbDevice == null){
                                    MySettings.addDevice(device);
                                    dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                                }

                                long deviceID = dbDevice.getId();
                                device.setId(deviceID);

                                Utils.log(TAG, "Adding device, deviceID = " + deviceID, true);

                                //loop over the number of lines
                                if(firstLinePreset){
                                    Line line = new Line();
                                    line.setPosition(0);
                                    line.setName(Utils.getString(getActivity(), R.string.line_1_name_hint));
                                    line.setPowerState(line0PowerState);
                                    line.setDimmingState(line0DimmingState);
                                    line.setDimmingVvalue(line0DimmingValue);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
                                }

                                if(secondLinePresent){
                                    Line line = new Line();
                                    line.setPosition(1);
                                    line.setName(Utils.getString(getActivity(), R.string.line_2_name_hint));
                                    line.setPowerState(line1PowerState);
                                    line.setDimmingState(line1DimmingState);
                                    line.setDimmingVvalue(line1DimmingValue);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
                                }

                                if(thirdLinePresent){
                                    Line line = new Line();
                                    line.setPosition(2);
                                    line.setName(Utils.getString(getActivity(), R.string.line_3_name_hint));
                                    line.setPowerState(line2PowerState);
                                    line.setDimmingState(line2DimmingState);
                                    line.setDimmingVvalue(line2DimmingValue);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
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

                                boolean firstLinePreset = false, secondLinePresent = false, thirdLinePresent = false;

                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    firstLinePreset = true;
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    secondLinePresent = true;
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    thirdLinePresent = true;
                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                }

                                //init device lines then insert it into DB
                                Type lineType = MySettings.getTypeByName("Appliance Plug");
                                int lineMode = Line.MODE_PRIMARY;

                                //create the lines then device.setLines/line.setDeviceID then MySettings.addDevice()
                                Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                                if(dbDevice == null){
                                    MySettings.addDevice(device);
                                    dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                                }

                                long deviceID = dbDevice.getId();
                                device.setId(deviceID);

                                Utils.log(TAG, "Adding device, deviceID = " + deviceID, true);

                                //loop over the number of lines
                                if(firstLinePreset){
                                    Line line = new Line();
                                    line.setPosition(0);
                                    line.setName(Utils.getString(getActivity(), R.string.line_1_name_hint));
                                    line.setPowerState(line0PowerState);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
                                }

                                if(secondLinePresent){
                                    Line line = new Line();
                                    line.setPosition(1);
                                    line.setName(Utils.getString(getActivity(), R.string.line_2_name_hint));
                                    line.setPowerState(line1PowerState);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
                                }

                                if(thirdLinePresent){
                                    Line line = new Line();
                                    line.setPosition(2);
                                    line.setName(Utils.getString(getActivity(), R.string.line_3_name_hint));
                                    line.setPowerState(line2PowerState);

                                    line.setTypeID(lineType.getId());
                                    line.setMode(lineMode);

                                    line.setDeviceID(deviceID);

                                    device.getLines().add(line);
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
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }
                    }
                }
            }catch (MalformedURLException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }catch (IOException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }catch (JSONException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
    }
}
