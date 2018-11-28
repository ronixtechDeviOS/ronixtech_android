package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
 * {@link UpdateDeviceAutoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDeviceAutoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDeviceAutoFragment extends android.support.v4.app.Fragment {
    private static final String TAG = UpdateDeviceAutoFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    DonutProgress progressCircle;

    public UpdateDeviceAutoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDeviceAutoFragment.
     */
    public static UpdateDeviceAutoFragment newInstance(String param1, String param2) {
        UpdateDeviceAutoFragment fragment = new UpdateDeviceAutoFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_device_auto, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.updating_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        progressCircle = view.findViewById(R.id.progress_circle);

        Device device = MySettings.getTempDevice();

        if(device != null){
            new Utils.InternetChecker(getActivity(), new Utils.InternetChecker.OnConnectionCallback() {
                @Override
                public void onConnectionSuccess() {
                    String url = String.format(Constants.DEVICE_FIRMWARE_URL, device.getDeviceTypeID(), MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()), "user1.bin");
                    UpdateFirmwareTask downloadTask = new UpdateFirmwareTask(getActivity(), UpdateDeviceAutoFragment.this);
                    downloadTask.execute(url);
                }

                @Override
                public void onConnectionFail(String errorMsg) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_internet_connection_try_later), Toast.LENGTH_SHORT).show();
                    goToHomeFragment();
                }
            }).execute();
        }else{
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.firmware_update_failed), Toast.LENGTH_SHORT).show();
            goToHomeFragment();
        }

        return view;
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

    private class UpdateFirmwareTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        UpdateDeviceAutoFragment fragment;

        int statusCode;

        public UpdateFirmwareTask(Context context, UpdateDeviceAutoFragment fragment) {
            this.context = context;
            this.fragment = fragment;
        }

        @Override
        protected void onProgressUpdate(Integer... progress){
            progressCircle.setDonut_progress(""+progress[0]);
            progressCircle.setText(context.getResources().getString(R.string.seconds, progress[1]));
        }

        @Override
        protected void onPostExecute(String result){
            if(MainActivity.getInstance() != null && MainActivity.isResumed){
                if(statusCode == 200) {
                    /** CountDownTimer starts with 45 seconds and every onTick is 1 second */
                    final int totalMillis = 1 * 40 * 1000; // 45 seconds in milli seconds

                    new CountDownTimer(totalMillis, 1) {
                        public void onTick(long millisUntilFinished) {

                            //forward progress
                            long finishedMillis = totalMillis - millisUntilFinished;
                            int totalProgress = (int) (((float)finishedMillis / (float)totalMillis) * 100.0);

                            long totalSeconds =  Math.round(((double)finishedMillis/(double)totalMillis) * 45.0);

                            publishProgress(totalProgress, 45 - (int) totalSeconds);

                        }

                        public void onFinish() {
                            // DO something when 1 minute is up
                            Toast.makeText(context, context.getResources().getString(R.string.firmware_update_successfull_rebooting), Toast.LENGTH_SHORT).show();
                            fragment.goToHomeFragment();
                        }
                    }.start();
                }else{
                    Toast.makeText(context, context.getResources().getString(R.string.firmware_update_failed), Toast.LENGTH_SHORT).show();
                    goToHomeFragment();
                }
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + MySettings.getTempDevice().getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    URL url = new URL(urlString);
                    Log.d(TAG,  "updateFirmware URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.PARAMETER_DEVICE_FIRMWARE_URL, sUrl[0]);
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Log.d(TAG,  "updateFirmware POST data: " + jsonObject.toString());

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
                    Log.d(TAG,  "updateFirmware response: " + result.toString());
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
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
