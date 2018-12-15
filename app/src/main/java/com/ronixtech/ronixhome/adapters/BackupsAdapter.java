package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Backup;

import java.util.List;

public class BackupsAdapter extends ArrayAdapter{
    Activity activity ;
    List<Backup> backups;
    ViewHolder vHolder = null;

    public BackupsAdapter(Activity activity, List backups){
        super(activity, R.layout.list_item_backup, backups);
        this.activity = activity;
        this.backups = backups;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return backups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return backups.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_backup, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.backup_item_name_textview);
            vHolder.timestampTextView = rowView.findViewById(R.id.backup_item_timestamp_name_textview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Backup item = backups.get(position);

        vHolder.nameTextView.setText(""+item.getName());
        vHolder.timestampTextView.setText(activity.getResources().getString(R.string.date_backup_variable, Utils.getTimeStringDateHoursMinutes(item.getTimestamp())));

        return rowView;
    }

    public static class ViewHolder{
        TextView nameTextView, timestampTextView;
    }
}
