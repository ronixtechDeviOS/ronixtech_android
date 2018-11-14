package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.adapters.FloorAdapter;
import com.ronixtech.ronixhome.entities.Floor;

import java.util.List;

//this is where you can pick a floor
public class PickFloorDialogFragment extends DialogFragment{
    private static final String TAG = PickFloorDialogFragment.class.getSimpleName();
    private PickFloorDialogFragment.OnFloorSelectedListener callback;

    List<Floor> floors;
    FloorAdapter adapter;

    public interface OnFloorSelectedListener {
        public void onFloorSelected(Floor floor);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickFloorDialogFragment newInstance() {
        PickFloorDialogFragment f = new PickFloorDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickFloorDialogFragment.OnFloorSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnFloorSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_floor_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        if(MySettings.getCurrentPlace() != null){
            floors = MySettings.getPlace(MySettings.getCurrentPlace().getId()).getFloors();
        }else {
            floors = MySettings.getAllFloors();
        }


        adapter = new FloorAdapter(getActivity(), floors);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Floor selectedFloor = (Floor) adapter.getItem(position);
                callback.onFloorSelected(selectedFloor);
                dismiss();
            }
        });

        return listView;
    }
}
