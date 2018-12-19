package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;
import com.ronixtech.ronixhome.fragments.EditRoomFragment;

import java.util.List;

public class RoomsDashboardListAdapter extends ArrayAdapter{
    Activity activity ;
    List<Room> rooms;
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

        vHolder.roomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if(id == R.id.action_edit_room){
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
        ImageView typeImageView, advancedOptionsMenuImageView;
        CardView roomLayout;
    }
}
