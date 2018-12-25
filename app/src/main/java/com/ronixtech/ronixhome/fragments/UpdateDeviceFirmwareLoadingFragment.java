package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
 * {@link UpdateDeviceFirmwareLoadingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDeviceFirmwareLoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDeviceFirmwareLoadingFragment extends android.support.v4.app.Fragment {
    private static final String TAG = UpdateDeviceFirmwareLoadingFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    DonutProgress progressCircle;

    Device device;

    CountDownTimer loadingCountDownTimer;

    public UpdateDeviceFirmwareLoadingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDeviceFirmwareLoadingFragment.
     */
    public static UpdateDeviceFirmwareLoadingFragment newInstance(String param1, String param2) {
        UpdateDeviceFirmwareLoadingFragment fragment = new UpdateDeviceFirmwareLoadingFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_device_firmware_loading, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.updating_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        progressCircle = view.findViewById(R.id.progress_circle);

        device = MySettings.getTempDevice();

        if(device != null) {
            //loop to check for device
            checkDevice(device);
        }

        return view;
    }

    private void checkDevice(Device device){
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
                goToHomeFragment();
            }
        }.start();

        DeviceChecker deviceChecker = new DeviceChecker(this, device);
        deviceChecker.execute();
    }

    private void goToHomeFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if(getFragmentManager() != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        }
    }

    private void goToDownloadFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if(getFragmentManager() != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                UpdateDeviceFirmwareDownloadFragment updateDeviceFirmwareDownloadFragment = new UpdateDeviceFirmwareDownloadFragment();
                fragmentTransaction.replace(R.id.fragment_view, updateDeviceFirmwareDownloadFragment, "updateDeviceFirmwareDownloadFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
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

    public class DeviceChecker extends AsyncTask<Void, Void, Void> {
        private final String TAG = UpdateDeviceFirmwareLoadingFragment.DeviceChecker.class.getSimpleName();

        Device device;
        boolean ronixUnit = true;
        int statusCode;

        UpdateDeviceFirmwareLoadingFragment fragment;

        public DeviceChecker(UpdateDeviceFirmwareLoadingFragment updateDeviceFirmwareLoadingFragment, Device device) {
            this.device = device;
            this.fragment = updateDeviceFirmwareLoadingFragment;
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
            if(statusCode == 200 && ronixUnit){
                loadingCountDownTimer.cancel();
                fragment.goToDownloadFragment();
            }else{
                DeviceChecker deviceChecker = new DeviceChecker(fragment, device);
                deviceChecker.execute();
            }

            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                Log.d(TAG,  "deviceChecker URL: " + urlString);

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
                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                Log.d(TAG,  "deviceChecker POST data: " + jObject.toString());


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
                Log.d(TAG,  "deviceChecker response: " + result.toString());
                if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                    ronixUnit = true;
                }else{
                    ronixUnit = false;
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
