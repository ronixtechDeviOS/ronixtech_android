package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

public class PlaceAdapter extends ArrayAdapter {
    Activity activity ;
    List<Place> places;
    ViewHolder vHolder = null;

    public PlaceAdapter(Activity activity, List places){
        super(activity, R.layout.list_item_place, places);
        this.activity = activity;
        this.places = places;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_place, null);
            vHolder = new ViewHolder();
            vHolder.placeNameTextView = rowView.findViewById(R.id.place_name_textview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        vHolder.placeNameTextView.setText(""+places.get(position).getName());

        return rowView;
    }

    public static class ViewHolder{
        TextView placeNameTextView;
    }
}
