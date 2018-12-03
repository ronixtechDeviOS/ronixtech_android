package com.ronixtech.ronixhome.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPlaceLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPlaceLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPlaceLocationFragment extends android.support.v4.app.Fragment {
    private static final String TAG = AddPlaceLocationFragment.class.getSimpleName();

    int PLACE_PICKER_REQUEST = 1001;

    private OnFragmentInteractionListener mListener;

    Button getMyLocationButton, enterAddressButton, skipForNowButton;

    public AddPlaceLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPlaceLocationFragment.
     */
    public static AddPlaceLocationFragment newInstance(String param1, String param2) {
        AddPlaceLocationFragment fragment = new AddPlaceLocationFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_place_location, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_place_location), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        getMyLocationButton = view.findViewById(R.id.add_place_get_location_button);
        enterAddressButton = view.findViewById(R.id.add_place_enter_address_button);
        skipForNowButton = view.findViewById(R.id.add_place_skip_button);

        getMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request placeData then go to AddPlaceLocationAddressFragment
                try{
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    //Context context = getApplicationContext();
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                }catch (GooglePlayServicesNotAvailableException e){
                    Log.d(TAG, "PlacePicker, Google Play Services not available.");
                }catch (GooglePlayServicesRepairableException e){
                    Log.d(TAG, "PlacePicker, Google Play Services not available.");
                }
            }
        });

        enterAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to AddPlaceLocationAddressFragment
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddPlaceLocationAddressFragment addPlaceLocationAddressFragment = new AddPlaceLocationAddressFragment();
                fragmentTransaction.replace(R.id.fragment_view, addPlaceLocationAddressFragment, "addPlaceLocationAddressFragment");
                fragmentTransaction.addToBackStack("addPlaceLocationAddressFragment");
                fragmentTransaction.commit();
            }
        });

        skipForNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to PlacesFragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                PlacesFragment placesFragment = new PlacesFragment();
                fragmentTransaction.replace(R.id.fragment_view, placesFragment, "placesFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                double latitude = place.getLatLng().latitude;
                double longitude = place.getLatLng().longitude;
                String address = place.getAddress().toString();

                com.ronixtech.ronixhome.entities.Place currentPlace = MySettings.getCurrentPlace();
                currentPlace.setLatitude(latitude);
                currentPlace.setLongitude(longitude);
                currentPlace.setAddress(address);

                MySettings.updatePlaceLatitude(currentPlace, currentPlace.getLatitude());
                MySettings.updatePlaceLongitude(currentPlace, currentPlace.getLongitude());
                MySettings.updatePlaceAddress(currentPlace, currentPlace.getAddress());

                new Utils.AddressGeocoder(getActivity(), latitude, longitude, new Utils.AddressGeocoder.OnGeocodingCallback() {
                    @Override
                    public void onGeocodingSuccess(String address, String city, String state, String country, String zipCode) {
                        currentPlace.setCity(city);
                        currentPlace.setState(state);
                        currentPlace.setCountry(country);
                        currentPlace.setZipCode(zipCode);

                        MySettings.updatePlaceCity(currentPlace, currentPlace.getCity());
                        MySettings.updatePlaceState(currentPlace, currentPlace.getState());
                        MySettings.updatePlaceCountry(currentPlace, currentPlace.getCountry());
                        MySettings.updatePlaceZipCode(currentPlace, currentPlace.getZipCode());

                        MySettings.setCurrentPlace(currentPlace);

                        //go to AddPlaceLocationAddressFragment
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        AddPlaceLocationAddressFragment addPlaceLocationAddressFragment = new AddPlaceLocationAddressFragment();
                        fragmentTransaction.replace(R.id.fragment_view, addPlaceLocationAddressFragment, "addPlaceLocationAddressFragment");
                        fragmentTransaction.addToBackStack("addPlaceLocationAddressFragment");
                        fragmentTransaction.commit();
                    }

                    @Override
                    public void onGeocodingFail(String errorMsg) {
                        Toast.makeText(getActivity(), ""+errorMsg, Toast.LENGTH_SHORT).show();
                        MySettings.setCurrentPlace(currentPlace);

                        //go to AddPlaceLocationAddressFragment
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        AddPlaceLocationAddressFragment addPlaceLocationAddressFragment = new AddPlaceLocationAddressFragment();
                        fragmentTransaction.replace(R.id.fragment_view, addPlaceLocationAddressFragment, "addPlaceLocationAddressFragment");
                        fragmentTransaction.addToBackStack("addPlaceLocationAddressFragment");
                        fragmentTransaction.commit();
                    }
                }).execute();
            }
        }
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
