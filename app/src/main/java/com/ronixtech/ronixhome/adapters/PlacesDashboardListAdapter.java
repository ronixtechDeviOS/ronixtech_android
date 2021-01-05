package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

public class PlacesDashboardListAdapter extends BaseAdapter {
    Activity activity;
    List<Place> places;
    ViewHolder vHolder = null;
    FragmentManager fragmentManager;

    private PlacesListener placesListener;

    public interface PlacesListener{
        public void onPlaceDeleted();
        public void onDefaultPlaceRequested();
    }

    public PlacesDashboardListAdapter(Activity activity, List<Place> places, FragmentManager fragmentManager, PlacesListener listener) {
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

    PopupWindow popup;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_place_dashboard, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = rowView.findViewById(R.id.place_name_textview);
            vHolder.backgroundImageView = rowView.findViewById(R.id.place_background_imageview);
            vHolder.advancedOptionsMenuImageView = rowView.findViewById(R.id.place_advanced_options_button);
            vHolder.placeLayout = rowView.findViewById(R.id.place_layout);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Place item = places.get(position);
        vHolder.nameTextView.setText("" + item.getName());

        if(item.getType().getBackgroundImageUrl() != null && item.getType().getBackgroundImageUrl().length() >= 1){
            GlideApp.with(activity)
                    .load(item.getType().getBackgroundImageUrl())
                    .placeholder(activity.getResources().getDrawable(R.drawable.place_background_house_1))
                    .into(vHolder.backgroundImageView);
        }else {
            if(item.getType().getBackgroundImageResourceName() != null && item.getType().getBackgroundImageResourceName().length() >= 1){
                vHolder.backgroundImageView.setImageResource(activity.getResources().getIdentifier(item.getType().getBackgroundImageResourceName(), "drawable", Constants.PACKAGE_NAME));
            }else {
                vHolder.backgroundImageView.setImageResource(item.getType().getBackgroundImageResourceID());
            }
        }

        vHolder.placeLayout.setOnClickListener(new View.OnClickListener() {
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
                            int mode = item.getMode();
                            Utils.togglePlace(item, Line.LINE_STATE_ON, mode);
                        }else if(id == R.id.action_place_device_off){
                            int mode = item.getMode();
                            Utils.togglePlace(item, Line.LINE_STATE_OFF, mode);
                        }else if(id == R.id.action_edit_place){
                            MySettings.setCurrentPlace(item);
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                            EditPlaceFragment editPlaceFragment = new EditPlaceFragment();
                            fragmentTransaction.replace(R.id.fragment_view, editPlaceFragment, "editPlaceFragment");
                            fragmentTransaction.addToBackStack("editPlaceFragment");
                            fragmentTransaction.commit();
                        }else if(id == R.id.action_remove_place){
                            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(activity)
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

        vHolder.placeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tempViewHolder.advancedOptionsMenuImageView.performClick();
                return true;
            }
        });

        return rowView;
    }

    private static class ViewHolder{
        TextView nameTextView;
        ImageView advancedOptionsMenuImageView, backgroundImageView;
        CardView placeLayout;
    }
}
