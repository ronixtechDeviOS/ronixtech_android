package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

public class PlacesGridAdapter extends BaseAdapter {
    Activity activity;
    List<Place> places;
    ViewHolder vHolder = null;

    public PlacesGridAdapter(Activity activity, List<Place> places) {
        this.activity = activity;
        this.places = places;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return places.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.grid_item_place, null);
            vHolder = new ViewHolder();
            vHolder.placeNameTextView = rowView.findViewById(R.id.place_item_name_textview);
            vHolder.placeImageView = rowView.findViewById(R.id.place_item_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Place item = places.get(position);
        vHolder.placeNameTextView.setText(""+item.getName());

        /*GlideApp.with(activity)
                .load(Utils.getImageUrl(item.getImageUrl()))
                .placeholder(activity.getResources().getDrawable(android.R.drawable.))
                .into(vHolder.roomImageView);*/

        return rowView;
    }

    private static class ViewHolder{
        TextView placeNameTextView;
        ImageView placeImageView;
    }
}
