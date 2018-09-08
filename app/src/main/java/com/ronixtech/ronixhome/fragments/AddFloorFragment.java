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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.ronixtech.ronixhome.adapters.FloorsGridAdapter;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddFloorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddFloorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFloorFragment extends Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener{
    private static final String TAG = AddFloorFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionMenu addMenu;
    FloatingActionButton addPlaceFab, addFloorFab, addRoomFab, addDeviceFab;

    EditText floorNameEditText;
    Spinner floorLevelSpinner;
    Button addFloorButton;
    TextView addFloorDescriptionTextView;
    LinearLayout floorInfoLayout;
    TextView noFloorsTextView;
    RelativeLayout floorPlaceLayout;

    GridView floorsGridView;
    FloorsGridAdapter floorAdapter;
    List<Floor> floors;

    EditText selectedPlaceEditText;

    TextView floorsTitleTextView;

    int selectedLevel = -1;

    private Place selectedPlace;

    int source = Constants.SOURCE_HOME_FRAGMENT;

    public AddFloorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFloorFragment.
     */
    public static AddFloorFragment newInstance(String param1, String param2) {
        AddFloorFragment fragment = new AddFloorFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_floor, container, false);
        if(source != Constants.SOURCE_NAV_DRAWER) {
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_floor), getResources().getColor(R.color.whiteColor));
        }else{
            if(MySettings.getCurrentPlace() != null){
                MainActivity.setActionBarTitle(MySettings.getCurrentPlace().getName(), getResources().getColor(R.color.whiteColor));
            }else{
                MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.floors), getResources().getColor(R.color.whiteColor));
            }
        }
        setHasOptionsMenu(true);

        addMenu = view.findViewById(R.id.add_layout);
        addPlaceFab = view.findViewById(R.id.add_place_fab);
        addFloorFab = view.findViewById(R.id.add_floor_fab);
        addRoomFab = view.findViewById(R.id.add_room_fab);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        addFloorDescriptionTextView = view.findViewById(R.id.add_floor_description_textview);
        floorInfoLayout = view.findViewById(R.id.floor_info_layout);
        floorNameEditText = view.findViewById(R.id.floor_name_edittedxt);
        floorLevelSpinner = view.findViewById(R.id.floor_level_spinner);
        addFloorButton = view.findViewById(R.id.add_floor_button);
        floorsGridView = view.findViewById(R.id.floors_gridview);
        noFloorsTextView = view.findViewById(R.id.no_floors_textview);
        floorsTitleTextView = view.findViewById(R.id.floors_listview_title_textview);
        selectedPlaceEditText = view.findViewById(R.id.floor_place_edittext);
        floorPlaceLayout = view.findViewById(R.id.floor_place_layout);
        if(MySettings.getCurrentPlace() != null){
            if(MySettings.getCurrentPlace() != null){
                floors = MySettings.getPlace(MySettings.getCurrentPlace().getId()).getFloors();
                floorsTitleTextView.setText(MySettings.getCurrentPlace().getName()+":");
            }
        }else {
            floors = MySettings.getAllFloors();
        }
        floorAdapter = new FloorsGridAdapter(getActivity(), floors);
        floorsGridView.setAdapter(floorAdapter);

        if(floors != null && floors.size() >= 1){
            noFloorsTextView.setVisibility(View.GONE);
        }else{
            noFloorsTextView.setVisibility(View.VISIBLE);
        }

        floorsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Floor selectedFloor= (Floor) floorAdapter.getItem(i);
                MySettings.setCurrentFloor(selectedFloor);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                RoomsFragment roomsFragment = new RoomsFragment();
                fragmentTransaction.replace(R.id.fragment_view, roomsFragment, "roomsFragment");
                fragmentTransaction.addToBackStack("roomsFragment");
                fragmentTransaction.commit();
            }
        });

        floorsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Floor selectedFloor = (Floor) floorAdapter.getItem(i);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure you want to delete the selected floor?")
                        .setMessage("Deleting the floor will also delete all associated rooms and devices")
                        //set positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                MySettings.removeFloor(selectedFloor);
                                floors.clear();
                                floors.addAll(MySettings.getAllFloors());
                                floorAdapter.notifyDataSetChanged();
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

        List<Integer> floorLevels = new ArrayList<>();
        for(int x = 1; x <= Floor.MAX_NUMBER; x++){
            floorLevels.add(x);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_item_floor_level, floorLevels);
        floorLevelSpinner.setAdapter(adapter);

        floorLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLevel = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        floorNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(floorNameEditText)){
                    if(selectedLevel != -1){
                        Utils.setButtonEnabled(addFloorButton, true);
                    }
                }else{
                    Utils.setButtonEnabled(addFloorButton, false);
                }
            }
        });


        selectedPlaceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
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
                    fragment.setTargetFragment(AddFloorFragment.this, 0);
                    fragment.show(ft, "pickPlaceDialogFragment");
                }
            }
        });
        selectedPlaceEditText.setOnClickListener(new View.OnClickListener() {
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
                fragment.setTargetFragment(AddFloorFragment.this, 0);
                fragment.show(ft, "pickPlaceDialogFragment");
            }
        });

        addFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedLevel != -1){
                    if(selectedPlace != null){
                        Floor floor = new Floor();
                        floor.setName(floorNameEditText.getText().toString());
                        floor.setLevel(selectedLevel);
                        floor.setPlaceID(selectedPlace.getId());
                        MySettings.addFloor(floor);
                        floors.clear();
                        floors.addAll(MySettings.getAllFloors());
                        floorAdapter.notifyDataSetChanged();
                        floorNameEditText.setText("");
                        floorLevelSpinner.setAdapter(adapter);
                        selectedLevel = -1;
                        selectedPlace = null;
                        selectedPlaceEditText.setText("");
                        Utils.setButtonEnabled(addFloorButton, false);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(floorNameEditText.getWindowToken(), 0);
                        noFloorsTextView.setVisibility(View.GONE);
                        if(source == Constants.SOURCE_NEW_DEVICE){
                            getFragmentManager().popBackStack();
                        }else{
                            getFragmentManager().popBackStack();
                        }
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(selectedPlaceEditText);
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(floorLevelSpinner);
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

        if(source == Constants.SOURCE_NAV_DRAWER){
            //hide adding floor layout & views
            addFloorDescriptionTextView.setVisibility(View.GONE);
            floorInfoLayout.setVisibility(View.GONE);
            floorPlaceLayout.setVisibility(View.GONE);
            floorNameEditText.setVisibility(View.GONE);
            addFloorButton.setVisibility(View.GONE);
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
    public void onPlaceSelected(Place place){
        this.selectedPlace = place;
        selectedPlaceEditText.setText(selectedPlace.getName());
        if(selectedLevel != -1){
            Utils.setButtonEnabled(addFloorButton, true);
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