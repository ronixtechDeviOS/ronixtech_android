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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.FloorsGridAdapter;
import com.ronixtech.ronixhome.entities.Floor;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FloorsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FloorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FloorsFragment extends Fragment{
    private static final String TAG = FloorsFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addMenu;
    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;

    TextView noFloorsTextView;

    GridView floorsGridView;
    FloorsGridAdapter floorAdapter;
    List<Floor> floors;
    TextView floorsGirdViewLongPressHint;

    int source = Constants.SOURCE_HOME_FRAGMENT;

    public FloorsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FloorsFragment.
     */
    public static FloorsFragment newInstance(String param1, String param2) {
        FloorsFragment fragment = new FloorsFragment();
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
        View view = inflater.inflate(R.layout.fragment_floors, container, false);
        if(MySettings.getCurrentPlace() != null){
            MainActivity.setActionBarTitle(MySettings.getCurrentPlace().getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.floors), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        addMenu = view.findViewById(R.id.add_fab_menu);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        floorsGirdViewLongPressHint = view.findViewById(R.id.floors_gridview_long_press_hint_textview);

        floorsGridView = view.findViewById(R.id.floors_gridview);
        noFloorsTextView = view.findViewById(R.id.no_floors_textview);
        if(MySettings.getCurrentPlace() != null){
            floors = MySettings.getPlace(MySettings.getCurrentPlace().getId()).getFloors();
        }else {
            floors = MySettings.getAllFloors();
        }
        floorAdapter = new FloorsGridAdapter(getActivity(), floors, getFragmentManager(), new FloorsGridAdapter.FloorsListener() {
            @Override
            public void onFloorDeleted() {
                MySettings.setCurrentFloor(null);
                MySettings.setCurrentRoom(null);

                floors.clear();
                if(MySettings.getCurrentPlace() != null){
                    floors.addAll(MySettings.getPlace(MySettings.getCurrentPlace().getId()).getFloors());
                }else {
                    floors.addAll(MySettings.getAllFloors());
                }
                floorAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFloorNameChanged() {
                floors.clear();
                if(MySettings.getCurrentPlace() != null){
                    floors.addAll(MySettings.getPlace(MySettings.getCurrentPlace().getId()).getFloors());
                }else {
                    floors.addAll(MySettings.getAllFloors());
                }
                floorAdapter.notifyDataSetChanged();
            }
        });
        floorsGridView.setAdapter(floorAdapter);

        if(floors != null && floors.size() >= 1){
            noFloorsTextView.setVisibility(View.GONE);
            floorsGirdViewLongPressHint.setVisibility(View.GONE);
        }else{
            noFloorsTextView.setVisibility(View.VISIBLE);
            //floorsGirdViewLongPressHint.setVisibility(View.VISIBLE);
            floorsGirdViewLongPressHint.setVisibility(View.GONE);
        }

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
        addDeviceFab.setOnClickListener(new View.OnClickListener() {
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