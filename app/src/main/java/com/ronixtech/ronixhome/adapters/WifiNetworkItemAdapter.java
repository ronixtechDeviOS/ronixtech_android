package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.List;

public class WifiNetworkItemAdapter extends ArrayAdapter{
    Activity activity ;
    List<WifiNetwork> networks;
    ViewHolder vHolder = null;

    public WifiNetworkItemAdapter(Activity activity, List networks){
        super(activity, R.layout.list_item_wifi_network, networks);
        this.activity = activity;
        this.networks = networks;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return networks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return networks.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_wifi_network, null);
            vHolder = new ViewHolder();
            vHolder.networkNameTextView = rowView.findViewById(R.id.wifi_network_item_name_textview);
            vHolder.networkSignalTextView = rowView.findViewById(R.id.wifi_network_item_signal_textview);
            vHolder.networkSignalImageView = rowView.findViewById(R.id.wifi_network_signal_status_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        WifiNetwork item = networks.get(position);

        vHolder.networkNameTextView.setText(""+item.getSsid());
        vHolder.networkSignalTextView.setText("Signal: "+item.getSignal());

        if(item.getSignal() != null && item.getSignal().length() >= 1){
            int signalStrenth = Integer.valueOf(item.getSignal());
            if(signalStrenth <= -90){
                //signal Unusable
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.redColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_0);
            }else if(signalStrenth <= -80){
                //signal Not Good
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.darkOrangeColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_1);
            }else if(signalStrenth <= -70){
                //signal Okay
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.lightestOrangeColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_2);
            }else if(signalStrenth <= -67){
                //signal Very Good
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.blueColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_3);
            }else if(signalStrenth <= -30){
                //signal Amazing
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.greenColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_4);
            }else{
                //signal Amazing
                vHolder.networkSignalTextView.setTextColor(activity.getResources().getColor(R.color.greenColor));
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_4);
            }
        }

        return rowView;
    }

    public static class ViewHolder{
        TextView networkNameTextView, networkSignalTextView;
        ImageView networkSignalImageView;
    }
}