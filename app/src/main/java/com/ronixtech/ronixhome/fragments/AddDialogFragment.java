package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;

public class AddDialogFragment extends DialogFragment {
    private static final String TAG = AddDialogFragment.class.getSimpleName();

    RelativeLayout addPlaceLayout, addRoomLayout, addDeviceLayout;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static AddDialogFragment newInstance() {
        AddDialogFragment f = new AddDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_addition, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        addPlaceLayout = view.findViewById(R.id.add_place_layout);
        addRoomLayout = view.findViewById(R.id.add_room_layout);
        addDeviceLayout = view.findViewById(R.id.add_device_layout);

        addPlaceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add place fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });

        addRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add room fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddRoomFragment addRoomFragment = new AddRoomFragment();
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });

        addDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add device fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}
