package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateDeviceFirmwareDownloadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDeviceFirmwareDownloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDeviceFirmwareDownloadFragment extends Fragment {
    private static final String TAG = UpdateDeviceFirmwareDownloadFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    DonutProgress progressCircle;
    TextView progressTextView;
    int totalNumberOfFiles = 2;
    int currentFile = 1;

    Device device;

    private UpdateDeviceFirmwareDownloadFragment fragment;

    public UpdateDeviceFirmwareDownloadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDeviceFirmwareDownloadFragment.
     */
    public static UpdateDeviceFirmwareDownloadFragment newInstance(String param1, String param2) {
        UpdateDeviceFirmwareDownloadFragment fragment = new UpdateDeviceFirmwareDownloadFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_device_firmware_download, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.updating_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        fragment = this;

        progressCircle = view.findViewById(R.id.progress_circle);
        progressTextView = view.findViewById(R.id.progress_textview);

        progressTextView.setText(getActivity().getResources().getString(R.string.downloading_file, currentFile, totalNumberOfFiles));

        device = MySettings.getTempDevice();

        if(device != null){
            new Utils.InternetChecker(getActivity(), new Utils.InternetChecker.OnConnectionCallback() {
                @Override
                public void onConnectionSuccess() {
                    String url = String.format(Constants.DEVICE_FIRMWARE_URL, device.getDeviceTypeID(), MySettings.getDeviceLatestFirmwareVersion(device.getDeviceTypeID()), "user1.bin");
                    DownloadTask downloadTask = new DownloadTask(getActivity(), fragment);
                    downloadTask.execute(url);
                }

                @Override
                public void onConnectionFail(String errorMsg) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    goToHomeFragment();
                }
            }).execute();
        }else{
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_download_firmware), Toast.LENGTH_SHORT).show();
            goToHomeFragment();
        }

        return view;
    }

    private void goToHomeFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    public void goToUploadFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
        UpdateDeviceFirmwareUploadFragment updateDeviceFirmwareUploadFragment = new UpdateDeviceFirmwareUploadFragment();
        fragmentTransaction.replace(R.id.fragment_view, updateDeviceFirmwareUploadFragment, "updateDeviceFirmwareUploadFragment");
        fragmentTransaction.addToBackStack("updateDeviceFirmwareUploadFragment");
        fragmentTransaction.commit();
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

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        UpdateDeviceFirmwareDownloadFragment fragment;

        int statusCode;

        public DownloadTask(Context context, UpdateDeviceFirmwareDownloadFragment fragment) {
            this.context = context;
            this.fragment = fragment;
        }

        @Override
        protected void onProgressUpdate(Integer... progress){
            progressCircle.setDonut_progress(""+progress[0]);
        }

        @Override
        protected void onPostExecute(String result){
            if(getActivity() != null){
                if(statusCode == 200) {
                    currentFile++;

                    progressTextView.setText(context.getResources().getString(R.string.downloading_file, 2, 2));

                    if (currentFile <= 2) {
                        String url = String.format(Constants.DEVICE_FIRMWARE_URL, device.getDeviceTypeID(), MySettings.getDeviceLatestFirmwareVersion(device.getDeviceTypeID()), "user2.bin");
                        DownloadTask downloadTask = new DownloadTask(getActivity(), fragment);
                        downloadTask.execute(url);
                    } else {
                        progressTextView.setText(context.getResources().getString(R.string.download_complete));
                        fragment.goToUploadFragment();
                    }
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.download_firmware_failed), Toast.LENGTH_SHORT).show();
                    goToHomeFragment();
                }
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {
            statusCode = 0;
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                Log.d(TAG, "downloadFirmwareFile URL: " + url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                statusCode = connection.getResponseCode();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                String filename = Constants.DEVICE_FIRMWARE_FILE_NAME_1;
                if(currentFile == 1){
                    filename = Constants.DEVICE_FIRMWARE_FILE_NAME_1;
                }else if(currentFile == 2){
                    filename = Constants.DEVICE_FIRMWARE_FILE_NAME_2;
                }
                output = context.openFileOutput(filename, Context.MODE_PRIVATE);

                //output = new FileOutputStream("/sdcard/file_name.extension");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}
