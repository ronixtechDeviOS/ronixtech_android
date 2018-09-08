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
import com.ronixtech.ronixhome.entities.Floor;

import java.util.List;

public class FloorsGridAdapter extends BaseAdapter {
    Activity activity;
    List<Floor> floors;
    ViewHolder vHolder = null;

    public FloorsGridAdapter(Activity activity, List<Floor> floors) {
        this.activity = activity;
        this.floors = floors;
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return floors.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.grid_item_floor, null);
            vHolder = new ViewHolder();
            vHolder.floorNameTextView = rowView.findViewById(R.id.floor_item_name_textview);
            vHolder.floorImageView = rowView.findViewById(R.id.floor_item_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Floor item = floors.get(position);
        vHolder.floorNameTextView.setText(""+item.getName()/* + "\n" + "(" + item.getPlaceName() + ")"*/);
        if(item.getType().getImageUrl() != null && item.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(item.getType().getImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.floor_icon))
                    .into(vHolder.floorImageView);
        }else {
            //vHolder.floorImageView.setImageResource(item.getType().getImageResourceID());
            vHolder.floorImageView.setImageResource(R.drawable.floor_icon);
        }

        return rowView;
    }

    private static class ViewHolder{
        TextView floorNameTextView;
        ImageView floorImageView;
    }
}
