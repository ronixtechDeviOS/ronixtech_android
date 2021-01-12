package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SuccessFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SuccessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuccessFragment extends androidx.fragment.app.Fragment {
    private static final String TAG = SuccessFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    TextView successMessageTextView;
    Button continueButton;
    Device device;
    private int successSource;

    public SuccessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuccessFragment.
     */
    public static SuccessFragment newInstance(String param1, String param2) {
        SuccessFragment fragment = new SuccessFragment();
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
        View view = inflater.inflate(R.layout.fragment_success, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.app_name), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);



        successMessageTextView = view.findViewById(R.id.succes_message_textview);
        continueButton = view.findViewById(R.id.continue_button);

        if(successSource == Constants.SUCCESS_SOURCE_PLACE){
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_place));
        }else if(successSource == Constants.SUCCESS_SOURCE_ROOM){
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_room));
        }else if(successSource == Constants.SUCCESS_SOURCE_DEVICE){
            device=MySettings.getTempDevice();
            addDevicetoDB();
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_device));
        }else if(successSource == Constants.SUCCESS_SOURCE_EXPORT){
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_export));
        }else if(successSource == Constants.SUCCESS_SOURCE_IMPORT){
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_import));
        }else if(successSource == Constants.SUCCESS_SOURCE_DEVICE_FIRMWARE){
            successMessageTextView.setText(Utils.getString(getActivity(), R.string.success_message_device_firmware));
        }


        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(successSource == Constants.SUCCESS_SOURCE_PLACE){
                    //go to PlacesFragment
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    PlacesFragment placesFragment = new PlacesFragment();
                    fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }else if(successSource == Constants.SUCCESS_SOURCE_ROOM){
                    //go to Rooms Dashboard
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }else if(successSource == Constants.SUCCESS_SOURCE_DEVICE){
                    //go to Rooms Dashboard
                    MySettings.setTempDevice(null);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }else if(successSource == Constants.SUCCESS_SOURCE_EXPORT){
                    //go to Rooms Dashboardt
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }else if(successSource == Constants.SUCCESS_SOURCE_IMPORT){
                    //restart app
                    if(getActivity() != null){
                        PackageManager packageManager = getActivity().getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(getActivity().getPackageName());
                        ComponentName componentName = intent.getComponent();
                        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                        getActivity().startActivity(mainIntent);
                        Runtime.getRuntime().exit(0);
                    }
                }else if(successSource == Constants.SUCCESS_SOURCE_DEVICE_FIRMWARE){
                    //go to Rooms Dashboard
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
    }

    private void addDevicetoDB() {
        MySettings.addDeviceOnly(device);
        device.setId(MySettings.getDeviceByChipID(device.getChipID(),device.getDeviceTypeID()).getId());
        MySettings.addDevice(device);/*
        MySettings.updateDeviceRoom(device, device.getRoomID());
        MySettings.updateDeviceName(device, device.getName());
        MySettings.updateDeviceIP(device,device.getIpAddress());*/

    }

    public void setSuccessSource(int source){
        this.successSource = source;
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
    }*/

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
