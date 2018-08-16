package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

public class RoomAdapter extends ArrayAdapter {
    Activity activity ;
    List<Room> rooms;
    ViewHolder vHolder = null;

    public RoomAdapter(Activity activity, List rooms){
        super(activity, R.layout.list_item_room, rooms);
        this.activity = activity;
        this.rooms = rooms;
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
            rowView = inflater.inflate(R.layout.list_item_room, null);
            vHolder = new ViewHolder();
            vHolder.roomNameTextView = rowView.findViewById(R.id.room_name_textview);
            vHolder.roomPositionTextView = rowView.findViewById(R.id.room_position_textview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        vHolder.roomNameTextView.setText(""+rooms.get(position).getName());
        vHolder.roomPositionTextView.setText(rooms.get(position).getFloorLevel()+" - "+rooms.get(position).getFloorName());

        return rowView;
    }

    public static class ViewHolder{
        TextView roomNameTextView, roomPositionTextView;
    }
}
