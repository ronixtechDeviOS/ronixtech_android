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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardRoomsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardRoomsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DashboardRoomsFragment extends Fragment {
    private static final String TAG = DashboardRoomsFragment.class.getSimpleName();

    FloatingActionMenu addFabMenu;
    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;
    Button addPlaceButton, addRoomButton, addDeviceButton;

    LinearLayout addLayout;
    RelativeLayout addPlaceLayout, addRoomLayout, addDeviceLayout;
    GridView roomsGridView;
    RoomsGridAdapter adapter;
    List<Room> rooms;
    TextView emptyTextView;


    Place place;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardRoomsFragment.
     */
    public static DashboardRoomsFragment newInstance(String param1, String param2) {
        DashboardRoomsFragment fragment = new DashboardRoomsFragment();
        return fragment;
    }
    public DashboardRoomsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard_rooms, container, false);
        if(place != null){
            MainActivity.setActionBarTitle(place.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.home), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        addLayout = view.findViewById(R.id.add_layout);
        addPlaceLayout = view.findViewById(R.id.add_new_place_layout);
        addRoomLayout = view.findViewById(R.id.add_new_room_layout);
        addDeviceLayout = view.findViewById(R.id.add_new_device_layout);

        addFabMenu = view.findViewById(R.id.add_fab_menu);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        emptyTextView = view.findViewById(R.id.empty_textview);
        addPlaceButton = view.findViewById(R.id.add_place_button);
        addRoomButton = view.findViewById(R.id.add_room_button);
        addDeviceButton = view.findViewById(R.id.add_device_button);

        if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
            List<Device> devices = MySettings.getAllDevices();
            if(devices != null && devices.size() >= 1){
                for (Device device:devices) {
                    if(device.getRoomID() == -1){
                        MySettings.removeDevice(device);
                    }
                }
            }

        }

        rooms = new ArrayList<>();
        if(MySettings.getAllRooms() != null && MySettings.getAllRooms().size() >= 1){
            rooms.addAll(MySettings.getAllRooms());
        }
        roomsGridView = view.findViewById(R.id.rooms_gridview);
        adapter = new RoomsGridAdapter(getActivity(), rooms);
        roomsGridView.setAdapter(adapter);

        boolean showAddLayout = false;
        if(MySettings.getAllPlaces() != null && MySettings.getAllPlaces().size() >= 1){
            //addPlaceLayout.setVisibility(View.GONE);
        }else{
            //addPlaceLayout.setVisibility(View.VISIBLE);
            showAddLayout = true;
        }
        if(MySettings.getAllRooms() != null && MySettings.getAllRooms().size() >= 1){
            //addRoomLayout.setVisibility(View.GONE);
        }else{
            //addRoomLayout.setVisibility(View.VISIBLE);
            showAddLayout = true;
        }
        if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
            //addDeviceLayout.setVisibility(View.GONE);
        }else{
            //addDeviceLayout.setVisibility(View.VISIBLE);
            showAddLayout = true;
        }

        if(showAddLayout){
            addLayout.setVisibility(View.VISIBLE);
            addFabMenu.setVisibility(View.GONE);
            roomsGridView.setVisibility(View.GONE);
        }else{
            addLayout.setVisibility(View.GONE);
            addFabMenu.setVisibility(View.VISIBLE);
            roomsGridView.setVisibility(View.VISIBLE);
        }

        /*if(MySettings.getAllPlaces() != null && MySettings.getAllPlaces().size() >= 1){
            addPlaceButton.setVisibility(View.GONE);
            if(MySettings.getAllFloors() != null && MySettings.getAllFloors().size() >= 1){
                addFloorButton.setVisibility(View.GONE);
                if(MySettings.getAllRooms() != null && MySettings.getAllRooms().size() >= 1){
                    addRoomButton.setVisibility(View.GONE);
                *//*if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1) {
                    addDeviceButton.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                }else{
                    emptyTextView.setText("You don't have any RonixTech smart controllers added yet.\nAdd a unit by clicking the button below.");
                    emptyTextView.setVisibility(View.VISIBLE);
                    addDeviceButton.setVisibility(View.VISIBLE);
                }*//*
                    addDeviceButton.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                }else{
                    emptyTextView.setText("You don't have any rooms added yet.\nAdd a room by clicking the button below.");
                    emptyTextView.setVisibility(View.VISIBLE);
                    addRoomButton.setVisibility(View.VISIBLE);
                    addDeviceButton.setVisibility(View.GONE);
                }
            }else{
                emptyTextView.setText("You don't have any floors added yet.\nAdd a floor by clicking the button below.");
                emptyTextView.setVisibility(View.VISIBLE);
                addPlaceButton.setVisibility(View.GONE);
                addFloorButton.setVisibility(View.VISIBLE);
                addRoomButton.setVisibility(View.GONE);
                addDeviceButton.setVisibility(View.GONE);
            }
        }else{
            emptyTextView.setText("You don't have any places added yet.\nAdd a place by clicking the button below.");
            emptyTextView.setVisibility(View.VISIBLE);
            addPlaceButton.setVisibility(View.VISIBLE);
            addFloorButton.setVisibility(View.GONE);
            addRoomButton.setVisibility(View.GONE);
            addDeviceButton.setVisibility(View.GONE);
        }*/

        roomsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Room clickedRoom = (Room) adapter.getItem(i);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                DashboardDevicesFragment dashboardDevicesFragment = new DashboardDevicesFragment();
                dashboardDevicesFragment.setRoom(clickedRoom);
                fragmentTransaction.replace(R.id.fragment_view, dashboardDevicesFragment, "dashboardDevicesFragment");
                fragmentTransaction.addToBackStack("dashboardDevicesFragment");
                fragmentTransaction.commit();
            }
        });

        addPlaceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });
        addRoomFab.setOnClickListener(new View.OnClickListener() {
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
        });
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
                }else {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }
            }
        });

        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });
        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_place_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddRoomFragment addRoomFragment = new AddRoomFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                    fragmentTransaction.addToBackStack("addRoomFragment");
                    fragmentTransaction.commit();
                }
            }
        });
        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }
            }
        });

        addPlaceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
                fragmentTransaction.addToBackStack("addPlaceFragment");
                fragmentTransaction.commit();
            }
        });
        addRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_place_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddRoomFragment addRoomFragment = new AddRoomFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                    fragmentTransaction.addToBackStack("addRoomFragment");
                    fragmentTransaction.commit();
                }
            }
        });
        addDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_room_first), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
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
