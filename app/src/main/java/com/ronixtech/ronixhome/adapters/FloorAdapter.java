package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Floor;

import java.util.List;

public class FloorAdapter extends ArrayAdapter {
    Activity activity ;
    List<Floor> floors;
    ViewHolder vHolder = null;

    public FloorAdapter(Activity activity, List floors){
        super(activity, R.layout.list_item_floor, floors);
        this.activity = activity;
        this.floors = floors;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return floors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_floor, null);
            vHolder = new ViewHolder();
            vHolder.floorNameTextView = rowView.findViewById(R.id.floor_name_textview);
            vHolder.floorPositionTextView = rowView.findViewById(R.id.floor_position_textview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        vHolder.floorNameTextView.setText(""+floors.get(position).getLevel()+" - "+floors.get(position).getName());
        vHolder.floorPositionTextView.setText(""+floors.get(position).getPlaceName());

        return rowView;
    }

    public static class ViewHolder{
        TextView floorNameTextView, floorPositionTextView;
    }
}
