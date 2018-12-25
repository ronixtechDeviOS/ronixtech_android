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
import com.ronixtech.ronixhome.adapters.LineAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import java.util.ArrayList;
import java.util.List;

//this is where you pick a line
public class PickLineDialogFragment extends DialogFragment {
    private static final String TAG = PickLineDialogFragment.class.getSimpleName();
    private PickLineDialogFragment.OnLineSelectedListener callback;

    List<Line> lines;
    LineAdapter adapter;

    public interface OnLineSelectedListener {
        public void onLineSelected(Line line);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickLineDialogFragment newInstance() {
        PickLineDialogFragment f = new PickLineDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickLineDialogFragment.OnLineSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnLineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_line_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        lines = new ArrayList<>();


        List<Device> devices = MySettings.getAllDevices();
        for (Device device:devices) {
            if(device.getDeviceTypeID() != Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                if(device.getLines() != null && device.getLines().size() >= 1){
                    lines.addAll(device.getLines());
                }
            }
        }

        adapter = new LineAdapter(getActivity(), lines);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Line selectedLine = (Line) adapter.getItem(position);
                callback.onLineSelected(selectedLine);
                dismiss();
            }
        });

        return listView;
    }
}
