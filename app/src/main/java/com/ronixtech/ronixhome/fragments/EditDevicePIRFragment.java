package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.LinePIRConfigurationAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;

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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditDevicePIRFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditDevicePIRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDevicePIRFragment extends android.support.v4.app.Fragment implements PickLineDialogFragment.OnLineSelectedListener{
    private static final String TAG = EditDevicePIRFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    EditText deviceNameEditText;

    RelativeLayout lineSelectionLayout;
    TextView lineNameTextView;
    ImageView lineImageView;

    LinePIRConfigurationAdapter adapter;
    ListView selectedLinesListView;
    List<Line> selectedLines;
    Button saveButton;

    Device device;

    public EditDevicePIRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditDevicePIRFragment.
     */
    public static EditDevicePIRFragment newInstance(String param1, String param2) {
        EditDevicePIRFragment fragment = new EditDevicePIRFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_device_pir, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.edit_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        deviceNameEditText = view.findViewById(R.id.device_name_edittext);

        lineSelectionLayout = view.findViewById(R.id.line_selection_layout);
        lineNameTextView = view.findViewById(R.id.selected_line_name_textview);
        lineImageView = view.findViewById(R.id.selected_line_image_view);
        selectedLinesListView = view.findViewById(R.id.selected_lines_listview);
        selectedLines = new ArrayList<>();
        adapter = new LinePIRConfigurationAdapter(getActivity(), selectedLines, new LinePIRConfigurationAdapter.OnLineRemovedListener() {
            @Override
            public void onLineRemoved() {
                adapter.notifyDataSetChanged();
                Utils.justifyListViewHeightBasedOnChildren(selectedLinesListView);
            }
        });
        selectedLinesListView.setAdapter(adapter);

        saveButton = view.findViewById(R.id.save_button);

        device = MySettings.getTempDevice();
        if(device == null){
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.error_adding_smart_controller), true);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        if(device != null) {
            this.selectedLines.addAll(device.getLines());
            adapter.notifyDataSetChanged();
            Utils.justifyListViewHeightBasedOnChildren(selectedLinesListView);

            deviceNameEditText.setText(device.getName());
        }

        lineSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickLineDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    PickLineDialogFragment fragment = PickLineDialogFragment.newInstance();
                    fragment.setTargetFragment(EditDevicePIRFragment.this, 0);
                    fragment.show(ft, "pickLineDialogFragment");
                }else{
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_devices_first), true);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    //remove device
                    MySettings.removeDevice(device);
                    device.setLines(null);

                    //initialize the pirdata configuration for the device
                    //create the lines and pirData.setLines then MySettings.addDevice()

                    if(deviceNameEditText.getText().toString().length() > 1){
                        device.setName(deviceNameEditText.getText().toString());
                    }else{
                        device.setName(Utils.getString(getActivity(), R.string.pir_controller_name_hint));
                    }

                    Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                    if(dbDevice == null){
                        MySettings.addDevice(device);
                        device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                    }

                    if(deviceNameEditText.getText().toString().length() > 1){
                        device.setName(deviceNameEditText.getText().toString());
                    }else{
                        device.setName(Utils.getString(getActivity(), R.string.pir_controller_name_hint));
                    }

                    PIRData pirData = new PIRData();
                    pirData.setDeviceID(device.getId());
                    device.setPIRData(pirData);

                    List<Line> lines = new ArrayList<>();
                    for (Line line:selectedLines) {
                        Line newLine = new Line(line);
                        newLine.setId(0);
                        newLine.setDeviceID(device.getId());
                        lines.add(newLine);
                    }
                    device.setLines(lines);

                    MySettings.addDevice(device);

                    device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                    MySettings.setTempDevice(device);

                    Utils.showLoading(getActivity());

                    PIRResetPairings pirResetPairings = new PIRResetPairings(getActivity(), EditDevicePIRFragment.this, device);
                    pirResetPairings.execute();
                }
            }
        });

        return view;
    }

    @Override
    public void onLineSelected(Line line){
        if(line != null && !selectedLines.contains(line)){
            if(selectedLines.size() >= 9){
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.pir_max_devices_reached), true);
                return;
            }

            Device primaryDevice = MySettings.getDeviceByID2(line.getDeviceID());
            if(primaryDevice != null){
                line.setPrimaryDeviceChipID(primaryDevice.getChipID());
            }

            line.setPirPowerState(Line.LINE_STATE_ON);
            line.setPirDimmingState(Line.DIMMING_STATE_ON);
            line.setPirDimmingValue(10);

            this.selectedLines.add(line);
            adapter.notifyDataSetChanged();
            Utils.justifyListViewHeightBasedOnChildren(selectedLinesListView);
            /*lineNameTextView.setText(line.getName());
            if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(line.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.device_colored_icon))
                        .into(lineImageView);
            }else {
                lineImageView.setImageResource(line.getType().getImageResourceID());
            }*/
        }

        /*if(validateInputs()){
            Utils.setButtonEnabled(saveButton, true);
        }else{
            Utils.setButtonEnabled(saveButton, false);
        }*/
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        /*if(selectedLines == null || selectedLines.size() <= 0){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(lineSelectionLayout);
        }*/

        return inputsValid;
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

    public static class PIRResetPairings extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        EditDevicePIRFragment fragment;
        Device device;

        public PIRResetPairings(Activity activity, EditDevicePIRFragment fragment, Device device) {
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
            if(statusCode == 200) {
                PIRAddPairings pirAddPairings = new PIRAddPairings(activity, fragment, device);
                pirAddPairings.execute();
            }else{
                Utils.dismissLoading();
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    if(fragment.getFragmentManager() != null) {
                        fragment.getFragmentManager().popBackStack();
                    }
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
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    URL url = new URL(urlString);
                    Utils.log(TAG, "resetPairings URL: " + url, true);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("U_P_STT", "0");
                    jsonObject.put("U_P_CID", "0");

                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "resetPairings POST data: " + jsonObject.toString(), true);

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "resetPairings response: " + result.toString(), true);
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
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static class PIRAddPairings extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        EditDevicePIRFragment fragment;
        Device device;

        public PIRAddPairings(Activity activity, EditDevicePIRFragment fragment, Device device) {
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
            if(statusCode == 200) {
                Utils.dismissLoading();
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    if(fragment.getFragmentManager() != null) {
                        fragment.getFragmentManager().popBackStack();
                    }
                }
            }else{
                Utils.dismissLoading();
                Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), true);
                if(MainActivity.getInstance() != null && MainActivity.isResumed) {
                    if(fragment.getFragmentManager() != null) {
                        fragment.getFragmentManager().popBackStack();
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            if(device.getLines() != null && device.getLines().size() >= 1){
                for (Line line:device.getLines()) {
                    statusCode = 0;
                    int numberOfRetries = 0;
                    while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                        try{
                            String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                            URL url = new URL(urlString);
                            Utils.log(TAG, "addPairing URL: " + url, true);

                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                            urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                            urlConnection.setDoOutput(true);
                            urlConnection.setDoInput(true);
                            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            urlConnection.setRequestProperty("Accept", "application/json");
                            urlConnection.setRequestMethod("POST");

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("U_P_STT", "1");
                            jsonObject.put("U_P_CID", line.getPrimaryDeviceChipID());
                            jsonObject.put("U_P_CIP", MySettings.getDeviceByChipID2(line.getPrimaryDeviceChipID()).getIpAddress());
                            jsonObject.put("U_P_LNO", ""+line.getPosition());
                            jsonObject.put("U_P_TYP", ""+MySettings.getDeviceByChipID2(line.getPrimaryDeviceChipID()).getDeviceTypeID());
                            if(line.getPirPowerState() == Line.LINE_STATE_ON){
                                if(line.getPirDimmingValue() == 10){
                                    jsonObject.put("U_P_LVN", ":");
                                }else{
                                    jsonObject.put("U_P_LVN", ""+line.getPirDimmingValue());
                                }
                                jsonObject.put("U_P_LVF", "0");
                            }else if(line.getPirPowerState() == Line.LINE_STATE_OFF){
                                jsonObject.put("U_P_LVN", "0");
                                jsonObject.put("U_P_LVF", ":");
                            }
                            //TODO send U_P_DUR in SECONDS not MILLISECONDS
                            jsonObject.put("U_P_DUR", "" +  Utils.getTimeUnitMilliseconds(line.getPirTriggerActionDurationTimeUnit(), line.getPirTriggerActionDuration()));

                            jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                            Utils.log(TAG, "addPairing POST data: " + jsonObject.toString(), true);

                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                            outputStreamWriter.write(jsonObject.toString());
                            outputStreamWriter.flush();
                            outputStreamWriter.close();

                            statusCode = urlConnection.getResponseCode();
                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder result = new StringBuilder();
                            String dataLine;
                            while((dataLine = bufferedReader.readLine()) != null) {
                                result.append(dataLine);
                            }
                            urlConnection.disconnect();
                            Utils.log(TAG, "addPairing response: " + result.toString(), true);
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
                            numberOfRetries++;
                        }
                    }
                }
            }else{
                statusCode = 200;
            }

            return null;
        }
    }
}
