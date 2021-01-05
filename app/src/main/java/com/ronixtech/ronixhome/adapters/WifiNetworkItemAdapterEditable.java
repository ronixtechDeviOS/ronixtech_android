package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.List;

public class WifiNetworkItemAdapterEditable extends ArrayAdapter {
    Activity activity ;
    List<WifiNetwork> networks;
    ViewHolder vHolder = null;
    int removeNetworkFromDB = Constants.REMOVE_NETWORK_FROM_DB_NO;
    int colorMode;

    private WifiNetworksListener wifiNetworksListener;

    public interface WifiNetworksListener{
        public void onNetworkDeleted();
    }

    public WifiNetworkItemAdapterEditable(Activity activity, List networks, WifiNetworksListener wifiNetworksListener, int removeNetworkFromDB, int colorMode){
        super(activity, R.layout.list_item_wifi_network_editable, networks);
        this.activity = activity;
        this.networks = networks;
        this.wifiNetworksListener = wifiNetworksListener;
        this.removeNetworkFromDB = removeNetworkFromDB;
        this.colorMode = colorMode;
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
            rowView = inflater.inflate(R.layout.list_item_wifi_network_editable, null);
            vHolder = new ViewHolder();
            vHolder.networkNameTextView = rowView.findViewById(R.id.wifi_network_item_name_textview);
            vHolder.placeNameTextView = rowView.findViewById(R.id.wifi_network_item_place_name_textview);
            vHolder.networkSignalImageView = rowView.findViewById(R.id.wifi_network_signal_status_imageview);
            vHolder.networkRemoveImageView = rowView.findViewById(R.id.wifi_network_remove_imageview);
            vHolder.networkEditImageView = rowView.findViewById(R.id.wifi_network_edit_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        WifiNetwork item = networks.get(position);

        vHolder.networkNameTextView.setText(""+item.getSsid());

        if(item.getPlaceID() != -1){
            Place place = MySettings.getPlace(item.getPlaceID());
            if(place != null){
                vHolder.placeNameTextView.setText("@" + place.getName());
            }else{
                vHolder.placeNameTextView.setText(Utils.getString(activity, R.string.wifi_no_place));
            }
        }else{
            vHolder.placeNameTextView.setText(Utils.getString(activity, R.string.wifi_no_place));
        }

        if(colorMode == Constants.COLOR_MODE_DARK_BACKGROUND){
            vHolder.networkNameTextView.setTextColor(activity.getResources().getColor(R.color.whiteColor));
            vHolder.placeNameTextView.setTextColor(activity.getResources().getColor(R.color.whiteColor));
        }else if(colorMode == Constants.COLOR_MODE_LIGHT_BACKGROUND){
            vHolder.networkNameTextView.setTextColor(activity.getResources().getColor(R.color.blackColor));
            vHolder.placeNameTextView.setTextColor(activity.getResources().getColor(R.color.blackColor));
        }

        if(item.getSignal() != null && item.getSignal().length() >= 1){
            int signalStrenth = Integer.valueOf(item.getSignal());
            if(signalStrenth <= -90){
                //signal Unusable
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_0);
            }else if(signalStrenth <= -80){
                //signal Not Good
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_1);
            }else if(signalStrenth <= -70){
                //signal Okay
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_2);
            }else if(signalStrenth <= -67){
                //signal Very Good
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_3);
            }else if(signalStrenth <= -30){
                //signal Amazing
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_4);
            }else{
                //signal Amazing
                vHolder.networkSignalImageView.setImageResource(R.drawable.signal_4);
            }
        }

        vHolder.networkEditImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(activity).create();
                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r = activity.getResources();
                float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                TextView passwordTextView = new TextView(activity);
                passwordTextView.setText(Utils.getString(activity, R.string.password_colon));
                passwordTextView.setTextSize(20);
                passwordTextView.setGravity(Gravity.CENTER);
                passwordTextView.setLayoutParams(layoutParams);

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r2 = activity.getResources();
                float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
                layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
                layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

                final EditText passwordEditText = new EditText(activity);
                passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                passwordEditText.setHint(Utils.getString(activity, R.string.password_hint));
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEditText.setLayoutParams(layoutParams2);

                Button submitButton = new Button(activity);
                submitButton.setText(Utils.getString(activity, R.string.done));
                submitButton.setTextColor(activity.getResources().getColor(R.color.whiteColor));
                submitButton.setBackgroundColor(activity.getResources().getColor(R.color.blueColor));
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(passwordEditText.getText().toString() != null && passwordEditText.getText().toString().length() >= 4) {
                            item.setPassword(passwordEditText.getText().toString());
                            MySettings.updateWifiNetworkPassword(item, passwordEditText.getText().toString());
                            dialog.dismiss();
                        }else{
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(passwordEditText);
                        }
                    }
                });

                layout.addView(passwordTextView);
                layout.addView(passwordEditText);
                layout.addView(submitButton);

                dialog.setView(layout);

                dialog.show();
            }
        });
        vHolder.networkRemoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    android.support.v7.app.AlertDialog alertDialog = new AlertDialog.Builder(activity)
                            .setTitle(Utils.getString(activity, R.string.remove_wifi_network_question))
                            .setMessage(Utils.getString(activity, R.string.remove_wifi_network_description))
                            //set positive button
                            .setPositiveButton(Utils.getString(activity, R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //set what would happen when positive button is clicked
                                    MySettings.removeWifiNetwork(item);
                                    networks.remove(item);
                                    wifiNetworksListener.onNetworkDeleted();
                                }
                            })
                            //set negative button
                            .setNegativeButton(Utils.getString(activity, R.string.no), new DialogInterface.OnClickListener() {
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
        TextView networkNameTextView, placeNameTextView;
        ImageView networkSignalImageView;
        ImageView networkRemoveImageView, networkEditImageView;
    }
}
