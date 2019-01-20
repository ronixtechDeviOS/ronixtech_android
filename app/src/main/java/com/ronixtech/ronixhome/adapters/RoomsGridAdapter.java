package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.EditRoomFragment;

import java.util.List;

public class RoomsGridAdapter extends BaseAdapter{
    Activity activity;
    List<Room> rooms;
    ViewHolder vHolder = null;
    FragmentManager fragmentManager;

    private RoomsListener roomsListener;

    public interface RoomsListener{
        public void onRoomDeleted();
        public void onRoomNameChanged();
    }

    public RoomsGridAdapter(Activity activity, List<Room> rooms, FragmentManager fragmentManager, RoomsListener roomsListener) {
        this.activity = activity;
        this.rooms = rooms;
        this.fragmentManager = fragmentManager;
        this.roomsListener = roomsListener;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.grid_item_room, null);
            vHolder = new ViewHolder();
            vHolder.roomNameTextView = rowView.findViewById(R.id.room_item_name_textview);
            vHolder.roomImageView = rowView.findViewById(R.id.room_item_imageview);
            vHolder.advancedOptionsMenuImageView = rowView.findViewById(R.id.room_item_advanced_options_button);
            vHolder.roomItemLayout = rowView.findViewById(R.id.room_item_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Room item = rooms.get(position);
        if(item.getFloorName() == null || item.getFloorName().length() < 1) {
            vHolder.roomNameTextView.setText("" + item.getName()/* + "\n" + "(" + item.getFloorLevel() + ")"*/);
        }else{
            vHolder.roomNameTextView.setText("" + item.getName()/* + "\n" + "(" + MySettings.getFloor(item.getFloorID()).getPlaceName() + " > " + item.getFloorName() + ")"*/);
        }

        if(item.getType().getImageUrl() != null && item.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(item.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.room_type_living_room))
                    .into(vHolder.roomImageView);
        }else {
            if(item.getType().getImageResourceName() != null && item.getType().getImageResourceName().length() >= 1){
                vHolder.roomImageView.setImageResource(activity.getResources().getIdentifier(item.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else{
                vHolder.roomImageView.setImageResource(item.getType().getImageResourceID());
            }
        }

        vHolder.roomItemLayout.setOnClickListener(new View.OnClickListener() {
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

        vHolder.roomItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tempViewHolder.advancedOptionsMenuImageView.performClick();
                return true;
            }
        });

        return rowView;
    }

    private static class ViewHolder{
        TextView roomNameTextView;
        ImageView roomImageView, advancedOptionsMenuImageView;
        RelativeLayout roomItemLayout;
    }
}
