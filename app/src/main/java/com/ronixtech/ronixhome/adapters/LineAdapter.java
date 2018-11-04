package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

public class LineAdapter extends ArrayAdapter {
    Activity activity ;
    List<Line> lines;
    ViewHolder vHolder = null;

    public LineAdapter(Activity activity, List lines){
        super(activity, R.layout.list_item_line, lines);
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
            rowView = inflater.inflate(R.layout.list_item_line, null);
            vHolder = new ViewHolder();
            vHolder.lineNameTextView= rowView.findViewById(R.id.line_textvie);
            vHolder.lineLocationTextView = rowView.findViewById(R.id.line_location_textview);
            vHolder.lineControllerTextView = rowView.findViewById(R.id.line_controller_textview);
            vHolder.lineImageView = rowView.findViewById(R.id.line_type_imageview);
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
            vHolder.lineImageView.setImageResource(lines.get(position).getType().getImageResourceID());
        }

        return rowView;
    }

    public static class ViewHolder{
        TextView lineNameTextView, lineControllerTextView,lineLocationTextView;
        ImageView lineImageView;
    }
}
