package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.WifiNetwork;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPlaceLocationAddressFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPlaceLocationAddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPlaceLocationAddressFragment extends android.support.v4.app.Fragment {
    private static final String TAG = AddPlaceLocationAddressFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    EditText addressEditText, cityEditText, stateEditText, countryEditText, zipCodeEditText;
    Button doneButton;

    Place place;

    public AddPlaceLocationAddressFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPlaceLocationAddressFragment.
     */
    public static AddPlaceLocationAddressFragment newInstance(String param1, String param2) {
        AddPlaceLocationAddressFragment fragment = new AddPlaceLocationAddressFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_place_location_address, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_place_location), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        addressEditText = view.findViewById(R.id.place_location_address_edittext);
        cityEditText = view.findViewById(R.id.place_location_city_edittext);
        stateEditText = view.findViewById(R.id.place_location_state_edittext);
        countryEditText = view.findViewById(R.id.place_location_country_edittext);
        zipCodeEditText = view.findViewById(R.id.place_location_zip_code_edittext);
        doneButton = view.findViewById(R.id.done_button);

        /*addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.validateInputsWithoutYoyo(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    Utils.setButtonEnabled(doneButton, true);
                }else{
                    Utils.setButtonEnabled(doneButton, false);
                }
            }
        });

        cityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.validateInputsWithoutYoyo(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    Utils.setButtonEnabled(doneButton, true);
                }else{
                    Utils.setButtonEnabled(doneButton, false);
                }
            }
        });

        stateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.validateInputsWithoutYoyo(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    Utils.setButtonEnabled(doneButton, true);
                }else{
                    Utils.setButtonEnabled(doneButton, false);
                }
            }
        });

        countryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.validateInputsWithoutYoyo(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    Utils.setButtonEnabled(doneButton, true);
                }else{
                    Utils.setButtonEnabled(doneButton, false);
                }
            }
        });

        zipCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.validateInputsWithoutYoyo(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    Utils.setButtonEnabled(doneButton, true);
                }else{
                    Utils.setButtonEnabled(doneButton, false);
                }
            }
        });*/

        if(MySettings.getTempPlace() != null){
            place = MySettings.getTempPlace();
        }else {
            place = MySettings.getCurrentPlace();
        }


        if(place != null){
            addressEditText.setText(""+place.getAddress());
            cityEditText.setText(""+place.getCity());
            stateEditText.setText(""+place.getState());
            countryEditText.setText(""+place.getCountry());
            zipCodeEditText.setText(""+place.getZipCode());
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.validateInputs(addressEditText, cityEditText, countryEditText, zipCodeEditText)){
                    if(place != null){
                        place.setAddress(""+addressEditText.getText().toString());
                        place.setCity(""+cityEditText.getText().toString());
                        place.setState(""+stateEditText.getText().toString());
                        place.setCountry(""+countryEditText.getText().toString());
                        place.setZipCode(""+zipCodeEditText.getText().toString());

                        if(MySettings.getTempPlace() != null){
                            MySettings.addPlace(place);
                            com.ronixtech.ronixhome.entities.Place dbPlace = MySettings.getPlaceByName(place.getName());
                            for (Floor floor : place.getFloors()) {
                                floor.setPlaceID(dbPlace.getId());
                                MySettings.addFloor(floor);
                            }
                            for (WifiNetwork network : place.getWifiNetworks()) {
                                network.setPlaceID(dbPlace.getId());
                                MySettings.addWifiNetwork(network);
                                MySettings.updateWifiNetworkPlace(network, dbPlace.getId());
                            }

                            MySettings.setCurrentPlace(dbPlace);

                            if(place.isDefaultPlace()){
                                MySettings.setDefaultPlaceID(dbPlace.getId());
                            }

                            MySettings.updatePlaceLatitude(dbPlace, place.getLatitude());
                            MySettings.updatePlaceLongitude(dbPlace, place.getLongitude());

                            MySettings.updatePlaceAddress(dbPlace, place.getAddress());
                            MySettings.updatePlaceCity(dbPlace, place.getCity());
                            MySettings.updatePlaceState(dbPlace, place.getState());
                            MySettings.updatePlaceCountry(dbPlace, place.getCountry());
                            MySettings.updatePlaceZipCode(dbPlace, place.getZipCode());

                            MySettings.setTempPlace(null);

                            //go to successFragment
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            SuccessFragment successFragment = new SuccessFragment();
                            successFragment.setSuccessSource(Constants.SUCCESS_SOURCE_PLACE);
                            fragmentTransaction.replace(R.id.fragment_view, successFragment, "successFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commit();
                        }else{
                            MySettings.updatePlaceLatitude(place, place.getLatitude());
                            MySettings.updatePlaceLongitude(place, place.getLongitude());

                            MySettings.updatePlaceAddress(place, place.getAddress());
                            MySettings.updatePlaceCity(place, place.getCity());
                            MySettings.updatePlaceState(place, place.getState());
                            MySettings.updatePlaceCountry(place, place.getCountry());
                            MySettings.updatePlaceZipCode(place, place.getZipCode());

                            //go to PlacesFragment
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            PlacesFragment placesFragment = new PlacesFragment();
                            fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commit();
                        }
                    }
                }
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
