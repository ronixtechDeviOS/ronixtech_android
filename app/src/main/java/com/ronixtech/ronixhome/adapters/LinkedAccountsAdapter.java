package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
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
            vHolder.emailTextView = rowView.findViewById(R.id.account_item_email_textview);
            vHolder.additionDateTextView = rowView.findViewById(R.id.account_item_addition_timestamp_textview);
            vHolder.removeImageView = rowView.findViewById(R.id.account_item_remove_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        User item = users.get(position);

        vHolder.nameTextView.setText(""+item.getFullName());
        vHolder.emailTextView.setText(""+item.getEmail());
        vHolder.additionDateTextView.setText(activity.getResources().getString(R.string.date_linked_account_variable, Utils.getDateString(item.getLinkTimestamp())));

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
                                //remove user from firebase DB
                                removeLinkedAccount(item);
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

    private void removeLinkedAccount(User user){
        Utils.showLoading(activity);

        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("linked_accounts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    if(snapshot.getData().get("email") != null && snapshot.getData().get("email").equals(user.getEmail())){
                        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("linked_accounts").document(snapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Utils.dismissLoading();
                                MySettings.removeUser(user);
                                users.remove(user);
                                accountActionListener.onUserDeleted();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.dismissLoading();
                                if(activity != null){
                                    Toast.makeText(activity, activity.getResources().getString(R.string.remove_linked_account_failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.dismissLoading();
                if(activity != null){
                    Toast.makeText(activity, activity.getResources().getString(R.string.remove_linked_account_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class ViewHolder{
        TextView nameTextView, emailTextView, additionDateTextView;
        ImageView removeImageView;
    }
}
