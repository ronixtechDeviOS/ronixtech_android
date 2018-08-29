package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DeviceAdapter extends BaseSwipeAdapter {
    private static final String TAG = DeviceAdapter.class.getSimpleName();

    Activity activity ;
    List<Device> devices;
    ViewHolder vHolder = null;
    SwipeLayout swipeLayout;

    public DeviceAdapter(Activity activity, List devices){
        //super(activity, R.layout.list_item_device, devices);
        this.activity = activity;
        this.devices = devices;
        mHandler = new android.os.Handler();
    }

    private void setLayoutEnabled(boolean enabled){
        vHolder.firstLineSwitch.setEnabled(enabled);
        vHolder.firstLineDimmingCheckBox.setEnabled(enabled);
        if(vHolder.firstLineSeekBar.isEnabled()) {
            vHolder.firstLineSeekBar.setEnabled(enabled);
        }

        vHolder.secondLineSwitch.setEnabled(enabled);
        vHolder.secondLineDimmingCheckBox.setEnabled(enabled);
        if(vHolder.secondLineSeekBar.isEnabled()) {
            vHolder.secondLineSeekBar.setEnabled(enabled);
        }

        vHolder.thirdLineSwitch.setEnabled(enabled);
        vHolder.thirdLineDimmingCheckBox.setEnabled(enabled);
        if(vHolder.thirdLineSeekBar.isEnabled()) {
            vHolder.thirdLineSeekBar.setEnabled(enabled);
        }
    }

    private void setLayoutEnabledDelayed(boolean enabled){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setLayoutEnabled(enabled);
            }
        }, 500);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
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
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public void fillValues(int position, View convertView){
        Device item = (Device) devices.get(position);

        //vHolder = new ViewHolder();
        vHolder.deviceNameTextView = convertView.findViewById(R.id.device_name_textview);
        vHolder.deviceLocationTextView = convertView.findViewById(R.id.device_location_textview);
        vHolder.firstLineLayout = convertView.findViewById(R.id.first_line_layout);
        vHolder.secondLineLayout = convertView.findViewById(R.id.second_line_layout);
        vHolder.thirdLineLayout = convertView.findViewById(R.id.third_line_layout);
        vHolder.firstLineTextView = convertView.findViewById(R.id.first_line_textvie);
        vHolder.secondLineTextView = convertView.findViewById(R.id.second_line_textview);
        vHolder.thirdLineTextView = convertView.findViewById(R.id.third_line_textview);
        vHolder.removeDeviceLayout = convertView.findViewById(R.id.remove_device_layout);
        vHolder.removeDeviceImageView = convertView.findViewById(R.id.remove_device_imageview);
        vHolder.firstLineSeekBar = convertView.findViewById(R.id.first_line_seekbar);
        vHolder.secondLineSeekBar = convertView.findViewById(R.id.second_line_seekbar);
        vHolder.thirdLineSeekBar = convertView.findViewById(R.id.third_line_seekbar);
        vHolder.firstLineDimmingCheckBox = convertView.findViewById(R.id.first_line_dimming_checkbox);
        vHolder.secondLineDimmingCheckBox = convertView.findViewById(R.id.second_line_dimming_checkbox);
        vHolder.thirdLineDimmingCheckBox = convertView.findViewById(R.id.third_line_dimming_checkbox);
        vHolder.firstLineSwitch = convertView.findViewById(R.id.first_line_switch);
        vHolder.secondLineSwitch = convertView.findViewById(R.id.second_line_switch);
        vHolder.thirdLineSwitch = convertView.findViewById(R.id.third_line_switch);

        vHolder.firstLineSeekBar.setMax(10);
        vHolder.secondLineSeekBar.setMax(10);
        vHolder.thirdLineSeekBar.setMax(10);

        vHolder.deviceNameTextView.setText(""+item.getName()/* + " (" + item.getLines().size() + " lines)"*/);
        vHolder.deviceLocationTextView.setText(""+MySettings.getRoom(item.getRoomID()).getName());

        if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old){
            vHolder.firstLineLayout.setVisibility(View.VISIBLE);
            vHolder.secondLineLayout.setVisibility(View.GONE);
            vHolder.thirdLineLayout.setVisibility(View.GONE);

            populateLineData(item);
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old){
            vHolder.firstLineLayout.setVisibility(View.VISIBLE);
            vHolder.secondLineLayout.setVisibility(View.VISIBLE);
            vHolder.thirdLineLayout.setVisibility(View.GONE);

            populateLineData(item);
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
            vHolder.firstLineLayout.setVisibility(View.VISIBLE);
            vHolder.secondLineLayout.setVisibility(View.VISIBLE);
            vHolder.thirdLineLayout.setVisibility(View.VISIBLE);

            populateLineData(item);
        }

        if(item.getIpAddress() == null || item.getIpAddress().length() <= 1){
            vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
            vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
            vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));

            vHolder.firstLineSeekBar.setEnabled(false);
            vHolder.secondLineSeekBar.setEnabled(false);
            vHolder.thirdLineSeekBar.setEnabled(false);
            vHolder.firstLineDimmingCheckBox.setEnabled(false);
            vHolder.secondLineDimmingCheckBox.setEnabled(false);
            vHolder.thirdLineDimmingCheckBox.setEnabled(false);
            vHolder.firstLineSwitch.setEnabled(false);
            vHolder.secondLineSwitch.setEnabled(false);
            vHolder.thirdLineSwitch.setEnabled(false);
        }else{
            vHolder.deviceNameTextView.setPaintFlags(vHolder.deviceNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
            vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
            vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));

            vHolder.firstLineSeekBar.setEnabled(true);
            vHolder.secondLineSeekBar.setEnabled(true);
            vHolder.thirdLineSeekBar.setEnabled(true);
            vHolder.firstLineDimmingCheckBox.setEnabled(true);
            vHolder.secondLineDimmingCheckBox.setEnabled(true);
            vHolder.thirdLineDimmingCheckBox.setEnabled(true);
            vHolder.firstLineSwitch.setEnabled(true);
            vHolder.secondLineSwitch.setEnabled(true);
            vHolder.thirdLineSwitch.setEnabled(true);

            populateLineData(item);

            vHolder.firstLineSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((ToggleButton)view).isChecked();
                    MySettings.setControlState(true);
                    if(checked){
                        //turn on this line
                        toggleLine(item,0, Line.LINE_STATE_ON);
                    }else{
                        //turn off this line
                        toggleLine(item, 0, Line.LINE_STATE_OFF);
                    }
                }
            });
            vHolder.secondLineSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((ToggleButton)view).isChecked();
                    MySettings.setControlState(true);
                    if(checked){
                        //turn on this line
                        toggleLine(item,1, Line.LINE_STATE_ON);
                    }else{
                        //turn off this line
                        toggleLine(item, 1, Line.LINE_STATE_OFF);
                    }
                }

            });
            vHolder.thirdLineSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MySettings.setControlState(true);
                    boolean checked = ((ToggleButton)view).isChecked();
                    if(checked){
                        //turn on this line
                        toggleLine(item,2, Line.LINE_STATE_ON);
                    }else{
                        //turn off this line
                        toggleLine(item, 2, Line.LINE_STATE_OFF);
                    }
                }
            });

            /*vHolder.firstLineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                        //turn on this line
                        toggleLine(item,0, Line.LINE_STATE_ON);
                    }else if(item.getLines().get(0).getPowerState() == Line.LINE_STATE_ON){
                        //turn off this line
                        toggleLine(item, 0, Line.LINE_STATE_OFF);
                    }
                }
            });
            vHolder.secondLineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF){
                        //turn on this line
                        toggleLine(item,1, Line.LINE_STATE_ON);
                    }else if(item.getLines().get(1).getPowerState() == Line.LINE_STATE_ON){
                        //turn off this line
                        toggleLine(item,1, Line.LINE_STATE_OFF);
                    }
                }
            });
            vHolder.thirdLineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.getLines().get(2).getPowerState() == Line.LINE_STATE_OFF){
                        //turn on this line
                        toggleLine(item,2, Line.LINE_STATE_ON);
                    }else if(item.getLines().get(2).getPowerState() == Line.LINE_STATE_ON){
                        //turn off this line
                        toggleLine(item,2, Line.LINE_STATE_OFF);
                    }
                }
            });*/

            vHolder.firstLineDimmingCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MySettings.setControlState(true);
                    boolean checked = ((CheckBox)view).isChecked();
                    if(checked){
                        toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                    }
                }
            });
            vHolder.secondLineDimmingCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MySettings.setControlState(true);
                    boolean checked = ((CheckBox)view).isChecked();
                    if(checked){
                        toggleDimming(item, 01, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 1, Line.DIMMING_STATE_OFF);
                    }
                }
            });
            vHolder.thirdLineDimmingCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MySettings.setControlState(true);
                    boolean checked = ((CheckBox)view).isChecked();
                    if(checked){
                        toggleDimming(item, 2, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 2, Line.DIMMING_STATE_OFF);
                    }
                }
            });

            /*vHolder.firstLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        toggleDimming(item, 0, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 0, Line.DIMMING_STATE_OFF);
                    }
                }
            });
            vHolder.secondLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        toggleDimming(item, 1, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 1, Line.DIMMING_STATE_OFF);
                    }
                }
            });
            vHolder.thirdLineDimmingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        toggleDimming(item, 2, Line.DIMMING_STATE_ON);
                    }else{
                        toggleDimming(item, 2, Line.DIMMING_STATE_OFF);
                    }
                }
            });*/

            vHolder.firstLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b) {
                        MySettings.setControlState(true);
                        controlDimming(item, 0, i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    MySettings.setControlState(true);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                    MySettings.setControlState(false);
                    int i = vHolder.firstLineSeekBar.getProgress();
                    //controlDimming(item, 0, i);
                }
            });
            vHolder.secondLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b) {
                        MySettings.setControlState(true);
                        controlDimming(item, 1, i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    MySettings.setControlState(true);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                    MySettings.setControlState(false);
                    int i = vHolder.secondLineSeekBar.getProgress();
                    //controlDimming(item, 1, i);
                }
            });
            vHolder.thirdLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b) {
                        MySettings.setControlState(true);
                        controlDimming(item, 2, i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    MySettings.setControlState(true);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                    }
                    MySettings.setControlState(false);
                    int i = vHolder.thirdLineSeekBar.getProgress();
                    //controlDimming(item, 2, i);
                }
            });
        }

        vHolder.removeDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDevice(item);
            }
        });
        vHolder.removeDeviceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDevice(item);
            }
        });
    }

    private void populateLineData(Device item){
        for (Line line : item.getLines()) {
            if(line.getPosition() == 0){
                vHolder.firstLineTextView.setText(line.getName());
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                    vHolder.firstLineSwitch.setChecked(true);
                    if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                        vHolder.firstLineDimmingCheckBox.setChecked(true);
                        vHolder.firstLineDimmingCheckBox.setEnabled(true);
                        vHolder.firstLineSeekBar.setEnabled(true);
                        vHolder.firstLineSeekBar.setProgress(line.getDimmingVvalue());
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                        vHolder.firstLineDimmingCheckBox.setChecked(false);
                        vHolder.firstLineDimmingCheckBox.setEnabled(true);
                        vHolder.firstLineSeekBar.setEnabled(false);
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
                }else if(line.getPowerState() == Line.LINE_STATE_PROCESSING){
                    //vHolder.firstLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                }
            }else if(line.getPosition() == 1){
                vHolder.secondLineTextView.setText(line.getName());
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    //vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                    vHolder.secondLineSwitch.setChecked(true);
                    if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                        vHolder.secondLineDimmingCheckBox.setChecked(true);
                        vHolder.secondLineDimmingCheckBox.setEnabled(true);
                        vHolder.secondLineSeekBar.setEnabled(true);
                        vHolder.secondLineSeekBar.setProgress(line.getDimmingVvalue());
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                        vHolder.secondLineDimmingCheckBox.setChecked(false);
                        vHolder.secondLineDimmingCheckBox.setEnabled(true);
                        vHolder.secondLineSeekBar.setEnabled(false);
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_PROCESSING){
                        vHolder.secondLineDimmingCheckBox.setEnabled(false);
                        vHolder.secondLineSeekBar.setEnabled(false);
                    }
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    //vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightGrayColor));
                    vHolder.secondLineSwitch.setChecked(false);
                    vHolder.secondLineDimmingCheckBox.setEnabled(false);
                    vHolder.secondLineSeekBar.setEnabled(false);
                    vHolder.secondLineDimmingCheckBox.setChecked(false);
                }else if(line.getPowerState() == Line.LINE_STATE_PROCESSING){
                    //vHolder.secondLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                }
            }else if(line.getPosition() == 2){
                vHolder.thirdLineTextView.setText(line.getName());
                if(line.getPowerState() == Line.LINE_STATE_ON){
                    //vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.whiteColor));
                    vHolder.thirdLineSwitch.setChecked(true);
                    if(line.getDimmingState() == Line.DIMMING_STATE_ON){
                        vHolder.thirdLineDimmingCheckBox.setChecked(true);
                        vHolder.thirdLineDimmingCheckBox.setEnabled(true);
                        vHolder.thirdLineSeekBar.setEnabled(true);
                        vHolder.thirdLineSeekBar.setProgress(line.getDimmingVvalue());
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_OFF){
                        vHolder.thirdLineDimmingCheckBox.setChecked(false);
                        vHolder.thirdLineDimmingCheckBox.setEnabled(true);
                        vHolder.thirdLineSeekBar.setEnabled(false);
                    }else if(line.getDimmingState() == Line.DIMMING_STATE_PROCESSING){
                        vHolder.thirdLineDimmingCheckBox.setEnabled(false);
                        vHolder.thirdLineSeekBar.setEnabled(false);
                    }
                }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                    //vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightGrayColor));
                    vHolder.thirdLineDimmingCheckBox.setEnabled(false);
                    vHolder.thirdLineSeekBar.setEnabled(false);
                    vHolder.thirdLineSwitch.setChecked(false);
                    vHolder.thirdLineDimmingCheckBox.setChecked(false);
                }else if(line.getPowerState() == Line.LINE_STATE_PROCESSING){
                    //vHolder.thirdLineLayout.setBackgroundColor(activity.getResources().getColor(R.color.lightestGrayColor));
                }
            }
        }
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(activity).inflate(R.layout.list_item_device, null);

        vHolder = new ViewHolder();
        vHolder.deviceNameTextView = v.findViewById(R.id.device_name_textview);
        vHolder.deviceLocationTextView = v.findViewById(R.id.device_location_textview);
        vHolder.firstLineLayout = v.findViewById(R.id.first_line_layout);
        vHolder.secondLineLayout = v.findViewById(R.id.second_line_layout);
        vHolder.thirdLineLayout = v.findViewById(R.id.third_line_layout);
        vHolder.firstLineTextView = v.findViewById(R.id.first_line_textvie);
        vHolder.secondLineTextView = v.findViewById(R.id.second_line_textview);
        vHolder.thirdLineTextView = v.findViewById(R.id.third_line_textview);
        vHolder.removeDeviceLayout = v.findViewById(R.id.remove_device_layout);
        vHolder.removeDeviceImageView = v.findViewById(R.id.remove_device_imageview);
        vHolder.firstLineSeekBar = v.findViewById(R.id.first_line_seekbar);
        vHolder.secondLineSeekBar = v.findViewById(R.id.second_line_seekbar);
        vHolder.thirdLineSeekBar = v.findViewById(R.id.third_line_seekbar);
        vHolder.firstLineDimmingCheckBox = v.findViewById(R.id.first_line_dimming_checkbox);
        vHolder.secondLineDimmingCheckBox = v.findViewById(R.id.second_line_dimming_checkbox);
        vHolder.thirdLineDimmingCheckBox = v.findViewById(R.id.third_line_dimming_checkbox);
        vHolder.firstLineSwitch = v.findViewById(R.id.first_line_switch);
        vHolder.secondLineSwitch = v.findViewById(R.id.second_line_switch);
        vHolder.thirdLineSwitch = v.findViewById(R.id.third_line_switch);


        /*if(v == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            v = inflater.inflate(R.layout.list_item_device, null);
            vHolder = new ViewHolder();
            vHolder.deviceNameTextView = v.findViewById(R.id.device_name_textview);
            vHolder.deviceLocationTextView = v.findViewById(R.id.device_location_textview);
            vHolder.firstLineButton = v.findViewById(R.id.first_line_button);
            vHolder.secondLineButton = v.findViewById(R.id.second_line_button);
            vHolder.thirdLineButton = v.findViewById(R.id.third_line_button);
            v.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) v.getTag();
        }*/

        return v;
    }

    /*@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_device, null);
            vHolder = new ViewHolder();
            vHolder.deviceNameTextView = rowView.findViewById(R.id.device_name_textview);
            vHolder.deviceLocationTextView = rowView.findViewById(R.id.device_location_textview);
            vHolder.firstLineButton = rowView.findViewById(R.id.first_line_button);
            vHolder.secondLineButton = rowView.findViewById(R.id.second_line_button);
            vHolder.thirdLineButton = rowView.findViewById(R.id.third_line_button);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Device item = (Device) devices.get(position);

        vHolder.deviceNameTextView.setText(""+item.getName() + " (" + item.getLines().size() + " lines)");
        vHolder.deviceLocationTextView.setText("Room ID: "+item.getRoomID());

        if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line){
            vHolder.firstLineButton.setVisibility(View.VISIBLE);
            vHolder.secondLineButton.setVisibility(View.INVISIBLE);
            vHolder.thirdLineButton.setVisibility(View.INVISIBLE);
            for (Line line : item.getLines()) {
                if(line.getPosition() == 0){
                    vHolder.firstLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 1){
                    vHolder.secondLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 2){
                    vHolder.thirdLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }
            }
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines){
            vHolder.firstLineButton.setVisibility(View.VISIBLE);
            vHolder.secondLineButton.setVisibility(View.VISIBLE);
            vHolder.thirdLineButton.setVisibility(View.INVISIBLE);
            for (Line line : item.getLines()) {
                if(line.getPosition() == 0){
                    vHolder.firstLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 1){
                    vHolder.secondLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 2){
                    vHolder.thirdLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }
            }
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines){
            vHolder.firstLineButton.setVisibility(View.VISIBLE);
            vHolder.secondLineButton.setVisibility(View.VISIBLE);
            vHolder.thirdLineButton.setVisibility(View.VISIBLE);
            for (Line line : item.getLines()) {
                if(line.getPosition() == 0){
                    vHolder.firstLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.firstLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 1){
                    vHolder.secondLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.secondLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }else if(line.getPosition() == 2){
                    vHolder.thirdLineButton.setText(line.getName());
                    if(line.getPowerState() == Line.LINE_STATE_ON){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                    }else if(line.getPowerState() == Line.LINE_STATE_OFF){
                        vHolder.thirdLineButton.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                    }
                }
            }
        }

        vHolder.firstLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                    //turn on this line
                    toggleLine(item,0, Line.LINE_STATE_ON);
                }else if(item.getLines().get(0).getPowerState() == Line.LINE_STATE_ON){
                    //turn off this line
                    toggleLine(item, 0, Line.LINE_STATE_OFF);
                }
            }
        });
        vHolder.secondLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF){
                    //turn on this line
                    toggleLine(item,1, Line.LINE_STATE_ON);
                }else if(item.getLines().get(1).getPowerState() == Line.LINE_STATE_ON){
                    //turn off this line
                    toggleLine(item,1, Line.LINE_STATE_OFF);
                }
            }
        });
        vHolder.thirdLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getLines().get(2).getPowerState() == Line.LINE_STATE_OFF){
                    //turn on this line
                    toggleLine(item,2, Line.LINE_STATE_ON);
                }else if(item.getLines().get(2).getPowerState() == Line.LINE_STATE_ON){
                    //turn off this line
                    toggleLine(item,2, Line.LINE_STATE_OFF);
                }
            }
        });

        return rowView;
    }*/

    private void removeDevice(Device device){
        devices.remove(device);
        MySettings.removeDevice(device);
        notifyDataSetChanged();
    }

    private void toggleLine(Device device, int position, final int state){
        LineToggler lineToggler = new LineToggler(device, position, state);
        lineToggler.execute();
        /*String url = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
        *//*if(position == 0){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + "0");
        }else if(position == 1){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + "1");
        }else if(position == 2){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + "2");
        }*//*

        setLayoutEnabled(false);
        setLayoutEnabledDelayed(true);

        if(position == 0){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
        }else if(position == 1){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
        }else if(position == 2){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
        }

        if(state == Line.LINE_STATE_ON){
            url = url.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
        }else if(state == Line.LINE_STATE_OFF){
            url = url.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + "0");
        }

        List<Line> lines = device.getLines();
        Line line = lines.get(position);
        final int oldState = line.getPowerState();
        *//*line.setPowerState(Line.LINE_STATE_PROCESSING);
        MySettings.updateLineState(line, Line.LINE_STATE_PROCESSING);
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().updateDevicesList();
        }*//*
        line.setPowerState(state);
        lines.remove(line);
        lines.add(position, line);
        device.setLines(lines);
        DevicesInMemory.updateDevice(device);
        //MySettings.updateLineState(line, state);
        *//*if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().updateDevicesList();
        }*//*

        Log.d(TAG,  "toggleLine URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "toggleLine response: " + response);
                MySettings.setControlState(false);
                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");
                *//*line.setPowerState(state);
                MySettings.updateLineState(line, state);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }*//*

                //notifyDataSetChanged();
                *//*try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){

                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }*//*
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(activity != null) {
                    //Toast.makeText(activity, activity.getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                MySettings.setControlState(false);
                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");

                line.setPowerState(oldState);
                lines.remove(line);
                lines.add(position, line);
                device.setLines(lines);
                DevicesInMemory.updateDevice(device);
                //MySettings.updateLineState(line, oldState);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }
                //MySettings.scanNetwork();
                //device.setIpAddress("");
                //MySettings.updateDeviceIP(device, "");
            }
        });
        request.setTag("controlRequest");
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONTROL_TIMEOUT, Device.CONTROL_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(activity).addToRequestQueue(request);*/
    }/**/

    private void toggleDimming(Device device, int position, int state){
        DimmingToggler dimmingToggler = new DimmingToggler(device, position, state);
        dimmingToggler.execute();
        /*String url = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
        if(position == 0){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_STATE);
        }else if(position == 1){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_STATE);
        }else if(position == 2){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_STATE);
        }

        setLayoutEnabled(false);
        setLayoutEnabledDelayed(true);

        List<Line> lines = device.getLines();
        Line line = lines.get(position);
        int oldState = line.getDimmingState();
        *//*line.setDimmingState(Line.DIMMING_STATE_PROCESSING);
        MySettings.updateLineDimmingState(line, Line.DIMMING_STATE_PROCESSING);
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().updateDevicesList();
        }*//*

        line.setDimmingState(state);

        lines.remove(line);
        lines.add(position, line);
        device.setLines(lines);
        DevicesInMemory.updateDevice(device);
        //MySettings.updateLineDimmingState(line, state);
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().updateDevicesList();
        }

        url = url.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + state);

        Log.d(TAG,  "toggleLineDimming URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "toggleLineDimming response: " + response);
                MySettings.setControlState(false);

                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");

                *//*line.setDimmingState(state);
                MySettings.updateLineDimmingState(line, state);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }*//*
                //notifyDataSetChanged();
                *//*try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){

                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }*//*
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(activity != null) {
                    //Toast.makeText(activity, activity.getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                MySettings.setControlState(false);
                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");

                line.setDimmingState(oldState);
                lines.remove(line);
                lines.add(position, line);
                device.setLines(lines);
                DevicesInMemory.updateDevice(device);
                //MySettings.updateLineDimmingState(line, oldState);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }
                //MySettings.scanNetwork();
                //device.setIpAddress("");
                //MySettings.updateDeviceIP(device, "");
            }
        });
        request.setTag("controlRequest");
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONTROL_TIMEOUT, Device.CONTROL_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(activity).addToRequestQueue(request);*/
    }

    private void controlDimming(Device device, int position, int value){
        DimmingController dimmingController = new DimmingController(device, position, value);
        dimmingController.execute();
        /*String url = "http://" + device.getIpAddress() + Constants.CONTROL_DEVICE_URL;
        if(position == 0){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE);
        }else if(position == 1){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE);
        }else if(position == 2){
            url = url.concat("?" + Constants.PARAMETER_COMMAND_ZERO + "=" + Constants.PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE);
        }

        if(value == 10){
            url = url.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + ":");
        }else{
            url = url.concat("&" + Constants.PARAMETER_COMMAND_ONE + "=" + value);
        }

        List<Line> lines = device.getLines();
        Line line = lines.get(position);
        int oldValue = line.getDimmingVvalue();

        line.setDimmingVvalue(value);

        lines.remove(line);
        lines.add(position, line);
        device.setLines(lines);
        DevicesInMemory.updateDevice(device);

        //MySettings.updateLineDimmingValue(line, value);
        *//*if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().updateDevicesList();
        }*//*

        Log.d(TAG,  "controlLineDimming URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "controlLineDimming response: " + response);

                MySettings.setControlState(false);
                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");

                *//*line.setDimmingVvalue(value);
                MySettings.updateLineDimmingValue(line, value);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }*//*
                *//*Line line = device.getLines().get(position);
                line.setDimmingVvalue(value);
                MySettings.updateLineDimmingValue(line, value);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }*//*
                //notifyDataSetChanged();
                *//*try{
                    JSONObject jsonObject= new JSONObject(response);
                    if(jsonObject != null && jsonObject.has(Constants.PARAMETER_DEVICE_TYPE_ID)){

                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }*//*
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                if(activity != null) {
                    //Toast.makeText(activity, activity.getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
                }
                MySettings.setControlState(false);
                HttpConnector.getInstance(activity).getRequestQueue().cancelAll("controlRequest");

                line.setDimmingVvalue(oldValue);

                lines.remove(line);
                lines.add(position, line);
                device.setLines(lines);
                DevicesInMemory.updateDevice(device);

                //MySettings.updateLineDimmingValue(line, oldValue);
                if(MainActivity.getInstance() != null) {
                    MainActivity.getInstance().updateDevicesList();
                }
                //MySettings.scanNetwork();
                //device.setIpAddress("");
                //MySettings.updateDeviceIP(device, "");
            }
        });
        request.setTag("controlRequest");
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.CONTROL_TIMEOUT, Device.CONTROL_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(activity).addToRequestQueue(request);*/
    }

    public static class ViewHolder{
        TextView deviceNameTextView, deviceLocationTextView;
        TextView firstLineTextView, secondLineTextView, thirdLineTextView;
        RelativeLayout removeDeviceLayout;
        CardView firstLineLayout, secondLineLayout, thirdLineLayout;
        ImageView removeDeviceImageView;
        SeekBar firstLineSeekBar, secondLineSeekBar, thirdLineSeekBar;
        CheckBox firstLineDimmingCheckBox, secondLineDimmingCheckBox, thirdLineDimmingCheckBox;
        ToggleButton firstLineSwitch, secondLineSwitch, thirdLineSwitch;
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

        public LineToggler(Device device, int position, int state) {
            this.device = device;
            this.position = position;
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            setLayoutEnabled(false);
            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldState = line.getPowerState();

            line.setPowerState(state);
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            /*if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
            }*/
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineState(line, oldState);
            if(MainActivity.getInstance() != null){
                MainActivity.getInstance().updateDevicesList();
            }
            MySettings.setControlState(false);
            setLayoutEnabled(true);
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            int statusCode = 0;
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
            }

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

        public DimmingToggler(Device device, int position, int state) {
            this.device = device;
            this.position = position;
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            setLayoutEnabled(false);
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
                MainActivity.getInstance().updateDevicesList();
            }
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
            //MySettings.updateLineDimmingState(line, oldState);
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().updateDevicesList();
            }

            MySettings.setControlState(false);

            setLayoutEnabled(true);
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            int statusCode = 0;
            try{
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
            }

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

        public DimmingController(Device device, int position, int value) {
            this.device = device;
            this.position = position;
            this.value = value;
        }

        @Override
        protected void onPreExecute(){
            //setLayoutEnabled(false);
            //setLayoutEnabledDelayed(true);

            lines = device.getLines();
            line = lines.get(position);
            oldValue = line.getDimmingVvalue();

            line.setDimmingVvalue(value);

            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            lines.remove(line);
            lines.add(position, line);
            device.setLines(lines);
            DevicesInMemory.updateDevice(device);

            //MySettings.updateLineDimmingValue(line, oldValue);
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().updateDevicesList();
            }

            //MySettings.setControlState(false);
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            HttpURLConnection urlConnection = null;
            int statusCode = 0;
            try{
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
            }

            return null;
        }
    }
}
