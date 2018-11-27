package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.WifiNetworkItemAdapter;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.List;

//this is where you can pick a wifi network
public class PickWifiNetworkDialogFragment extends DialogFragment {
    private static final String TAG = PickWifiNetworkDialogFragment.class.getSimpleName();
    private PickWifiNetworkDialogFragment.OnNetworkSelectedListener callback;

    List<WifiNetwork> networks;
    WifiNetworkItemAdapter adapter;

    long placeID = -1;

    Fragment placeFragment;

    public interface OnNetworkSelectedListener {
        public void onWifiNetworkSelected(WifiNetwork wifiNetwork);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickWifiNetworkDialogFragment newInstance() {
        PickWifiNetworkDialogFragment f = new PickWifiNetworkDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickWifiNetworkDialogFragment.OnNetworkSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnNetworkSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_wifi_network_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        if(placeID != -1){
            networks = MySettings.getPlaceWifiNetworks(placeID);
        }else{
            networks = MySettings.getAllWifiNetworks();
        }

        adapter = new WifiNetworkItemAdapter(getActivity(), networks);
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_wifi_network_footer, null, false);
        listView.addFooterView(footerView, null, false);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add wifi network sequence and then come back here
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
                wifiInfoFragment.setSource(Constants.SOURCE_NEW_PLACE);
                wifiInfoFragment.setTargetFragment(placeFragment, 0);
                fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
                fragmentTransaction.addToBackStack("wifiInfoFragment");
                fragmentTransaction.commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiNetwork selectedWifiNetwork = (WifiNetwork) adapter.getItem(position);
                callback.onWifiNetworkSelected(selectedWifiNetwork);
                dismiss();
            }
        });

        return listView;
    }

    public void setParentFragment(Fragment fragment){
        this.placeFragment = fragment;
    }

    public void setPlaceID(long placeID){
        this.placeID = placeID;
    }
}