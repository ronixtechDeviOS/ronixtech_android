package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogViewerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogViewerFragment extends android.support.v4.app.Fragment {
    private static final String TAG = LogViewerFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    Button shareLogButon;
    TextView logsTextView;

    String logText = "";

    public LogViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogViewerFragment.
     */
    public static LogViewerFragment newInstance(String param1, String param2) {
        LogViewerFragment fragment = new LogViewerFragment();
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
        View view = inflater.inflate(R.layout.fragment_log_viewer, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.log_viewer), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        shareLogButon = view.findViewById(R.id.share_log_button);
        logsTextView = view.findViewById(R.id.logs_textview);

        try {
            String command = "pidof -s " + Constants.PACKAGE_NAME;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder logSB = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                logSB.append(line);
            }

            logText = logSB.toString();
            logsTextView.setText(""+logText);

            int pid = -1;
            if(logText != null && logText.length() >= 1) {
                pid = Integer.parseInt(logText);
            }
            Utils.log(TAG, "pid = " + pid, true);

            if(pid != -1){
                command = "logcat -d -v brief --pid=" + pid;
                process = Runtime.getRuntime().exec(command);
                bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                logSB = new StringBuilder();
                line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    logSB.append(line + "\n================\n");
                }

                logText = logSB.toString();
                logsTextView.setText(""+logText);
            }else{
                //command = "logcat -d -v brief -s " + Constants.PACKAGE_NAME;
                //command = "logcat -d -v brief";
                command = "logcat -d -v brief -s MainActivity -s RegistrationFragment -s LoginFragment -s DeviceAdapter -s AddDeviceFragmentSearch -s AddDeviceFragmentGetData -s AddDeviceFragmentSendData -s AddDeviceFragmentSendData -s LinkedAccountsFragment -s ExportImportDB-s ExportDataFragment -s ImportDataFragment -s UpdateDeviceFirmwareDownloadFragment -s UpdateDeviceFirmwareUploadFragment -s UpdateDeviceFirmwareLoadingFragment -s UpdateDeviceAutoFragment -s NetworkDiscovery -s NetworkScannerAsyncTask -s DashboardDevicesFragment -s DashboardRoomsFragment -s StatusGetter -s DeviceSyncer -s ModeGetter -s DeviceChecker";                process = Runtime.getRuntime().exec(command);
                bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                logSB = new StringBuilder();
                line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    logSB.append(line + "\n================\n");
                }

                logText = logSB.toString();
                logsTextView.setText(""+logText);
            }

        } catch (IOException e) {
            // Handle Exception
            Utils.log(TAG, e.getMessage(), true);
        }

        shareLogButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(logText.length() >= 1){
                    String emailBody = Utils.getDeviceInfo(getActivity());
                    emailBody = emailBody.concat("\n\n" + logText);
                    Intent sharingIntent = new Intent(Intent.ACTION_SENDTO);
                    sharingIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    sharingIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "joeyoggie@gmail.com" });
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Utils.getString(getActivity(), R.string.share_log_message));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
                    if(sharingIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(sharingIntent);
                    }
                }else{
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.no_log_text), true);
                }
            }
        });

        return view;
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
