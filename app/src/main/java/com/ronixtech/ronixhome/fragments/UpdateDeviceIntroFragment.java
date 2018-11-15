package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateDeviceIntroFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDeviceIntroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDeviceIntroFragment extends Fragment {
    private static final String TAG = UpdateDeviceIntroFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    Button startButton;

    public UpdateDeviceIntroFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDeviceIntroFragment.
     */
    public static UpdateDeviceIntroFragment newInstance(String param1, String param2) {
        UpdateDeviceIntroFragment fragment = new UpdateDeviceIntroFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_device_intro, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.update_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        startButton = view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Device device = MySettings.getTempDevice();
                if(device != null){
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines
                            || device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        UpdateDeviceAutoFragment updateDeviceAutoFragment = new UpdateDeviceAutoFragment();
                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceAutoFragment, "updateDeviceAutoFragment");
                        fragmentTransaction.addToBackStack("updateDeviceAutoFragment");
                        fragmentTransaction.commit();
                    }else{
                        int firmwareVersion = Integer.valueOf(device.getFirmwareVersion());
                        if(firmwareVersion >= Device.DEVICE_FIRMWARE_VERSION_AUTO_UPDATE_METHOD){
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            UpdateDeviceAutoFragment updateDeviceAutoFragment = new UpdateDeviceAutoFragment();
                            fragmentTransaction.replace(R.id.fragment_view, updateDeviceAutoFragment, "updateDeviceAutoFragment");
                            fragmentTransaction.addToBackStack("updateDeviceAutoFragment");
                            fragmentTransaction.commit();
                        }else{
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            UpdateDeviceFirmwareDownloadFragment updateDeviceFirmwareDownloadFragment = new UpdateDeviceFirmwareDownloadFragment();
                            fragmentTransaction.replace(R.id.fragment_view, updateDeviceFirmwareDownloadFragment, "updateDeviceFirmwareDownloadFragment");
                            fragmentTransaction.addToBackStack("updateDeviceFirmwareDownloadFragment");
                            fragmentTransaction.commit();
                        }
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
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
    }
*/
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
