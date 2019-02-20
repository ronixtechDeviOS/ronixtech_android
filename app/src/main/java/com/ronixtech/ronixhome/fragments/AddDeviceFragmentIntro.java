package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.ViewPagerAdapter;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.WifiNetwork;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragmentIntro.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragmentIntro#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragmentIntro extends Fragment implements WifiInfoFragment.OnNetworkAddedListener{
    private static final String TAG = AddDeviceFragmentIntro.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    TabLayout mTabLayout;

    Button continueButton;

    public AddDeviceFragmentIntro() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceFragmentIntro.
     */
    public static AddDeviceFragmentIntro newInstance(String param1, String param2) {
        AddDeviceFragmentIntro fragment = new AddDeviceFragmentIntro();
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
        View view = inflater.inflate(R.layout.fragment_add_device_intro, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        /*if(MySettings.getHomeNetwork() == null) {
            //if home network is not defined, configure it first so when adding the device/place, they're connected to it
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
            wifiInfoFragment.setSource(Constants.SOURCE_NEW_DEVICE);
            wifiInfoFragment.setTargetFragment(this, 0);
            fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
            //fragmentTransaction.addToBackStack("wifiInfoFragment");
            fragmentTransaction.commit();
        }*/

        // Locate the viewpager
        viewPager = (ViewPager) view.findViewById(R.id.devices_hints_pager);
        mTabLayout = (TabLayout) view.findViewById(R.id.devices_hints_tabs);

        // Set the ViewPagerAdapter into ViewPager
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        DeviceConfigurationHintFragment deviceConfigurationHintFragment = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment.setDeviceType(Device.DEVICE_TYPE_wifi_3lines);
        pagerAdapter.addFrag(deviceConfigurationHintFragment, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_wifi_3lines));

        DeviceConfigurationHintFragment deviceConfigurationHintFragment2 = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment2.setDeviceType(Device.DEVICE_TYPE_PLUG_3lines);
        pagerAdapter.addFrag(deviceConfigurationHintFragment2, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_PLUG_3lines));

        DeviceConfigurationHintFragment deviceConfigurationHintFragment3 = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment3.setDeviceType(Device.DEVICE_TYPE_PIR_MOTION_SENSOR);
        pagerAdapter.addFrag(deviceConfigurationHintFragment3, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_PIR_MOTION_SENSOR));

        DeviceConfigurationHintFragment deviceConfigurationHintFragment4 = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment4.setDeviceType(Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER);
        pagerAdapter.addFrag(deviceConfigurationHintFragment4, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER));

        DeviceConfigurationHintFragment deviceConfigurationHintFragment5 = new DeviceConfigurationHintFragment();
        deviceConfigurationHintFragment5.setDeviceType(Device.DEVICE_TYPE_SHUTTER);
        pagerAdapter.addFrag(deviceConfigurationHintFragment5, Device.getDeviceTypeCategoryString(Device.DEVICE_TYPE_SHUTTER));

        pagerAdapter.notifyDataSetChanged();

        mTabLayout.setupWithViewPager(viewPager);

        continueButton = view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentSearch addDeviceFragmentSearch = new AddDeviceFragmentSearch();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentSearch, "addDeviceFragmentSearch");
                fragmentTransaction.addToBackStack("addDeviceFragmentSearch");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onNetworkAdded(WifiNetwork wifiNetwork){

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
