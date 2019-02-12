package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Type;

import java.util.List;

public class TypeAdapter extends ArrayAdapter {
    Activity activity ;
    List<Type> types;
    ViewHolder vHolder = null;
    int padding;

    public TypeAdapter(Activity activity, List types){
        super(activity, R.layout.list_item_type, types);
        this.activity = activity;
        this.types = types;
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, activity.getResources().getDisplayMetrics());
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return types.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return types.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_type, null);
            vHolder = new ViewHolder();
            vHolder.typeNameTextView = rowView.findViewById(R.id.type_name_textview);
            vHolder.typeImageView = rowView.findViewById(R.id.type_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        vHolder.typeNameTextView.setText(""+types.get(position).getName());
        if(types.get(position).getImageResourceName() != null && types.get(position).getImageResourceName().length() >= 1){
            vHolder.typeImageView.setImageResource(activity.getResources().getIdentifier(types.get(position).getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
        }else{
            vHolder.typeImageView.setImageResource(types.get(position).getImageResourceID());
        }

        if(types.get(position).getCategoryID() == Constants.TYPE_LINE || types.get(position).getCategoryID() == Constants.TYPE_LINE_PLUG){
            vHolder.typeImageView.setBackgroundResource(R.drawable.rooms_dashboard_line_default_background);
            vHolder.typeImageView.setPadding(padding, padding, padding, padding);
        }

        return rowView;
    }

    public static class ViewHolder{
        TextView typeNameTextView;
        ImageView typeImageView;
    }
}
