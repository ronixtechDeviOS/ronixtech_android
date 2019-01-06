package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.fragments.DashboardRoomsFragment;
import com.ronixtech.ronixhome.fragments.EditPlaceFragment;
import com.ronixtech.ronixhome.fragments.FloorsFragment;

import java.util.List;

public class PlacesGridAdapter extends BaseAdapter {
    Activity activity;
    List<Place> places;
    ViewHolder vHolder = null;
    FragmentManager fragmentManager;

    private PlacesListener placesListener;

    public interface PlacesListener{
        public void onPlaceDeleted();
        public void onDefaultPlaceRequested();
        public void onPlaceDevicesToggled(Place place, int newState);
    }

    public PlacesGridAdapter(Activity activity, List<Place> places, FragmentManager fragmentManager, PlacesListener listener) {
        this.activity = activity;
        this.places = places;
        this.fragmentManager = fragmentManager;
        this.placesListener = listener;
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
            vHolder.advancedOptionsMenuImageView = rowView.findViewById(R.id.place_item_advanced_options_button);
            vHolder.placeItemLayout = rowView.findViewById(R.id.place_item_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Place item = places.get(position);
        vHolder.placeNameTextView.setText(""+item.getName());
        if(item.getType().getImageUrl() != null && item.getType().getImageUrl().length() >= 1){
            GlideApp.with(activity)
                .load(item.getType().getImageUrl())
                .placeholder(activity.getResources().getDrawable(R.drawable.place_type_house))
                .into(vHolder.placeImageView);
        }else {
            if(item.getType().getImageResourceName() != null && item.getType().getImageResourceName().length() >= 1){
                vHolder.placeImageView.setImageResource(activity.getResources().getIdentifier(item.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else {
                vHolder.placeImageView.setImageResource(item.getType().getImageResourceID());
            }
        }

        vHolder.placeItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySettings.setCurrentPlace(item);
                if(MySettings.getPlaceFloors(item.getId()) != null && MySettings.getPlaceFloors(item.getId()).size() > 1) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    FloorsFragment floorsFragment = new FloorsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, floorsFragment, "floorsFragment");
                    fragmentTransaction.addToBackStack("floorsFragment");
                    fragmentTransaction.commit();
                }else if(MySettings.getPlaceFloors(item.getId()) != null){
                    List<Floor> floors = MySettings.getPlaceFloors(item.getId());
                    Floor selectedFloor= (Floor) floors.get(0);
                    MySettings.setCurrentFloor(selectedFloor);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentTransaction.addToBackStack("dashboardRoomsFragment");
                    fragmentTransaction.commit();
                }
            }
        });


        final ViewHolder tempViewHolder = vHolder;
        vHolder.advancedOptionsMenuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                PopupMenu popup = new PopupMenu(activity, v);
                popup.getMenuInflater().inflate(R.menu.menu_place_item, popup.getMenu());

                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item1) {
                        int id = item1.getItemId();
                        if(id == R.id.action_place_device_on){
                            placesListener.onPlaceDevicesToggled(item, Line.LINE_STATE_ON);
                        }else if(id == R.id.action_place_device_off){
                            placesListener.onPlaceDevicesToggled(item, Line.LINE_STATE_OFF);
                        }else if(id == R.id.action_edit_place){
                            MySettings.setCurrentPlace(item);
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            EditPlaceFragment editPlaceFragment = new EditPlaceFragment();
                            fragmentTransaction.replace(R.id.fragment_view, editPlaceFragment, "editPlaceFragment");
                            fragmentTransaction.addToBackStack("editPlaceFragment");
                            fragmentTransaction.commit();
                        }else if(id == R.id.action_remove_place){
                            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(activity)
                                    .setTitle(Utils.getString(activity, R.string.remove_place_question))
                                    .setMessage(Utils.getString(activity, R.string.remove_place_description))
                                    //set positive button
                                    .setPositiveButton(Utils.getString(activity, R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //set what would happen when positive button is clicked
                                            MySettings.removePlace(item);
                                            places.remove(item);
                                            placesListener.onPlaceDeleted();
                                            if(MySettings.getDefaultPlaceID() == item.getId()){
                                                placesListener.onDefaultPlaceRequested();
                                            }
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
                        return true;
                    }
                });
            }
        });

        vHolder.placeItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tempViewHolder.advancedOptionsMenuImageView.performClick();
                return true;
            }
        });



        return rowView;
    }

    private static class ViewHolder{
        TextView placeNameTextView;
        ImageView placeImageView, advancedOptionsMenuImageView;
        RelativeLayout placeItemLayout;
    }
}
