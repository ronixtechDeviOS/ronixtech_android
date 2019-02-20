package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;

import java.util.ArrayList;
import java.util.List;

public class TypePickerDeviceDialogFragment extends DialogFragment {
    private static final String TAG = TypePickerDeviceDialogFragment.class.getSimpleName();
    private TypePickerDeviceDialogFragment.OnDeviceTypeSelectedListener callback;


    public interface OnDeviceTypeSelectedListener {
        public void onDeviceTypeSelected(Device deviceType);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static TypePickerDeviceDialogFragment newInstance() {
        TypePickerDeviceDialogFragment f = new TypePickerDeviceDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (TypePickerDeviceDialogFragment.OnDeviceTypeSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnDeviceTypeSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_device_type_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());

        List<Device> deviceTypes = new ArrayList<>();

        deviceTypes.addAll(Utils.getDeviceTypes());

        ArrayAdapter<Device> typesAdapter = new ArrayAdapter<Device>(getActivity(), android.R.layout.simple_list_item_1, deviceTypes);

        listView.setAdapter(typesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device selectedDeviceType = (Device) typesAdapter.getItem(position);
                callback.onDeviceTypeSelected(selectedDeviceType);
                dismiss();
            }
        });

        return listView;
    }
}
