package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.PlaceAdapter;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

public class PickPlaceDialogFragment extends DialogFragment {
    private static final String TAG = PickPlaceDialogFragment.class.getSimpleName();
    private PickPlaceDialogFragment.OnPlaceSelectedListener callback;

    List<Place> places;
    PlaceAdapter adapter;

    private AddDeviceSelectLocationFragment addDeviceSelectLocationFragment;

    public interface OnPlaceSelectedListener {
        public void onPlaceSelected(Place place);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickPlaceDialogFragment newInstance() {
        PickPlaceDialogFragment f = new PickPlaceDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickPlaceDialogFragment.OnPlaceSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnPlaceSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_place_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        places = MySettings.getAllPlaces();

        adapter = new PlaceAdapter(getActivity(), places);
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_place_footer, null, false);
        listView.addFooterView(footerView, null, false);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add place fragment and then come back here
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                addPlaceFragment.setTargetFragment(addDeviceSelectLocationFragment, 0);
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place selectedPlace = (Place) adapter.getItem(position);
                MySettings.setCurrentPlace(selectedPlace);
                callback.onPlaceSelected(selectedPlace);
                dismiss();
            }
        });

        return listView;
    }

    public void setParentFragment(AddDeviceSelectLocationFragment fragment){
        this.addDeviceSelectLocationFragment = fragment;
    }
}
