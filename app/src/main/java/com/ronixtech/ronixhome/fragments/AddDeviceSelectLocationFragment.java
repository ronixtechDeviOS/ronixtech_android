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
import android.widget.ListView;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.FloorAdapter;
import com.ronixtech.ronixhome.adapters.RoomAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceSelectLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceSelectLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceSelectLocationFragment extends Fragment {
    private static final String TAG = AddDeviceSelectLocationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ListView floorsListView, roomsListView;
    List<Floor> floors;
    List<Room> rooms;
    FloorAdapter floorAdapter;
    RoomAdapter roomAdapter;
    Button doneButton;
    TextView noRoomsTextView, noFloorsTextView, selectFloorFirstTextiew;
    Button addRoomButton, addFloorButton;

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

        floorsListView = view.findViewById(R.id.floors_listview);
        roomsListView = view.findViewById(R.id.rooms_listview);
        doneButton = view.findViewById(R.id.done_button);
        noRoomsTextView = view.findViewById(R.id.no_rooms_textview);
        addRoomButton = view.findViewById(R.id.add_new_room_button);
        noFloorsTextView = view.findViewById(R.id.no_floors_textview);
        addFloorButton = view.findViewById(R.id.add_new_floor_button);
        selectFloorFirstTextiew = view.findViewById(R.id.select_floor_first_textview);

        addFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddFloorFragment addFloorFragment = new AddFloorFragment();
                addFloorFragment.setSource(Constants.SOURCE_NEW_DEVICE);
                fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
                fragmentTransaction.addToBackStack("addFloorFragment");
                fragmentTransaction.commit();
            }
        });
        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddRoomFragment addRoomFragment = new AddRoomFragment();
                addRoomFragment.setSource(Constants.SOURCE_NEW_DEVICE);
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });

        if(MySettings.getAllFloors() == null || MySettings.getAllFloors().size() < 1){
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
        }


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
