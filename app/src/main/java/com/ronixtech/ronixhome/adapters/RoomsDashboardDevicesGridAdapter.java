package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.DimmingControlDialogFragment_new;

import java.util.List;

public class RoomsDashboardDevicesGridAdapter extends BaseAdapter {
    Activity activity;
    List<Line> lines;
    List<Device> devices;
    ViewHolder vHolder = null;
    Room currentRoom;
    FragmentManager fragmentManager;

    public RoomsDashboardDevicesGridAdapter(Activity activity,Room room,List<Device> roomDevices, List<Line> lines, FragmentManager fragmentManager) {
        this.activity = activity;
        this.lines = lines;
        this.devices=roomDevices;
        this.currentRoom=room;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_room_dashboard_device, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.line_name_textview);
            vHolder.typeImageView = rowView.findViewById(R.id.line_type_imageview);
            vHolder.deviceLayout = rowView.findViewById(R.id.device_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Line line = lines.get(position);
        Device item = devices.get(position);
        vHolder.nameTextView.setText("" + item.getName());
        vHolder.nameTextView.setSelected(true);

        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_LOCAL){
                if(item != null && item.getIpAddress() != null && item.getIpAddress().length() >= 1){
                    if(getDeviceLinesState(item)){
                        vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_on_background);
                     }else if(!getDeviceLinesState(item)){
                        vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_off_background);
                     }
                }else{
                    vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_error_background);
                }
            }else if(MySettings.getCurrentPlace().getMode() == Place.PLACE_MODE_REMOTE){
                if(item != null && item.isDeviceMQTTReachable()){
                    if(getDeviceLinesState(item)){
                        vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_on_background);
                    }else if(!getDeviceLinesState(item)){
                        vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_off_background);
                    }
                }else{
                    vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_error_background);
                }
            }
        }else{
            if(getDeviceLinesState(item)){
                vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_on_background);

            }else if(!getDeviceLinesState(item)){
                vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_off_background);
                }
        }

        if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.smart_sw))
                    .placeholder(activity.getResources().getDrawable(R.drawable.smart_sw))
                    .into(vHolder.typeImageView);
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.plug))
                    .into(vHolder.typeImageView);
        }

        final ViewHolder tempViewHolder = vHolder;
        vHolder.deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MySettings.setCurrentRoom(currentRoom);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                DashboardDevicesFragment dashboardDevicesFragment = new DashboardDevicesFragment();
                dashboardDevicesFragment.setRoom(currentRoom);
                fragmentTransaction.replace(R.id.fragment_view, dashboardDevicesFragment, "dashboardDevicesFragment");
                fragmentTransaction.addToBackStack("dashboardDevicesFragment");
                fragmentTransaction.commit();
            }
        });


        vHolder.deviceLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment prev = fragmentManager.findFragmentByTag("dimmingControlDialogFragment_new");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DimmingControlDialogFragment_new fragment = DimmingControlDialogFragment_new.newInstance();
                fragment.setDevice(item);
                fragment.show(ft, "dimmingControlDialogFragment_new");


                /*if(item.getDimmingState() == Line.DIMMING_STATE_ON){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    Fragment prev = fragmentManager.findFragmentByTag("dimmingControlDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    DimmingControlDialogFragment fragment = DimmingControlDialogFragment.newInstance();
                    fragment.setLine(item);
                    fragment.show(ft, "dimmingControlDialogFragment");
                }else if(item.getDimmingState() == Line.DIMMING_STATE_OFF){
                    Utils.showToast(activity, Utils.getString(activity, R.string.line_dimming_disabled), true);
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(tempViewHolder.lineLayout);
                }

                return true;*/
                return true;
            }
        });



        return rowView;
    }

    private boolean getDeviceLinesState(Device item) {
        for(int i = 0; i< item.getLines().size(); i++)
        {
            if(item.getLines().get(i).getPowerState() == Line.LINE_STATE_ON)
            {
                return true;
            }
        }
        return false;
    }

    private static class ViewHolder{
        TextView nameTextView;
        ImageView typeImageView;
        LinearLayout deviceLayout;
    }
}
