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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
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
public class AddDeviceConfigurationFragment extends Fragment implements TypePickerDialogFragment.OnTypeSelectedListener, PickLineDialogFragment.OnLineSelectedListener {
    private static final String TAG = AddDeviceConfigurationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout firstLineLayout, secondLineLayout, thirdLineLayout;
    EditText deviceNameEditText, firstLineNameEditText, secondLineNameEditText, thirdLineNameEditText;
    RadioGroup firstLineModeRadioGroup, secondLineModeRadioGroup, thirdLineModeRadioGroup;
    RelativeLayout firstLineTypeSelectionLayout, secondLineTypeSelectionLayout, thirdLineTypeSelectionLayout;
    RelativeLayout firstLineTypeLayout, secondLineTypeLayout, thirdLineTypeLayout;
    TextView firstLineTypeTextView, secondLineTypeTextView, thirdLineTypeTextView;
    ImageView firstLineTypeImageView, secondLineTypeImageView, thirdLineTypeImageView;
    RelativeLayout firstLineSelectedLineLayout, secondLineSelectedLineLayout, thirdLineSelectedLineLayout;
    TextView firstLineSelectedLineNameTextView, secondLineSelectedLineNameTextView, thirdLineSelectedLineNameTextView;
    TextView firstLineSelectedLineLocationTextView, secondLineSelectedLineLocationTextView, thirdLineSelectedLineLocationTextView;
    ImageView firstLineSelectedLineImageView, secondLineSelectedLineImageView, thirdLineSelectedLineImageView;
    RelativeLayout firstLineDimmingLayout, secondLineDimmingLayout, thirdLineDimmingLayout;
    CheckBox firstLineDimmingCheckBox, secondLineDimmingCheckBox, thirdLineDimmingCheckBox;
    TextView firstLineDimmingTextView, secondLineDimmingTextView, thirdLineDimmingTextView;
    Button continueButton;
    TextView deviceNameTextView;

    Type firstLineType, secondLineType, thirdLineType;

    Line firstLine, secondLine, thirdLine;
    Line firstLineSelectedLine, secondLineSelectedLine, thirdLineSelectedLine;

    Device device;

    int selectedLineIndex;

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

        firstLineNameEditText = view.findViewById(R.id.first_line_name_edittxt);
        secondLineNameEditText = view.findViewById(R.id.second_line_name_edittxt);
        thirdLineNameEditText = view.findViewById(R.id.third_line_name_edittxt);

        firstLineModeRadioGroup = view.findViewById(R.id.first_line_mode_radiogroup);
        secondLineModeRadioGroup = view.findViewById(R.id.second_line_mode_radiogroup);
        thirdLineModeRadioGroup = view.findViewById(R.id.third_line_mode_radiogroup);

        firstLineTypeLayout = view.findViewById(R.id.first_line_type_layout);
        secondLineTypeLayout = view.findViewById(R.id.second_line_type_layout);
        thirdLineTypeLayout = view.findViewById(R.id.third_line_type_layout);

        firstLineTypeSelectionLayout = view.findViewById(R.id.first_line_type_selection_layout);
        secondLineTypeSelectionLayout = view.findViewById(R.id.second_line_type_selection_layout);
        thirdLineTypeSelectionLayout = view.findViewById(R.id.third_line_type_selection_layout);

        firstLineTypeTextView = view.findViewById(R.id.first_line_type_textview);
        secondLineTypeTextView = view.findViewById(R.id.second_line_type_textview);
        thirdLineTypeTextView = view.findViewById(R.id.third_line_type_textview);

        firstLineTypeImageView = view.findViewById(R.id.first_line_type_imageview);
        secondLineTypeImageView = view.findViewById(R.id.second_line_type_imageview);
        thirdLineTypeImageView = view.findViewById(R.id.third_line_type_imageview);

        firstLineSelectedLineLayout = view.findViewById(R.id.first_line_selected_line_layout);
        secondLineSelectedLineLayout = view.findViewById(R.id.second_line_selected_line_layout);
        thirdLineSelectedLineLayout = view.findViewById(R.id.third_line_selected_line_layout);

        firstLineSelectedLineNameTextView = view.findViewById(R.id.first_line_selected_line_textvie);
        secondLineSelectedLineNameTextView = view.findViewById(R.id.second_line_selected_line_textvie);
        thirdLineSelectedLineNameTextView = view.findViewById(R.id.third_line_selected_line_textvie);

        firstLineSelectedLineLocationTextView = view.findViewById(R.id.first_line_selected_line_location_textview);
        secondLineSelectedLineLocationTextView = view.findViewById(R.id.second_line_selected_line_location_textview);
        thirdLineSelectedLineLocationTextView = view.findViewById(R.id.third_line_selected_line_location_textview);

        firstLineSelectedLineImageView = view.findViewById(R.id.first_line_selected_line_type_imageview);
        secondLineSelectedLineImageView = view.findViewById(R.id.second_line_selected_line_type_imageview);
        thirdLineSelectedLineImageView = view.findViewById(R.id.third_line_selected_line_type_imageview);

        firstLineDimmingLayout = view.findViewById(R.id.first_line_dimming__layout);
        secondLineDimmingLayout = view.findViewById(R.id.second_line_dimming__layout);
        thirdLineDimmingLayout = view.findViewById(R.id.third_line_dimming__layout);

        firstLineDimmingCheckBox = view.findViewById(R.id.first_line_dimming_checkbox);
        secondLineDimmingCheckBox = view.findViewById(R.id.second_line_dimming_checkbox);
        thirdLineDimmingCheckBox = view.findViewById(R.id.third_line_dimming_checkbox);

        firstLineDimmingTextView = view.findViewById(R.id.first_line_dimming_textview);
        secondLineDimmingTextView = view.findViewById(R.id.second_line_dimming_textview);
        thirdLineDimmingTextView = view.findViewById(R.id.third_line_dimming_textview);


        continueButton = view.findViewById(R.id.continue_button);

        device = MySettings.getTempDevice();
        if(device == null){
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_adding_smart_controller), Toast.LENGTH_LONG).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
            return null;
        }
        firstLine = new Line();
        secondLine = new Line();
        thirdLine = new Line();
        deviceNameEditText.setText(device.getName());
        deviceNameEditText.setEnabled(false);
        deviceNameEditText.setVisibility(View.GONE);
        deviceNameTextView.setText(device.getName());
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.GONE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.VISIBLE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_2_name_hint));
            thirdLineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines ) {
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.GONE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_1_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines) {
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.GONE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_2_name_hint));
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
            firstLineLayout.setVisibility(View.VISIBLE);
            secondLineLayout.setVisibility(View.VISIBLE);
            thirdLineLayout.setVisibility(View.VISIBLE);
            firstLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_1_name_hint));
            secondLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_2_name_hint));
            thirdLineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_3_name_hint));
        }
        else{
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unknown_smart_controller_type, device.getDeviceTypeID()), Toast.LENGTH_LONG).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
            firstLineType = MySettings.getTypeByName("Appliance Plug");
            secondLineType = MySettings.getTypeByName("Appliance Plug");
            thirdLineType = MySettings.getTypeByName("Appliance Plug");
            firstLineModeRadioGroup.setVisibility(View.GONE);
            secondLineModeRadioGroup.setVisibility(View.GONE);
            thirdLineModeRadioGroup.setVisibility(View.GONE);
        }else{
            firstLineType = MySettings.getTypeByName("Fluorescent Lamp");
            secondLineType = MySettings.getTypeByName("Fluorescent Lamp");
            thirdLineType = MySettings.getTypeByName("Fluorescent Lamp");
            firstLineModeRadioGroup.setVisibility(View.VISIBLE);
            secondLineModeRadioGroup.setVisibility(View.VISIBLE);
            thirdLineModeRadioGroup.setVisibility(View.VISIBLE);
        }

        if(firstLineType != null){
            firstLineTypeTextView.setText(firstLineType.getName());
            if(firstLineType.getImageUrl() != null && firstLineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(firstLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(firstLineTypeImageView);
            }else {
                if(firstLineType.getImageResourceName() != null && firstLineType.getImageResourceName().length() >= 1){
                    firstLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(firstLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    firstLineTypeImageView.setImageResource(firstLineType.getImageResourceID());
                }
            }
        }
        if(secondLineType != null) {
            secondLineTypeTextView.setText(secondLineType.getName());
            if (secondLineType.getImageUrl() != null && secondLineType.getImageUrl().length() >= 1) {
                GlideApp.with(getActivity())
                        .load(secondLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(secondLineTypeImageView);
            } else {
                if(secondLineType.getImageResourceName() != null && secondLineType.getImageResourceName().length() >= 1){
                    secondLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(secondLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    secondLineTypeImageView.setImageResource(secondLineType.getImageResourceID());
                }
            }
        }
        if(thirdLineType != null){
            thirdLineTypeTextView.setText(thirdLineType.getName());
            if(thirdLineType.getImageUrl() != null && thirdLineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(thirdLineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(thirdLineTypeImageView);
            }else {
                if(thirdLineType.getImageResourceName() != null && thirdLineType.getImageResourceName().length() >= 1){
                    thirdLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(thirdLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    thirdLineTypeImageView.setImageResource(thirdLineType.getImageResourceID());
                }
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

        firstLineModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.first_line_mode_primary_mode_radiobutton:
                        firstLine.setMode(Line.MODE_PRIMARY);
                        firstLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        firstLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        break;
                    case R.id.first_line_mode_secondary_mode_radiobutton:
                        firstLine.setMode(Line.MODE_SECONDARY);
                        firstLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        firstLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        break;
                }
            }
        });
        secondLineModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.second_line_mode_primary_mode_radiobutton:
                        secondLine.setMode(Line.MODE_PRIMARY);
                        secondLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        secondLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        break;
                    case R.id.second_line_mode_secondary_mode_radiobutton:
                        secondLine.setMode(Line.MODE_SECONDARY);
                        secondLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        secondLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        break;
                }
            }
        });
        thirdLineModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.third_line_mode_primary_mode_radiobutton:
                        thirdLine.setMode(Line.MODE_PRIMARY);
                        thirdLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        thirdLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        break;
                    case R.id.third_line_mode_secondary_mode_radiobutton:
                        thirdLine.setMode(Line.MODE_SECONDARY);
                        thirdLineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        thirdLineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        break;
                }
            }
        });

        firstLineTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstLine.getMode() == Line.MODE_PRIMARY){
                    if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                        selectedLineIndex = 0;
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
            }
        });
        secondLineTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(secondLine.getMode() == Line.MODE_PRIMARY){
                    if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                        selectedLineIndex = 1;
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
            }
        });
        thirdLineTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(thirdLine.getMode() == Line.MODE_PRIMARY){
                    if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
                        selectedLineIndex = 2;
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
            }
        });

        firstLineDimmingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLineDimmingCheckBox.performClick();
            }
        });
        firstLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    firstLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_on));
                }else{
                    firstLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_off));
                }
            }
        });
        secondLineDimmingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secondLineDimmingCheckBox.performClick();
            }
        });
        secondLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    secondLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_on));
                }else{
                    secondLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_off));
                }
            }
        });
        thirdLineDimmingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thirdLineDimmingCheckBox.performClick();
            }
        });
        thirdLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    thirdLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_on));
                }else{
                    thirdLineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_off));
                }
            }
        });

        firstLineSelectedLineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstLine.getMode() == Line.MODE_SECONDARY){
                    if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                        selectedLineIndex = 0;
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickLineDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickLineDialogFragment fragment = PickLineDialogFragment.newInstance();
                        fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                        fragment.show(ft, "pickLineDialogFragment");
                    }else{
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_devices_first), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        secondLineSelectedLineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(secondLine.getMode() == Line.MODE_SECONDARY){
                    if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                        selectedLineIndex = 1;
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickLineDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickLineDialogFragment fragment = PickLineDialogFragment.newInstance();
                        fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                        fragment.show(ft, "pickLineDialogFragment");
                    }else{
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_devices_first), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        thirdLineSelectedLineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thirdLine.getMode() == Line.MODE_SECONDARY){
                    if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                        selectedLineIndex = 2;
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickLineDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickLineDialogFragment fragment = PickLineDialogFragment.newInstance();
                        fragment.setTargetFragment(AddDeviceConfigurationFragment.this, 0);
                        fragment.show(ft, "pickLineDialogFragment");
                    }else{
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_devices_first), Toast.LENGTH_SHORT).show();
                    }
                }
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

                    MySettings.addDevice(device);
                    device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){
                        firstLine = new Line();
                        firstLine.setPosition(0);
                        if(firstLineNameEditText.getText().toString().length() >= 1){
                            firstLine.setName(firstLineNameEditText.getText().toString());
                        }else{
                            firstLine.setName(getActivity().getResources().getString(R.string.line_1_name_hint));
                        }
                        firstLine.setTypeID(firstLineType.getId());
                        firstLine.setPowerState(Line.LINE_STATE_OFF);
                        firstLine.setDeviceID(device.getId());
                        if(firstLineDimmingCheckBox.isChecked()){
                            firstLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            firstLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(firstLine.getMode() == Line.MODE_SECONDARY){
                            firstLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(firstLineSelectedLine.getDeviceID()).getChipID());
                            firstLine.setPrimaryLinePosition(firstLineSelectedLine.getPosition());
                        }
                        lines.add(firstLine);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines){
                        firstLine = new Line();
                        firstLine.setPosition(0);
                        if(firstLineNameEditText.getText().toString().length() >= 1){
                            firstLine.setName(firstLineNameEditText.getText().toString());
                        }else{
                            firstLine.setName(getActivity().getResources().getString(R.string.line_1_name_hint));
                        }
                        firstLine.setTypeID(firstLineType.getId());
                        firstLine.setPowerState(Line.LINE_STATE_OFF);
                        firstLine.setDeviceID(device.getId());
                        if(firstLineDimmingCheckBox.isChecked()){
                            firstLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            firstLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(firstLine.getMode() == Line.MODE_SECONDARY){
                            firstLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(firstLineSelectedLine.getDeviceID()).getChipID());
                            firstLine.setPrimaryLinePosition(firstLineSelectedLine.getPosition());
                        }
                        lines.add(firstLine);

                        secondLine = new Line();
                        secondLine.setPosition(1);
                        if(secondLineNameEditText.getText().toString().length() >= 1){
                            secondLine.setName(secondLineNameEditText.getText().toString());
                        }else{
                            secondLine.setName(getActivity().getResources().getString(R.string.line_2_name_hint));
                        }
                        secondLine.setTypeID(secondLineType.getId());
                        secondLine.setPowerState(Line.LINE_STATE_OFF);
                        secondLine.setDeviceID(device.getId());
                        if(secondLineDimmingCheckBox.isChecked()){
                            secondLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            secondLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(secondLine.getMode() == Line.MODE_SECONDARY){
                            secondLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(secondLineSelectedLine.getDeviceID()).getChipID());
                            secondLine.setPrimaryLinePosition(secondLineSelectedLine.getPosition());
                        }
                        lines.add(secondLine);
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                        firstLine.setPosition(0);
                        if(firstLineNameEditText.getText().toString().length() >= 1){
                            firstLine.setName(firstLineNameEditText.getText().toString());
                        }else{
                            firstLine.setName(getActivity().getResources().getString(R.string.line_1_name_hint));
                        }
                        firstLine.setTypeID(firstLineType.getId());
                        firstLine.setPowerState(Line.LINE_STATE_OFF);
                        firstLine.setDeviceID(device.getId());
                        if(firstLineDimmingCheckBox.isChecked()){
                            firstLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            firstLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(firstLine.getMode() == Line.MODE_SECONDARY){
                            firstLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(firstLineSelectedLine.getDeviceID()).getChipID());
                            firstLine.setPrimaryLinePosition(firstLineSelectedLine.getPosition());
                        }
                        lines.add(firstLine);

                        secondLine.setPosition(1);
                        if(secondLineNameEditText.getText().toString().length() >= 1){
                            secondLine.setName(secondLineNameEditText.getText().toString());
                        }else{
                            secondLine.setName(getActivity().getResources().getString(R.string.line_2_name_hint));
                        }
                        secondLine.setTypeID(secondLineType.getId());
                        secondLine.setPowerState(Line.LINE_STATE_OFF);
                        secondLine.setDeviceID(device.getId());
                        if(secondLineDimmingCheckBox.isChecked()){
                            secondLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            secondLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(secondLine.getMode() == Line.MODE_SECONDARY){
                            secondLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(secondLineSelectedLine.getDeviceID()).getChipID());
                            secondLine.setPrimaryLinePosition(secondLineSelectedLine.getPosition());
                        }
                        lines.add(secondLine);

                        thirdLine.setPosition(2);
                        if(thirdLineNameEditText.getText().toString().length() >= 1){
                            thirdLine.setName(thirdLineNameEditText.getText().toString());
                        }else{
                            thirdLine.setName(getActivity().getResources().getString(R.string.line_3_name_hint));
                        }
                        thirdLine.setTypeID(thirdLineType.getId());
                        thirdLine.setPowerState(Line.LINE_STATE_OFF);
                        thirdLine.setDeviceID(device.getId());
                        if(thirdLineDimmingCheckBox.isChecked()){
                            thirdLine.setDimmingState(Line.DIMMING_STATE_ON);
                        }else{
                            thirdLine.setDimmingState(Line.DIMMING_STATE_OFF);
                        }
                        if(thirdLine.getMode() == Line.MODE_SECONDARY){
                            thirdLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(thirdLineSelectedLine.getDeviceID()).getChipID());
                            thirdLine.setPrimaryLinePosition(thirdLineSelectedLine.getPosition());
                        }
                        lines.add(thirdLine);
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
                    //fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
    }

    @Override
    public void onTypeSelected(Type type){
        if(type != null){
            switch (selectedLineIndex){
                case 0:
                    firstLineType = type;
                    firstLineTypeTextView.setText(firstLineType.getName());
                    if(firstLineType.getImageUrl() != null && firstLineType.getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(firstLineType.getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_led__lamp))
                                .into(firstLineTypeImageView);
                    }else {
                        if(firstLineType.getImageResourceName() != null && firstLineType.getImageResourceName().length() >= 1) {
                            firstLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(firstLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            firstLineTypeImageView.setImageResource(firstLineType.getImageResourceID());
                        }
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
                        if(secondLineType.getImageResourceName() != null && secondLineType.getImageResourceName().length() >= 1) {
                            secondLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(secondLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            secondLineTypeImageView.setImageResource(secondLineType.getImageResourceID());
                        }
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
                        if(thirdLineType.getImageResourceName() != null && thirdLineType.getImageResourceName().length() >= 1) {
                            thirdLineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(thirdLineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            thirdLineTypeImageView.setImageResource(thirdLineType.getImageResourceID());
                        }
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

    @Override
    public void onLineSelected(Line line){
        if(line != null){
            Device device;
            Room room;
            Floor floor;
            switch (selectedLineIndex){
                case 0:
                    firstLineSelectedLine = line;
                    device = MySettings.getDeviceByID2(firstLineSelectedLine.getDeviceID());
                    room = MySettings.getRoom(device.getRoomID());
                    floor = MySettings.getFloor(room.getFloorID());

                    firstLineSelectedLineNameTextView.setText(device.getName() + "\\" + firstLineSelectedLine.getName());
                    firstLineSelectedLineLocationTextView.setText(floor.getPlaceName() + "\\" + room.getName());
                    if(firstLineSelectedLine.getType().getImageUrl() != null && firstLineSelectedLine.getType().getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(firstLineSelectedLine.getType().getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                                .into(firstLineSelectedLineImageView);
                    }else {
                        if(firstLineSelectedLine.getType().getImageResourceName() != null && firstLineSelectedLine.getType().getImageResourceName().length() >= 1) {
                            firstLineSelectedLineImageView.setImageResource(getActivity().getResources().getIdentifier(firstLineSelectedLine.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            firstLineSelectedLineImageView.setImageResource(firstLineSelectedLine.getType().getImageResourceID());
                        }
                    }

                    break;
                case 1:
                    secondLineSelectedLine = line;
                    device = MySettings.getDeviceByID2(secondLineSelectedLine.getDeviceID());
                    room = MySettings.getRoom(device.getRoomID());
                    floor = MySettings.getFloor(room.getFloorID());

                    secondLineSelectedLineNameTextView.setText(device.getName() + "\\" + secondLineSelectedLine.getName());
                    secondLineSelectedLineLocationTextView.setText(floor.getPlaceName() + "\\" + room.getName());
                    if(secondLineSelectedLine.getType().getImageUrl() != null && secondLineSelectedLine.getType().getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(secondLineSelectedLine.getType().getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                                .into(secondLineSelectedLineImageView);
                    }else {
                        if(secondLineSelectedLine.getType().getImageResourceName() != null && secondLineSelectedLine.getType().getImageResourceName().length() >= 1) {
                            secondLineSelectedLineImageView.setImageResource(getActivity().getResources().getIdentifier(secondLineSelectedLine.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            secondLineSelectedLineImageView.setImageResource(secondLineSelectedLine.getType().getImageResourceID());
                        }
                    }
                    break;
                case 2:
                    thirdLineSelectedLine = line;
                    device = MySettings.getDeviceByID2(thirdLineSelectedLine.getDeviceID());
                    room = MySettings.getRoom(device.getRoomID());
                    floor = MySettings.getFloor(room.getFloorID());

                    thirdLineSelectedLineNameTextView.setText(device.getName() + "\\" + thirdLineSelectedLine.getName());
                    thirdLineSelectedLineLocationTextView.setText(floor.getPlaceName() + "\\" + room.getName());
                    if(thirdLineSelectedLine.getType().getImageUrl() != null && thirdLineSelectedLine.getType().getImageUrl().length() >= 1){
                        GlideApp.with(getActivity())
                                .load(thirdLineSelectedLine.getType().getImageUrl())
                                .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                                .into(thirdLineSelectedLineImageView);
                    }else {
                        if(thirdLineSelectedLine.getType().getImageResourceName() != null && thirdLineSelectedLine.getType().getImageResourceName().length() >= 1) {
                            thirdLineSelectedLineImageView.setImageResource(getActivity().getResources().getIdentifier(thirdLineSelectedLine.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                        }else{
                            thirdLineSelectedLineImageView.setImageResource(thirdLineSelectedLine.getType().getImageResourceID());
                        }
                    }
                    break;
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(firstLineNameEditText);
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineTypeSelectionLayout);
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineSelectedLineLayout);
                }
            }

        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(firstLineNameEditText);
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineTypeSelectionLayout);
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineSelectedLineLayout);
                }
            }

            /*if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(secondLineNameEditText);
            }*/
            if(secondLine.getMode() == Line.MODE_PRIMARY){
                if(secondLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(secondLineTypeSelectionLayout);
                }
            }else if(secondLine.getMode() == Line.MODE_SECONDARY){
                if(secondLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(secondLineSelectedLineLayout);
                }
            }

        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(firstLineNameEditText);
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineTypeSelectionLayout);
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(firstLineSelectedLineLayout);
                }
            }

            /*if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(secondLineNameEditText);
            }*/
            if(secondLine.getMode() == Line.MODE_PRIMARY){
                if(secondLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(secondLineTypeSelectionLayout);
                }
            }else if(secondLine.getMode() == Line.MODE_SECONDARY){
                if(secondLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(secondLineSelectedLineLayout);
                }
            }

            /*if(thirdLineNameEditText.getText().toString() == null || thirdLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(thirdLineNameEditText);
            }*/
            if(thirdLine.getMode() == Line.MODE_PRIMARY){
                if(thirdLineType == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(thirdLineTypeSelectionLayout);
                }
            }else if(thirdLine.getMode() == Line.MODE_SECONDARY){
                if(thirdLineSelectedLine == null){
                    inputsValid = false;
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(thirdLineSelectedLineLayout);
                }
            }

        }

        return inputsValid;
    }

    private boolean validateInputsWithoutYoyo(){
        boolean inputsValid = true;
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                }
            }

        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                }
            }

            /*if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(secondLine.getMode() == Line.MODE_PRIMARY){
                if(secondLineType == null){
                    inputsValid = false;
                }
            }else if(secondLine.getMode() == Line.MODE_SECONDARY){
                if(secondLineSelectedLine == null){
                    inputsValid = false;
                }
            }

        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){

            /*if(firstLineNameEditText.getText().toString() == null || firstLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(firstLine.getMode() == Line.MODE_PRIMARY){
                if(firstLineType == null){
                    inputsValid = false;
                }
            }else if(firstLine.getMode() == Line.MODE_SECONDARY){
                if(firstLineSelectedLine == null){
                    inputsValid = false;
                }
            }

            /*if(secondLineNameEditText.getText().toString() == null || secondLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(secondLine.getMode() == Line.MODE_PRIMARY){
                if(secondLineType == null){
                    inputsValid = false;
                }
            }else if(secondLine.getMode() == Line.MODE_SECONDARY){
                if(secondLineSelectedLine == null){
                    inputsValid = false;
                }
            }

            /*if(thirdLineNameEditText.getText().toString() == null || thirdLineNameEditText.getText().toString().length() < 1){
                inputsValid = false;
            }*/
            if(thirdLine.getMode() == Line.MODE_PRIMARY){
                if(thirdLineType == null){
                    inputsValid = false;
                }
            }else if(thirdLine.getMode() == Line.MODE_SECONDARY){
                if(thirdLineSelectedLine == null){
                    inputsValid = false;
                }
            }

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
