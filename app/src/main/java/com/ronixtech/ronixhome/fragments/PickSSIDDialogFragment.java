package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.adapters.WifiNetworkItemAdapter;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

public class PickSSIDDialogFragment extends DialogFragment {
    private static final String TAG = PickSSIDDialogFragment.class.getSimpleName();
    private OnNetworkSelectedListener callback;

    ListView listView;
    List<WifiNetwork> networks;
    WifiNetworkItemAdapter adapter;

    Fragment placeFragment;

    public interface OnNetworkSelectedListener {
        public void onWifiNetworkSelected(WifiNetwork wifiNetwork);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickSSIDDialogFragment newInstance() {
        PickSSIDDialogFragment f = new PickSSIDDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (OnNetworkSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnNetworkSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_ssid_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        listView = new ListView(getActivity());

        if(networks == null){
            networks = new ArrayList<>();
        }

        adapter = new WifiNetworkItemAdapter(getActivity(), networks, Constants.WIFI_NETWORK_SEARCH, Constants.COLOR_MODE_LIGHT_BACKGROUND);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiNetwork selectedWifiNetwork = (WifiNetwork) adapter.getItem(position);
                dismiss();
                callback.onWifiNetworkSelected(selectedWifiNetwork);
            }
        });

        return listView;
    }

    public void addNetworkToList(WifiNetwork network){
        if(networks == null){
            networks = new ArrayList<>();
        }
        if(networks != null){
            if(!networks.contains(network)) {
                networks.add(network);
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}