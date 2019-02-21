package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.FloorAdapterEditable;
import com.ronixtech.ronixhome.adapters.WifiNetworkItemAdapterEditable;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Type;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditPlaceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditPlaceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditPlaceFragment extends android.support.v4.app.Fragment implements TypePickerDialogFragment.OnTypeSelectedListener,
        PickWifiNetworkDialogFragment.OnNetworkSelectedListener,
        WifiInfoFragment.OnNetworkAddedListener{
    private static final String TAg = EditPlaceFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    TextView editPlaceTitleTextView, editPlaceDescriptionTextView;
    EditText placeNameEditText;
    RelativeLayout placeTypeSelectionLayout, wifiNetworkSelectionLayout;
    ImageView placeTypeImageView;
    TextView placeTypeNameTextView;
    ListView selectedWifiNetworksListView, placeFloorsListView;
    WifiNetworkItemAdapterEditable selectedWifiNetworksAdapter;
    List<Floor> placeFloors;
    FloorAdapterEditable placeFloorsAdapter;
    RelativeLayout editPlaceLocationLayout;
    CheckBox defaultPlaceCheckBox;
    Button savePlaceButton;

    Type selectedPlaceType;
    List<WifiNetwork> selectedWifiNetworks;

    private Place place;

    public EditPlaceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditPlaceFragment.
     */
    public static EditPlaceFragment newInstance(String param1, String param2) {
        EditPlaceFragment fragment = new EditPlaceFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_place, container, false);
        place = MySettings.getCurrentPlace();
        if(place != null){
            MainActivity.setActionBarTitle(place.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.edit_place), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        editPlaceTitleTextView = view.findViewById(R.id.edit_place_title_textview);
        editPlaceDescriptionTextView = view.findViewById(R.id.edit_place_description_textview);
        placeNameEditText= view.findViewById(R.id.place_name_edittedxt);
        placeTypeSelectionLayout = view.findViewById(R.id.place_type_selection_layout);
        placeTypeImageView = view.findViewById(R.id.type_imageview);
        placeTypeNameTextView = view.findViewById(R.id.type_name_textview);
        editPlaceLocationLayout = view.findViewById(R.id.place_address_edit_layout);
        defaultPlaceCheckBox = view.findViewById(R.id.default_place_checkbox);
        wifiNetworkSelectionLayout = view.findViewById(R.id.wifi_network_selection_layout);

        selectedWifiNetworksListView = view.findViewById(R.id.selected_wifi_networks_listview);
        if(selectedWifiNetworks == null) {
            selectedWifiNetworks = new ArrayList<>();
        }
        selectedWifiNetworksAdapter = new WifiNetworkItemAdapterEditable(getActivity(), selectedWifiNetworks, new WifiNetworkItemAdapterEditable.WifiNetworksListener() {
            @Override
            public void onNetworkDeleted() {
                //selectedWifiNetworks.clear();
                //selectedWifiNetworks.addAll(MySettings.getPlaceWifiNetworks(place.getId()));
                selectedWifiNetworksAdapter.notifyDataSetChanged();
                Utils.justifyListViewHeightBasedOnChildren(selectedWifiNetworksListView);
            }
        }, Constants.REMOVE_NETWORK_FROM_DB_NO, Constants.COLOR_MODE_DARK_BACKGROUND);
        selectedWifiNetworksListView.setAdapter(selectedWifiNetworksAdapter);

        placeFloorsListView = view.findViewById(R.id.place_floors_listview);
        if(placeFloors == null) {
            placeFloors = new ArrayList<>();
        }
        placeFloorsAdapter = new FloorAdapterEditable(getActivity(), placeFloors, new FloorAdapterEditable.FloorsListener() {
            @Override
            public void onFloorDeleted() {
                MySettings.setCurrentFloor(null);
                MySettings.setCurrentRoom(null);
                //placeFloors.clear();
                //placeFloors.addAll(MySettings.getPlaceFloors(place.getId()));
                place.setFloors(placeFloors);
                placeFloorsAdapter.notifyDataSetChanged();
                Utils.justifyListViewHeightBasedOnChildren(placeFloorsListView);
            }
        });
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_floor_footer, null, false);
        placeFloorsListView.addFooterView(footerView, null, true);
        placeFloorsListView.setAdapter(placeFloorsAdapter);

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastLevel = 0;
                for (Floor floor : placeFloors) {
                    lastLevel = floor.getLevel();
                }
                Floor floor = new Floor();
                floor.setId(-1);
                int x = lastLevel+1;
                if(x == 1){
                    floor.setName("Ground Floor");
                }else if(x == 2){
                    floor.setName("First Floor");
                }else if(x == 3){
                    floor.setName("Second Floor");
                }else if(x == 4){
                    floor.setName("Third Floor");
                }else if(x == 5){
                    floor.setName("Forth Floor");
                }else if(x == 6){
                    floor.setName("Fifth Floor");
                }else if(x == 7){
                    floor.setName("Sixth Floor");
                }else if(x == 8){
                    floor.setName("Seventh Floor");
                }else if(x == 9){
                    floor.setName("Eighth Floor");
                }else if(x == 10){
                    floor.setName("Ninth Floor");
                }else{
                    floor.setName("Floor #" + x);
                }
                floor.setLevel(lastLevel+1);
                floor.setPlaceID(place.getId());
                placeFloors.add(floor);
                placeFloorsAdapter.notifyDataSetChanged();
                Utils.justifyListViewHeightBasedOnChildren(placeFloorsListView);
            }
        });

        savePlaceButton = view.findViewById(R.id.save_place_button);

        if(place != null) {
            place = MySettings.getPlace(place.getId());
            placeNameEditText.setText(place.getName());
            selectedPlaceType = place.getType();
            placeTypeNameTextView.setText(selectedPlaceType.getName());
            if (selectedPlaceType.getImageUrl() != null && selectedPlaceType.getImageUrl().length() >= 1) {
                GlideApp.with(getActivity())
                        .load(selectedPlaceType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeTypeImageView);
            } else {
                if (selectedPlaceType.getImageResourceName() != null && selectedPlaceType.getImageResourceName().length() >= 1) {
                    placeTypeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlaceType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                } else {
                    placeTypeImageView.setImageResource(selectedPlaceType.getImageResourceID());
                }
            }

            if(placeFloors.size() < 1){
                placeFloors.addAll(place.getFloors());
            }
            placeFloorsAdapter.notifyDataSetChanged();
            Utils.justifyListViewHeightBasedOnChildren(placeFloorsListView);

            if(selectedWifiNetworks.size() < 1) {
                selectedWifiNetworks.addAll(MySettings.getPlaceWifiNetworks(place.getId()));
            }
            selectedWifiNetworksAdapter.notifyDataSetChanged();
            Utils.justifyListViewHeightBasedOnChildren(selectedWifiNetworksListView);

            if(MySettings.getDefaultPlaceID() == place.getId()){
                defaultPlaceCheckBox.setChecked(true);
            }else{
                defaultPlaceCheckBox.setChecked(false);
            }
        }

        placeNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(validateInputs()){
                    Utils.setButtonEnabled(savePlaceButton, true);
                }else{
                    Utils.setButtonEnabled(savePlaceButton, false);
                }
            }
        });

        placeTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_PLACE) != null && MySettings.getTypes(Constants.TYPE_PLACE).size() >= 1){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TypePickerDialogFragment fragment = TypePickerDialogFragment.newInstance();
                    fragment.setTypesCategory(Constants.TYPE_PLACE);
                    fragment.setTargetFragment(EditPlaceFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.no_types_available), true);
                    Utils.generatePlaceTypes();
                }

            }
        });

        wifiNetworkSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllWifiNetworks() != null && MySettings.getAllWifiNetworks().size() >= 1){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("wifiNetworkPickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    PickWifiNetworkDialogFragment fragment = PickWifiNetworkDialogFragment.newInstance();
                    fragment.setTargetFragment(EditPlaceFragment.this, 0);
                    fragment.setParentFragment(EditPlaceFragment.this);
                    fragment.show(ft, "wifiNetworkPickerDialogFragment");
                }else{
                    //go to add wifi network sequence and then come back here
                    android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    WifiInfoFragment wifiInfoFragment = new WifiInfoFragment();
                    wifiInfoFragment.setSource(Constants.SOURCE_NEW_PLACE);
                    wifiInfoFragment.setTargetFragment(EditPlaceFragment.this, 0);
                    fragmentTransaction.replace(R.id.fragment_view, wifiInfoFragment, "wifiInfoFragment");
                    fragmentTransaction.addToBackStack("wifiInfoFragment");
                    fragmentTransaction.commit();
                }
            }
        });

        editPlaceLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputs()){
                    Place oldPlace = MySettings.getPlaceByName(placeNameEditText.getText().toString());
                    if(oldPlace != null && oldPlace.getId() != place.getId()){
                        placeNameEditText.setError(Utils.getString(getActivity(), R.string.place_already_exists_error));
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(placeNameEditText);
                    }else{
                        MySettings.updatePlaceName(place, placeNameEditText.getText().toString());
                        MySettings.updatePlaceType(place, selectedPlaceType);
                        if(placeFloors != null && placeFloors.size() >= 1){
                            for (Floor floor : placeFloors) {
                                if(floor.getId() == -1){
                                    floor.setId(0);
                                    MySettings.addFloor(floor);
                                }else {
                                    MySettings.updateFloorName(floor, floor.getName());
                                }
                            }
                        }
                        place = MySettings.getPlace(place.getId());
                        MySettings.setCurrentPlace(place);

                        for (WifiNetwork network : selectedWifiNetworks) {
                            network.setPlaceID(place.getId());
                            MySettings.addWifiNetwork(network);
                            MySettings.updateWifiNetworkPlace(network, place.getId());
                        }

                        if(defaultPlaceCheckBox.isChecked()){
                            MySettings.setDefaultPlaceID(place.getId());
                        }

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(placeNameEditText.getWindowToken(), 0);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        AddPlaceLocationFragment addPlaceLocationFragment = new AddPlaceLocationFragment();
                        fragmentTransaction.replace(R.id.fragment_view, addPlaceLocationFragment, "addPlaceLocationFragment");
                        fragmentTransaction.addToBackStack("addPlaceLocationFragment");
                        fragmentTransaction.commit();
                    }
                }
            }
        });

        savePlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    Place oldPlace = MySettings.getPlaceByName(placeNameEditText.getText().toString());
                    if(oldPlace != null && oldPlace.getId() != place.getId()){
                        placeNameEditText.setError(Utils.getString(getActivity(), R.string.place_already_exists_error));
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(placeNameEditText);
                    }else{
                        MySettings.updatePlaceName(place, placeNameEditText.getText().toString());
                        MySettings.updatePlaceType(place, selectedPlaceType);
                        if(placeFloors != null && placeFloors.size() >= 1){
                            for (Floor floor : placeFloors) {
                                if(floor.getId() == -1){
                                    floor.setId(0);
                                    MySettings.addFloor(floor);
                                }else {
                                    MySettings.updateFloorName(floor, floor.getName());
                                }
                            }
                        }
                        place = MySettings.getPlace(place.getId());
                        MySettings.setCurrentPlace(place);

                        for (WifiNetwork network : selectedWifiNetworks) {
                            network.setPlaceID(place.getId());
                            MySettings.addWifiNetwork(network);
                            MySettings.updateWifiNetworkPlace(network, place.getId());
                        }

                        if(defaultPlaceCheckBox.isChecked()){
                            MySettings.setDefaultPlaceID(place.getId());
                        }

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(placeNameEditText.getWindowToken(), 0);

                        getFragmentManager().popBackStack();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onTypeSelected(Type type){
        if(type != null){
            selectedPlaceType = type;
            placeTypeNameTextView.setText(selectedPlaceType.getName());
            if(selectedPlaceType.getImageUrl() != null && selectedPlaceType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlaceType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeTypeImageView);
            }else {
                if(selectedPlaceType.getImageResourceName() != null && selectedPlaceType.getImageResourceName().length() >= 1) {
                    placeTypeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlaceType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeTypeImageView.setImageResource(selectedPlaceType.getImageResourceID());
                }
            }
            if(validateInputs()){
                Utils.setButtonEnabled(savePlaceButton, true);
            }else{
                Utils.setButtonEnabled(savePlaceButton, false);
            }
        }
    }

    @Override
    public void onWifiNetworkSelected(WifiNetwork network){
        if(network != null){
            if(!selectedWifiNetworks.contains(network)){
                selectedWifiNetworks.add(network);
                selectedWifiNetworksAdapter.notifyDataSetChanged();
            }
            Utils.justifyListViewHeightBasedOnChildren(selectedWifiNetworksListView);
            if(validateInputs()){
                Utils.setButtonEnabled(savePlaceButton, true);
            }else{
                Utils.setButtonEnabled(savePlaceButton, false);
            }
        }
    }

    @Override
    public void onNetworkAdded(WifiNetwork wifiNetwork){
        if(wifiNetwork != null){
            if(!selectedWifiNetworks.contains(wifiNetwork)){
                selectedWifiNetworks.add(wifiNetwork);
                selectedWifiNetworksAdapter.notifyDataSetChanged();
            }
            Utils.justifyListViewHeightBasedOnChildren(selectedWifiNetworksListView);
            if(validateInputs()){
                Utils.setButtonEnabled(savePlaceButton, true);
            }else{
                Utils.setButtonEnabled(savePlaceButton, false);
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        if(!Utils.validateInputs(placeNameEditText)){
            inputsValid = false;
        }

        if(selectedPlaceType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(placeTypeSelectionLayout);
        }

        if(selectedWifiNetworks == null || selectedWifiNetworks.size() < 1){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(wifiNetworkSelectionLayout);
        }

        return inputsValid;
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
