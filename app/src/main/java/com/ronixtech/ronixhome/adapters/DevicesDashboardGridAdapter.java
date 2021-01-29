package com.ronixtech.ronixhome.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

public class DevicesDashboardGridAdapter extends BaseAdapter {
    Activity activity;
    List<Line> lines;
    List<Device> devices;
    DeviceAdapter deviceAdapter;
    DevicesDashboardGridAdapter.ViewHolder vHolder = null;
    Room currentRoom;
    int selectedDevice;
    FragmentManager fragmentManager;

    public DevicesDashboardGridAdapter(Activity activity,Room room,List<Device> roomDevices,  FragmentManager fragmentManager,int selectedDevice) {

        this.activity = activity;
        this.selectedDevice=selectedDevice;
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

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_device_dashboard, null);
            vHolder = new DevicesDashboardGridAdapter.ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.line_name_textview);
            vHolder.typeImageView = rowView.findViewById(R.id.line_type_imageview);
            vHolder.deviceLayout = rowView.findViewById(R.id.devices_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (DevicesDashboardGridAdapter.ViewHolder) rowView.getTag();
        }


        Device item = devices.get(position);
        vHolder.nameTextView.setText("" + item.getName());
        vHolder.nameTextView.setSelected(true);
        vHolder.nameTextView.setTextColor(ContextCompat.getColor(MainActivity.getInstance(),R.color.graycolor));

        if(item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old){
            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.switch_gray))
                    .placeholder(activity.getResources().getDrawable(R.drawable.switch_gray))
                    .into(vHolder.typeImageView);
        }else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.plug_gray))
                    .placeholder(activity.getResources().getDrawable(R.drawable.plug_gray))
                    .into(vHolder.typeImageView);
        }
        else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines)
        {
            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.magic_gray))
                    .placeholder(activity.getResources().getDrawable(R.drawable.magic_gray))
                    .into(vHolder.typeImageView);
        }
        else if(item.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR)
        {

            GlideApp.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.ir_gray))
                    .placeholder(activity.getResources().getDrawable(R.drawable.ir_gray))
                    .into(vHolder.typeImageView);
        }

        if(position==selectedDevice) {
            vHolder.nameTextView.setTextColor(ContextCompat.getColor(MainActivity.getInstance(), R.color.orangeColor2));

            //currently selected device
            if (item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                    item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || item.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old) {
                GlideApp.with(activity)
                        .load(activity.getResources().getDrawable(R.drawable.switch_orange))
                        .placeholder(activity.getResources().getDrawable(R.drawable.switch_orange))
                        .into(vHolder.typeImageView);

            } else if (item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
                GlideApp.with(activity)
                        .load(activity.getResources().getDrawable(R.drawable.magic_orange))
                        .placeholder(activity.getResources().getDrawable(R.drawable.magic_orange))
                        .into(vHolder.typeImageView);

            } else if (item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || item.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines) {
                GlideApp.with(activity)
                        .load(activity.getResources().getDrawable(R.drawable.plug_orange))
                        .placeholder(activity.getResources().getDrawable(R.drawable.plug_orange))
                        .into(vHolder.typeImageView);
            } else if (item.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR) {
                GlideApp.with(activity)
                        .load(activity.getResources().getDrawable(R.drawable.ir_orange))
                        .placeholder(activity.getResources().getDrawable(R.drawable.ir_orange))
                        .into(vHolder.typeImageView);
            }
        }


        return rowView;
    }

    public void setSelectedDevice(int devicePos)
    {
        selectedDevice=devicePos;
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

