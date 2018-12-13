package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.User;

import java.util.List;

public class LinkedAccountsAdapter extends ArrayAdapter {
    Activity activity ;
    List<User> users;
    ViewHolder vHolder = null;

    private AccountActionListener accountActionListener;

    public interface AccountActionListener{
        public void onUserDeleted();
    }

    public LinkedAccountsAdapter(Activity activity, List users, AccountActionListener accountActionListener){
        super(activity, R.layout.list_item_linked_account, users);
        this.activity = activity;
        this.users = users;
        this.accountActionListener = accountActionListener;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_linked_account, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.account_item_name_textview);
            vHolder.emailTextView = rowView.findViewById(R.id.account_item_name_email_textview);
            vHolder.removeImageView = rowView.findViewById(R.id.account_item_remove_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        User item = users.get(position);

        vHolder.nameTextView.setText(""+item.getFullName());
        vHolder.emailTextView.setText(""+item.getEmail());

        vHolder.removeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setTitle(activity.getResources().getString(R.string.remove_linked_account_question))
                        .setMessage(activity.getResources().getString(R.string.remove_linked_account_description))
                        //set positive button
                        .setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                //TODO remove user from firebase DB for this place

                                MySettings.removeUser(item);
                                users.remove(item);
                                accountActionListener.onUserDeleted();
                            }
                        })
                        //set negative button
                        .setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();
            }
        });

        return rowView;
    }

    public static class ViewHolder{
        TextView nameTextView, emailTextView;
        ImageView removeImageView;
    }
}
