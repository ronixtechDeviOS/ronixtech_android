package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.RoomsGridAdapter;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddRoomFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRoomFragment extends Fragment implements PickFloorDialogFragment.OnFloorSelectedListener{
    private static final  String TAG = AddRoomFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addMenu;
    FloatingActionButton addPlaceFab, addFloorFab, addRoomFab, addDeviceFab;

    EditText roomNameEditText, roomLocationEditText;
    Button addRoomButton;
    TextView addRoomDescriptionTextView, floorLevelDescriptionTextView;
    LinearLayout roomInfoLayout;
    TextView noRoomsTextView;

    GridView roomsGridView;
    RoomsGridAdapter roomAdapter;
    List<Room> rooms;

    TextView roomsTitleTextView;

    Floor selectedFloor;

    int source = Constants.SOURCE_HOME_FRAGMENT;

    public AddRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddRoomFragment.
     */
    public static AddRoomFragment newInstance(String param1, String param2) {
        AddRoomFragment fragment = new AddRoomFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_room, container, false);
        if(source != Constants.SOURCE_NAV_DRAWER){
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_room), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.rooms), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        addMenu = view.findViewById(R.id.add_layout);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addFloorFab = view.findViewById(R.id.add_floor_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        addRoomDescriptionTextView = view.findViewById(R.id.add_room_description_textview);
        floorLevelDescriptionTextView = view.findViewById(R.id.floor_level_decsription_textview);
        roomInfoLayout = view.findViewById(R.id.room_info_layout);
        roomNameEditText = view.findViewById(R.id.room_name_edittedxt);
        roomLocationEditText = view.findViewById(R.id.room_floor_edittext);
        addRoomButton = view.findViewById(R.id.add_room_button);
        noRoomsTextView = view.findViewById(R.id.no_rooms_textview);
        roomsGridView = view.findViewById(R.id.rooms_gridview);
        roomsTitleTextView = view.findViewById(R.id.rooms_listview_title_textview);
        if(MySettings.getCurrentFloor() != null){
            rooms = MySettings.getFloor(MySettings.getCurrentFloor().getId()).getRooms();
            roomsTitleTextView.setText(MySettings.getCurrentFloor().getName()+":");
        }else {
            rooms = MySettings.getAllRooms();
        }
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
                Room clickedRoom = (Room) roomAdapter.getItem(i);

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

        roomLocationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickFloorDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    PickFloorDialogFragment fragment = PickFloorDialogFragment.newInstance();
                    fragment.setTargetFragment(AddRoomFragment.this, 0);
                    fragment.show(ft, "pickFloorDialogFragment");
                }
            }
        });
        roomLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.  We also want to remove any currently showing
                // dialog, so make our own transaction and take care of that here.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickFloorDialogFragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                PickFloorDialogFragment fragment = PickFloorDialogFragment.newInstance();
                fragment.setTargetFragment(AddRoomFragment.this, 0);
                fragment.show(ft, "pickFloorDialogFragment");
            }
        });

        roomNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(roomNameEditText)){
                    if(selectedFloor != null){
                        Utils.setButtonEnabled(addRoomButton, true);
                    }
                }else{
                    Utils.setButtonEnabled(addRoomButton, false);
                }
            }
        });

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.validateInputs(roomNameEditText)){
                    if(selectedFloor != null){
                        Room room = new Room();
                        //room.setId(Long.valueOf(roomLocationEditText.getText().toString()));
                        room.setName(roomNameEditText.getText().toString());
                        room.setFloorID(selectedFloor.getId());
                        MySettings.addRoom(room);
                        rooms.clear();
                        rooms.addAll(MySettings.getAllRooms());
                        roomAdapter.notifyDataSetChanged();
                        roomNameEditText.setText("");
                        roomLocationEditText.setText("");
                        selectedFloor = null;
                        Utils.setButtonEnabled(addRoomButton, false);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(roomNameEditText.getWindowToken(), 0);
                        noRoomsTextView.setVisibility(View.GONE);
                        if(source == Constants.SOURCE_NEW_DEVICE){
                            getFragmentManager().popBackStack();
                        }else{
                            getFragmentManager().popBackStack();
                        }
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(roomLocationEditText);
                    }
                }
            }
        });

        addPlaceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
                addPlaceFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
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
                addRoomFragment.setSource(Constants.SOURCE_HOME_FRAGMENT);
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

        if(source == Constants.SOURCE_NAV_DRAWER){
            //hide adding room layout & views
            addRoomDescriptionTextView.setVisibility(View.GONE);
            floorLevelDescriptionTextView.setVisibility(View.GONE);
            roomInfoLayout.setVisibility(View.GONE);
            addRoomButton.setVisibility(View.GONE);
            addMenu.setVisibility(View.VISIBLE);
        }else{
            addMenu.setVisibility(View.GONE);
        }

        return view;
    }

    public void setSource(int source){
        this.source = source;
    }

    @Override
    public void onFloorSelected(Floor floor){
        this.selectedFloor = floor;
        roomLocationEditText.setText(selectedFloor.getName());
        if(Utils.validateInputsWithoutYoyo(roomNameEditText)){
            Utils.setButtonEnabled(addRoomButton, true);
        }
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
