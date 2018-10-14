package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationFragment extends Fragment implements TypePickerDialogFragment.OnTypeSelectedListener {
    private static final String TAG = AddDeviceConfigurationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout firstLineLayout, secondLineLayout, thirdLineLayout;
    RelativeLayout firstLineTypeLayout, secondLineTypeLayout, thirdLineTypeLayout;
    TextView firstLineTypeTextView, secondLineTypeTextView, thirdLineTypeTextView;
    ImageView firstLineTypeImageView, secondLineTypeImageView, thirdLineTypeImageView;
    EditText deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText;
    Button continueButton;
    TextView deviceNameTextView;

    Type firstLineType, secondLineType, thirdLineType;

    Device device;

    int selectedLineType;

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

        deviceNameTextView = view.findViewById(R.id.device_name_title_textivew);
        deviceNameEditText = view.findViewById(R.id.device_name_edittext);

        firstLineLayout = view.findViewById(R.id.first_line_configuration_layout);
        secondLineLayout = view.findViewById(R.id.second_line_configuration_layout);
        thirdLineLayout = view.findViewById(R.id.third_line_configuration_layout);

        firstLineTypeLayout = view.findViewById(R.id.first_line_type_selection_layout);
        secondLineTypeLayout = view.findViewById(R.id.second_line_type_selection_layout);
        thirdLineTypeLayout = view.findViewById(R.id.third_line_type_selection_layout);

        firstLineNameEditText = view.findViewById(R.id.first_line_name_edittxt);
        secondLineNameEditText = view.findViewById(R.id.second_line_name_edittxt);
        thirdLineNameEditText = view.findViewById(R.id.third_line_name_edittxt);

        firstLineTypeTextView = view.findViewById(R.id.first_line_type_textview);
        secondLineTypeTextView = view.findViewById(R.id.second_line_type_textview);
        thirdLineTypeTextView = view.findViewById(R.id.third_line_type_textview);

        firstLineTypeImageView = view.findViewById(R.id.first_line_type_imageview);
        secondLineTypeImageView = view.findViewById(R.id.second_line_type_imageview);
        thirdLineTypeImageView = view.findViewById(R.id.third_line_type_imageview);

        continueButton = view.findViewById(R.id.continue_button);

        device = MySettings.getTempDevice();
        if(device == null){
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_adding_smart_controller), Toast.LENGTH_LONG).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }
        deviceNameEditText.setText(device.getName());
        deviceNameEditText.setEnabled(false);
        deviceNameEditText.setVisibility(View.GONE);
        deviceNameTextView.setText(device.getName());
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.GONE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
            //secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
            //thirdLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
            //thirdLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.VISIBLE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_2_name_hint));
            thirdLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
        }else{
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unknown_smart_controller_type, device.getDeviceTypeID()), Toast.LENGTH_LONG).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        firstLineType = MySettings.getTypeByName("Fluorescent Lamp");
        if(firstLineType != null){
            firstLineTypeTextView.setText(firstLineType.getName());
            if(firstLineType.getImageUrl() != null && firstLineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(firstLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(firstLineTypeImageView);
            }else {
                firstLineTypeImageView.setImageResource(firstLineType.getImageResourceID());
            }
        }
        secondLineType = MySettings.getTypeByName("Fluorescent Lamp");
        if(secondLineType != null) {
            secondLineTypeTextView.setText(secondLineType.getName());
            if (secondLineType.getImageUrl() != null && secondLineType.getImageUrl().length() >= 1) {
                GlideApp.with(getActivity())
                        .load(secondLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(secondLineTypeImageView);
            } else {
                secondLineTypeImageView.setImageResource(secondLineType.getImageResourceID());
            }
        }
        thirdLineType = MySettings.getTypeByName("Fluorescent Lamp");
        if(thirdLineType != null){
            thirdLineTypeTextView.setText(thirdLineType.getName());
            if(thirdLineType.getImageUrl() != null && thirdLineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(thirdLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(thirdLineTypeImageView);
            }else {
                thirdLineTypeImageView.setImageResource(thirdLineType.getImageResourceID());
            }
        }

        deviceNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(validateInputsWithoutYoyo()){
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
                if(validateInputsWithoutYoyo()){
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
                if(validateInputsWithoutYoyo()){
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
                if(validateInputsWithoutYoyo()){
                    Utils.setButtonEnabled(continueButton, true);
                }else{
                    Utils.setButtonEnabled(continueButton, false);
                }
            }
        });

        firstLineTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                    selectedLineType = 0;
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TypePickerDialogFragment fragment = TypePickerDialogFragment.newInstance();
                    fragment.setTypesCategory(Constants.TYPE_LINE);
                    fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                    Utils.generateLineTypes();
                }
            }
        });
        secondLineTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                    selectedLineType = 1;
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TypePickerDialogFragment fragment = TypePickerDialogFragment.newInstance();
                    fragment.setTypesCategory(Constants.TYPE_LINE);
                    fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                    Utils.generateLineTypes();
                }
            }
        });
        thirdLineTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                    selectedLineType = 2;
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TypePickerDialogFragment fragment = TypePickerDialogFragment.newInstance();
                    fragment.setTypesCategory(Constants.TYPE_LINE);
                    fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                    Utils.generateLineTypes();
                }
            }
        });

        thirdLineNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //if all valid
                if(validateInputs()){
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
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setTypeID(secondLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setTypeID(secondLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(2);
                        line.setName(thirdLineNameEditText.getText().toString());
                        line.setTypeID(thirdLineType.getId());
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
                return false;
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if all valid
                if(validateInputs()){
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
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setTypeID(secondLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        line = new Line();
                        line.setPosition(0);
                        line.setName(firstLineNameEditText.getText().toString());
                        line.setTypeID(firstLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(1);
                        line.setName(secondLineNameEditText.getText().toString());
                        line.setTypeID(secondLineType.getId());
                        line.setPowerState(Line.LINE_STATE_OFF);
                        line.setDeviceID(device.getId());
                        lines.add(line);

                        line = new Line();
                        line.setPosition(2);
                        line.setName(thirdLineNameEditText.getText().toString());
                        line.setTypeID(thirdLineType.getId());
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
    public void onTypeSelected(Type type){
        if(type != null){
            switch (selectedLineType){
                case 0:
                    firstLineType = type;
                    firstLineTypeTextView.setText(firstLineType.getName());
                    if(firstLineType.getImageUrl() != null && firstLineType.getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(firstLineType.getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_led__lamp))
                                .into(firstLineTypeImageView);
                    }else {
                        firstLineTypeImageView.setImageResource(firstLineType.getImageResourceID());
                    }
                    if(validateInputsWithoutYoyo()){
                        Utils.setButtonEnabled(continueButton, true);
                    }else{
                        Utils.setButtonEnabled(continueButton, false);
                    }
                    break;
                case 1:
                    secondLineType = type;
                    secondLineTypeTextView.setText(secondLineType.getName());
                    if(secondLineType.getImageUrl() != null && secondLineType.getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(secondLineType.getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_led__lamp))
                                .into(secondLineTypeImageView);
                    }else {
                        secondLineTypeImageView.setImageResource(secondLineType.getImageResourceID());
                    }
                    if(validateInputsWithoutYoyo()){
                        Utils.setButtonEnabled(continueButton, true);
                    }else{
                        Utils.setButtonEnabled(continueButton, false);
                    }
                    break;
                case 2:
                    thirdLineType = type;
                    thirdLineTypeTextView.setText(thirdLineType.getName());
                    if(thirdLineType.getImageUrl() != null && thirdLineType.getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(thirdLineType.getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_led__lamp))
                                .into(thirdLineTypeImageView);
                    }else {
                        thirdLineTypeImageView.setImageResource(thirdLineType.getImageResourceID());
                    }
                    if(validateInputsWithoutYoyo()){
                        Utils.setButtonEnabled(continueButton, true);
                    }else{
                        Utils.setButtonEnabled(continueButton, false);
                    }
                    break;
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;
        if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(firstLineNameEditText);
        }
        if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(secondLineNameEditText);
        }
        if(thirdLineNameEditText.getText().toString() == null || thirdLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(thirdLineNameEditText);
        }

        if(firstLineType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(firstLineTypeLayout);
        }
        if(secondLineType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(secondLineTypeLayout);
        }
        if(thirdLineType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(thirdLineTypeLayout);
        }

        return inputsValid;
    }

    private boolean validateInputsWithoutYoyo(){
        boolean inputsValid = true;
        if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
        }
        if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
        }
        if(thirdLineNameEditText.getText().toString() == null || thirdLineNameEditText.getText().toString().length() < 1){
            inputsValid = false;
        }

        if(firstLineType == null){
            inputsValid = false;
        }
        if(secondLineType == null){
            inputsValid = false;
        }
        if(thirdLineType == null){
            inputsValid = false;
        }

        return inputsValid;
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
