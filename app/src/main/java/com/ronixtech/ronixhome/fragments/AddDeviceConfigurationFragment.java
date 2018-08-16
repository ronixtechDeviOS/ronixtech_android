package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

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

    RelativeLayout firstLineLayout, secondLineLayout, thirdLineLayout;
    GifImageView deviceLinesAnimationView;
    EditText deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText;
    Button continueButton;

    Device device;

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
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.configure_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        deviceLinesAnimationView = view.findViewById(R.id.device_lines_gif_imageview);
        firstLineLayout = view.findViewById(R.id.first_line_configuration_layout);
        secondLineLayout = view.findViewById(R.id.second_line_configuration_layout);
        thirdLineLayout = view.findViewById(R.id.third_line_configuration_layout);
        deviceNameEditText = view.findViewById(R.id.device_name_edittext);
        firstLineNameEditText = view.findViewById(R.id.first_line_name_edittxt);
        secondLineNameEditText = view.findViewById(R.id.second_line_name_edittxt);
        thirdLineNameEditText = view.findViewById(R.id.third_line_name_edittxt);
        continueButton = view.findViewById(R.id.continue_button);

        device = MySettings.getTempDevice();
        if(device == null){
            Toast.makeText(getActivity(), "Error adding smart controller, please try again.", Toast.LENGTH_SHORT).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.GONE);
            thirdLineLayout.setVisibility(View.GONE);
            //deviceLinesAnimationView.setBackgroundResource(unit with 1 line gif, no device highlighted);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.GONE);
            //deviceLinesAnimationView.setBackgroundResource(unit with 2 lines gif, no device highlighted);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.VISIBLE);
            //deviceLinesAnimationView.setBackgroundResource(unit with 3 lines gif, no device highlighted);
        }else{
            Toast.makeText(getActivity(), "Unknown smart controller type (" + device.getDeviceTypeID() + "). Please try again later or contact support.", Toast.LENGTH_SHORT).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        firstLineNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 1 line gif, first line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 2 lines gif, first line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 3 lines gif, first line highlighted);
                    }
                }
            }
        });
        secondLineNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 1 line gif, second line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 2 lines gif, second line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 3 lines gif, second line highlighted);
                    }
                }
            }
        });
        thirdLineNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 1 line gif, third line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 2 lines gif, third line highlighted);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
                        //deviceLinesAnimationView.setBackgroundResource(unit with 3 lines gif, third line highlighted);
                    }
                }
            }
        });

        deviceNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText)){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }
            }
        });
        firstLineNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText)){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }
            }
        });
        secondLineNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText)){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }
            }
        });
        thirdLineNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText)){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if all valid
                if(Utils.validateInputs(deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText)){
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(deviceNameEditText.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(firstLineNameEditText.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(secondLineNameEditText.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(thirdLineNameEditText.getWindowToken(), 0);
                    //create the lines and device.setLines then MySettings.setDevice()
                    List<Line> lines = new ArrayList<>();
                    Line line;

                    MySettings.addDevice(device);
                    device = MySettings.getDeviceByMAC(device.getMacAddress());

                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(2);
                        line.setName(thirdLineNameEditText.getText().toString());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }
                    device.setLines(lines);
                    device.setName(deviceNameEditText.getText().toString());
                    MySettings.setTempDevice(device);

                    //Device tempDevice = MySettings.getDeviceByMAC(device.getMacAddress());
                    //tempDevice.setLines(lines);
                    //tempDevice.setName(deviceNameEditText.getText().toString());
                    //MySettings.addDevice(device);

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocationFragment");
                    fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                    fragmentTransaction.commit();
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
