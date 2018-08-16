package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentSendData.HomeConnectedListenerInterface} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentSendData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentSendData extends Fragment {
    private static final String TAG = AddDeviceFragmentSendData.class.getSimpleName();

    private HomeConnectedListenerInterface mListener;

    public AddDeviceFragmentSendData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceFragmentSendData.
     */
    public static AddDeviceFragmentSendData newInstance(String param1, String param2) {
        AddDeviceFragmentSendData fragment = new AddDeviceFragmentSendData();
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
        View view = inflater.inflate(R.layout.fragment_add_device_send_data, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_device_send_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        sendConfigurationToDevice();

        return view;
    }

    private void sendConfigurationToDevice(){
        //debugTextView.append("Sending home network info to your RonixTech device...\n");

        //volley request to device to send ssid/password and then get device info for next steps
        String url = Constants.SEND_SSID_PASSWORD_URL;
        //?essid=%SSID%&passwd=%PASS%

        url = url.concat("?").concat(Constants.PARAMETER_SSID).concat("=").concat(MySettings.getHomeNetwork().getSsid())
                .concat("&").concat(Constants.PARAMETER_PASSWORD).concat("=").concat(MySettings.getHomeNetwork().getPassword());

        Log.d(TAG,  "sendConfigurationToDevice URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "sendConfigurationToDevice response: " + response);
                //debugTextView.append("Connecting to your home network now...\n");
                //debugTextView.append("delwa2ty el device is configured. @Mina Please tell me if you get to this step\n");
                //debugTextView.append(" go to next step, AddDeviceConfigurationFragment\n");
                Device tempDevice = MySettings.getTempDevice();
                //MySettings.addDevice(tempDevice);
                if(mListener != null){
                    mListener.onStartListening();
                }
                connectToWifiNetwork(MySettings.getHomeNetwork().getSsid(), MySettings.getHomeNetwork().getPassword());

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationFragment addDeviceConfigurationFragment = new AddDeviceConfigurationFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationFragment, "addDeviceConfigurationFragment");
                fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                fragmentTransaction.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(getActivity() != null){
                    Toast.makeText(getActivity(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                //trial failed, start over from the beginning
                //debugTextView.setText("Attempt failed, trying again...\n");
                goToSearchFragment();
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void goToSearchFragment(){
        getFragmentManager().popBackStack();
    }

    private void connectToWifiNetwork(final String ssid, String password){
        WifiManager mWifiManager;
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!mWifiManager.isWifiEnabled()){
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.toLowerCase().contains(Constants.DEVICE_NAME_IDENTIFIER.toLowerCase())) {
                mWifiManager.removeNetwork(i.networkId);
                break;
            }
        }

        WifiConfiguration conf = new WifiConfiguration();
        /*if(Build.VERSION.SDK_INT >= 23){
            conf.SSID = ssid;
        }else{
            conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        }*/
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\""+ password +"\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        mWifiManager.addNetwork(conf);

        mWifiManager.setWifiEnabled(true);

        list = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list) {
            if(i.SSID != null && i.SSID.toLowerCase().contains("\"" + ssid.toLowerCase() + "\"")) {
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                mWifiManager.reconnect();
                break;
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
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeConnectedListenerInterface) {
            mListener = (HomeConnectedListenerInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeConnectedListenerInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HomeConnectedListenerInterface{
        void onStartListening();
    }
}
