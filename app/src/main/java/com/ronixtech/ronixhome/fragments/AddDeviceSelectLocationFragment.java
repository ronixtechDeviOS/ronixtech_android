package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceSelectLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceSelectLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceSelectLocationFragment extends Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener,
                            PickRoomDialogFragment.OnRoomSelectedListener{
    private static final String TAG = AddDeviceSelectLocationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout placeSelectionLayout, selectedFloorLayout, roomSelectionLayout;
    TextView placeNameTextView, roomNameTextView;
    ImageView placeImageView, roomImageView;
    TextView selectedFloorTextView;
    Button incrementFloorButton, decremetnFloorButton;

    Button doneButton;

    private Place selectedPlace;
    private Floor selectedFloor;
    private int selectedFloorIndex = 0;
    private Room selectedRoom;

    public AddDeviceSelectLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceSelectLocationFragment.
     */
    public static AddDeviceSelectLocationFragment newInstance(String param1, String param2) {
        AddDeviceSelectLocationFragment fragment = new AddDeviceSelectLocationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_device_select_location, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.locate_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

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

        doneButton = view.findViewById(R.id.done_button);

        if(MySettings.getCurrentPlace() != null){
            selectedPlace = MySettings.getPlace(MySettings.getCurrentPlace().getId());
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
            }

            if(MySettings.getCurrentFloor() != null){
                selectedFloor = MySettings.getFloor(MySettings.getCurrentFloor().getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getLevel());
            }else{
                selectedFloor = MySettings.getFloor(selectedPlace.getFloors().get(0).getId());
                selectedFloorIndex = selectedFloor.getLevel();
                selectedFloorTextView.setText(""+selectedFloor.getLevel());
            }
        }

        if(MySettings.getCurrentRoom() != null){
            selectedRoom = MySettings.getRoom(MySettings.getCurrentRoom().getId());
            roomNameTextView.setText(selectedRoom.getName());
            if(selectedRoom.getType().getImageUrl() != null && selectedRoom.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoom.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.room_type_living_room))
                        .into(roomImageView);
            }else {
                roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
            }
        }

        placeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
                fragment.show(ft, "pickPlaceDialogFragment");
            }
        });

        incrementFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex < selectedPlace.getFloors().size() - 1){
                        selectedFloorIndex++;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getLevel());
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
                        selectedFloorTextView.setText(""+selectedFloor.getLevel());
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
                        PickRoomDialogFragment fragment = PickRoomDialogFragment.newInstance();
                        fragment.setFloorID(selectedFloor.getId());
                        fragment.setTargetFragment(AddDeviceSelectLocationFragment.this, 0);
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

        if(validateInputs()){
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Device device = MySettings.getTempDevice();

                //Device tempDevice = MySettings.getDeviceByMAC(device.getMacAddress());
                //tempDevice.setRoomID(clickedRoom.getId());
                device.setRoomID(selectedRoom.getId());
                MySettings.addDevice(device);
                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                    Device tempDevice = MySettings.getTempDevice();
                    tempDevice = MySettings.getDeviceByMAC(tempDevice.getMacAddress(), tempDevice.getDeviceTypeID());
                    MySettings.setTempDevice(tempDevice);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    UpdateDeviceIntroFragment updateDeviceIntroFragment = new UpdateDeviceIntroFragment();
                    fragmentTransaction.replace(R.id.fragment_view, updateDeviceIntroFragment, "updateDeviceIntroFragment");
                    fragmentTransaction.addToBackStack("updateDeviceIntroFragment");
                    fragmentTransaction.commit();
                }else{
                    MySettings.setTempDevice(null);

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                    fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }
            }
        });
        /*addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddRoomFragment addRoomFragment = new AddRoomFragment();
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });*/

        /*if(MySettings.getAllFloors() == null || MySettings.getAllFloors().size() < 1){
            noFloorsTextView.setVisibility(View.VISIBLE);
            addFloorButton.setVisibility(View.VISIBLE);
        }else{
            floors = MySettings.getAllFloors();

            selectFloorFirstTextiew.setVisibility(View.GONE);
            noFloorsTextView.setVisibility(View.GONE);
            addFloorButton.setVisibility(View.VISIBLE);

            floorAdapter = new FloorAdapter(getActivity(), floors);
            floorsListView.setAdapter(floorAdapter);

            floorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Floor clickedFloor = (Floor) floorAdapter.getItem(i);
                    Floor clickedFloorWithRooms = MySettings.getFloor(clickedFloor.getId());
                    if(clickedFloorWithRooms.getRooms() == null || clickedFloorWithRooms.getRooms().size() < 1) {
                        noRoomsTextView.setVisibility(View.VISIBLE);
                        addRoomButton.setVisibility(View.VISIBLE);
                    }else{
                        noRoomsTextView.setVisibility(View.GONE);
                        addRoomButton.setVisibility(View.VISIBLE);
                        //add rooms to the rooms listview
                        rooms = clickedFloorWithRooms.getRooms();
                        roomAdapter = new RoomAdapter(getActivity(), rooms);
                        roomsListView.setAdapter(roomAdapter);

                        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Room clickedRoom = (Room) roomAdapter.getItem(i);
                                Utils.setButtonEnabled(doneButton, true);

                                doneButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Device device = MySettings.getTempDevice();

                                        //Device tempDevice = MySettings.getDeviceByMAC(device.getMacAddress());
                                        //tempDevice.setRoomID(clickedRoom.getId());
                                        device.setRoomID(clickedRoom.getId());
                                        MySettings.addDevice(device);
                                        MySettings.setTempDevice(null);

                                        FragmentManager fragmentManager = getFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                                        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                        fragmentTransaction.commit();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }*/


        return view;
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
                placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
            }
            selectedFloorIndex = 0;
            selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
            if(selectedFloor != null){
                selectedFloorTextView.setText(""+selectedFloor.getLevel());

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
                            roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
                        }
                    }
                }
            }
        }

        if(validateInputs()){
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
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
                roomImageView.setImageResource(selectedRoom.getType().getImageResourceID());
            }
        }
        if(validateInputs()){
            Utils.setButtonEnabled(doneButton, true);
        }else{
            Utils.setButtonEnabled(doneButton, false);
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;
        if(selectedPlace == null){
            inputsValid = false;
        }
        if(selectedFloor == null){
            inputsValid = false;
        }
        if(selectedRoom == null){
            inputsValid = false;
        }

        return inputsValid;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
