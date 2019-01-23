package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

public class RoomsDashboardLinesGridAdapter extends BaseAdapter {
    Activity activity;
    List<Line> lines;
    ViewHolder vHolder = null;

    public interface RoomsListener{
        public void onRoomDeleted();
        public void onRoomNameChanged();
    }

    public RoomsDashboardLinesGridAdapter(Activity activity, List<Line> lines) {
        this.activity = activity;
        this.lines = lines;
    }

    @Override
    public int getCount() {
        return lines.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return lines.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_room_dashboard_line, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.line_name_textview);
            vHolder.typeImageView = rowView.findViewById(R.id.line_type_imageview);
            vHolder.lineLayout = rowView.findViewById(R.id.line_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Line item = lines.get(position);
        vHolder.nameTextView.setText("" + item.getName());
        vHolder.nameTextView.setSelected(true);

        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
                Device device = DevicesInMemory.getDeviceByID(item.getDeviceID());
                if(device != null && device.getIpAddress() != null && device.getIpAddress().length() >= 1){
                    if(item.getPowerState() == Line.LINE_STATE_ON){
                        //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                        vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.blueColor));

                    }else if(item.getPowerState() == Line.LINE_STATE_OFF){
                        //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                        vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.lightGrayColor));
                    }
                }else{
                    vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.redColor));
                }
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                Device device = MySettings.getDeviceByID2(item.getDeviceID());
                if(device != null && device.isDeviceMQTTReachable()){
                    if(item.getPowerState() == Line.LINE_STATE_ON){
                        //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                        vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.blueColor));

                    }else if(item.getPowerState() == Line.LINE_STATE_OFF){
                        //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                        vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.lightGrayColor));
                    }
                }else{
                    vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.redColor));
                }
            }
        }else{
            if(item.getPowerState() == Line.LINE_STATE_ON){
                //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.greenColor));
                vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.blueColor));

            }else if(item.getPowerState() == Line.LINE_STATE_OFF){
                //lineTypeImageView.setBackgroundColor(activity.getResources().getColor(R.color.redColor));
                vHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.lightGrayColor));
            }
        }


        if(item.getType().getImageUrl() != null && item.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(item.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.line_type_led__lamp))
                    .into(vHolder.typeImageView);
        }else {
            if(item.getType().getImageResourceName() != null && item.getType().getImageResourceName().length() >= 1) {
                vHolder.typeImageView.setImageResource(activity.getResources().getIdentifier(item.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else{
                vHolder.typeImageView.setImageResource(item.getType().getImageResourceID());
            }
        }

        final ViewHolder tempViewHolder = vHolder;
        vHolder.lineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.image_on_click_animation));
                int mode = MySettings.getCurrentPlace().getMode();
                Device device = MySettings.getDeviceByID2(item.getDeviceID());
                if(item.getPowerState() == Line.LINE_STATE_ON){
                    tempViewHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.lightGrayColor));
                    Utils.toggleLine(device, item.getPosition(), Line.LINE_STATE_OFF, mode, new Utils.LineToggler.ToggleCallback() {
                        @Override
                        public void onToggleSuccess() {
                            item.setPowerState(Line.LINE_STATE_OFF);
                        }
                        @Override
                        public void onToggleFail() {
                            tempViewHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.blueColor));
                            Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), false);
                        }
                    });
                }else if(item.getPowerState() == Line.LINE_STATE_OFF){
                    tempViewHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.blueColor));
                    Utils.toggleLine(device, item.getPosition(), Line.LINE_STATE_ON, mode, new Utils.LineToggler.ToggleCallback() {
                        @Override
                        public void onToggleSuccess() {
                            item.setPowerState(Line.LINE_STATE_ON);
                        }
                        @Override
                        public void onToggleFail() {
                            tempViewHolder.typeImageView.setColorFilter(ContextCompat.getColor(activity, R.color.lightGrayColor));
                            Utils.showToast(activity, Utils.getString(activity, R.string.smart_controller_connection_error), false);
                        }
                    });
                }
            }
        });

        return rowView;
    }

    private static class ViewHolder{
        TextView nameTextView;
        ImageView typeImageView;
        LinearLayout lineLayout;
    }
}
