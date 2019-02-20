package com.ronixtech.ronixhome.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.PlacesDashboardListAdapter;
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
public class PlacesFragment extends Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener{
    private static final String TAG = PlacesFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addFabMenu;
    FloatingActionButton addPlaceFab, addRoomFab, addDeviceFab;
    RelativeLayout addPlaceLayout;

    GridView placesGridView;
    PlacesGridAdapter placeAdapter;
    List<Place> places;
    TextView placesGridViewLongPressHint;

    ListView placesListView;
    PlacesDashboardListAdapter placesListAdapter;

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
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.places), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);


        addFabMenu = view.findViewById(R.id.add_fab_menu);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        placesGridViewLongPressHint = view.findViewById(R.id.places_gridview_long_press_hint_textview);

        addPlaceLayout = view.findViewById(R.id.add_new_place_layout);

        /*placesGridView = view.findViewById(R.id.places_gridview);
        places = MySettings.getAllPlaces();
        placeAdapter = new PlacesGridAdapter(getActivity(), places, getFragmentManager(), new PlacesGridAdapter.PlacesListener() {
            @Override
            public void onPlaceDeleted() {
                MySettings.setCurrentPlace(null);
                MySettings.setCurrentFloor(null);
                MySettings.setCurrentRoom(null);
                places.clear();
                places.addAll(MySettings.getAllPlaces());
                placeAdapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
            @Override
            public void onDefaultPlaceRequested() {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Utils.getString(getActivity(), R.string.select_default_place_title))
                        //set message
                        .setMessage(Utils.getString(getActivity(), R.string.select_default_place_message))
                        //set positive button
                        .setPositiveButton(Utils.getString(getActivity(), R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                if(MySettings.getAllPlaces() != null || MySettings.getAllPlaces().size() >= 1){
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
                                    fragment.setTargetFragment(PlacesFragment.this, 0);
                                    fragment.show(ft, "pickPlaceDialogFragment");
                                }
                            }
                        })
                        //set negative button
                        .setNegativeButton(Utils.getString(getActivity(), R.string.later), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();
            }
        });
        placesGridView.setAdapter(placeAdapter);*/


        placesListView = view.findViewById(R.id.places_listview);
        places = MySettings.getAllPlaces();
        placesListAdapter = new PlacesDashboardListAdapter(getActivity(), places, getFragmentManager(), new PlacesDashboardListAdapter.PlacesListener() {
            @Override
            public void onPlaceDeleted() {
                MySettings.setCurrentPlace(null);
                MySettings.setCurrentFloor(null);
                MySettings.setCurrentRoom(null);
                places.clear();
                places.addAll(MySettings.getAllPlaces());
                placesListAdapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
            @Override
            public void onDefaultPlaceRequested() {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Utils.getString(getActivity(), R.string.select_default_place_title))
                        //set message
                        .setMessage(Utils.getString(getActivity(), R.string.select_default_place_message))
                        //set positive button
                        .setPositiveButton(Utils.getString(getActivity(), R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                if(MySettings.getAllPlaces() != null || MySettings.getAllPlaces().size() >= 1){
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
                                    fragment.setTargetFragment(PlacesFragment.this, 0);
                                    fragment.show(ft, "pickPlaceDialogFragment");
                                }
                            }
                        })
                        //set negative button
                        .setNegativeButton(Utils.getString(getActivity(), R.string.later), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();
            }
        });
        placesListView.setAdapter(placesListAdapter);

        setLayoutVisibility();

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
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_place_first), true);
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
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
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

    private void setLayoutVisibility(){
        boolean showAddPlaceLayout = false;
        if(MySettings.getAllPlaces() == null || MySettings.getAllPlaces().size() < 1){
            showAddPlaceLayout = true;
        }

        if(showAddPlaceLayout){
            addPlaceLayout.setVisibility(View.VISIBLE);
        }else{
            addPlaceLayout.setVisibility(View.GONE);
        }

        if(showAddPlaceLayout){
            addFabMenu.setVisibility(View.GONE);
            //placesGridView.setVisibility(View.GONE);
            placesListView.setVisibility(View.GONE);
            placesGridViewLongPressHint.setVisibility(View.GONE);
        }else{
            //addFabMenu.setVisibility(View.VISIBLE);
            addFabMenu.setVisibility(View.GONE);
            //placesGridView.setVisibility(View.VISIBLE);
            placesListView.setVisibility(View.VISIBLE);
            //placesGridViewLongPressHint.setVisibility(View.VISIBLE);
            placesGridViewLongPressHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlaceSelected(Place place){
        if(place != null){
            MySettings.setDefaultPlaceID(place.getId());
        }
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
        inflater.inflate(R.menu.menu_places, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add) {
            /*// DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("additionDialogFragment");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            AddDialogFragment fragment = AddDialogFragment.newInstance();
            fragment.show(ft, "additionDialogFragment");*/
            //go to add place fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
            AddPlaceFragment addPlaceFragment = new AddPlaceFragment();
            fragmentTransaction.replace(R.id.fragment_view, addPlaceFragment, "addPlaceFragment");
            fragmentTransaction.addToBackStack("addPlaceFragment");
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
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
