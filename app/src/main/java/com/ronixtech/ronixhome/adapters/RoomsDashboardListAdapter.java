package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.DevicesInMemory;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.EditRoomFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsDashboardListAdapter extends ArrayAdapter{
    private static final String TAG = RoomsDashboardListAdapter.class.getSimpleName();
    Activity activity ;
    List<Room> rooms;
    Map<Long, RoomsDashboardLinesGridAdapter> linesAdaptersMap;
    ViewHolder vHolder = null;
    FragmentManager fragmentManager;

    private RoomsListener roomsListener;

    public interface RoomsListener{
        public void onRoomDeleted();
        public void onRoomNameChanged();
    }

    public RoomsDashboardListAdapter(Activity activity, List rooms, FragmentManager fragmentManager, RoomsListener roomsListener){
        super(activity, R.layout.list_item_room_dashboard, rooms);
        this.activity = activity;
        this.rooms = rooms;
        this.linesAdaptersMap = new HashMap<>();
        this.fragmentManager = fragmentManager;
        this.roomsListener = roomsListener;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_room_dashboard, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.room_name_textview);
            vHolder.typeImageView = rowView.findViewById(R.id.room_type_imageview);
            vHolder.advancedOptionsMenuImageView = rowView.findViewById(R.id.room_advanced_options_button);
            vHolder.roomLayout = rowView.findViewById(R.id.room_layout);
            vHolder.roomDevicesLayout = rowView.findViewById(R.id.room_devices_layout);
            vHolder.roomLinesGridView = rowView.findViewById(R.id.room_lines_gridview);
            vHolder.backgroundImageView = rowView.findViewById(R.id.room_background_imageview);
            vHolder.roomInfoLayout = rowView.findViewById(R.id.room_info_layout);
            vHolder.scrollPreviousImageView = rowView.findViewById(R.id.scroll_previous_imageview);
            vHolder.scrollNextImageView = rowView.findViewById(R.id.scroll_next_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Room item = rooms.get(position);

        vHolder.nameTextView.setText(""+item.getName());
        if(item.getType().getImageUrl() != null && item.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(item.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.room_icon))
                    .into(vHolder.typeImageView);
        }else {
            if(item.getType().getImageResourceName() != null && item.getType().getImageResourceName().length() >= 1){
                vHolder.typeImageView.setImageResource(activity.getResources().getIdentifier(item.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else {
                vHolder.typeImageView.setImageResource(item.getType().getImageResourceID());
            }
        }

        //TODO remvoe this and replace with actual images from Fahad, depending on room type
        if(position == 0){
            vHolder.backgroundImageView.setImageResource(R.drawable.bedroom_sample_1);
        }else if(position == 1){
            vHolder.backgroundImageView.setImageResource(R.drawable.kitchen_sample_1);
        }else if(position == 2){
            vHolder.backgroundImageView.setImageResource(R.drawable.workspace_sample);
        }else if(position == 3){
            vHolder.backgroundImageView.setImageResource(R.drawable.bedroom_sample_1);
        }

        if(item.getType().getColorHexCode().length() == 6){
            vHolder.roomInfoLayout.setBackgroundColor(Color.parseColor(Utils.getColorHex(item.getType().getColorHexCode(), 20)));
        }else{
            vHolder.roomInfoLayout.setBackgroundColor(Color.parseColor(Utils.getColorHex("000000", 20)));
        }


        if(DevicesInMemory.getRoomDevices(item.getId()) != null && DevicesInMemory.getRoomDevices(item.getId()).size() >= 1/*MySettings.getRoomDevices(item.getId()) != null && MySettings.getRoomDevices(item.getId()).size() >= 1*/){
            List<Device> roomDevices = DevicesInMemory.getRoomDevices(item.getId());
            Utils.log(TAG, "Room " + item.getName() + " has " + roomDevices.size() + " devices", true);

            List<Line> roomLines = new ArrayList<>();

            //vHolder.roomDevicesLayout.removeAllViews();
            for (Device device : roomDevices) {
                Utils.log(TAG, "Adding device " + device.getName() + " to room list item", true);
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                    if(device.getLines() != null && device.getLines().size() >= 1){
                        for (Line line:device.getLines()) {
                            if(!roomLines.contains(line)){
                                roomLines.add(line);
                            }
                        }
                    }
                }
            }

            if(roomLines.size() < 1) {
                Utils.log(TAG, "Room " + item.getName() + " has no lines", true);
                //vHolder.nameTextView.append(" - " + " No devices");
                /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vHolder.nameTextView.getLayoutParams();
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                vHolder.nameTextView.setLayoutParams(lp);*/
            }else{
                Utils.log(TAG, "Room " + item.getName() + " has " + roomLines.size() + " lines", true);
                //vHolder.nameTextView.append(" - " + roomLines.size() + " devices");
                /*RoomsDashboardLinesGridAdapter adapter = (RoomsDashboardLinesGridAdapter) linesAdaptersMap.get(item.getId());
                if(adapter == null) {
                    adapter = new RoomsDashboardLinesGridAdapter(activity, roomLines);
                    linesAdaptersMap.put(item.getId(), adapter);
                }*/
                RoomsDashboardLinesGridAdapter adapter = new RoomsDashboardLinesGridAdapter(activity, roomLines);
                vHolder.roomLinesGridView.setAdapter(adapter);

                Utils.setGridViewWidthBasedOnChildren(vHolder.roomLinesGridView);

                final ViewHolder tempViewHolder = vHolder;
                int numberOfVisibleItems = tempViewHolder.roomLinesGridView.getLastVisiblePosition() - tempViewHolder.roomLinesGridView.getFirstVisiblePosition();
                Utils.log(TAG, "firstVisiblePosition: " + tempViewHolder.roomLinesGridView.getFirstVisiblePosition(), false);
                Utils.log(TAG, "lastVisiblePosition: " + tempViewHolder.roomLinesGridView.getLastVisiblePosition(), false);
                Utils.log(TAG, "numberOfVisibleItems: " + numberOfVisibleItems, false);
                Utils.log(TAG, "getCount: " + tempViewHolder.roomLinesGridView.getCount(), false);

                if(numberOfVisibleItems > 0 && tempViewHolder.roomLinesGridView.getCount() > numberOfVisibleItems){
                    //enable arrows
                    tempViewHolder.scrollPreviousImageView.setImageResource(R.drawable.ic_keyboard_arrow_left_blue_24dp);
                    tempViewHolder.scrollNextImageView.setImageResource(R.drawable.ic_keyboard_arrow_right_blue_24dp);
                    if(tempViewHolder.roomLinesGridView.getFirstVisiblePosition() == 0){
                        tempViewHolder.scrollPreviousImageView.setImageResource(R.drawable.ic_keyboard_arrow_left_gray_24dp);
                    }
                    tempViewHolder.scrollNextImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            tempViewHolder.roomLinesGridView.smoothScrollToPosition(tempViewHolder.roomLinesGridView.getCount() - 1);
                            Utils.showToast(activity, "scrolling to position: " + (tempViewHolder.roomLinesGridView.getCount() - 1), true);
                        }
                    });
                    tempViewHolder.scrollPreviousImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                            tempViewHolder.roomLinesGridView.smoothScrollToPosition(0);
                            Utils.showToast(activity, "scrolling to position: " + (0), true);
                        }
                    });
                }else{
                    //disable arrows
                    tempViewHolder.scrollPreviousImageView.setImageResource(R.drawable.ic_keyboard_arrow_left_gray_24dp);
                    tempViewHolder.scrollNextImageView.setImageResource(R.drawable.ic_keyboard_arrow_right_gray_24dp);

                    tempViewHolder.scrollNextImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        }
                    });
                    tempViewHolder.scrollPreviousImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        }
                    });
                }

                /*if (Build.VERSION.SDK_INT >= 17) {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vHolder.nameTextView.getLayoutParams();
                    lp.removeRule(RelativeLayout.CENTER_VERTICAL);
                    vHolder.nameTextView.setLayoutParams(lp);
                }*/
            }
        }

        vHolder.roomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySettings.setCurrentRoom(item);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                DashboardDevicesFragment dashboardDevicesFragment = new DashboardDevicesFragment();
                dashboardDevicesFragment.setRoom(item);
                fragmentTransaction.replace(R.id.fragment_view, dashboardDevicesFragment, "dashboardDevicesFragment");
                fragmentTransaction.addToBackStack("dashboardDevicesFragment");
                fragmentTransaction.commit();
            }
        });

        final ViewHolder tempViewHolder = vHolder;
        vHolder.advancedOptionsMenuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                PopupMenu popup = new PopupMenu(activity, v);
                popup.getMenuInflater().inflate(R.menu.menu_room_item, popup.getMenu());

                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item1) {
                        int id = item1.getItemId();
                        if(id == R.id.action_room_device_on){
                            int mode = MySettings.getCurrentPlace().getMode();
                            Utils.toggleRoom(item, Line.LINE_STATE_ON, mode);
                        }else if(id == R.id.action_room_device_off){
                            int mode = MySettings.getCurrentPlace().getMode();
                            Utils.toggleRoom(item, Line.LINE_STATE_OFF, mode);
                        }else if(id == R.id.action_edit_room){
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            EditRoomFragment editRoomFragment = new EditRoomFragment();
                            editRoomFragment.setRoom(item);
                            fragmentTransaction.replace(R.id.fragment_view, editRoomFragment, "editRoomFragment");
                            fragmentTransaction.addToBackStack("editRoomFragment");
                            fragmentTransaction.commit();
                        }else if(id == R.id.action_remove_room){
                            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(activity)
                                    .setTitle(Utils.getString(activity, R.string.remove_room_question))
                                    .setMessage(Utils.getString(activity, R.string.remove_room_description))
                                    //set positive button
                                    .setPositiveButton(Utils.getString(activity, R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what would happen when positive button is clicked
                                            MySettings.removeRoom(item);
                                            rooms.remove(item);
                                            roomsListener.onRoomDeleted();
                                        }
                                    })
                                    //set negative button
                                    .setNegativeButton(Utils.getString(activity, R.string.no), new DialogInterface.OnClickListener() {
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
        vHolder.roomLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tempViewHolder.advancedOptionsMenuImageView.performClick();
                return true;
            }
        });

        return rowView;
    }

    public static class ViewHolder{
        TextView nameTextView;
        ImageView typeImageView, advancedOptionsMenuImageView, backgroundImageView;
        CardView roomLayout;
        LinearLayout roomDevicesLayout;
        GridView roomLinesGridView;
        RelativeLayout roomInfoLayout;
        ImageView scrollPreviousImageView, scrollNextImageView;
    }
}
