package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.adapters.PlaceAdapter;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

public class PickPlaceDialogFragment extends DialogFragment {
    private static final String TAG = PickPlaceDialogFragment.class.getSimpleName();
    private PickPlaceDialogFragment.OnPlaceSelectedListener callback;

    List<Place> places;
    PlaceAdapter adapter;

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
        ListView listView = new ListView(getActivity());
        places = MySettings.getAllPlaces();


        adapter = new PlaceAdapter(getActivity(), places);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place selectedPlace = (Place) adapter.getItem(position);
                callback.onPlaceSelected(selectedPlace);
                dismiss();
            }
        });

        return listView;
    }
}
