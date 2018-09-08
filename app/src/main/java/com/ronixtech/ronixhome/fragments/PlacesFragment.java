package com.ronixtech.ronixhome.fragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.ronixtech.ronixhome.adapters.PlacesGridAdapter;
import com.ronixtech.ronixhome.entities.Place;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlacesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlacesFragment extends Fragment {
    private static final String TAG = PlacesFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addMenu;
    FloatingActionButton addPlaceFab, addFloorFab, addRoomFab, addDeviceFab;

    TextView noPlacesTextView;

    GridView placesGridView;
    PlacesGridAdapter placeAdapter;
    List<Place> places;

    public PlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlacesFragment.
     */
    public static PlacesFragment newInstance(String param1, String param2) {
        PlacesFragment fragment = new PlacesFragment();
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
        View view = inflater.inflate(R.layout.fragment_places, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.places), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);


        addMenu = view.findViewById(R.id.add_layout);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addFloorFab = view.findViewById(R.id.add_floor_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        placesGridView = view.findViewById(R.id.places_gridview);
        noPlacesTextView = view.findViewById(R.id.no_places_textview);
        places = MySettings.getAllPlaces();
        placeAdapter = new PlacesGridAdapter(getActivity(), places);
        placesGridView.setAdapter(placeAdapter);

        if(places != null && places.size() >= 1){
            noPlacesTextView.setVisibility(View.GONE);
        }else{
            noPlacesTextView.setVisibility(View.VISIBLE);
        }

        placesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Place selectedPlace = (Place) placeAdapter.getItem(i);
                MySettings.setCurrentPlace(selectedPlace);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddFloorFragment addFloorFragment = new AddFloorFragment();
                addFloorFragment.setSource(Constants.SOURCE_NAV_DRAWER);
                fragmentTransaction.replace(R.id.fragment_view, addFloorFragment, "addFloorFragment");
                fragmentTransaction.addToBackStack("addFloorFragment");
                fragmentTransaction.commit();
            }
        });

        placesGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Place selectedPlace = (Place) placeAdapter.getItem(i);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure you want to delete the selected place?")
                        .setMessage("Deleting the place will also delete all associated floors, rooms and devices")
                        //set positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                MySettings.removePlace(selectedPlace);
                                places.clear();
                                places.addAll(MySettings.getAllPlaces());
                                placeAdapter.notifyDataSetChanged();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
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
