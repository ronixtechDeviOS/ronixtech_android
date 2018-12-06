package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.Type;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationLineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationLineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationLineFragment extends android.support.v4.app.Fragment implements TypePickerDialogFragment.OnTypeSelectedListener, PickLineDialogFragment.OnLineSelectedListener{
    private static final String TAG = AddDeviceConfigurationLineFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout lineLayout;
    EditText lineNameEditText;
    RadioGroup lineModeRadioGroup;
    RelativeLayout lineTypeSelectionLayout;
    RelativeLayout lineTypeLayout;
    TextView lineTypeTextView;
    ImageView lineTypeImageView;
    RelativeLayout lineSelectedLineLayout;
    TextView lineSelectedLineNameTextView;
    TextView lineSelectedLineLocationTextView;
    ImageView lineSelectedLineImageView;
    RelativeLayout lineDimmingLayout;
    CheckBox lineDimmingCheckBox;
    TextView lineDimmingTextView;
    GifImageView lineGifImageView;
    Button continueButton;

    Type lineType;
    int lineMode = Line.MODE_PRIMARY;
    Line lineSelectedLine;

    Device device;

    private int DEVICE_NUMBER_OF_LINES;
    private int LINE_POSITION;
    private FragmentManager fragmentManager;

    AddDeviceConfigurationFragment parentFragment;

    boolean unsavedChanges = false;

    public AddDeviceConfigurationLineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationLineFragment.
     */
    public static AddDeviceConfigurationLineFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationLineFragment fragment = new AddDeviceConfigurationLineFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_line, container, false);

        lineLayout = view.findViewById(R.id.line_configuration_layout);
        lineNameEditText = view.findViewById(R.id.line_name_edittxt);
        lineModeRadioGroup = view.findViewById(R.id.line_mode_radiogroup);
        lineTypeLayout = view.findViewById(R.id.line_type_layout);
        lineTypeSelectionLayout = view.findViewById(R.id.line_type_selection_layout);
        lineTypeTextView = view.findViewById(R.id.line_type_textview);
        lineTypeImageView = view.findViewById(R.id.line_type_imageview);
        lineSelectedLineLayout = view.findViewById(R.id.line_selected_line_layout);
        lineSelectedLineNameTextView = view.findViewById(R.id.line_selected_line_textvie);
        lineSelectedLineLocationTextView = view.findViewById(R.id.line_selected_line_location_textview);
        lineSelectedLineImageView = view.findViewById(R.id.line_selected_line_type_imageview);
        lineDimmingLayout = view.findViewById(R.id.line_dimming__layout);
        lineDimmingCheckBox = view.findViewById(R.id.line_dimming_checkbox);
        lineDimmingTextView = view.findViewById(R.id.line_dimming_textview);
        lineGifImageView = view.findViewById(R.id.line_gif_imageview);

        continueButton = view.findViewById(R.id.continue_button);

        device = MySettings.getTempDevice();

        if(device == null){
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_adding_smart_controller), Toast.LENGTH_LONG).show();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
            return null;
        }

        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
            if(DEVICE_NUMBER_OF_LINES == 1){
                lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
                lineGifImageView.setImageResource(R.drawable.line_left);
            }else if(DEVICE_NUMBER_OF_LINES == 2){
                if(LINE_POSITION == 0){
                    lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
                    lineGifImageView.setImageResource(R.drawable.line_left);
                }else if(LINE_POSITION == 1){
                    lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
                    lineGifImageView.setImageResource(R.drawable.line_right);
                }
            }else if(DEVICE_NUMBER_OF_LINES == 3){
                if(LINE_POSITION == 0){
                    lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_1_name_hint));
                    lineGifImageView.setImageResource(R.drawable.line_left);
                }else if(LINE_POSITION == 1){
                    lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_2_name_hint));
                    lineGifImageView.setImageResource(R.drawable.line_center);
                }else if(LINE_POSITION == 2){
                    lineNameEditText.setHint(getActivity().getResources().getString(R.string.line_3_name_hint));
                    lineGifImageView.setImageResource(R.drawable.line_right);
                }
            }
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
            if(LINE_POSITION == 0){
                lineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_1_name_hint));
            }else if(LINE_POSITION == 1){
                lineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_2_name_hint));
            }else if(LINE_POSITION == 2){
                lineNameEditText.setHint(getActivity().getResources().getString(R.string.plug_3_name_hint));
            }
            lineModeRadioGroup.setVisibility(View.GONE);
        }

        int last = DEVICE_NUMBER_OF_LINES - 1;
        if(LINE_POSITION < last){
            continueButton.setText(getActivity().getResources().getString(R.string.save_continue_button));
        }else{
            continueButton.setText(getActivity().getResources().getString(R.string.done));
        }

        if(lineType == null) {
            if (device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
                lineType = MySettings.getTypeByName("Appliance Plug");
                lineModeRadioGroup.setVisibility(View.GONE);
            } else {
                lineType = MySettings.getTypeByName("Fluorescent Lamp");
                lineModeRadioGroup.setVisibility(View.VISIBLE);
            }
        }

        if(lineType != null){
            lineTypeTextView.setText(lineType.getName());
            if(lineType.getImageUrl() != null && lineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(lineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(lineTypeImageView);
            }else {
                if(lineType.getImageResourceName() != null && lineType.getImageResourceName().length() >= 1){
                    lineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(lineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    lineTypeImageView.setImageResource(lineType.getImageResourceID());
                }
            }
        }

        lineNameEditText.addTextChangedListener(new TextWatcher() {
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

        lineModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.line_mode_primary_mode_radiobutton:
                        lineMode = Line.MODE_PRIMARY;
                        lineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        lineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        break;
                    case R.id.line_mode_secondary_mode_radiobutton:
                        lineMode = Line.MODE_SECONDARY;
                        lineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                        lineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                        break;
                }
            }
        });

        lineTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lineMode == Line.MODE_PRIMARY){
                    if(MySettings.getTypes(Constants.TYPE_LINE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
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
                        fragment.setTargetFragment(AddDeviceConfigurationLineFragment.this, 0);
                        fragment.show(ft, "typePickerDialogFragment");
                    }else{
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                        Utils.generateLineTypes();
                    }
                }
            }
        });

        lineDimmingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineDimmingCheckBox.performClick();
            }
        });
        lineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    lineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_on));
                }else{
                    lineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_off));
                }
            }
        });

        lineSelectedLineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineMode == Line.MODE_SECONDARY){
                    if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
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
                        fragment.setTargetFragment(AddDeviceConfigurationLineFragment.this, 0);
                        fragment.show(ft, "pickLineDialogFragment");
                    }else{
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_devices_first), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if(validateInputsWithoutYoyo()){
            Utils.setButtonEnabled(continueButton, true);
        }else{
            Utils.setButtonEnabled(continueButton, false);
        }

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "continueButton - onClick");
                //if all valid
                if(validateInputs()){
                    Log.d(TAG, "continueButton - validateInputs()");
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(lineNameEditText.getWindowToken(), 0);

                    //create the lines then device.setLines/line.setDeviceID then MySettings.addDevice()
                    Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                    if(dbDevice == null){
                        MySettings.addDevice(device);
                        dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                    }

                    long deviceID = dbDevice.getId();
                    device.setId(deviceID);

                    Log.d(TAG, "Adding line, deviceID = " + deviceID);

                    Line newLine = new Line();
                    newLine.setPosition(LINE_POSITION);
                    if(lineNameEditText.getText().toString().length() >= 1){
                        newLine.setName(lineNameEditText.getText().toString());
                    }else{
                        newLine.setName(lineNameEditText.getHint().toString());
                    }
                    newLine.setTypeID(lineType.getId());
                    newLine.setPowerState(Line.LINE_STATE_OFF);
                    newLine.setDeviceID(deviceID);
                    if(lineDimmingCheckBox.isChecked()){
                        newLine.setDimmingState(Line.DIMMING_STATE_ON);
                    }else{
                        newLine.setDimmingState(Line.DIMMING_STATE_OFF);
                    }
                    newLine.setMode(lineMode);
                    if(lineMode == Line.MODE_SECONDARY){
                        newLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(lineSelectedLine.getDeviceID()).getChipID());
                        newLine.setPrimaryLinePosition(lineSelectedLine.getPosition());
                    }

                    List<Line> tempDeviceLines = new ArrayList<>();
                    tempDeviceLines.addAll(device.getLines());
                    int numberOfTempDeviceLines = tempDeviceLines.size();
                    for(int x = 0; x < numberOfTempDeviceLines; x++){
                        Line tempDeviceLine = tempDeviceLines.get(x);
                        if(tempDeviceLine.getPosition() == newLine.getPosition()){
                            device.getLines().remove(x);
                        }
                    }
                    device.getLines().add(newLine);
                    MySettings.setTempDevice(device);
                    Log.d(TAG, "TempDevice: deviceID " + MySettings.getTempDevice().getId());
                    for (Line line:MySettings.getTempDevice().getLines()) {
                        Log.d(TAG, "TempDevice: Line: Pos: " + line.getPosition() + " - Name: " + line.getName() + " - DeviceID: " + line.getDeviceID());
                    }

                    int last = DEVICE_NUMBER_OF_LINES - 1;
                    if(LINE_POSITION < last){
                        Log.d(TAG, "continueButton - move to next fragment");
                        parentFragment.moveToNextFragment();
                    }else{
                        MySettings.addDevice(device);
                        Log.d(TAG, "continueButton - move to location fragment!");
                        FragmentTransaction fragmentTransaction = parentFragment.getFragmentManager().beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                        fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocationFragment");
                        //fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                        fragmentTransaction.commit();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onTypeSelected(Type type){
        if(type != null){
            lineType = type;
            lineTypeTextView.setText(lineType.getName());
            if(lineType.getImageUrl() != null && lineType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(lineType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_led__lamp))
                        .into(lineTypeImageView);
            }else {
                if(lineType.getImageResourceName() != null && lineType.getImageResourceName().length() >= 1) {
                    lineTypeImageView.setImageResource(getActivity().getResources().getIdentifier(lineType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    lineTypeImageView.setImageResource(lineType.getImageResourceID());
                }
            }
            if(validateInputsWithoutYoyo()){
                Utils.setButtonEnabled(continueButton, true);
            }else{
                Utils.setButtonEnabled(continueButton, false);
            }
        }
    }

    @Override
    public void onLineSelected(Line line){
        if(line != null){
            Device device;
            Room room;
            Floor floor;
            lineSelectedLine = line;
            device = MySettings.getDeviceByID2(lineSelectedLine.getDeviceID());
            room = MySettings.getRoom(device.getRoomID());
            floor = MySettings.getFloor(room.getFloorID());

            lineSelectedLineNameTextView.setText(device.getName() + "\\" + lineSelectedLine.getName());
            lineSelectedLineLocationTextView.setText(floor.getPlaceName() + "\\" + room.getName());
            if(lineSelectedLine.getType().getImageUrl() != null && lineSelectedLine.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(lineSelectedLine.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                        .into(lineSelectedLineImageView);
            }else {
                if(lineSelectedLine.getType().getImageResourceName() != null && lineSelectedLine.getType().getImageResourceName().length() >= 1) {
                    lineSelectedLineImageView.setImageResource(getActivity().getResources().getIdentifier(lineSelectedLine.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    lineSelectedLineImageView.setImageResource(lineSelectedLine.getType().getImageResourceID());
                }
            }
        }
    }

    public void setDeviceNumberOfLines(int numberOfLines){
        this.DEVICE_NUMBER_OF_LINES = numberOfLines;
    }

    public void setCurrentLinePosition(int position){
        this.LINE_POSITION = position;
    }

    public void setFragmentManager(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
    }

    public void setParentFragment(AddDeviceConfigurationFragment fragment){
        this.parentFragment = fragment;
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        if(lineMode == Line.MODE_PRIMARY){
            if(lineType == null){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(lineTypeSelectionLayout);
            }
        }else if(lineMode == Line.MODE_SECONDARY){
            if(lineSelectedLine == null){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(lineSelectedLineLayout);
            }
        }

        return inputsValid;
    }

    private boolean validateInputsWithoutYoyo(){
        boolean inputsValid = true;

        if(lineMode == Line.MODE_PRIMARY){
            if(lineType == null){
                inputsValid = false;
            }
        }else if(lineMode == Line.MODE_SECONDARY){
            if(lineSelectedLine == null){
                inputsValid = false;
            }
        }

        return inputsValid;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(!isVisibleToUser) {
            Log.d(TAG, "Line " + LINE_POSITION + " NOT VISIBLE");
            if(validateInputsWithoutYoyo()){
                Log.d(TAG, "setUserVisibleHint - validateInputs()");
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(lineNameEditText.getWindowToken(), 0);

                //create the lines then device.setLines/line.setDeviceID then MySettings.addDevice()
                Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                if(dbDevice == null){
                    MySettings.addDevice(device);
                    dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                }

                long deviceID = dbDevice.getId();
                device.setId(deviceID);

                Log.d(TAG, "Adding line, deviceID = " + deviceID);

                Line newLine = new Line();
                newLine.setPosition(LINE_POSITION);
                if(lineNameEditText.getText().toString().length() >= 1){
                    newLine.setName(lineNameEditText.getText().toString());
                }else{
                    newLine.setName(lineNameEditText.getHint().toString());
                }
                newLine.setTypeID(lineType.getId());
                newLine.setPowerState(Line.LINE_STATE_OFF);
                newLine.setDeviceID(deviceID);
                if(lineDimmingCheckBox.isChecked()){
                    newLine.setDimmingState(Line.DIMMING_STATE_ON);
                }else{
                    newLine.setDimmingState(Line.DIMMING_STATE_OFF);
                }
                newLine.setMode(lineMode);
                if(lineMode == Line.MODE_SECONDARY){
                    newLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(lineSelectedLine.getDeviceID()).getChipID());
                    newLine.setPrimaryLinePosition(lineSelectedLine.getPosition());
                }

                List<Line> tempDeviceLines = new ArrayList<>();
                tempDeviceLines.addAll(device.getLines());
                int numberOfTempDeviceLines = tempDeviceLines.size();
                for(int x = 0; x < numberOfTempDeviceLines; x++){
                    Line tempDeviceLine = tempDeviceLines.get(x);
                    if(tempDeviceLine.getPosition() == newLine.getPosition()){
                        device.getLines().remove(x);
                    }
                }
                device.getLines().add(newLine);
                MySettings.setTempDevice(device);
                Log.d(TAG, "TempDevice: deviceID " + MySettings.getTempDevice().getId());
                for (Line line:MySettings.getTempDevice().getLines()) {
                    Log.d(TAG, "TempDevice: Line: Pos: " + line.getPosition() + " - Name: " + line.getName() + " - DeviceID: " + line.getDeviceID());
                }

            }
        }else{
            Log.d(TAG, "Line " + LINE_POSITION + " VISIBLE");
        }
        /*if (isResumed() && isVisibleToUser) {
            //do stuff
        }*/
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
