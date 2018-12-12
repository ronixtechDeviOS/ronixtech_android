package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.DeviceInfoFragment;
import com.ronixtech.ronixhome.fragments.EditDeviceFragment;
import com.ronixtech.ronixhome.fragments.EditDeviceLocationFragment;
import com.ronixtech.ronixhome.fragments.EditDevicePIRFragment;
import com.ronixtech.ronixhome.fragments.UpdateDeviceIntroFragment;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DeviceAdapter extends ArrayAdapter {
    private static final String TAG = DeviceAdapter.class.getSimpleName();

    Activity activity ;
    List<Device> devices;
    ViewHolder vHolder = null;
    SwipeLayout swipeLayout;
    boolean layoutEnabled = true;
    FragmentManager fragmentManager;
    boolean controlsEnabled;
    int placeMode;
    //Stuff for remote/MQTT mode
    MqttAndroidClient mqttAndroidClient;

    public DeviceAdapter(Activity activity, List devices, FragmentManager fragmentManager, int mode){
        super(activity, R.layout.list_item_device, devices);
        this.activity = activity;
        this.devices = devices;
        mHandler = new android.os.Handler();
        this.fragmentManager = fragmentManager;
        this.placeMode = mode;
        //String clientId = MqttClient.generateClientId();
        //getMqttClient(activity, Constants.MQTT_URL + ":" + Constants.MQTT_PORT, clientId);
        DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
        if(fragment != null){
            mqttAndroidClient = fragment.getMqttAndroidClient();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        int deviceType = devices.get(position).getDeviceTypeID();
        if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines|| deviceType == Device.DEVICE_TYPE_PLUG_3lines){
            return 0;
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            return 1;
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            return 2;
        }
        return 0;
    }

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
        int viewType = getItemViewType(position);
        Device item = (Device) devices.get(position);
        //int deviceType = item.getDeviceTypeID();
        if(viewType == 0){
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
                vHolder.mqttReachabilityLayout = rowView.findViewById(R.id.mqtt_reachability_layout);

                vHolder.firstLineSeekBar.setMax(100);
                vHolder.secondLineSeekBar.setMax(100);
                vHolder.thirdLineSeekBar.setMax(100);

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

                if(placeMode == Place.PLACE_MODE_LOCAL){
                    vHolder.mqttReachabilityLayout.setVisibility(View.GONE);

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
                }else if(placeMode == Place.PLACE_MODE_REMOTE){
                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);

                    if(item.isDeviceMQTTReachable()){
                        vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                        vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                        vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.GONE);
                    }else{
                        vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                        vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                        vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.VISIBLE);
                    }
                }


                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);

                    //show full date if not same day (if it's a new day)
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    long currentTimestamp = cal2.getTimeInMillis();
                    cal1.setTimeInMillis(item.getLastSeenTimestamp());
                    cal2.setTimeInMillis(currentTimestamp);
                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                    if (!sameDay) {
                        vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, Utils.getTimeStringDateHoursMinutes(item.getLastSeenTimestamp())));
                    }else{
                        vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, Utils.getTimeStringHoursMinutesSeconds(item.getLastSeenTimestamp())));
                    }

                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround) {
                    populateLineData(item);
                }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines|| item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                    populatePlugLineData(item);
                }

                controlsEnabled = true;

                if(item.isFirmwareUpdateAvailable() || item.isHwFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                    if(item.getFirmwareVersion() != null && item.getFirmwareVersion().length() >= 1){
                        Integer currentVersion = Integer.valueOf(item.getFirmwareVersion());
                        if(currentVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                            controlsEnabled = false;
                        }
                    }
                    vHolder.firmwareUpadteAvailableLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(placeMode == Place.PLACE_MODE_LOCAL) {
                                MySettings.setTempDevice(item);

                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                fragmentTransaction.commit();
                            }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                final ViewHolder tempViewHolder = vHolder;
                vHolder.firstLineTypeImageView.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 0, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 0, Line.LINE_STATE_OFF);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.firstLineLayout.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            tempViewHolder.firstLineTypeImageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 0, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 0, Line.LINE_STATE_OFF);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.secondLineTypeImageView.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 1, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 1, Line.LINE_STATE_OFF);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }

                });
                vHolder.secondLineLayout.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            tempViewHolder.secondLineTypeImageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 1, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 1, Line.LINE_STATE_OFF);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }

                });
                vHolder.thirdLineTypeImageView.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(2).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 2, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 2, Line.LINE_STATE_OFF);
                            }
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                vHolder.thirdLineLayout.setOnClickListener(new View.OnClickListener() {
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
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            tempViewHolder.thirdLineTypeImageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                            if (item.getLines().get(2).getPowerState() == Line.LINE_STATE_OFF) {
                                //turn on this line
                                tempViewHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                                toggleLine(item, 2, Line.LINE_STATE_ON);
                            } else {
                                //turn off this line
                                tempViewHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
                                toggleLine(item, 2, Line.LINE_STATE_OFF);
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
                        if(controlsEnabled) {
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
                            int i = seekBar.getProgress();
                            double progressValue = i/10.0;
                            int progress = (int) (progressValue);
                            controlDimming(item, 0, progress);
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
                        if(controlsEnabled) {
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
                            int i = seekBar.getProgress();
                            double progressValue = i/10.0;
                            int progress = (int) (progressValue);
                            controlDimming(item, 1, progress);
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
                        if(controlsEnabled) {
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
                            int i = seekBar.getProgress();
                            double progressValue = i/10.0;
                            int progress = (int) (progressValue);
                            controlDimming(item, 2, progress);
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                vHolder.firstLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        /*if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
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
                        }*/

                        popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                        popup.getMenu().removeItem(R.id.action_toggle_dimming);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            int dimmingState = item.getLines().get(0).getDimmingState();
                                            if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                                            } else {
                                                toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                                            }
                                        }else{
                                            Toast.makeText(activity, "controls active", Toast.LENGTH_SHORT).show();
                                        }

                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_device_info){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
                                    deviceInfoFragment.setDevice(item);
                                    deviceInfoFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, deviceInfoFragment, "deviceInfoFragment");
                                    fragmentTransaction.addToBackStack("deviceInfoFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceFragment editDeviceFragment = new EditDeviceFragment();
                                    editDeviceFragment.setPlaceMode(placeMode);
                                    editDeviceFragment.setMqttClient(mqttAndroidClient);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceFragment, "editDeviceFragment");
                                    fragmentTransaction.addToBackStack("editDeviceFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device_location){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceLocationFragment editDeviceLocationFragment = new EditDeviceLocationFragment();
                                    editDeviceLocationFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceLocationFragment, "editDeviceLocationFragment");
                                    fragmentTransaction.addToBackStack("editDeviceLocationFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_update_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_remove_device){
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
                vHolder.secondLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        /*if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
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
                        }*/

                        popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                        popup.getMenu().removeItem(R.id.action_toggle_dimming);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            int dimmingState = item.getLines().get(1).getDimmingState();
                                            if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                toggleDimming(item, 1, Line.DIMMING_STATE_ON);
                                            } else {
                                                toggleDimming(item, 1, Line.DIMMING_STATE_OFF);
                                            }
                                        }
                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_device_info){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
                                    deviceInfoFragment.setDevice(item);
                                    deviceInfoFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, deviceInfoFragment, "deviceInfoFragment");
                                    fragmentTransaction.addToBackStack("deviceInfoFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceFragment editDeviceFragment = new EditDeviceFragment();
                                    editDeviceFragment.setPlaceMode(placeMode);
                                    editDeviceFragment.setMqttClient(mqttAndroidClient);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceFragment, "editDeviceFragment");
                                    fragmentTransaction.addToBackStack("editDeviceFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device_location){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceLocationFragment editDeviceLocationFragment = new EditDeviceLocationFragment();
                                    editDeviceLocationFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceLocationFragment, "editDeviceLocationFragment");
                                    fragmentTransaction.addToBackStack("editDeviceLocationFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_update_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_remove_device){
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
                vHolder.thirdLineAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_line, popup.getMenu());

                        /*if(deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
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
                        }*/

                        popup.getMenu().findItem(R.id.action_toggle_dimming).setVisible(false);
                        popup.getMenu().removeItem(R.id.action_toggle_dimming);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_toggle_dimming){
                                    if(controlsEnabled) {
                                        if (!MySettings.isControlActive()) {
                                            int dimmingState = item.getLines().get(2).getDimmingState();
                                            if (dimmingState == Line.DIMMING_STATE_OFF) {
                                                toggleDimming(item, 2, Line.DIMMING_STATE_ON);
                                            } else {
                                                toggleDimming(item, 2, Line.DIMMING_STATE_OFF);
                                            }
                                        }
                                    }else{
                                        Toast.makeText(activity, activity.getResources().getString(R.string.firmware_update_required), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_device_info){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
                                    deviceInfoFragment.setDevice(item);
                                    deviceInfoFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, deviceInfoFragment, "deviceInfoFragment");
                                    fragmentTransaction.addToBackStack("deviceInfoFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceFragment editDeviceFragment = new EditDeviceFragment();
                                    editDeviceFragment.setPlaceMode(placeMode);
                                    editDeviceFragment.setMqttClient(mqttAndroidClient);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceFragment, "editDeviceFragment");
                                    fragmentTransaction.addToBackStack("editDeviceFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device_location){
                                    MySettings.setTempDevice(item);

                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    EditDeviceLocationFragment editDeviceLocationFragment = new EditDeviceLocationFragment();
                                    editDeviceLocationFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, editDeviceLocationFragment, "editDeviceLocationFragment");
                                    fragmentTransaction.addToBackStack("editDeviceLocationFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_update_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_remove_device){
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

                vHolder.firstLineTypeImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.firstLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
                vHolder.firstLineLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.firstLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
                vHolder.secondLineTypeImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.secondLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
                vHolder.secondLineLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.secondLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
                vHolder.thirdLineTypeImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.thirdLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
                vHolder.thirdLineLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.thirdLineAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
            }
        }else if(viewType == 1){
            if(rowView == null){
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item_device_sound_system_controller, null);
                vHolder = new ViewHolder();
                vHolder.soundDeviceLayout = rowView.findViewById(R.id.device_layout);
                vHolder.soundDeviceNameTextView = rowView.findViewById(R.id.device_name_textview);
                vHolder.speakerVolumeSeekBar = rowView.findViewById(R.id.device_volume_seekbar);
                vHolder.speakersLayout = rowView.findViewById(R.id.speakers_layout);
                vHolder.deviceModeLayout = rowView.findViewById(R.id.active_mode_layout);
                vHolder.deviceModeTextView = rowView.findViewById(R.id.device_active_mode_textview);
                vHolder.deviceModeArrowImageView = rowView.findViewById(R.id.mode_arrow_imageview);
                vHolder.soundDeviceAdvancedOptionsButton = rowView.findViewById(R.id.device_advanced_options_button);
                vHolder.soundDeviceTypeImageView = rowView.findViewById(R.id.device_type_imageview);
                vHolder.scanningNetworkLayout = rowView.findViewById(R.id.scanning_network_layout);
                vHolder.lastSeenLayout = rowView.findViewById(R.id.last_seen_layout);
                vHolder.lastSeenTextView = rowView.findViewById(R.id.last_seen_textview);
                vHolder.lastSeenImageView = rowView.findViewById(R.id.last_seen_imageview);
                vHolder.firmwareUpadteAvailableLayout = rowView.findViewById(R.id.firmware_available_layout);
                vHolder.mqttReachabilityLayout = rowView.findViewById(R.id.mqtt_reachability_layout);

                vHolder.speakerVolumeSeekBar.setMax(100);

                rowView.setTag(vHolder);
            }
            else{
                vHolder = (ViewHolder) rowView.getTag();
            }

            if(item != null){

                populateSoundSystemDeviceData(item);

                if(placeMode == Place.PLACE_MODE_LOCAL) {
                    vHolder.mqttReachabilityLayout.setVisibility(View.GONE);

                    if (item.getIpAddress() == null || item.getIpAddress().length() <= 1) {
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                        vHolder.scanningNetworkLayout.setVisibility(View.VISIBLE);
                    } else {
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                        vHolder.scanningNetworkLayout.setVisibility(View.GONE);
                    }
                }else if(placeMode == Place.PLACE_MODE_REMOTE){
                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);

                    if(item.isDeviceMQTTReachable()){
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.GONE);
                    }else{
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.soundDeviceLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.VISIBLE);
                    }
                }

                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, Utils.getTimeStringHoursMinutesSeconds(item.getLastSeenTimestamp())));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(item.isFirmwareUpdateAvailable() || item.isHwFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                    vHolder.firmwareUpadteAvailableLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(placeMode == Place.PLACE_MODE_LOCAL) {
                                MySettings.setTempDevice(item);

                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                fragmentTransaction.commit();
                            }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                final ViewHolder tempViewHolder = vHolder;
                vHolder.deviceModeArrowImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_mode_switcher, popup.getMenu());

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.mode_line_1){
                                    changeMode(item, SoundDeviceData.MODE_LINE_IN);
                                }else if(id == R.id.mode_line_2){
                                    changeMode(item, SoundDeviceData.MODE_LINE_IN_2);
                                }else if(id == R.id.mode_upnp){
                                    changeMode(item, SoundDeviceData.MODE_UPNP);
                                }else if(id == R.id.mode_usb){
                                    changeMode(item, SoundDeviceData.MODE_USB);
                                }
                                return true;
                            }
                        });
                    }
                });
                vHolder.deviceModeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tempViewHolder.deviceModeArrowImageView.performClick();
                    }
                });
                vHolder.soundDeviceLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tempViewHolder.deviceModeArrowImageView.performClick();
                    }
                });
                vHolder.speakerVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if(b) {
                            if(!MySettings.isControlActive()){
                                //controlDimming(item, 0, i);
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        MySettings.setControlState(true);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int i = seekBar.getProgress();
                        controlDimming(item, 0, i); //control Volume
                    }
                });
                vHolder.soundDeviceAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_device_sound_system, popup.getMenu());

                        popup.getMenu().findItem(R.id.action_update_device).setVisible(false);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_device_info){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
                                    deviceInfoFragment.setDevice(item);
                                    deviceInfoFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, deviceInfoFragment, "deviceInfoFragment");
                                    fragmentTransaction.addToBackStack("deviceInfoFragment");
                                    fragmentTransaction.commit();
                                }
                                else if(id == R.id.action_update_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_remove_device){
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

                vHolder.soundDeviceLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.soundDeviceAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
            }
        }else if(viewType == 2){
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
                vHolder.mqttReachabilityLayout = rowView.findViewById(R.id.mqtt_reachability_layout);

                rowView.setTag(vHolder);
            }
            else{
                vHolder = (ViewHolder) rowView.getTag();
            }

            if(item != null){
                populatePIRData(item);

                if(placeMode == Place.PLACE_MODE_LOCAL) {
                    vHolder.mqttReachabilityLayout.setVisibility(View.GONE);

                    if (item.getIpAddress() == null || item.getIpAddress().length() <= 1) {
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                        vHolder.scanningNetworkLayout.setVisibility(View.VISIBLE);
                    } else {
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                        vHolder.scanningNetworkLayout.setVisibility(View.GONE);
                    }
                }else if(placeMode == Place.PLACE_MODE_REMOTE){
                    vHolder.scanningNetworkLayout.setVisibility(View.GONE);

                    if(item.isDeviceMQTTReachable()){
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.GONE);
                    }else{
                        //vHolder.soundDeviceNameTextView.setPaintFlags(vHolder.soundDeviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        vHolder.pirLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

                        vHolder.mqttReachabilityLayout.setVisibility(View.VISIBLE);
                    }
                }

                if(item.getLastSeenTimestamp() != 0) {
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, Utils.getTimeStringHoursMinutesSeconds(item.getLastSeenTimestamp())));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }else{
                    //vHolder.lastSeenLayout.setVisibility(View.GONE);
                    vHolder.lastSeenTextView.setText(activity.getResources().getString(R.string.last_seen, "--:--"));
                    vHolder.lastSeenLayout.setVisibility(View.VISIBLE);
                }

                if(item.isFirmwareUpdateAvailable() || item.isHwFirmwareUpdateAvailable()){
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.VISIBLE);
                    vHolder.firmwareUpadteAvailableLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(placeMode == Place.PLACE_MODE_LOCAL) {
                                MySettings.setTempDevice(item);

                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                fragmentTransaction.commit();
                            }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    vHolder.firmwareUpadteAvailableLayout.setVisibility(View.GONE);
                }

                final ViewHolder tempViewHolder = vHolder;
                vHolder.pirAdvancedOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        PopupMenu popup = new PopupMenu(activity, view);
                        popup.getMenuInflater().inflate(R.menu.menu_pir, popup.getMenu());

                        popup.getMenu().findItem(R.id.action_update_device).setVisible(true);

                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item1) {
                                int id = item1.getItemId();
                                if(id == R.id.action_device_info){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                    DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
                                    deviceInfoFragment.setDevice(item);
                                    deviceInfoFragment.setPlaceMode(placeMode);
                                    fragmentTransaction.replace(R.id.fragment_view, deviceInfoFragment, "deviceInfoFragment");
                                    fragmentTransaction.addToBackStack("deviceInfoFragment");
                                    fragmentTransaction.commit();
                                }else if(id == R.id.action_edit_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        EditDevicePIRFragment editDevicePIRFragment = new EditDevicePIRFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, editDevicePIRFragment, "editDevicePIRFragment");
                                        fragmentTransaction.addToBackStack("editDevicePIRFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_edit_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_update_device){
                                    if(placeMode == Place.PLACE_MODE_LOCAL) {
                                        MySettings.setTempDevice(item);

                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                                        UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                                        fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                                        fragmentTransaction.commit();
                                    }else if(placeMode == Place.PLACE_MODE_REMOTE){
                                        Toast.makeText(activity, activity.getResources().getString(R.string.device_update_disabled_only_local_mode), Toast.LENGTH_LONG).show();
                                    }
                                }else if(id == R.id.action_remove_device){
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

                vHolder.pirLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        tempViewHolder.pirAdvancedOptionsButton.performClick();
                        return true;
                    }
                });
            }
        }

        return rowView;
    }

    private void populateLineData(Device item){
        vHolder.deviceNameTextView.setText(""+item.getName()/* + " (" + item.getLines().size() + " lines)"*/);
        if(MySettings.getRoom(item.getRoomID()) != null){
            vHolder.deviceLocationTextView.setText(""+MySettings.getRoom(item.getRoomID()).getName());
        }
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
                    vHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                    if(Utils.difference(line.getDimmingVvalue() * 10, vHolder.firstLineSeekBar.getProgress()) > 10){
                        vHolder.firstLineSeekBar.setProgress(line.getDimmingVvalue() * 10);
                    }
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
                    vHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                    if(Utils.difference(line.getDimmingVvalue() * 10, vHolder.secondLineSeekBar.getProgress()) > 10){
                        vHolder.secondLineSeekBar.setProgress(line.getDimmingVvalue() * 10);
                    }
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
                    vHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                    if(Utils.difference(line.getDimmingVvalue() * 10, vHolder.thirdLineSeekBar.getProgress()) > 10){
                        vHolder.thirdLineSeekBar.setProgress(line.getDimmingVvalue() * 10);
                    }
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
        if(MySettings.getRoom(item.getRoomID()) != null){
            vHolder.deviceLocationTextView.setText(""+MySettings.getRoom(item.getRoomID()).getName());
        }
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
                    vHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.firstLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
                    vHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.secondLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
                    vHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    vHolder.thirdLineTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
            vHolder.deviceModeTextView.setText(activity.getResources().getString(R.string.line_in_1));
            //vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_LINE_IN_2){
            vHolder.deviceModeTextView.setText(activity.getResources().getString(R.string.line_in_2));
            //vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_UPNP){
            vHolder.deviceModeTextView.setText(activity.getResources().getString(R.string.upnp));
            //vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }else if(item.getSoundDeviceData().getMode() == SoundDeviceData.MODE_USB){
            vHolder.deviceModeTextView.setText(activity.getResources().getString(R.string.usb));
            //vHolder.speakerVolumeSeekBar.setVisibility(View.VISIBLE);
        }

        /*if(item.getSoundDeviceData().getSpeakers() != null){
            vHolder.speakersLayout.removeAllViews();
            for (Speaker speaker : item.getSoundDeviceData().getSpeakers()) {
                TextView speakerNameTextView = new TextView(activity);
                speakerNameTextView.setText(speaker.getName());
                SeekBar seekBar = (SeekBar) activity.getLayoutInflater().inflate(R.layout.list_item_device_sound_system_controller_speaker, null);
                seekBar.setMax(100);
                seekBar.setProgress(speaker.getVolume());

                vHolder.speakersLayout.addView(speakerNameTextView);
                vHolder.speakersLayout.addView(seekBar);

                //TODO add onclick listeners for each speaker here
            }
        }*/

        vHolder.speakerVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    if(!MySettings.isControlActive()){
                        //controlDimming(item, 0, i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                MySettings.setControlState(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                controlDimming(item, 0, i); //control Volume
            }
        });

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
        if(item.getPIRData().getState() == Line.LINE_STATE_ON){
            vHolder.pirTypeImageView.setBackgroundResource(R.drawable.circle_indicator_green);
        }else if(item.getPIRData().getState() == Line.LINE_STATE_OFF){
            vHolder.pirTypeImageView.setBackgroundResource(R.drawable.circle_indicator_gray);
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
        MainActivity.getInstance().refreshDevicesListFromMemory();
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle(activity.getResources().getString(R.string.factory_reset_unit_question))
                //set message
                .setMessage(activity.getResources().getString(R.string.factory_reset_unit_message))
                //set positive button
                .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        FactoryResetter factoryResetter = new FactoryResetter(device);
                        factoryResetter.execute();
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

    private void toggleLine(Device device, int position, final int state){
        if(placeMode == Place.PLACE_MODE_LOCAL){
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
        }else{
            if(mqttAndroidClient == null) {
                DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
                if (fragment != null) {
                    mqttAndroidClient = fragment.getMqttAndroidClient();
                }
            }
            //send command usint MQTT
            if(mqttAndroidClient != null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        if(state == Line.LINE_STATE_ON){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_DIM", ":");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_DIM", ":");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_DIM", ":");
                                    break;
                            }
                        }else if(state == Line.LINE_STATE_OFF){
                            switch (position){
                                case 0:
                                    jsonObject.put("L_0_DIM", "0");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_DIM", "0");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_DIM", "0");
                                    break;
                            }
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                        if(state == Line.LINE_STATE_ON){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_STT", 1);
                                    break;
                                case 1:
                                    jsonObject.put("L_1_STT", 1);
                                    break;
                                case 2:
                                    jsonObject.put("L_2_STT", 1);
                                    break;
                            }
                        }else if(state == Line.LINE_STATE_OFF){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_STT", 0);
                                    break;
                                case 1:
                                    jsonObject.put("L_1_STT", 0);
                                    break;
                                case 2:
                                    jsonObject.put("L_2_STT", 0);
                                    break;
                            }
                        }
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Log.d(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                    Log.d(TAG, "MQTT publish data: " + mqttMessage);
                    mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (MqttException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }else{
                Log.d(TAG, "mqttAndroidClient is null");
            }
            MySettings.setControlState(false);
        }
    }

    private void toggleDimming(Device device, int position, int state){
        if(placeMode == Place.PLACE_MODE_LOCAL){
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
        }else{
            if(mqttAndroidClient == null) {
                DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
                if (fragment != null) {
                    mqttAndroidClient = fragment.getMqttAndroidClient();
                }
            }
            //send command usint MQTT
            if(mqttAndroidClient != null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    if(state == Line.DIMMING_STATE_ON){
                        switch(position){
                            case 0:
                                jsonObject.put("L_0_D_S", "1");
                                break;
                            case 1:
                                jsonObject.put("L_1_D_S", "1");
                                break;
                            case 2:
                                jsonObject.put("L_2_D_S", "1");
                                break;
                        }
                    }else if(state == Line.DIMMING_STATE_OFF){
                        switch (position){
                            case 0:
                                jsonObject.put("L_0_D_S", "0");
                                break;
                            case 1:
                                jsonObject.put("L_1_D_S", "0");
                                break;
                            case 2:
                                jsonObject.put("L_2_D_S", "0");
                                break;
                        }
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Log.d(TAG, "MQTT Publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                    Log.d(TAG, "MQTT Publish data: " + mqttMessage);
                    mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (MqttException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
            MySettings.setControlState(false);
        }
    }

    private void controlDimming(Device device, int position, int value){
        if(placeMode == Place.PLACE_MODE_LOCAL){
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
        }else{
            if(mqttAndroidClient == null) {
                DashboardDevicesFragment fragment = (DashboardDevicesFragment) fragmentManager.findFragmentByTag("dashboardDevicesFragment");
                if (fragment != null) {
                    mqttAndroidClient = fragment.getMqttAndroidClient();
                }
            }
            //send command usint MQTT
            if(mqttAndroidClient != null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    switch(position){
                        case 0:
                            if(value == 10){
                                jsonObject.put("L_0_DIM", ":");
                            }else{
                                jsonObject.put("L_0_DIM", ""+value);
                            }
                            break;
                        case 1:
                            if(value == 10){
                                jsonObject.put("L_1_DIM", ":");
                            }else{
                                jsonObject.put("L_1_DIM", ""+value);
                            }
                            break;
                        case 2:
                            if(value == 10){
                                jsonObject.put("L_2_DIM", ":");
                            }else{
                                jsonObject.put("L_2_DIM", ""+value);
                            }
                            break;
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Log.d(TAG, "MQTT Publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()));
                    Log.d(TAG, "MQTT Publish data: " + mqttMessage);
                    mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                }catch (JSONException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }catch (MqttException e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
            MySettings.setControlState(false);
        }
    }

    private void changeMode(Device device, final int mode){
        DevicePinger devicePinger = new DevicePinger(device, mode);
        devicePinger.execute();
    }

    public static class ViewHolder{
        TextView deviceNameTextView, deviceLocationTextView;
        TextView firstLineTextView, secondLineTextView, thirdLineTextView;
        CardView firstLineLayout, secondLineLayout, thirdLineLayout;
        SeekBar firstLineSeekBar, secondLineSeekBar, thirdLineSeekBar;
        ImageView firstLineAdvancedOptionsButton, secondLineAdvancedOptionsButton, thirdLineAdvancedOptionsButton;
        ImageView firstLineTypeImageView, secondLineTypeImageView, thirdLineTypeImageView;

        TextView soundDeviceNameTextView;
        CardView soundDeviceLayout;
        ImageView soundDeviceTypeImageView;
        ImageView soundDeviceAdvancedOptionsButton;
        RelativeLayout deviceModeLayout;
        ImageView deviceModeArrowImageView;
        TextView deviceModeTextView;
        SeekBar speakerVolumeSeekBar;//will be multiple ones, depending on number of speakers
        LinearLayout speakersLayout;

        TextView pirNameTextView;
        CardView pirLayout;
        ImageView pirTypeImageView;
        ImageView pirAdvancedOptionsButton;

        RelativeLayout scanningNetworkLayout;
        RelativeLayout lastSeenLayout;
        TextView lastSeenTextView;
        ImageView lastSeenImageView;

        RelativeLayout mqttReachabilityLayout;

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
            MySettings.setControlState(true);

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
            MySettings.setControlState(true);

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
            MySettings.setControlState(true);

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

            MySettings.setControlState(false);

            return null;
        }
    }

    public class DevicePinger extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.DevicePinger.class.getSimpleName();

        Device device;
        int mode;

        int statusCode;

        public DevicePinger(Device device, int mode) {
            this.device = device;
            this.mode = mode;
        }

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "Enabling getStatus flag...");
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200){
                ModeChanger modeChanger = new ModeChanger(device, mode);
                modeChanger.execute();
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.CONTROL_SOUND_DEVICE_CHANGE_MODE_URL);
                Log.d(TAG,  "devicePinger URL: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");

                JSONObject jObject = new JSONObject();
                jObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, "");
                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                Log.d(TAG,  "devicePinger POST data: " + jObject.toString());

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jObject.toString());
                outputStreamWriter.flush();

                statusCode = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }
                urlConnection.disconnect();
                Log.d(TAG,  "devicePinger response: " + result.toString());
            }catch (MalformedURLException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (IOException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }catch (JSONException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Log.d(TAG, "Disabling getStatus flag...");
                MySettings.setGetStatusState(false);
            }

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
            this.oldMode = device.getSoundDeviceData().getMode();
            device.getSoundDeviceData().setMode(mode);
            DevicesInMemory.updateDevice(device);
            if(mode == SoundDeviceData.MODE_USB){
                Utils.openApp(activity, "Hi-Fi Cast - Music Player", "com.findhdmusic.app.upnpcast");
            }
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
            if(statusCode != 200 && mode != SoundDeviceData.MODE_USB){
                Toast.makeText(activity, activity.getResources().getString(R.string.smart_controller_connection_error), Toast.LENGTH_SHORT).show();
            }else if(statusCode == 200 && mode == SoundDeviceData.MODE_UPNP){
                Utils.openApp(activity, "Hi-Fi Cast - Music Player", "com.findhdmusic.app.upnpcast");
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
            MySettings.setControlState(true);

            layoutEnabled = false;

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
                    }else if(mode == SoundDeviceData.MODE_LINE_IN_2){
                        jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN_2);
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

            return null;
        }
    }

    public class FactoryResetter extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.LineToggler.class.getSimpleName();

        Device device;
        int statusCode;

        public FactoryResetter(Device device) {
            this.device = device;
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
            if(statusCode == 200){
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.factory_reset_unit_successfull), Toast.LENGTH_SHORT).show();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    DeviceRebooterPost deviceRebooterPost = new DeviceRebooterPost(device);
                    deviceRebooterPost.execute();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    Toast.makeText(activity, activity.getResources().getString(R.string.factory_reset_unit_successfull), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(activity, activity.getResources().getString(R.string.factory_reset_unit_failed), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
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
            while(statusCode != 200 && numberOfAttempts <= Device.CONFIG_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "factoryReset attempt #"+numberOfAttempts);
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_FACTORY_RESET;

                    URL url = new URL(urlString);

                    Log.d(TAG,  "factoryReset URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(false);
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "factoryReset response: " + result.toString());
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttempts++;
                    delay = true;
                }
            }

            return null;
        }
    }

    public class DeviceRebooterPost extends AsyncTask<Void, Void, Void> {
        private final String TAG = DeviceAdapter.LineToggler.class.getSimpleName();

        Device device;
        int statusCode;

        public DeviceRebooterPost(Device device) {
            this.device = device;
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
            Toast.makeText(activity, activity.getResources().getString(R.string.factory_reset_unit_successfull), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
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
            while(statusCode != 200 && numberOfAttempts <= Device.CONFIG_NUMBER_OF_RETRIES){
                if(delay){
                    try {
                        Thread.sleep(Constants.DELAY_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                }
                try{
                    Log.d(TAG, "rebootDevice attempt #"+numberOfAttempts);
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_SOUND_SYSTEM_SHUTDOWN_URL;

                    URL url = new URL(urlString);

                    Log.d(TAG,  "rebootDevice URL: " + url);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setReadTimeout(Device.CONFIG_TIMEOUT);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.PARAMETER_SOUND_CONTROLLER_SHUTDOWN_MODE, Constants.PARAMETER_SOUND_CONTROLLER_OPTION_REBOOT);
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, ""+device.getAccessToken());

                    Log.d(TAG,  "rebootDevice POST data: " + jsonObject.toString());

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    Log.d(TAG,  "rebootDevice response: " + result.toString());
                }catch (MalformedURLException e){
                    Log.d(TAG, "Exception MalformedURLException: " + e.getMessage());
                }catch (IOException e){
                    Log.d(TAG, "Exception IOException: " + e.getMessage());
                }catch (Exception e){
                    Log.d(TAG, "Exception: " + e.getMessage());
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfAttempts++;
                    delay = true;
                }
            }

            return null;
        }
    }

    public void disconnectMQTT(){
        //stop MQTT
        if(mqttAndroidClient != null){
            try {
                if(mqttAndroidClient.isConnected()) {
                    mqttAndroidClient.disconnect();
                    mqttAndroidClient.unregisterResources();
                    mqttAndroidClient.close();
                }
            }catch (MqttException e){
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void getMqttClient(Context context, String brokerUrl, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        /*mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.d(TAG, "Connection lost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.d(TAG, "Message arrived: " + mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.d(TAG, "Delivery complete");
            }
        });*/
        if(mqttAndroidClient != null){
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    Log.d(TAG, "Connection complete on " + s);
                }
                @Override
                public void connectionLost(Throwable throwable) {
                    Log.d(TAG, "Connection lost");
                }
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    //setMessageNotification(s, new String(mqttMessage.getPayload()));
                    Log.d(TAG, "Message arrived: 's': " + s);
                    Log.d(TAG, "Message arrived: 'mqttMessage': " + new String(mqttMessage.getPayload()));
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Log.d(TAG, "Delivery complete");
                }
            });
            try {
                if(!mqttAndroidClient.isConnected()){
                    IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
                    if(token != null){
                        token.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                                Log.d(TAG, "Success");
                                try {
                                    for (Device device:devices) {
                                        subscribe(mqttAndroidClient, String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), 1);
                                    }
                                }catch (MqttException e){
                                    Log.d(TAG, "Exception " + e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d(TAG, "Failure " + exception.toString());
                            }
                        });
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void subscribe(@NonNull final MqttAndroidClient client,
                          @NonNull final String topic, int qos) throws MqttException {
        final IMqttToken token = client.subscribe(topic, qos);
        if(token != null) {
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "Subscribe Successfully on " + topic);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "Subscribe Failed on " + topic);
                }
            });
        }
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setWill(Constants.MQTT_URL, "I am going offline".getBytes(), 1, false);
        mqttConnectOptions.setUserName(Constants.MQTT_USERNAME);
        mqttConnectOptions.setPassword(Constants.MQTT_PASSWORD.toCharArray());
        return mqttConnectOptions;
    }
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }
}