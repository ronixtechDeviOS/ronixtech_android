package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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
import android.widget.Toast;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.ViewPagerAdapter;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditDeviceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditDeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDeviceFragment extends android.support.v4.app.Fragment {
    private static final String TAG = EditDeviceFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    TabLayout mTabLayout;

    TextView deviceNameTextView;
    EditText deviceNameEditText;

    private Device device;

    boolean unsavedChangesFirstLine = false, unsavedChangesSecondLine = false, unsavedChangesThirdLine = false;

    public EditDeviceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditDeviceFragment.
     */
    public static EditDeviceFragment newInstance(String param1, String param2) {
        EditDeviceFragment fragment = new EditDeviceFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_device, container, false);
        device = MySettings.getTempDevice();
        if(device != null){
            MainActivity.setActionBarTitle(device.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.edit_device), getResources().getColor(R.color.whiteColor));
        }

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


        //deviceNameTextView.setText(device.getName());
        deviceNameTextView.setVisibility(View.GONE);

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
                EditDeviceLineFragment editDeviceLineFragment1 = new EditDeviceLineFragment();
                editDeviceLineFragment1.setCurrentLinePosition(0);
                editDeviceLineFragment1.setDeviceNumberOfLines(1);
                editDeviceLineFragment1.setFragmentManager(getFragmentManager());
                editDeviceLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment1, getActivity().getResources().getString(R.string.line_1_name_hint));
                mTabLayout.setVisibility(View.GONE);
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines){
                EditDeviceLineFragment editDeviceLineFragment1 = new EditDeviceLineFragment();
                editDeviceLineFragment1.setCurrentLinePosition(0);
                editDeviceLineFragment1.setDeviceNumberOfLines(2);
                editDeviceLineFragment1.setFragmentManager(getFragmentManager());
                editDeviceLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment1, getActivity().getResources().getString(R.string.line_1_name_hint));

                EditDeviceLineFragment editDeviceLineFragment2 = new EditDeviceLineFragment();
                editDeviceLineFragment2.setCurrentLinePosition(1);
                editDeviceLineFragment2.setDeviceNumberOfLines(2);
                editDeviceLineFragment2.setFragmentManager(getFragmentManager());
                editDeviceLineFragment2.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment2, getActivity().getResources().getString(R.string.line_3_name_hint));
            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                EditDeviceLineFragment editDeviceLineFragment1 = new EditDeviceLineFragment();
                editDeviceLineFragment1.setCurrentLinePosition(0);
                editDeviceLineFragment1.setDeviceNumberOfLines(3);
                editDeviceLineFragment1.setFragmentManager(getFragmentManager());
                editDeviceLineFragment1.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment1, getActivity().getResources().getString(R.string.line_1_name_hint));

                EditDeviceLineFragment editDeviceLineFragment2 = new EditDeviceLineFragment();
                editDeviceLineFragment2.setCurrentLinePosition(1);
                editDeviceLineFragment2.setDeviceNumberOfLines(3);
                editDeviceLineFragment2.setFragmentManager(getFragmentManager());
                editDeviceLineFragment2.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment2, getActivity().getResources().getString(R.string.line_2_name_hint));

                EditDeviceLineFragment editDeviceLineFragment3 = new EditDeviceLineFragment();
                editDeviceLineFragment3.setCurrentLinePosition(2);
                editDeviceLineFragment3.setDeviceNumberOfLines(3);
                editDeviceLineFragment3.setFragmentManager(getFragmentManager());
                editDeviceLineFragment3.setParentFragment(this);
                pagerAdapter.addFrag(editDeviceLineFragment3, getActivity().getResources().getString(R.string.line_3_name_hint));
            }else{
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unknown_smart_controller_type, device.getDeviceTypeID()), Toast.LENGTH_LONG).show();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        }else{
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unknown_smart_controller_type, device.getDeviceTypeID()), Toast.LENGTH_LONG).show();
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

    public void tabUserChangesState(int index, boolean unfinishedChanges){
        if(unfinishedChanges){
            mTabLayout.getTabAt(index).setText(mTabLayout.getTabAt(index).getText().toString().replace("*", ""));
            mTabLayout.getTabAt(index).setText(mTabLayout.getTabAt(index).getText().toString().concat("*"));
        }else{
            mTabLayout.getTabAt(index).setText(mTabLayout.getTabAt(index).getText().toString().replace("*", ""));
        }
        if(index == 0){
            this.unsavedChangesFirstLine = unfinishedChanges;
        }else if(index == 1){
            this.unsavedChangesSecondLine = unfinishedChanges;
        }else if(index == 2){
            this.unsavedChangesThirdLine = unfinishedChanges;
        }
    }

    public boolean getUnsavedChanges(){
        if(unsavedChangesFirstLine || unsavedChangesSecondLine || unsavedChangesThirdLine){
            return true;
        }else{
            return false;
        }
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
