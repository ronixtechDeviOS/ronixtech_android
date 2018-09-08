package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

public class RoomsGridAdapter extends BaseAdapter{
    Activity activity;
    List<Room> rooms;
    ViewHolder vHolder = null;

    public RoomsGridAdapter(Activity activity, List<Room> rooms) {
        this.activity = activity;
        this.rooms = rooms;
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
            vHolder.roomImageView.setImageResource(item.getType().getImageResourceID());
        }

        return rowView;
    }

    private static class ViewHolder{
        TextView roomNameTextView;
        ImageView roomImageView;
    }
}
