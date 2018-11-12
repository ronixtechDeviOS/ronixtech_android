package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.daimajia.swipe.SwipeLayout;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.fragments.UpdateDeviceIntroFragment;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends ArrayAdapter {
    private static final String TAG = DeviceAdapter.class.getSimpleName();

    Activity activity ;
    List<Device> devices;
    ViewHolder vHolder = null;
    SwipeLayout swipeLayout;
    boolean layoutEnabled = true;
    FragmentManager fragmentManager;
    SimpleDateFormat simpleDateFormat;
    boolean controlsEnabled;

    public DeviceAdapter(Activity activity, List devices, FragmentManager fragmentManager){
        super(activity, R.layout.list_item_device, devices);
        this.activity = activity;
        this.devices = devices;
        mHandler = new android.os.Handler();
        this.fragmentManager = fragmentManager;
        simpleDateFormat = new SimpleDateFormat("h:mm a");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /*@Override
    public int getViewTypeCount() {
        return Device.DEVICE_NUMBER_OF_TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        return devices.get(position).getDeviceTypeID();
    }*/

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View rowView = convertView;
        //int deviceType = getItemViewType(position);
        Device item = (Device) devices.get(position);
        int deviceType = item.getDeviceTypeID();
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
            if(rowView == null){
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item_device, null);
                vHolder = new ViewHolder();
                vHolder.deviceNameTextView = rowView.findViewById(R.id.device_name_textview);
                vHolder.deviceLocationTextView = rowView.findViewById(R.id.device_location_textview);
                vHolder.firstLineLayout = rowView.findViewById(R.id.first_line_layout);
                vHolder.secondLineLayout = rowView.findViewById(R.id.second_line_layout);
                vHolder.thirdLineLayout = rowView.findViewById(R.id.third_line_layout);
                vHolder.firstLineTextView = rowView.findViewById(R.id.first_line_textvie);
                vHolder.secondLineTextView = rowView.findViewById(R.id.second_line_textview);
                vHolder.thirdLineTextView = rowView.findViewById(R.id.third_line_textview);
                vHolder.firstLineSeekBar = rowView.findViewById(R.id.first_line_seekbar);
                vHolder.secondLineSeekBar = rowView.findViewById(R.id.second_line_seekbar);
                vHolder.thirdLineSeekBar = rowView.findViewById(R.id.third_line_seekbar);
                vHolder.firstLineSwitch = rowView.findViewById(R.id.first_line_switch);
                vHolder.secondLineSwitch = rowView.findViewById(R.id.second_line_switch);
                vHolder.thirdLineSwitch = rowView.findViewById(R.id.third_line_switch);
                vHolder.firstLineAdvancedOptionsButton = rowView.findViewById(R.id.first_line_advanced_options_button);
                vHolder.secondLineAdvancedOptionsButton = rowView.findViewById(R.id.second_line_advanced_options_button);
                vHolder.thirdLineAdvancedOptionsButton = rowView.findViewById(R.id.third_line_advanced_options_button);
                vHolder.firstLineTypeImageView = rowView.findViewById(R.id.first_line_type_imageview);
                vHolder.secondLineTypeImageView = rowView.findViewById(R.id.second_line_type_imageview);
                vHolder.thirdLineTypeImageView = rowView.findViewById(R.id.third_line_type_imageview);
                vHolder.scanningNetworkLayout = rowView.findViewById(R.id.scanning_network_layout);
                vHolder.lastSeenLayout = rowView.findViewById(R.id.last_seen_layout);
                vHolder.lastSeenTextView = rowView.findViewById(R.id.last_seen_textview);
                vHolder.lastSeenImageView = rowView.findViewById(R.id.last_seen_imageview);
                vHolder.firmwareUpadteAvailableLayout = rowView.findViewById(R.id.firmware_available_layout);

                vHolder.firstLineSeekBar.setMax(10);
                vHolder.secondLineSeekBar.setMax(10);
                vHolder.thirdLineSeekBar.setMax(10);

                rowView.setTag(vHolder);
            }
            else{
                vHolder = (ViewHolder) rowView.getTag();
            }

            if(item != null){
                if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines){
                    vHolder.firstLineLayout.setVisibility(View.VISIBLE);
                    vHolder.secondLineLayout.setVisibility(View.GONE);
                    vHolder.thirdLineLayout.setVisibility(View.GONE);
                }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines){
                    vHolder.firstLineLayout.setVisibility(View.VISIBLE);
                    vHolder.secondLineLayout.setVisibility(View.VISIBLE);
                    vHolder.thirdLineLayout.setVisibility(View.GONE);
                }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                    vHolder.firstLineLayout.setVisibility(View.VISIBLE);
                    vHolder.secondLineLayout.setVisibility(View.VISIBLE);
                    vHolder.thirdLineLayout.setVisibility(View.VISIBLE);
                }

                if(item.getIpAddress() == null || item.getIpAddress().length() <= 1){
                    vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                    vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                    vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.VISIBLE);
                }else{
                    vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                    vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                    vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);
                }

                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, simpleDateFormat.format(item.getLastSeenTimestamp())));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                        deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                        deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
                    populateLineData(item);
                }else if(deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
                    populatePlugLineData(item);
                }

                controlsEnabled = true;

                if(item.isFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                    if(item.getFirmwareVersion() != null && item.getFirmwareVersion().length() >= 1){
                        Integer currentVersion = Integer.valueOf(item.getFirmwareVersion());
                        if(currentVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                            controlsEnabled = false;
                        }
                    }
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                vHolder.firstLineSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if(!MySettings.isControlActive()){
                            if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                boolean checked = ((ToggleButton) view).isChecked();
                                MySettings.setControlState(true);
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 0, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 0, Line.LINE_STATE_OFF);
                                }
                            }
                        }*/
                        if(controlsEnabled){
                            if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                boolean checked = ((ToggleButton) view).isChecked();
                                MySettings.setControlState(true);
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 0, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 0, Line.LINE_STATE_OFF);
                                }
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.secondLineSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if(!MySettings.isControlActive()){
                            if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                boolean checked = ((ToggleButton) view).isChecked();
                                MySettings.setControlState(true);
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 1, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 1, Line.LINE_STATE_OFF);
                                }
                            }
                        }*/
                        if(controlsEnabled) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                boolean checked = ((ToggleButton) view).isChecked();
                                MySettings.setControlState(true);
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 1, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 1, Line.LINE_STATE_OFF);
                                }
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }

                });
                vHolder.thirdLineSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if(!MySettings.isControlActive()){
                            if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                                boolean checked = ((ToggleButton) view).isChecked();
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 2, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 2, Line.LINE_STATE_OFF);
                                }
                            }
                        }*/
                        if(controlsEnabled) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                                boolean checked = ((ToggleButton) view).isChecked();
                                if (checked) {
                                    //turn on this line
                                    toggleLine(item, 2, Line.LINE_STATE_ON);
                                } else {
                                    //turn off this line
                                    toggleLine(item, 2, Line.LINE_STATE_OFF);
                                }
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                vHolder.firstLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b) {
                            /*if(!MySettings.isControlActive()){
                                if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                    MySettings.setControlState(true);
                                    controlDimming(item, 0, i);
                                }
                            }*/
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                            }
                        }*/
                        if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                            MySettings.setControlState(true);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                int i = seekBar.getProgress();
                                controlDimming(item, 0, i);
                            }
                        }*/
                        if(controlsEnabled) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                int i = seekBar.getProgress();
                                controlDimming(item, 0, i);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.secondLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b) {
                            /*if(!MySettings.isControlActive()) {
                                if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                    MySettings.setControlState(true);
                                    controlDimming(item, 1, i);
                                }
                            }*/
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                            }
                        }*/
                        if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                            MySettings.setControlState(true);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            int i = seekBar.getProgress();
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                controlDimming(item, 1, i);
                            }
                        }*/
                        if(controlsEnabled) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                int i = seekBar.getProgress();
                                controlDimming(item, 1, i);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.thirdLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b) {
                            /*if(!MySettings.isControlActive()) {
                                if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                    MySettings.setControlState(true);
                                    controlDimming(item, 2, i);
                                }
                            }*/
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                            }
                        }*/
                        if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                            MySettings.setControlState(true);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        /*if(!MySettings.isControlActive()) {
                            int i = seekBar.getProgress();
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                controlDimming(item, 2, i);
                            }
                        }*/
                        if(controlsEnabled) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                int i = seekBar.getProgress();
                                controlDimming(item, 2, i);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                vHolder.firstLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(true);

                            int dimmingState = item.getLines().get(0).getDimmingState();
                            if(dimmingState == Line.DIMMING_STATE_ON){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.disable_dimming));
                            }else if(dimmingState == Line.DIMMING_STATE_OFF){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.enable_dimming));
                            }
                        }else if(deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                            popup.getMenu().removeItem(R.id.action_toggle_dimming);
                        }

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                                MySettings.setControlState(true);
                                                int dimmingState = item.getLines().get(0).getDimmingState();
                                                if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                    toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                                                } else {
                                                    toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                                                }
                                            }
                                        }
                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_update){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_delete){
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            //set icon
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            //set title
                                            .setTitle(activity.getResources().getString(R.string.remove_unit_question))
                                            //set message
                                            .setMessage(activity.getResources().getString(R.string.remove_unit_message))
                                            //set positive button
                                            .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what would happen when positive button is clicked
                                                    removeDevice(item);
                                                }
                                            })
                                            //set negative button
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what should happen when negative button is clicked
                                                }
                                            })
                                            .show();
                                }
                                return true;
                            }
                        });
                    }
                });
                vHolder.secondLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(true);

                            int dimmingState = item.getLines().get(1).getDimmingState();
                            if(dimmingState == Line.DIMMING_STATE_ON){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.disable_dimming));
                            }else if(dimmingState == Line.DIMMING_STATE_OFF){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.enable_dimming));
                            }
                        }else if(deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                            popup.getMenu().removeItem(R.id.action_toggle_dimming);
                        }

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                                MySettings.setControlState(true);
                                                int dimmingState = item.getLines().get(1).getDimmingState();
                                                if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                    toggleDimming(item, 1, Line.DIMMING_STATE_ON);
                                                } else {
                                                    toggleDimming(item, 1, Line.DIMMING_STATE_OFF);
                                                }
                                            }
                                        }
                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_update){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_delete){
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            //set icon
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            //set title
                                            .setTitle(activity.getResources().getString(R.string.remove_unit_question))
                                            //set message
                                            .setMessage(activity.getResources().getString(R.string.remove_unit_message))
                                            //set positive button
                                            .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what would happen when positive button is clicked
                                                    removeDevice(item);
                                                }
                                            })
                                            //set negative button
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what should happen when negative button is clicked
                                                }
                                            })
                                            .show();
                                }
                                return true;
                            }
                        });
                    }
                });
                vHolder.thirdLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround) {
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(true);

                            int dimmingState = item.getLines().get(2).getDimmingState();
                            if(dimmingState == Line.DIMMING_STATE_ON){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.disable_dimming));
                            }else if(dimmingState == Line.DIMMING_STATE_OFF){
                                popup.getMenu().findItem(R.id.action_toggle_dimming).setTitle(activity.getResources().getString(R.string.enable_dimming));
                            }
                        }else if(deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
                            popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                            popup.getMenu().removeItem(R.id.action_toggle_dimming);
                        }

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                                MySettings.setControlState(true);
                                                int dimmingState = item.getLines().get(2).getDimmingState();
                                                if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                    toggleDimming(item, 2, Line.DIMMING_STATE_ON);
                                                } else {
                                                    toggleDimming(item, 2, Line.DIMMING_STATE_OFF);
                                                }
                                            }
                                        }
                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_update){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_delete){
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            //set icon
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            //set title
                                            .setTitle(activity.getResources().getString(R.string.remove_unit_question))
                                            //set message
                                            .setMessage(activity.getResources().getString(R.string.remove_unit_message))
                                            //set positive button
                                            .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what would happen when positive button is clicked
                                                    removeDevice(item);
                                                }
                                            })
                                            //set negative button
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what should happen when negative button is clicked
                                                }
                                            })
                                            .show();
                                }
                                return true;
                            }
                        });
                    }
                });
            }
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            if(rowView == null){
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item_device_sound_system_controller, null);
                vHolder = new ViewHolder();
                vHolder.soundDeviceLayout = rowView.findViewById(R.id.device_layout);
                vHolder.soundDeviceNameTextView = rowView.findViewById(R.id.device_name_textview);
                vHolder.speakerVolumeSeekBar = rowView.findViewById(R.id.device_volume_seekbar);
                vHolder.modeSwitch = rowView.findViewById(R.id.device_mode_switch);
                vHolder.soundDeviceAdvancedOptionsButton = rowView.findViewById(R.id.device_advanced_options_button);
                vHolder.soundDeviceTypeImageView = rowView.findViewById(R.id.device_type_imageview);
                vHolder.scanningNetworkLayout = rowView.findViewById(R.id.scanning_network_layout);
                vHolder.lastSeenLayout = rowView.findViewById(R.id.last_seen_layout);
                vHolder.lastSeenTextView = rowView.findViewById(R.id.last_seen_textview);
                vHolder.lastSeenImageView = rowView.findViewById(R.id.last_seen_imageview);
                vHolder.firmwareUpadteAvailableLayout = rowView.findViewById(R.id.firmware_available_layout);

                vHolder.speakerVolumeSeekBar.setMax(100);

                rowView.setTag(vHolder);
            }
            else{
                vHolder = (ViewHolder) rowView.getTag();
            }

            if(item != null){

                populateSoundSystemDeviceData(item);

                if(item.getIpAddress() == null || item.getIpAddress().length() <= 1){
                    //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);
                }

                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, simpleDateFormat.format(item.getLastSeenTimestamp())));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(item.isFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                vHolder.modeSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!MySettings.isControlActive()){
                            if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                                if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_LINE_IN){
                                    changeMode(item, SoundDeviceData.MODE_UPNP);
                                    Utils.openApp(activity, "Hi-Fi Cast - Music Player", "com.findhdmusic.app.upnpcast");
                                }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_UPNP){
                                    changeMode(item, SoundDeviceData.MODE_USB);
                                }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_USB){
                                    changeMode(item, SoundDeviceData.MODE_LINE_IN);
                                }
                            }
                        }
                    }
                });
                vHolder.speakerVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b) {
                            if(!MySettings.isControlActive()){
                                if(item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                    MySettings.setControlState(true);
                                    //controlDimming(item, 0, i);
                                }                        }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                MySettings.setControlState(true);
                            }
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if(!MySettings.isControlActive()) {
                            if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                int i = seekBar.getProgress();
                                //controlDimming(item, 0, i); control Volume
                            }
                        }
                    }
                });
                vHolder.soundDeviceAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_device_sound_system, popup.getMenu());

                        popup.getMenu().findItem(R.id.action_update).setVisible(false);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(!MySettings.isControlActive()) {
                                        if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                            MySettings.setControlState(true);
                                            int dimmingState = item.getLines().get(0).getDimmingState();
                                            if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                                            } else {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                                            }
                                        }
                                    }
                                }else if(id == R.id.action_update){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_delete){
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            //set icon
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            //set title
                                            .setTitle(activity.getResources().getString(R.string.remove_unit_question))
                                            //set message
                                            .setMessage(activity.getResources().getString(R.string.remove_unit_message))
                                            //set positive button
                                            .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what would happen when positive button is clicked
                                                    removeDevice(item);
                                                }
                                            })
                                            //set negative button
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what should happen when negative button is clicked
                                                }
                                            })
                                            .show();
                                }
                                return true;
                            }
                        });
                    }
                });
            }
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            if(rowView == null){
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item_device_pir_sensor, null);
                vHolder = new ViewHolder();
                vHolder.deviceNameTextView = rowView.findViewById(R.id.device_name_textview);
                vHolder.deviceLocationTextView = rowView.findViewById(R.id.device_location_textview);
                vHolder.pirNameTextView = rowView.findViewById(R.id.pir_textvie);
                vHolder.pirLayout = rowView.findViewById(R.id.pir_layout);
                vHolder.pirTypeImageView = rowView.findViewById(R.id.pir_type_imageview);
                vHolder.pirAdvancedOptionsButton = rowView.findViewById(R.id.pir_advanced_options_button);
                vHolder.scanningNetworkLayout = rowView.findViewById(R.id.scanning_network_layout);
                vHolder.lastSeenLayout = rowView.findViewById(R.id.last_seen_layout);
                vHolder.lastSeenTextView = rowView.findViewById(R.id.last_seen_textview);
                vHolder.lastSeenImageView = rowView.findViewById(R.id.last_seen_imageview);
                vHolder.firmwareUpadteAvailableLayout = rowView.findViewById(R.id.firmware_available_layout);

                rowView.setTag(vHolder);
            }
            else{
                vHolder = (ViewHolder) rowView.getTag();
            }

            if(item != null){
                populatePIRData(item);

                if(item.getIpAddress() == null || item.getIpAddress().length() <= 1){
                    //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);
                }

                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, simpleDateFormat.format(item.getLastSeenTimestamp())));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(item.isFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                vHolder.pirAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_device_sound_system, popup.getMenu());

                        popup.getMenu().findItem(R.id.action_update).setVisible(true);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(!MySettings.isControlActive()) {
                                        if (item.getIpAddress() != null && item.getIpAddress().length() >= 1) {
                                            MySettings.setControlState(true);
                                            int dimmingState = item.getLines().get(0).getDimmingState();
                                            if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                                            } else {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                                            }
                                        }
                                    }
                                }else if(id == R.id.action_update){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_delete){
                                    AlertDialog alertDialog = new AlertDialog.Builder(activity)
                                            //set icon
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            //set title
                                            .setTitle(activity.getResources().getString(R.string.remove_unit_question))
                                            //set message
                                            .setMessage(activity.getResources().getString(R.string.remove_unit_message))
                                            //set positive button
                                            .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what would happen when positive button is clicked
                                                    removeDevice(item);
                                                }
                                            })
                                            //set negative button
                                            .setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //set what should happen when negative button is clicked
                                                }
                                            })
                                            .show();
                                }
                                return true;
                            }
                        });
                    }
                });
            }
        }

        return rowView;
    }

    private void populateLineData(Device item){
        vHolder.deviceNameTextView.setText(""+item.getName()/* + " (" + item.getLines().size() + " lines)"*/);
        vHolder.deviceLocationTextView.setText(""+MySettings.getRoom(item.getRoomID()).getName());
        List<Line> lines = new ArrayList<>();
        lines.addAll(item.getLines());
        for (Line line : lines) {
            if(line.getPosition() == 0){
                vHolder.firstLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.firstLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.firstLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.firstLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.firstLineSwitch.setChecked(true);
                    vHolder.firstLineSeekBar.setProgress(line.getDimmingVvalue());
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.firstLineSwitch.setChecked(false);
                    vHolder.firstLineSeekBar.setProgress(0);
                }
                if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                    vHolder.firstLineSeekBar.setEnabled(true);
                    vHolder.firstLineSeekBar.setVisibility(View.VISIBLE);
                }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                    vHolder.firstLineSeekBar.setEnabled(false);
                    vHolder.firstLineSeekBar.setVisibility(View.INVISIBLE);
                }
            }else if(line.getPosition() == 1){
                vHolder.secondLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.secondLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.secondLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.secondLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.secondLineSwitch.setChecked(true);
                    vHolder.secondLineSeekBar.setProgress(line.getDimmingVvalue());
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.secondLineSwitch.setChecked(false);
                    vHolder.secondLineSeekBar.setProgress(0);
                }
                if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                    vHolder.secondLineSeekBar.setEnabled(true);
                    vHolder.secondLineSeekBar.setVisibility(View.VISIBLE);
                }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                    vHolder.secondLineSeekBar.setEnabled(false);
                    vHolder.secondLineSeekBar.setVisibility(View.INVISIBLE);
                }
            }else if(line.getPosition() == 2){
                vHolder.thirdLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.thirdLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.thirdLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.thirdLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.thirdLineSwitch.setChecked(true);
                    vHolder.thirdLineSeekBar.setProgress(line.getDimmingVvalue());
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.thirdLineSwitch.setChecked(false);
                    vHolder.thirdLineSeekBar.setProgress(0);
                }
                if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                    vHolder.thirdLineSeekBar.setEnabled(true);
                    vHolder.thirdLineSeekBar.setVisibility(View.VISIBLE);
                }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                    vHolder.thirdLineSeekBar.setEnabled(false);
                    vHolder.thirdLineSeekBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void populatePlugLineData(Device item){
        vHolder.deviceNameTextView.setText(""+item.getName()/* + " (" + item.getLines().size() + " lines)"*/);
        vHolder.deviceLocationTextView.setText(""+MySettings.getRoom(item.getRoomID()).getName());
        List<Line> lines = new ArrayList<>();
        lines.addAll(item.getLines());
        for (Line line : lines) {
            if(line.getPosition() == 0){
                vHolder.firstLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.firstLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.firstLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.firstLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.firstLineSwitch.setChecked(true);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.firstLineSwitch.setChecked(false);
                }
                vHolder.firstLineSeekBar.setVisibility(View.GONE);
            }else if(line.getPosition() == 1){
                vHolder.secondLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.secondLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.secondLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.secondLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.secondLineSwitch.setChecked(true);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.secondLineSwitch.setChecked(false);
                }
                vHolder.secondLineSeekBar.setVisibility(View.GONE);
            }else if(line.getPosition() == 2){
                vHolder.thirdLineTextView.setText(line.getName());
                if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                    GlideApp.with(activity)
                            .load(line.getType().getImageUrl())
                            .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                            .into(vHolder.thirdLineTypeImageView);
                }else {
                    if(line.getType().getImageResourceName() != null && line.getType().getImageResourceName().length() >= 1) {
                        vHolder.thirdLineTypeImageView.setImageResource(activity.getResources().getIdentifier(line.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        vHolder.thirdLineTypeImageView.setImageResource(line.getType().getImageResourceID());
                    }
                }
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    vHolder.thirdLineSwitch.setChecked(true);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.thirdLineSwitch.setChecked(false);
                }
                vHolder.thirdLineSeekBar.setVisibility(View.GONE);
            }
        }
    }

    private void populateSoundSystemDeviceData(Device item){
        vHolder.soundDeviceNameTextView.setText(""+item.getName());

        /*if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(line.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                    .into(vHolder.firstLineTypeImageView);
        }else {
            vHolder.firstLineTypeImageView.setImageResource(line.getType().getImageResourceID());
        }*/
        vHolder.soundDeviceTypeImageView.setImageResource(R.drawable.speaker_icon);
        if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_LINE_IN){
            vHolder.modeSwitch.setText(activity.getResources().getString(R.string.line_in));
            vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_UPNP){
            vHolder.modeSwitch.setText(activity.getResources().getString(R.string.upnp));
            vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_USB){
            vHolder.modeSwitch.setText(activity.getResources().getString(R.string.usb));
            vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }

        /*if(line.getPowerState() == Line.LINE_STATE_ON){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
            vHolder.firstLineSwitch.setChecked(true);
            vHolder.firstLineSeekBar.setVisibility(View.VISIBLE);
            if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                vHolder.firstLineDimmingCheckBox.setChecked(true);
                vHolder.firstLineDimmingCheckBox.setEnabled(true);
                vHolder.firstLineSeekBar.setEnabled(true);
                vHolder.firstLineSeekBar.setProgress(line.getDimmingVvalue());
                vHolder.firstLineSeekBar.setVisibility(View.VISIBLE);
            }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                vHolder.firstLineDimmingCheckBox.setChecked(false);
                vHolder.firstLineDimmingCheckBox.setEnabled(true);
                vHolder.firstLineSeekBar.setEnabled(false);
                vHolder.firstLineSeekBar.setProgress(0);
                vHolder.firstLineSeekBar.setVisibility(View.INVISIBLE);
            }else if(line.getDimmingState() == Line.DIMMING_STATE_PROCESSING){
                vHolder.firstLineDimmingCheckBox.setEnabled(false);
                vHolder.firstLineSeekBar.setEnabled(false);
            }
        }else if(line.getPowerState() == Line.LINE_STATE_OFF){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightGrayColor));
            vHolder.firstLineSwitch.setChecked(false);
            vHolder.firstLineDimmingCheckBox.setEnabled(false);
            vHolder.firstLineSeekBar.setEnabled(false);
            vHolder.firstLineDimmingCheckBox.setChecked(false);
            vHolder.firstLineSeekBar.setProgress(0);
            vHolder.firstLineSeekBar.setVisibility(View.INVISIBLE);
        }else if(line.getPowerState() == Line.LINE_STATE_PROCESSING){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
        }*/
    }

    private void populatePIRData(Device item){
        vHolder.pirNameTextView.setText(""+item.getName());

        /*if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(line.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                    .into(vHolder.firstLineTypeImageView);
        }else {
            vHolder.firstLineTypeImageView.setImageResource(line.getType().getImageResourceID());
        }*/
        vHolder.pirTypeImageView.setImageResource(R.drawable.motion_sensor_icon);
        if(item.getPIRData().getState() == Line.LINE_STATE_OFF){
            vHolder.pirNameTextView.setTextColor(activity.getResources().getColor(R.color.redColor));
        }else if(item.getPIRData().getState() == Line.LINE_STATE_ON){
            vHolder.pirNameTextView.setTextColor(activity.getResources().getColor(R.color.greenColor));
        }

        /*if(line.getPowerState() == Line.LINE_STATE_ON){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
            vHolder.firstLineSwitch.setChecked(true);
            vHolder.firstLineSeekBar.setVisibility(View.VISIBLE);
            if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                vHolder.firstLineDimmingCheckBox.setChecked(true);
                vHolder.firstLineDimmingCheckBox.setEnabled(true);
                vHolder.firstLineSeekBar.setEnabled(true);
                vHolder.firstLineSeekBar.setProgress(line.getDimmingVvalue());
                vHolder.firstLineSeekBar.setVisibility(View.VISIBLE);
            }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                vHolder.firstLineDimmingCheckBox.setChecked(false);
                vHolder.firstLineDimmingCheckBox.setEnabled(true);
                vHolder.firstLineSeekBar.setEnabled(false);
                vHolder.firstLineSeekBar.setProgress(0);
                vHolder.firstLineSeekBar.setVisibility(View.INVISIBLE);
            }else if(line.getDimmingState() == Line.DIMMING_STATE_PROCESSING){
                vHolder.firstLineDimmingCheckBox.setEnabled(false);
                vHolder.firstLineSeekBar.setEnabled(false);
            }
        }else if(line.getPowerState() == Line.LINE_STATE_OFF){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightGrayColor));
            vHolder.firstLineSwitch.setChecked(false);
            vHolder.firstLineDimmingCheckBox.setEnabled(false);
            vHolder.firstLineSeekBar.setEnabled(false);
            vHolder.firstLineDimmingCheckBox.setChecked(false);
            vHolder.firstLineSeekBar.setProgress(0);
            vHolder.firstLineSeekBar.setVisibility(View.INVISIBLE);
        }else if(line.getPowerState() == Line.LINE_STATE_PROCESSING){
            //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
        }*/
    }

    private void removeDevice(Device device){
        devices.remove(device);
        MySettings.removeDevice(device);
        DevicesInMemory.removeDevice(device);
        notifyDataSetChanged();
    }

    private void toggleLine(Device device, int position, final int state){
        if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
            Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
            if(currentFirmwareVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                //old method for controls
                LineToggler lineToggler = new LineToggler(device, position, state);
                lineToggler.execute();
                //temp hack for deviator demo, will be removed
                /*Line clickedLine = device.getLines().get(position);
                if(clickedLine.getMode() == Line.MODE_PRIMARY){
                    List<Line> secondaryLines = MySettings.getSecondaryLines(device);
                    if(secondaryLines != null && secondaryLines.size() >= 1){
                        for (Line secondaryLine:secondaryLines) {
                            Device secondaryDevice = MySettings.getDeviceByID2(secondaryLine.getDeviceID());
                            if(secondaryLine.getPosition() == position) {
                                LineToggler lineToggler1 = new LineToggler(secondaryDevice, secondaryLine.getPosition(), state);
                                lineToggler1.execute();
                            }
                        }
                    }
                }else if(clickedLine.getMode() == Line.MODE_SECONDARY){
                    String mainLineDeviceChipID = clickedLine.getPrimaryDeviceChipID();
                    int mainLinePosition = clickedLine.getPrimaryLinePosition();
                    Device mainDevice = MySettings.getDeviceByChipID2(mainLineDeviceChipID);
                    LineToggler lineToggler1 = new LineToggler(mainDevice, mainLinePosition, state);
                    lineToggler1.execute();
                }*/
            }else{
                //new method for controls
                Device localDevice = DevicesInMemory.getLocalDevice(device);
                List<Line> lines = localDevice.getLines();
                Line line = lines.get(position);

                line.setPowerState(state);
                if(state == Line.LINE_STATE_ON){
                    line.setDimmingVvalue(10);
                }else if(state == Line.LINE_STATE_OFF){
                    line.setDimmingVvalue(0);
                }
                lines.remove(line);
                lines.add(position, line);
                localDevice.setLines(lines);

                DevicesInMemory.updateLocalDevice(localDevice);

                MySettings.setControlState(false);

                //temp hack for deviator demo, will be removed
                /*if(line.getMode() == Line.MODE_PRIMARY){
                    List<Line> secondaryLines = MySettings.getSecondaryLines(device);
                    if(secondaryLines != null && secondaryLines.size() >= 1){
                        for (Line secondaryLine:secondaryLines) {
                            Device secondaryDevice = MySettings.getDeviceByID2(secondaryLine.getDeviceID());
                            if(secondaryLine.getPosition() == position) {
                                LineToggler lineToggler1 = new LineToggler(secondaryDevice, secondaryLine.getPosition(), state);
                                lineToggler1.execute();
                            }
                        }
                    }
                }else if(line.getMode() == Line.MODE_SECONDARY){
                    String mainLineDeviceChipID = line.getPrimaryDeviceChipID();
                    int mainLinePosition = line.getPrimaryLinePosition();
                    Device mainDevice = MySettings.getDeviceByChipID2(mainLineDeviceChipID);
                    LineToggler lineToggler1 = new LineToggler(mainDevice, mainLinePosition, state);
                    lineToggler1.execute();
                }*/
            }
        }else{
            MySettings.setControlState(false);
        }
    }

    private void toggleDimming(Device device, int position, int state){
        if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
            Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
            if(currentFirmwareVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                //old method for controls
                DimmingToggler dimmingToggler = new DimmingToggler(device, position, state);
                dimmingToggler.execute();
            }else{
                //new method for controls
                Device localDevice = DevicesInMemory.getLocalDevice(device);
                List<Line> lines = localDevice.getLines();
                Line line = lines.get(position);

                line.setDimmingState(state);

                lines.remove(line);
                lines.add(position, line);
                localDevice.setLines(lines);

                DevicesInMemory.updateLocalDevice(localDevice);

                MySettings.setControlState(false);
            }
        }else{
            MySettings.setControlState(false);
        }
    }

    private void controlDimming(Device device, int position, int value){
        if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
            Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
            if(currentFirmwareVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                //old method for controls
                DimmingController dimmingController = new DimmingController(device, position, value);
                dimmingController.execute();
            }else{
                //new method for controls
                Device localDevice = DevicesInMemory.getLocalDevice(device);
                List<Line> lines = localDevice.getLines();
                Line line = lines.get(position);

                line.setDimmingVvalue(value);

                lines.remove(line);
                lines.add(position, line);
                localDevice.setLines(lines);

                DevicesInMemory.updateLocalDevice(localDevice);

                MySettings.setControlState(false);
            }
        }else{
            MySettings.setControlState(false);
        }
    }

    private void changeMode(Device device, final int mode){
        ModeChanger modeChanger = new ModeChanger(device, mode);
        modeChanger.execute();
    }

    public static class ViewHolder{
        TextView deviceNameTextView, deviceLocationTextView;
        TextView firstLineTextView, secondLineTextView, thirdLineTextView;
        CardView firstLineLayout, secondLineLayout, thirdLineLayout;
        SeekBar firstLineSeekBar, secondLineSeekBar, thirdLineSeekBar;
        ToggleButton firstLineSwitch, secondLineSwitch, thirdLineSwitch;
        ImageView firstLineAdvancedOptionsButton, secondLineAdvancedOptionsButton, thirdLineAdvancedOptionsButton;
        ImageView firstLineTypeImageView, secondLineTypeImageView, thirdLineTypeImageView;

        TextView soundDeviceNameTextView;
        CardView soundDeviceLayout;
        ImageView soundDeviceTypeImageView;
        ImageView soundDeviceAdvancedOptionsButton;
        Button modeSwitch;
        SeekBar speakerVolumeSeekBar;//will be multiple ones, depending on number of speakers

        TextView pirNameTextView;
        CardView pirLayout;
        ImageView pirTypeImageView;
        ImageView pirAdvancedOptionsButton;

        RelativeLayout scanningNetworkLayout;
        RelativeLayout lastSeenLayout;
        TextView lastSeenTextView;
        ImageView lastSeenImageView;

        RelativeLayout firmwareUpadteAvailableLayout;
    }

    android.os.Handler mHandler;

    public class LineToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.LineToggler.class.getSimpleName();

        Device device;
        int position;
        int state;

        List<Line> lines;
        Line line;
        int oldState;

        int statusCode;

        public LineToggler(Device device, int position, int state) {
            this.device = device;
            this.position = position;
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            /*layoutEnabled = false;
            notifyDataSetChanged();
            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getPowerState();

            line.setPowerState(state);
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);*/
            /*if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }*/
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200){
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
            /*lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineState(line, oldState);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setControlState(false);
            layoutEnabled = true;*/
            //notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            layoutEnabled = false;

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getPowerState();

            line.setPowerState(state);
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Log.d(TAG, "getStatusActive, doing nothing...");
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfAttempts = 0;
            boolean delay = false;
            while(statusCode != 200 && numberOfAttempts <= Device.CONTROL_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "toggleLine attempt #"+numberOfAttempts);
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
                    if(position == 0){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 1){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 2){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
                    }

                    if(state == Line.LINE_STATE_ON){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
                    }else if(state == Line.LINE_STATE_OFF){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + "0");
                    }
                    URL url = new URL(urlString);

                    Log.d(TAG,  "toggleLine URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    //Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "toggleLine response: " + result.toString());*/
                }catch (MalformedURLException e){
                    line.setPowerState(oldState);
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    line.setPowerState(oldState);
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    line.setPowerState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttempts++;
                    delay = true;
                }
            }

            if(statusCode == 200){
                line.setPowerState(state);
            }else{
                line.setPowerState(oldState);
            }
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            layoutEnabled = true;
            //MySettings.updateLineState(line, oldState);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setControlState(false);

/*
            HttpURLConnection urlConnection = null;
            int statusCode = 0;
            int count = 0;
            while(statusCode != 200){
                Log.d(TAG,  "toggleLine attempt #: " + count);

                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
                    if(position == 0){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 1){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 2){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
                    }

                    if(state == Line.LINE_STATE_ON){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
                    }else if(state == Line.LINE_STATE_OFF){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + "0");
                    }
                    URL url = new URL(urlString);

                    Log.d(TAG,  "toggleLine URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    //Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    */
                /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "toggleLine response: " + result.toString());*//*

                }catch (MalformedURLException e){
                    line.setPowerState(oldState);
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    //line.setPowerState(oldState);
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    //line.setPowerState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    count++;
                }
            }
*/

            return null;
        }
    }

    public class DimmingToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.DimmingToggler.class.getSimpleName();

        Device device;
        int position;
        int state;

        List<Line> lines;
        Line line;
        int oldState;

        int statusCode;

        public DimmingToggler(Device device, int position, int state) {
            this.device = device;
            this.position = position;
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            /*layoutEnabled = false;
            notifyDataSetChanged();
            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getDimmingState();

            line.setDimmingState(state);

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingState(line, state);
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }*/
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200){
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
            /*lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingState(line, oldState);
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }

            MySettings.setControlState(false);

            layoutEnabled = true;
            notifyDataSetChanged();*/
        }

        @Override
        protected Void doInBackground(Void... params) {
            layoutEnabled = false;

            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getDimmingState();

            line.setDimmingState(state);

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingState(line, state);

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Log.d(TAG, "getStatusActive, doing nothing...");
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfAttempts = 0;
            boolean delay = false;
            while(statusCode != 200 && numberOfAttempts <= Device.CONTROL_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "toggleDimming attempt #"+numberOfAttempts);
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
                    if(position == 0){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_STATE);
                    }else if(position == 1){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_STATE);
                    }else if(position == 2){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_STATE);
                    }
                    urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + state);
                    URL url = new URL(urlString);

                    Log.d(TAG,  "toggleDimming URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "toggleDimming response: " + result.toString());*/

                }catch (MalformedURLException e){
                    line.setDimmingState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    line.setDimmingState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (Exception e){
                    line.setDimmingState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "toggleDimming responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttempts++;
                    delay = true;
                }
            }

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingState(line, oldState);
            layoutEnabled = true;
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }

            MySettings.setControlState(false);

            return null;
        }
    }

    public class DimmingController extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.DimmingController.class.getSimpleName();

        Device device;
        int position;
        int value;

        List<Line> lines;
        Line line;
        int oldValue;

        int statusCode;

        public DimmingController(Device device, int position, int value) {
            this.device = device;
            this.position = position;
            this.value = value;
        }

        @Override
        protected void onPreExecute(){
            //setLayoutEnabled(false);
            //setLayoutEnabledDelayed(true);
            /*layoutEnabled = false;
            notifyDataSetChanged();

            lines = device.getLines();
            line = lines.get(position);
            oldValue = line.getDimmingVvalue();

            line.setDimmingVvalue(value);

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);*/
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200){
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
            /*lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);

            //MySettings.updateLineDimmingValue(line, oldValue);
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }

            //MySettings.setControlState(false);

            layoutEnabled = true;
            notifyDataSetChanged();*/
        }

        @Override
        protected Void doInBackground(Void... params) {
            layoutEnabled = false;

            lines = device.getLines();
            line = lines.get(position);
            oldValue = line.getDimmingVvalue();

            line.setDimmingVvalue(value);

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Log.d(TAG, "getStatusActive, doing nothing...");
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfAttemtps = 0;
            boolean delay = false;
            while(statusCode != 200 && numberOfAttemtps <= Device.CONTROL_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "controlDimming attempt #"+numberOfAttemtps);
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
                    if(position == 0){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 1){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 2){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
                    }

                    if(value == 10){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
                    }else{
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + value);
                    }
                    URL url = new URL(urlString);

                    Log.d(TAG,  "controlDimming URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "controlDimming response: " + result.toString());*/
                }catch (MalformedURLException e){
                    line.setDimmingVvalue(oldValue);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (IOException e){
                    line.setDimmingVvalue(oldValue);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (Exception e){
                    line.setDimmingVvalue(oldValue);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "controlDimming responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttemtps++;
                    delay = true;
                }
            }

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingValue(line, oldValue);
            layoutEnabled = true;
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }

            //MySettings.setControlState(false);

            return null;
        }
    }

    public class ModeChanger extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.ModeChanger.class.getSimpleName();

        Device device;
        int mode;

        int oldMode;

        int statusCode;

        public ModeChanger(Device device, int mode) {
            this.device = device;
            this.mode = mode;
        }

        @Override
        protected void onPreExecute(){
            /*layoutEnabled = false;
            notifyDataSetChanged();
            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getPowerState();

            line.setPowerState(state);
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);*/
            /*if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }*/
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200){
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
            /*lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineState(line, oldState);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setControlState(false);
            layoutEnabled = true;*/
            //notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            layoutEnabled = false;

            oldMode = device.getSoundDeviceData().getMode();

            device.getSoundDeviceData().setMode(mode);
            DevicesInMemory.updateDevice(device);

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Log.d(TAG, "getStatusActive, doing nothing...");
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfAttempts = 0;
            boolean delay = false;
            while(statusCode != 200 && numberOfAttempts <= Device.CONTROL_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "changeMode attempt #"+numberOfAttempts);
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_SOUND_DEVICE_CHANGE_MODE_URL;
                    /*if(mode == SoundDeviceData.MODE_LINE_IN){
                        urlString = urlString.concat("?" + Constants.PARAMETER_SOUND_CONTROLLER_MODE + "=" + Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN);
                    }else if(mode == SoundDeviceData.MODE_UPNP){
                        urlString = urlString.concat("?" + Constants.PARAMETER_SOUND_CONTROLLER_MODE + "=" + Constants.PARAMETER_SOUND_CONTROLLER_MODE_UPNP);
                    }else if(mode == SoundD-eviceData.MODE_USB){
                        urlString = urlString.concat("?" + Constants.PARAMETER_SOUND_CONTROLLER_MODE + "=" + Constants.PARAMETER_SOUND_CONTROLLER_MODE_USB);
                    }*/

                    //urlString = urlString.concat("&" + Constants.PARAMETER_ACCESS_TOKEN + "=" + device.getAccessToken());

                    URL url = new URL(urlString);

                    Log.d(TAG,  "changeMode URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    if(mode == SoundDeviceData.MODE_LINE_IN){
                        jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN);
                    }else if(mode == SoundDeviceData.MODE_UPNP){
                        jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, Constants.PARAMETER_SOUND_CONTROLLER_MODE_UPNP);
                    }else if(mode == SoundDeviceData.MODE_USB){
                        jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, Constants.PARAMETER_SOUND_CONTROLLER_MODE_USB);
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Log.d(TAG,  "changeMode POST data: " + jsonObject.toString());

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    //Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "toggleLine response: " + result.toString());*/
                }catch (MalformedURLException e){
                    device.getSoundDeviceData().setMode(oldMode);
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    device.getSoundDeviceData().setMode(oldMode);
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    device.getSoundDeviceData().setMode(oldMode);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "changeMode responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttempts++;
                    delay = true;
                }
            }

            DevicesInMemory.updateDevice(device);
            layoutEnabled = true;
            //MySettings.updateLineState(line, oldState);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setControlState(false);

/*
            HttpURLConnection urlConnection = null;
            int statusCode = 0;
            int count = 0;
            while(statusCode != 200){
                Log.d(TAG,  "toggleLine attempt #: " + count);

                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
                    if(position == 0){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 1){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
                    }else if(position == 2){
                        urlString = urlString.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
                    }

                    if(state == Line.LINE_STATE_ON){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
                    }else if(state == Line.LINE_STATE_OFF){
                        urlString = urlString.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + "0");
                    }
                    URL url = new URL(urlString);

                    Log.d(TAG,  "toggleLine URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONTROL_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONTROL_TIMEOUT);
                    statusCode = urlConnection.getResponseCode();
                    //Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    */
/*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "toggleLine response: " + result.toString());*//*

                }catch (MalformedURLException e){
                    line.setPowerState(oldState);
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    //line.setPowerState(oldState);
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    //line.setPowerState(oldState);
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    Log.d(TAG,  "toggleLine responseCode: " + statusCode);
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    count++;
                }
            }
*/

            return null;
        }
    }
}