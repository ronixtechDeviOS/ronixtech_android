package com.ronixtech.ronixhome.fragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RoomsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RoomsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomsFragment extends Fragment {
    private static final String TAG = RoomsFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addMenu;
    FloatingActionButton addPlaceFab, addFloorFab, addRoomFab, addDeviceFab;

    TextView noRoomsTextView;

    GridView roomsGridView;
    RoomsGridAdapter roomAdapter;
    List<Room> rooms;

    public RoomsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RoomsFragment.
     */
    public static RoomsFragment newInstance(String param1, String param2) {
        RoomsFragment fragment = new RoomsFragment();
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
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        if(MySettings.getCurrentPlace() != null && MySettings.getCurrentFloor() != null){
            MainActivity.setActionBarTitle(MySettings.getCurrentPlace().getName() + " - " + MySettings.getCurrentFloor().getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.places), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        addMenu = view.findViewById(R.id.add_layout);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addFloorFab = view.findViewById(R.id.add_floor_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        roomsGridView = view.findViewById(R.id.rooms_gridview);
        noRoomsTextView = view.findViewById(R.id.no_rooms_textview);
        rooms = MySettings.getFloorRooms(MySettings.getCurrentFloor().getId());
        roomAdapter = new RoomsGridAdapter(getActivity(), rooms);
        roomsGridView.setAdapter(roomAdapter);

        if(rooms != null && rooms.size() >= 1){
            noRoomsTextView.setVisibility(View.GONE);
        }else{
            noRoomsTextView.setVisibility(View.VISIBLE);
        }

        roomsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Room selectedRoom = (Room) roomAdapter.getItem(i);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                DashboardDevicesFragment dashboardDevicesFragment = new DashboardDevicesFragment();
                dashboardDevicesFragment.setRoom(selectedRoom);
                fragmentTransaction.replace(R.id.fragment_view, dashboardDevicesFragment, "dashboardDevicesFragment");
                fragmentTransaction.addToBackStack("dashboardDevicesFragment");
                fragmentTransaction.commit();
            }
        });

        roomsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Room selectedRoom = (Room) roomAdapter.getItem(i);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure you want to delete the selected room?")
                        .setMessage("Deleting the room will also delete all associated devices")
                        //set positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                MySettings.removeRoom(selectedRoom);
                                rooms.clear();
                                rooms.addAll(MySettings.getAllRooms());
                                roomAdapter.notifyDataSetChanged();
                            }
                        })
                        //set negative button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();
                return false;
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
        addFloorFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddFloorFragment addFloorFragment = new AddFloorFragment();
                addFloorFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
                fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
                fragmentTransaction.addToBackStack("addFloorFragment");
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
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }
        });


        return view;
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
