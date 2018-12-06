package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.RoomAdapter;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

//this is where you can pick a room
public class PickRoomDialogFragment extends DialogFragment{
    private static final String TAG = PickRoomDialogFragment.class.getSimpleName();
    private PickRoomDialogFragment.OnRoomSelectedListener callback;

    List<Room> rooms;
    RoomAdapter adapter;

    long floorID;

    private Fragment parentFragment;

    public interface OnRoomSelectedListener {
        public void onRoomSelected(Room room);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickRoomDialogFragment newInstance() {
        PickRoomDialogFragment f = new PickRoomDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (PickRoomDialogFragment.OnRoomSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnRoomSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_room_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        rooms = MySettings.getFloorRooms(floorID);

        adapter = new RoomAdapter(getActivity(), rooms);
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_room_footer, null, false);
        listView.addFooterView(footerView, null, false);
        listView.setAdapter(adapter);
        listView.setDivider(null);

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //go to add room fragment and then come back here
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddRoomFragment addRoomFragment = new AddRoomFragment();
                addRoomFragment.setTargetFragment(parentFragment, 0);
                fragmentTransaction.replace(R.id.fragment_view, addRoomFragment, "addRoomFragment");
                fragmentTransaction.addToBackStack("addRoomFragment");
                fragmentTransaction.commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = (Room) adapter.getItem(position);
                MySettings.setCurrentRoom(selectedRoom);
                callback.onRoomSelected(selectedRoom);
                dismiss();
            }
        });

        return listView;
    }

    public void setFloorID(long floorID){
        this.floorID = floorID;
    }

    public void setParentFragment(Fragment fragment){
        this.parentFragment = fragment;
    }
}
