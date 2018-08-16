package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.adapters.RoomAdapter;
import com.ronixtech.ronixhome.entities.Room;

import java.util.List;

//this is where you can pick a room
public class PickRoomDialogFragment extends DialogFragment{
    private static final String TAG = PickRoomDialogFragment.class.getSimpleName();
    private PickRoomDialogFragment.OnRoomSelectedListener callback;

    List<Room> rooms;
    RoomAdapter adapter;

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
        ListView listView = new ListView(getActivity());
        rooms = MySettings.getAllRooms();


        adapter = new RoomAdapter(getActivity(), rooms);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = (Room) adapter.getItem(position);
                callback.onRoomSelected(selectedRoom);
                dismiss();
            }
        });

        return listView;
    }
}
