package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.TimeUnit;

import java.util.ArrayList;
import java.util.List;

public class LinePIRConfigurationAdapter extends ArrayAdapter {
    Activity activity ;
    List<Line> lines;
    ViewHolder vHolder = null;

    public LinePIRConfigurationAdapter(Activity activity, List lines){
        super(activity, R.layout.list_item_line_pir_configuration, lines);
        this.activity = activity;
        this.lines = lines;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return lines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return lines.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_line_pir_configuration, null);
            vHolder = new ViewHolder();
            vHolder.lineNameTextView= rowView.findViewById(R.id.line_textvie);
            vHolder.lineLocationTextView = rowView.findViewById(R.id.line_location_textview);
            vHolder.lineControllerTextView = rowView.findViewById(R.id.line_controller_textview);
            vHolder.lineImageView = rowView.findViewById(R.id.line_type_imageview);
            vHolder.lineTriggerActionRadioGroup = rowView.findViewById(R.id.line_trigger_action_radiogroup);
            vHolder.lineTriggerOnRadioButton = rowView.findViewById(R.id.line_trigger_action_on_radiobutton);
            vHolder.lineTriggerOffRadioButton = rowView.findViewById(R.id.line_trigger_action_off_radiobutton);
            vHolder.dimmingValueSeekBar = rowView.findViewById(R.id.line_trigger_action_dimming_seekbar);
            vHolder.triggerActionDurationTimeUnitSpinner = rowView.findViewById(R.id.line_trigger_action_duration_spinner);
            vHolder.triggerActionDurationSeekBar = rowView.findViewById(R.id.line_trigger_action_duration_seekbar);
            vHolder.triggerActionDurationTextView = rowView.findViewById(R.id.line_trigger_action_duration_textview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Line item = lines.get(position);

        Device device = MySettings.getDeviceByID2(item.getDeviceID());
        Room room = MySettings.getRoom(device.getRoomID());
        Floor floor = MySettings.getFloor(room.getFloorID());

        vHolder.lineNameTextView.setText(""+item.getName());
        vHolder.lineControllerTextView.setText(""+device.getName());
        vHolder.lineLocationTextView.setText(""+floor.getPlaceName() + ":" + room.getName());
        if(lines.get(position).getType().getImageUrl() != null && lines.get(position).getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(lines.get(position).getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.line_type_fluorescent_lamp))
                    .into(vHolder.lineImageView);
        }else {
            if(lines.get(position).getType().getImageResourceName() != null && lines.get(position).getType().getImageResourceName().length() >= 1) {
                vHolder.lineImageView.setImageResource(activity.getResources().getIdentifier(lines.get(position).getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else{
                vHolder.lineImageView.setImageResource(lines.get(position).getType().getImageResourceID());
            }
        }

        vHolder.lineTriggerActionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.line_trigger_action_off_radiobutton:
                        item.setPirPowerState(Line.LINE_STATE_OFF);
                        item.setPirDimmingValue(0);
                        vHolder.dimmingValueSeekBar.setProgress(0);
                        vHolder.dimmingValueSeekBar.setEnabled(false);
                        break;
                    case R.id.line_trigger_action_on_radiobutton:
                        item.setPirPowerState(Line.LINE_STATE_ON);
                        item.setPirDimmingValue(10);
                        vHolder.dimmingValueSeekBar.setProgress(10);
                        vHolder.dimmingValueSeekBar.setEnabled(true);
                        break;
                }
            }
        });
        if(item.getPirPowerState() == Line.LINE_STATE_ON) {
            vHolder.lineTriggerOnRadioButton.setChecked(true);
            vHolder.lineTriggerOffRadioButton.setChecked(false);
            vHolder.dimmingValueSeekBar.setProgress(10);
            vHolder.dimmingValueSeekBar.setEnabled(true);
        }else{
            vHolder.lineTriggerOnRadioButton.setChecked(false);
            vHolder.lineTriggerOffRadioButton.setChecked(true);
            item.setPirDimmingValue(0);
            vHolder.dimmingValueSeekBar.setProgress(0);
            vHolder.dimmingValueSeekBar.setEnabled(false);
        }
        vHolder.dimmingValueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    if (progress != 0) {
                        item.setPirDimmingValue(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        List<TimeUnit> data = new ArrayList<>();
        data.addAll(Utils.getTimeUnits());
        ArrayAdapter<TimeUnit> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, data);
        vHolder.triggerActionDurationTimeUnitSpinner.setAdapter(adapter);

        TimeUnit secondsUnit = new TimeUnit(TimeUnit.UNIT_SECONDS, "Seconds");
        int index = data.indexOf(secondsUnit);
        vHolder.triggerActionDurationTimeUnitSpinner.setSelection(index);
        vHolder.triggerActionDurationSeekBar.setMax(60);
        vHolder.triggerActionDurationSeekBar.setProgress(30);
        item.setPirTriggerActionDuration(vHolder.triggerActionDurationSeekBar.getProgress());
        item.setPirTriggerActionDurationTimeUnit(TimeUnit.UNIT_SECONDS);
        vHolder.triggerActionDurationTextView.setText("" + vHolder.triggerActionDurationSeekBar.getProgress() + " seconds");

        vHolder.triggerActionDurationTimeUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeUnit selectedUnit = adapter.getItem(position);
                switch (selectedUnit.getID()){
                    case TimeUnit.UNIT_SECONDS:
                        vHolder.triggerActionDurationSeekBar.setEnabled(true);
                        vHolder.triggerActionDurationSeekBar.setProgress(30);
                        vHolder.triggerActionDurationSeekBar.setMax(60);
                        item.setPirTriggerActionDurationTimeUnit(selectedUnit.getID());
                        item.setPirTriggerActionDuration(vHolder.triggerActionDurationSeekBar.getProgress());
                        vHolder.triggerActionDurationTextView.setText("" + vHolder.triggerActionDurationSeekBar.getProgress() + " second(s)");
                        break;
                    case TimeUnit.UNIT_MINUTES:
                        vHolder.triggerActionDurationSeekBar.setEnabled(true);
                        vHolder.triggerActionDurationSeekBar.setProgress(30);
                        vHolder.triggerActionDurationSeekBar.setMax(60);
                        item.setPirTriggerActionDurationTimeUnit(selectedUnit.getID());
                        item.setPirTriggerActionDuration(vHolder.triggerActionDurationSeekBar.getProgress());
                        vHolder.triggerActionDurationTextView.setText("" + vHolder.triggerActionDurationSeekBar.getProgress() + " minutes(s)");
                        break;
                    case TimeUnit.UNIT_HOURS:
                        vHolder.triggerActionDurationSeekBar.setEnabled(true);
                        vHolder.triggerActionDurationSeekBar.setProgress(1);
                        vHolder.triggerActionDurationSeekBar.setMax(24);
                        item.setPirTriggerActionDurationTimeUnit(selectedUnit.getID());
                        item.setPirTriggerActionDuration(vHolder.triggerActionDurationSeekBar.getProgress());
                        vHolder.triggerActionDurationTextView.setText("" + vHolder.triggerActionDurationSeekBar.getProgress() + " hours(s)");
                        break;
                    case TimeUnit.UNIT_DAYS:
                        vHolder.triggerActionDurationSeekBar.setEnabled(true);
                        vHolder.triggerActionDurationSeekBar.setProgress(1);
                        vHolder.triggerActionDurationSeekBar.setMax(30);
                        item.setPirTriggerActionDurationTimeUnit(selectedUnit.getID());
                        item.setPirTriggerActionDuration(vHolder.triggerActionDurationSeekBar.getProgress());
                        vHolder.triggerActionDurationTextView.setText("" + vHolder.triggerActionDurationSeekBar.getProgress() + " day(s)");
                        break;
                    case TimeUnit.UNIT_INDEFINITE:
                        vHolder.triggerActionDurationSeekBar.setEnabled(false);
                        vHolder.triggerActionDurationSeekBar.setProgress(0);
                        vHolder.triggerActionDurationSeekBar.setMax(0);
                        item.setPirTriggerActionDurationTimeUnit(selectedUnit.getID());
                        item.setPirTriggerActionDuration(0);
                        vHolder.triggerActionDurationTextView.setText( "No time limit");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        vHolder.triggerActionDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress != 0){
                    item.setPirTriggerActionDuration(progress);
                    switch (item.getPirTriggerActionDurationTimeUnit()){
                        case TimeUnit.UNIT_SECONDS:
                            vHolder.triggerActionDurationTextView.setText("" + progress + " second(s)");
                            break;
                        case TimeUnit.UNIT_MINUTES:
                            vHolder.triggerActionDurationTextView.setText("" + progress + " minute(s)");
                            break;
                        case TimeUnit.UNIT_HOURS:
                            vHolder.triggerActionDurationTextView.setText("" + progress + " hours(s)");
                            break;
                        case TimeUnit.UNIT_DAYS:
                            vHolder.triggerActionDurationTextView.setText("" + progress + " day(s)");
                            break;
                        case TimeUnit.UNIT_INDEFINITE:
                            vHolder.triggerActionDurationTextView.setText( "No time limit");
                            break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return rowView;
    }

    public static class ViewHolder{
        TextView lineNameTextView, lineControllerTextView,lineLocationTextView;
        ImageView lineImageView;
        RadioGroup lineTriggerActionRadioGroup;
        RadioButton lineTriggerOnRadioButton, lineTriggerOffRadioButton;
        SeekBar dimmingValueSeekBar, triggerActionDurationSeekBar;
        Spinner triggerActionDurationTimeUnitSpinner;
        TextView triggerActionDurationTextView;
    }
}
