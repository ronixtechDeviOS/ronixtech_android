package com.ronixtech.ronixhome.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.LineAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

import java.util.ArrayList;
import java.util.List;

//this is where you pick a line
public class PickLineDialogFragment extends DialogFragment implements PickPlaceDialogFragment.OnPlaceSelectedListener,
        PickRoomDialogFragment.OnRoomSelectedListener{
    private static final String TAG = PickLineDialogFragment.class.getSimpleName();
    private PickLineDialogFragment.OnLineSelectedListener callback;

    ScrollView scrollView;
    TextView titleTextView;
    RelativeLayout placeSelectionLayout, selectedFloorLayout, roomSelectionLayout;
    TextView placeNameTextView, roomNameTextView;
    ImageView placeImageView, roomImageView;
    TextView selectedFloorTextView;
    Button incrementFloorButton, decremetnFloorButton;

    private Place selectedPlace;
    private Floor selectedFloor;
    private int selectedFloorIndex = 0;
    private Room selectedRoom;

    ListView linesListView;
    List<Line> lines;
    LineAdapter adapter;
    TextView noLinesTextView;

    public interface OnLineSelectedListener {
        public void onLineSelected(Line line);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickLineDialogFragment newInstance() {
        PickLineDialogFragment f = new PickLineDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickLineDialogFragment.OnLineSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnLineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_line_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        scrollView = view.findViewById(R.id.lines_picker_scroll_view);
        titleTextView = view.findViewById(R.id.pick_line_title_textview);
        placeSelectionLayout = view.findViewById(R.id.place_selection_layout);
        placeNameTextView = view.findViewById(R.id.selected_place_name_textview);
        placeImageView = view.findViewById(R.id.selected_place_image_view);
        selectedFloorLayout = view.findViewById(R.id.floor_layout);
        selectedFloorTextView = view.findViewById(R.id.selected_floor_textview);
        roomSelectionLayout = view.findViewById(R.id.room_selection_layout);
        roomNameTextView = view.findViewById(R.id.selected_room_name_textview);
        roomImageView = view.findViewById(R.id.selected_room_image_view);
        incrementFloorButton = view.findViewById(R.id.increment_button);
        decremetnFloorButton = view.findViewById(R.id.decrement_button);
        noLinesTextView = view.findViewById(R.id.no_lines_textview);

        linesListView = view.findViewById(R.id.lines_listview);


        if(selectedPlace == null){
            if(MySettings.getCurrentPlace() != null) {
                selectedPlace = MySettings.getPlace(MySettings.getCurrentPlace().getId());
            }
        }
        if(selectedPlace != null){
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1){
                    placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                }
            }

            if(MySettings.getCurrentFloor() != null){
                selectedFloor = MySettings.getFloor(MySettings.getCurrentFloor().getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getName());
            }else{
                selectedFloor = MySettings.getFloor(selectedPlace.getFloors().get(0).getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getName());
            }
        }

        if(selectedRoom == null){
            if(MySettings.getCurrentRoom() != null) {
                selectedRoom = MySettings.getRoom(MySettings.getCurrentRoom().getId());
                if(selectedRoom != null){
                    if(selectedRoom.getFloorID() != selectedFloor.getId()){
                        selectedRoom = null;
                        roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                        roomImageView.setImageResource(R.drawable.room_icon);
                    }
                }
            }
        }
        if(selectedRoom != null){
            roomNameTextView.setText(selectedRoom.getName());
            if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoom.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                        .into(roomImageView);
            }else {
                if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                    roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                }
            }
        }

        placeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_place_first), true);
                }else{
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickPlaceDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    PickPlaceDialogFragment fragment = PickPlaceDialogFragment.newInstance();
                    fragment.setTargetFragment(PickLineDialogFragment.this, 0);
                    fragment.setParentFragment(PickLineDialogFragment.this);
                    fragment.show(ft, "pickPlaceDialogFragment");
                }
            }
        });

        incrementFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex < selectedPlace.getFloors().size() - 1){
                        selectedFloorIndex++;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                        if(selectedRoom != null){
                            if(selectedRoom.getFloorID() != selectedFloor.getId()){
                                selectedRoom = null;
                                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                                roomImageView.setImageResource(R.drawable.room_icon);
                            }
                        }
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        decremetnFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex >= 1){
                        selectedFloorIndex--;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                        if(selectedRoom != null){
                            if(selectedRoom.getFloorID() != selectedFloor.getId()){
                                selectedRoom = null;
                                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                                roomImageView.setImageResource(R.drawable.room_icon);
                            }
                        }
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        roomSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloor != null){
                        /*if(MySettings.getFloorRooms(selectedFloor.getId()) == null || MySettings.getFloorRooms(selectedFloor.getId()).size() < 1){
                            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                        }else{
                            // DialogFragment.show() will take care of adding the fragment
                            // in a transaction.  We also want to remove any currently showing
                            // dialog, so make our own transaction and take care of that here.
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickRoomDialogFragment");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);

                            // Create and show the dialog.
                            PickRoomDialogFragment fragment = PickRoomDialogFragment.newInstance();
                            fragment.setFloorID(selectedFloor.getId());
                            fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                            fragment.setParentFragment(AddDeviceSelectLocationFragment.this);
                            fragment.show(ft, "pickRoomDialogFragment");
                        }*/
                        // DialogFragment.show() will take care of adding the fragment
                        // in a transaction.  We also want to remove any currently showing
                        // dialog, so make our own transaction and take care of that here.
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickRoomDialogFragment");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        PickRoomDialogFragment fragment = PickRoomDialogFragment.newInstance();
                        fragment.setFloorID(selectedFloor.getId());
                        fragment.setTargetFragment(PickLineDialogFragment.this, 0);
                        fragment.setParentFragment(PickLineDialogFragment.this);
                        fragment.show(ft, "pickRoomDialogFragment");
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(selectedFloorLayout);
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(placeSelectionLayout);
                }
            }
        });

        lines = new ArrayList<>();
        adapter = new LineAdapter(getActivity(), lines);
        linesListView.setAdapter(adapter);

        populateLines();

        linesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Line selectedLine = (Line) adapter.getItem(position);
                callback.onLineSelected(selectedLine);
                dismiss();
            }
        });

        return view;
    }

    private void populateLines(){
        lines.clear();
        if(selectedRoom != null){
            List<Device> devices = MySettings.getRoomDevices(selectedRoom.getId());
            for (Device device:devices) {
                if(device.getDeviceTypeID() != Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    if(device.getLines() != null && device.getLines().size() >= 1){
                        lines.addAll(device.getLines());
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        Utils.justifyListViewHeightBasedOnChildren(linesListView);
        if(lines.size() >= 1){
            noLinesTextView.setVisibility(View.GONE);
        }else{
            noLinesTextView.setVisibility(View.VISIBLE);
        }

        titleTextView.requestFocus();
        scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void onPlaceSelected(Place place){
        if(place != null){
            this.selectedPlace = MySettings.getPlace(place.getId());
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1){
                    placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                }
            }
            selectedFloorIndex = 0;
            selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
            if(selectedFloor != null){
                selectedFloorTextView.setText(""+selectedFloor.getName());
                if(MySettings.getFloorRooms(selectedFloor.getId()) != null && MySettings.getFloorRooms(selectedFloor.getId()).size() >= 1){
                    selectedRoom = MySettings.getRoom(MySettings.getFloorRooms(selectedFloor.getId()).get(0).getId());
                    if(selectedRoom != null){
                        roomNameTextView.setText(selectedRoom.getName());
                        if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                            GlideApp.with(getActivity())
                                    .load(selectedRoom.getType().getImageUrl())
                                    .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                                    .into(roomImageView);
                        }else {
                            if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                                roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                            }else{
                                roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                            }
                        }
                    }else{
                        selectedRoom = null;
                        roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                        roomImageView.setImageResource(R.drawable.room_icon);
                    }
                }else{
                    selectedRoom = null;
                    roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                    roomImageView.setImageResource(R.drawable.room_icon);
                }
            }else{
                selectedRoom = null;
                roomNameTextView.setText(Utils.getString(getActivity(), R.string.room_selection_hint));
                roomImageView.setImageResource(R.drawable.room_icon);
            }
            populateLines();
        }
    }

    @Override
    public void onRoomSelected(Room room){
        if(room != null){
            this.selectedRoom = MySettings.getRoom(room.getId());
            roomNameTextView.setText(selectedRoom.getName());
            if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoom.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                        .into(roomImageView);
            }else {
                if(selectedRoom.getType().getImageResourceName() != null && selectedRoom.getType().getImageResourceName().length() >= 1) {
                    roomImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoom.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                }
            }
            populateLines();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
