package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.ViewPagerAdapter;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationFragment extends Fragment {
    private static final String TAG = AddDeviceConfigurationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    TabLayout mTabLayout;

    TextView deviceNameTextView;
    EditText deviceNameEditText;

    private Device device;

    boolean unsavedChangesFirstLine = false, unsavedChangesSecondLine = false, unsavedChangesThirdLine = false;

    public AddDeviceConfigurationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationFragment.
     */
    public static AddDeviceConfigurationFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationFragment fragment = new AddDeviceConfigurationFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.configure_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        deviceNameTextView = view.findViewById(R.id.device_name_title_textivew);
        deviceNameEditText = view.findViewById(R.id.device_name_edittext);

        // Locate the viewpager
        viewPager = (ViewPager) view.findViewById(R.id.devices_pager);
        mTabLayout = (TabLayout) view.findViewById(R.id.devices_tabs);

        // Set the ViewPagerAdapter into ViewPager
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(pagerAdapter);

        device = MySettings.getTempDevice();

        deviceNameTextView.setText(device.getName());
        //deviceNameTextView.setVisibility(View.GONE);

        deviceNameEditText.setText(device.getName());
        deviceNameEditText.setEnabled(false);
        deviceNameEditText.setVisibility(View.GONE);
        deviceNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /*if(validateInputsWithoutYoyo()){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }*/
            }
        });


        if(device != null){
            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){
                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment1 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment1.setCurrentLinePosition(0);
                addDeviceConfigurationLineFragment1.setDeviceNumberOfLines(1);
                addDeviceConfigurationLineFragment1.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment1, Utils.getString(getActivity(), R.string.line_1_name_hint));
                mTabLayout.setVisibility(View.GONE);
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines){
                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment1 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment1.setCurrentLinePosition(0);
                addDeviceConfigurationLineFragment1.setDeviceNumberOfLines(2);
                addDeviceConfigurationLineFragment1.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment1, Utils.getString(getActivity(), R.string.line_1_name_hint));

                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment2 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment2.setCurrentLinePosition(1);
                addDeviceConfigurationLineFragment2.setDeviceNumberOfLines(2);
                addDeviceConfigurationLineFragment2.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment2.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment2, Utils.getString(getActivity(), R.string.line_3_name_hint));
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment1 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment1.setCurrentLinePosition(0);
                addDeviceConfigurationLineFragment1.setDeviceNumberOfLines(3);
                addDeviceConfigurationLineFragment1.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment1, Utils.getString(getActivity(), R.string.line_1_name_hint));

                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment2 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment2.setCurrentLinePosition(1);
                addDeviceConfigurationLineFragment2.setDeviceNumberOfLines(3);
                addDeviceConfigurationLineFragment2.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment2.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment2, Utils.getString(getActivity(), R.string.line_2_name_hint));

                AddDeviceConfigurationLineFragment addDeviceConfigurationLineFragment3 = new AddDeviceConfigurationLineFragment();
                addDeviceConfigurationLineFragment3.setCurrentLinePosition(2);
                addDeviceConfigurationLineFragment3.setDeviceNumberOfLines(3);
                addDeviceConfigurationLineFragment3.setFragmentManager(getFragmentManager());
                addDeviceConfigurationLineFragment3.setParentFragment(this);
                pagerAdapter.addFrag(addDeviceConfigurationLineFragment3, Utils.getString(getActivity(), R.string.line_3_name_hint));
            }else{
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.unknown_smart_controller_type), true);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        }else{
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.error_adding_smart_controller), true);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        pagerAdapter.notifyDataSetChanged();

        mTabLayout.setupWithViewPager(viewPager);

        return view;
    }

    public void moveToNextFragment(){
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
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
