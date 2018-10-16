package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateDeviceFirmwareUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDeviceFirmwareUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDeviceFirmwareUploadFragment extends Fragment {
    private static final String TAG = UpdateDeviceFirmwareUploadFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    Device device;
    String filename;

    public UpdateDeviceFirmwareUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDeviceFirmwareUploadFragment.
     */
    public static UpdateDeviceFirmwareUploadFragment newInstance(String param1, String param2) {
        UpdateDeviceFirmwareUploadFragment fragment = new UpdateDeviceFirmwareUploadFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_device_firmware_upload, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.updating_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        device = MySettings.getTempDevice();

        if(device != null){
            getFirmwareFileName();
        }

        //check user1 or user2 file
        //upload file to smart controller
        //reboot device

        return view;
    }

    private void goToHomeFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    public void getFirmwareFileName(){
        /*6. Get information about the device:
        - Device chip id (unique)
                => HTTP GET: "LOCAL_HOST/ronix/getchipid"
                - Device type id, defines what is the device for and what is its features and version
                => HTTP GET: "LOCAL_HOST/ronix/gettypeid"*/
        //debugTextView.append("Getting device type...\n");
        FirmwareFileNameGetter firmwareFileNameGetter = new FirmwareFileNameGetter(getActivity(), this);
        firmwareFileNameGetter.execute();

        //volley request to device to send ssid/password and then get device info for next steps
/*        String url = Constants.DEVICE_URL + Constants.GET_DEVICE_TYPE_URL;

        Log.d(TAG,  "getDeviceType URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDeviceType response: " + response);
                try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){
                        String typeIDString = jsonObject.getString(Constants.PARAMETER_DEVICE_TYPE_ID);
                        int deviceTypeID = Integer.valueOf(typeIDString);
                        Device tempDevice = MySettings.getTempDevice();
                        tempDevice.setDeviceTypeID(deviceTypeID);
                        MySettings.setTempDevice(tempDevice);
                        //debugTextView.append("Device type ID: " + deviceTypeID + "\n");
                        getChipID();
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                    if(getActivity() != null) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                    }
                    //trial failed, start over from the beginning
                    //debugTextView.setText("Attempt failed, trying again...\n");
                    goToSearchFragment();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                Log.d(TAG, "Volley Error: " + error.getNetworkTimeMs());
                Log.d(TAG, "Volley Error: " + error.getCause());
                Log.d(TAG, "Volley Error: " + error.getLocalizedMessage());
                try {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e(TAG,"TEST " + new String(htmlBodyBytes), error);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "Volley Error: statusCode: " + error.networkResponse.statusCode);
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_get_device_type_id), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                //debugTextView.setText("Attempt failed, trying again...\n");
                goToSearchFragment();
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONFIG_TIMEOUT, Device.CONFIG_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);*/
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

    public class FirmwareFileNameGetter extends AsyncTask<Void, Void, Void> {
        private final String TAG = UpdateDeviceFirmwareUploadFragment.FirmwareFileNameGetter.class.getSimpleName();

        int statusCode;

        Activity activity;
        UpdateDeviceFirmwareUploadFragment fragment;

        public FirmwareFileNameGetter(Activity activity, UpdateDeviceFirmwareUploadFragment fragment) {
            this.activity = activity;
            this.fragment = fragment;
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
                FirmwareUploader firmwareUploader = new FirmwareUploader(activity, fragment);
                firmwareUploader.execute();
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.unable_to_get_device_firmware_file_name), Toast.LENGTH_SHORT).show();
                fragment.goToHomeFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    URL url = new URL("http://" + device.getIpAddress() + Constants.DEVICE_GET_FIRMWARE_FILE_NAME_URL);
                    Log.d(TAG,  "getFirmwareFileName URL: " + url);

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
                    Log.d(TAG,  "getFirmwareFileName response: " + result.toString());
                    filename = result.toString();
                }catch (MalformedURLException e){
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

    public class FirmwareUploader extends AsyncTask<Void, Void, Void> {
        private final String TAG = UpdateDeviceFirmwareUploadFragment.FirmwareUploader.class.getSimpleName();

        int statusCode;

        Activity activity;
        UpdateDeviceFirmwareUploadFragment fragment;

        public FirmwareUploader(Activity activity, UpdateDeviceFirmwareUploadFragment fragment) {
            this.activity = activity;
            this.fragment = fragment;
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
                DeviceRebooter deviceRebooter = new DeviceRebooter(activity, fragment);
                deviceRebooter.execute();
                Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_successfull), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.unable_to_upload_firmware), Toast.LENGTH_SHORT).show();
                fragment.goToHomeFragment();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            try {
                String upLoadServerUri = "http://" + device.getIpAddress() /*+ ":88"*/ + Constants.DEVICE_UPLOAD_FIRMWARE_URL;
                Log.d(TAG, "uploadFirmware URL: " + upLoadServerUri);

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = activity.openFileInput(filename);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                //conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //conn.setRequestProperty(filename, filename);

                dos = new DataOutputStream(conn.getOutputStream());

                //dos.writeBytes(twoHyphens + boundary + lineEnd);
                //dos.writeBytes("Content-Disposition: form-data; name=\""+filename+"\";filename=\"" + filename + "\"" + lineEnd);

                //dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file
                // data...
                //dos.writeBytes(lineEnd);
                //dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }

                Log.d(TAG, "uploadFirmware response: " + result.toString());

                // Responses from the server (code and message)
                statusCode = conn.getResponseCode();

                Log.d(TAG, "uploadFirmware response code: " + statusCode);

                if (statusCode == 200) {
                    // messageText.setText(msg);
                    //Toast.makeText(ctx, "File Upload Complete.",
                    //      Toast.LENGTH_SHORT).show();
                    // recursiveDelete(mDirectory1);
                }
                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
                in.close();
                bufferedReader.close();

            } catch (Exception e) {
                // dialog.dismiss();
                e.printStackTrace();
            }
            // dialog.dismiss();

            return null;
        }
    }

    public class DeviceRebooter extends AsyncTask<Void, Void, Void> {
        private final String TAG = UpdateDeviceFirmwareUploadFragment.DeviceRebooter.class.getSimpleName();

        int statusCode;

        Activity activity;
        UpdateDeviceFirmwareUploadFragment fragment;

        public DeviceRebooter(Activity activity, UpdateDeviceFirmwareUploadFragment fragment) {
            this.activity = activity;
            this.fragment = fragment;
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
                fragment.goToHomeFragment();
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.unable_to_reboot_device), Toast.LENGTH_SHORT).show();
                fragment.goToHomeFragment();
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
                    Log.d(TAG,  "rebootDevice URL: " + url);

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
                    Log.d(TAG,  "rebootDevice response: " + result.toString());
                    filename = result.toString();
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    urlConnection.disconnect();
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

}
