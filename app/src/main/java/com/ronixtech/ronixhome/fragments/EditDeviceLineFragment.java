package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditDeviceLineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditDeviceLineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDeviceLineFragment extends android.support.v4.app.Fragment implements TypePickerDialogFragment.OnTypeSelectedListener, PickLineDialogFragment.OnLineSelectedListener{
    private static final String TAG = EditDeviceLineFragment.class.getSimpleName();

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
    Line currentLine;

    private int DEVICE_NUMBER_OF_LINES;
    private int LINE_POSITION;
    private FragmentManager fragmentManager;

    EditDeviceFragment parentFragment;

    boolean unsavedChanges = false;

    public EditDeviceLineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditDeviceLineFragment.
     */
    public static EditDeviceLineFragment newInstance(String param1, String param2) {
        EditDeviceLineFragment fragment = new EditDeviceLineFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_device_line, container, false);

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
        }

        int last = DEVICE_NUMBER_OF_LINES - 1;
        if(LINE_POSITION < last){
            continueButton.setText(getActivity().getResources().getString(R.string.save_continue_button));
        }else{
            continueButton.setText(getActivity().getResources().getString(R.string.done));
        }

        currentLine = new Line();
        currentLine.setPosition(-1);
        for (Line line : device.getLines()) {
            if(line.getPosition() == LINE_POSITION){
                currentLine = line;
            }
        }
        if(currentLine != null && currentLine.getPosition() != -1){
            lineNameEditText.setText(currentLine.getName());

            lineType = currentLine.getType();
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


                if(currentLine.getMode() == Line.MODE_PRIMARY){
                    lineMode = Line.MODE_PRIMARY;
                    lineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                    lineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                }else if(currentLine.getMode() == Line.MODE_SECONDARY){
                    lineMode = Line.MODE_SECONDARY;
                    lineTypeLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.lightestGrayColor));
                    lineSelectedLineLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.whiteColor));
                }

                if(currentLine.getDimmingState() == Line.DIMMING_STATE_ON){
                    lineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_on));
                    lineDimmingCheckBox.setChecked(true);
                }else if(currentLine.getDimmingState() == Line.DIMMING_STATE_OFF){
                    lineDimmingTextView.setText(getActivity().getResources().getString(R.string.line_dimming_off));
                    lineDimmingCheckBox.setChecked(false);
                }

                if(currentLine.getMode() == Line.MODE_SECONDARY){
                    String primaryDeviceChipID = currentLine.getPrimaryDeviceChipID();
                    Device primaryDevice = MySettings.getDeviceByChipID2(primaryDeviceChipID);
                    if(primaryDevice != null){
                        int indexOfPrimaryLine = -1;
                        for (Line line : primaryDevice.getLines()) {
                            if(line.getPosition() == currentLine.getPrimaryLinePosition()){
                                indexOfPrimaryLine = primaryDevice.getLines().indexOf(line);
                            }
                        }
                        if(indexOfPrimaryLine != -1){
                            lineSelectedLine = primaryDevice.getLines().get(indexOfPrimaryLine);

                            Room room;
                            Floor floor;
                            room = MySettings.getRoom(primaryDevice.getRoomID());
                            floor = MySettings.getFloor(room.getFloorID());

                            lineSelectedLineNameTextView.setText(primaryDevice.getName() + ":" + lineSelectedLine.getName());
                            lineSelectedLineLocationTextView.setText(floor.getPlaceName() + ":" + room.getName());
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
                }

            }

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
                if(!lineNameEditText.getText().toString().equals(currentLine.getName())){
                    unsavedChanges = true;
                    parentFragment.tabUserChangesState(LINE_POSITION, true);
                    Log.d("AAAA", "lineNameEditText - afterTextChanged - changes");
                }else{
                    unsavedChanges = false;
                    parentFragment.tabUserChangesState(LINE_POSITION, false);
                    Log.d("AAAA", "lineNameEditText - afterTextChanged - no changes");
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
                if(lineMode != currentLine.getMode()){
                    unsavedChanges = true;
                    parentFragment.tabUserChangesState(LINE_POSITION, true);
                    Log.d("AAAA", "lineModeRadioGroup - onCheckedChanged - changes");
                }else{
                    unsavedChanges = false;
                    parentFragment.tabUserChangesState(LINE_POSITION, false);
                    Log.d("AAAA", "lineModeRadioGroup - onCheckedChanged - no changes");
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
                        fragment.setTargetFragment(EditDeviceLineFragment.this, 0);
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
                //try to sync with device
                DimmingSyncer dimmingSyncer = new DimmingSyncer(getActivity(), device, currentLine);
                dimmingSyncer.execute();
                //lineDimmingCheckBox.performClick();
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
                /*if(isChecked && currentLine.getDimmingState() == Line.DIMMING_STATE_ON){
                    unsavedChanges = false;
                    parentFragment.tabUserChangesState(LINE_POSITION, false);
                    Log.d("AAAA", "lineDimmingCheckBox - onCheckedChanged - no changes");
                }else if(!isChecked && currentLine.getDimmingState() == Line.DIMMING_STATE_OFF){
                    unsavedChanges = false;
                    parentFragment.tabUserChangesState(LINE_POSITION, false);
                    Log.d("AAAA", "lineDimmingCheckBox - onCheckedChanged - no changes");
                }else{
                    unsavedChanges = true;
                    parentFragment.tabUserChangesState(LINE_POSITION, true);
                    Log.d("AAAA", "lineDimmingCheckBox - onCheckedChanged - changes");
                }*/
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
                        fragment.setTargetFragment(EditDeviceLineFragment.this, 0);
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
                Log.d("continueButton", "onClick");
                //if all valid
                if(validateInputs()){
                    Log.d("continueButton", "validateInputs()");
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(lineNameEditText.getWindowToken(), 0);

                    Line newLine = new Line();
                    newLine.setPosition(LINE_POSITION);
                    if(lineNameEditText.getText().toString().length() >= 1){
                        newLine.setName(lineNameEditText.getText().toString());
                    }else{
                        newLine.setName(lineNameEditText.getHint().toString());
                    }
                    newLine.setTypeID(lineType.getId());
                    newLine.setPowerState(Line.LINE_STATE_OFF);
                    newLine.setDeviceID(device.getId());
                    /*if(lineDimmingCheckBox.isChecked()){
                        newLine.setDimmingState(Line.DIMMING_STATE_ON);
                    }else{
                        newLine.setDimmingState(Line.DIMMING_STATE_OFF);
                    }*/
                    newLine.setMode(lineMode);
                    if(lineMode == Line.MODE_SECONDARY){
                        newLine.setPrimaryDeviceChipID(MySettings.getDeviceByID2(lineSelectedLine.getDeviceID()).getChipID());
                        newLine.setPrimaryLinePosition(lineSelectedLine.getPosition());
                    }

                    MySettings.updateLine(currentLine.getId(), newLine);

                    unsavedChanges = false;
                    parentFragment.tabUserChangesState(LINE_POSITION, false);

                    int last = DEVICE_NUMBER_OF_LINES - 1;
                    Log.d("continueButton", "last: " + last);
                    Log.d("continueButton", "LINE_POSITION: " + LINE_POSITION);
                    if(LINE_POSITION < last){
                        Log.d("AAAA", "move to next fragment");
                        parentFragment.moveToNextFragment();
                    }else{
                        if(parentFragment.getUnsavedChanges()){
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                    //set icon
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    //set title
                                    .setTitle(getActivity().getResources().getString(R.string.edit_device_unsaved_changes_title))
                                    //set message
                                    .setMessage(getActivity().getResources().getString(R.string.edit_device_unsaved_changes_message))
                                    //set positive button
                                    .setPositiveButton(getActivity().getResources().getString(R.string.edit_device_unsaved_changes_discard), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what would happen when positive button is clicked
                                            Log.d("continueButton", "pop backstack!");
                                            if(parentFragment.getFragmentManager() != null){
                                                parentFragment.getFragmentManager().popBackStack();
                                            }
                                        }
                                    })
                                    //set negative button
                                    .setNegativeButton(getActivity().getResources().getString(R.string.edit_device_unsaved_changes_finish_changes_first), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what should happen when negative button is clicked
                                        }
                                    })
                                    .show();
                        }else{
                            Log.d("continueButton", "pop backstack!");
                            if(parentFragment.getFragmentManager() != null){
                                parentFragment.getFragmentManager().popBackStack();
                            }
                        }

                        /*FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                        fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocationFragment");
                        //fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                        fragmentTransaction.commit();*/
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


            if(type.getId() != currentLine.getTypeID()){
                unsavedChanges = true;
                parentFragment.tabUserChangesState(LINE_POSITION, true);
                Log.d("AAAA", "onTypeSelected - changes");
            }else{
                unsavedChanges = false;
                parentFragment.tabUserChangesState(LINE_POSITION, false);
                Log.d("AAAA", "onTypeSelected - no changes");
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


            Line oldSelectedLine;
            String primaryDeviceChipID = currentLine.getPrimaryDeviceChipID();
            Device primaryDevice = MySettings.getDeviceByChipID2(primaryDeviceChipID);
            if(primaryDevice != null){
                int indexOfPrimaryLine = -1;
                for (Line tempLine : primaryDevice.getLines()) {
                    if(tempLine.getPosition() == currentLine.getPrimaryLinePosition()){
                        indexOfPrimaryLine = primaryDevice.getLines().indexOf(line);
                    }
                }
                if(indexOfPrimaryLine != -1){
                    oldSelectedLine = primaryDevice.getLines().get(indexOfPrimaryLine);

                    if(lineSelectedLine.getId() != oldSelectedLine.getId()){
                        unsavedChanges = true;
                        Log.d("AAAA", "onLineSelected - changes");
                        parentFragment.tabUserChangesState(LINE_POSITION, true);
                    }else{
                        unsavedChanges = false;
                        Log.d("AAAA", "onLineSelected - no changes");
                        parentFragment.tabUserChangesState(LINE_POSITION, false);
                    }
                }
            }else{
                unsavedChanges = true;
                Log.d("AAAA", "onLineSelected - changes");
                parentFragment.tabUserChangesState(LINE_POSITION, true);
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

    public void setParentFragment(EditDeviceFragment fragment){
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

    public class DimmingSyncer extends AsyncTask<Void, Void, Void> {
        int statusCode;

        Activity activity;
        Device device;
        Line line;

        public DimmingSyncer(Activity activity, Device device, Line line) {
            this.activity = activity;
            this.device = device;
            this.line = line;
        }

        @Override
        protected void onPreExecute(){
            Utils.showLoading(activity);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200){
                //sync success
                Utils.dismissLoading();

                int newDimmingState = 0;
                if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                    newDimmingState = Line.DIMMING_STATE_OFF;
                }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                    newDimmingState = Line.DIMMING_STATE_ON;
                }

                currentLine.setDimmingState(newDimmingState);

                if(currentLine.getDimmingState() == Line.DIMMING_STATE_ON){
                    lineDimmingCheckBox.setChecked(true);
                }else if(currentLine.getDimmingState() == Line.DIMMING_STATE_OFF){
                    lineDimmingCheckBox.setChecked(false);
                }

                MySettings.updateLineDimmingState(currentLine, currentLine.getDimmingState());
            }else{
                Utils.dismissLoading();
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while(statusCode != 200 && numberOfRetries <= Device.CONFIG_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    URL url = new URL(urlString);
                    Log.d(TAG,  "syncDimmingState URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    int newDimmingState = 0;
                    if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                        newDimmingState = Line.DIMMING_STATE_OFF;
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                        newDimmingState = Line.DIMMING_STATE_ON;
                    }
                    if(line.getPosition() == 0){
                        jsonObject.put("L_0_D_S", "" + newDimmingState);
                    }else if(line.getPosition() == 1){
                        jsonObject.put("L_1_D_S", "" + newDimmingState);
                    }else if(line.getPosition() == 2){
                        jsonObject.put("L_2_D_S", "" + newDimmingState);
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Log.d(TAG,  "syncDimmingState POST data: " + jsonObject.toString());

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Log.d(TAG,  "syncDimmingState response: " + result.toString());
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

}
