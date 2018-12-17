package com.ronixtech.ronixhome.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.WifiNetwork;

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

    private static final int PLACE_PICKER_REQUEST = 1000;

    private static final int RC_PERMISSION_LOCATION = 1004;
    private static final int RC_PERMISSION_ACCESS_WIFI_STATE = 1005;
    private static final int RC_PERMISSION_CHANGE_WIFI_STATE= 1006;

    private static final int RC_ACTIVITY_WIFI_TURN_ON = 1007;
    private static final int RC_ACTIVITY_LOCATION_TURN_ON = 1008;

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
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_place_location), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        getMyLocationButton = view.findViewById(R.id.add_place_get_location_button);
        enterAddressButton = view.findViewById(R.id.add_place_enter_address_button);
        skipForNowButton = view.findViewById(R.id.add_place_skip_button);

        getMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermissions();
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
                com.ronixtech.ronixhome.entities.Place tempPlace = MySettings.getTempPlace();

                MySettings.addPlace(tempPlace);
                com.ronixtech.ronixhome.entities.Place dbPlace = MySettings.getPlaceByName(tempPlace.getName());
                for (Floor floor : tempPlace.getFloors()) {
                    floor.setPlaceID(dbPlace.getId());
                    MySettings.addFloor(floor);
                }
                for (WifiNetwork network : tempPlace.getWifiNetworks()) {
                    network.setPlaceID(dbPlace.getId());
                    MySettings.addWifiNetwork(network);
                    MySettings.updateWifiNetworkPlace(network, dbPlace.getId());
                }

                MySettings.setCurrentPlace(dbPlace);

                if(tempPlace.isDefaultPlace()){
                    MySettings.setDefaultPlaceID(dbPlace.getId());
                }

                MySettings.setCurrentPlace(dbPlace);

                MySettings.setTempPlace(null);

                //go to successFragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                SuccessFragment successFragment = new SuccessFragment();
                successFragment.setSuccessSource(Constants.SUCCESS_SOURCE_PLACE);
                fragmentTransaction.replace(R.id.fragment_view, successFragment, "successFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private void checkLocationPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //debugTextView.append("location permissions granted\n");
                checkWifiAccessPermissions();
            }else{
                requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_LOCATION);
            }
        }else{
            //no need to show runtime permission stuff
            //debugTextView.append("location permissions granted from manifest file\n");
            checkWifiAccessPermissions();
        }
    }

    private void checkWifiAccessPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //debugTextView.append("wifi access permissions granted\n");
                checkWifiChangePermissions();
            }else{
                requestPermissions(new String[]{"android.permission.ACCESS_WIFI_STATE"}, RC_PERMISSION_ACCESS_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            //debugTextView.append("wifi access permissions granted from manifest file\n");
            checkWifiChangePermissions();
        }
    }

    private void checkWifiChangePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                if(checkLocationServices() && checkWifiService()){
                    //request placeData then go to AddPlaceLocationAddressFragment
                    try{
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        //Context context = getApplicationContext();
                        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                    }catch (GooglePlayServicesNotAvailableException e){
                        Log.d(TAG, "PlacePicker, Google Play Services not available.");
                        Utils.showToast(getActivity(), "Google Play Services not available.", true);
                        Log.d(TAG, "PlacePicker,");
                    }catch (GooglePlayServicesRepairableException e){
                        Log.d(TAG, "PlacePicker, Google Play Services not available.");
                        Utils.showToast(getActivity(), "Google Play Services not available.", true);
                    }
                }
            }else{
                requestPermissions(new String[]{"android.permission.CHANGE_WIFI_STATE"}, RC_PERMISSION_CHANGE_WIFI_STATE);
            }
        }else{
            //no need to show runtime permission stuff
            if(checkLocationServices() && checkWifiService()){
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
        }
    }

    private boolean checkLocationServices(){
        boolean enabled = true;
        if(getActivity() != null && getActivity().getSystemService(Context.LOCATION_SERVICE) != null){
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsProviderEnabled, isNetworkProviderEnabled;
            isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                enabled = false;
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(Utils.getString(getActivity(), R.string.location_required_title));
                builder.setMessage(Utils.getString(getActivity(), R.string.location_required_message));
                builder.setPositiveButton(Utils.getString(getActivity(), R.string.go_to_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, RC_ACTIVITY_LOCATION_TURN_ON);
                    }
                });
                builder.setNegativeButton(Utils.getString(getActivity(), R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                        DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                        fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commitAllowingStateLoss();
                    }
                });
                builder.show();
            }
        }
        return enabled;
    }

    private boolean checkWifiService(){
        boolean enabled = true;
        WifiManager mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(mWifiManager != null){
            if(!mWifiManager.isWifiEnabled()){
                enabled = false;
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle(Utils.getString(getActivity(), R.string.wifi_required_title))
                        //set message
                        .setMessage(Utils.getString(getActivity(), R.string.wifi_required_message))
                        //set positive button
                        .setPositiveButton(Utils.getString(getActivity(), R.string.go_to_wifi_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), RC_ACTIVITY_WIFI_TURN_ON);
                            }
                        })
                        //set negative button
                        .setNegativeButton(Utils.getString(getActivity(), R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
                                DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
                                fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
                                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                fragmentTransaction.commitAllowingStateLoss();
                            }
                        })
                        .show();
            }
        }

        return enabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    //debugTextView.append("location permissions granted\n");
                    checkWifiAccessPermissions();
                }
                else{
                    //denied
                    Utils.showToast(getActivity(), "You need to enable location permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Location permission")
                                .setMessage("You need to enable location permissions for the app to detect nearby devices")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_LOCATION);
                                    }
                                })
                                .show();
                    }
                }
            }
            case RC_PERMISSION_ACCESS_WIFI_STATE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    //debugTextView.append("wifi access permissions granted\n");
                    checkWifiChangePermissions();
                }
                else{
                    //denied
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.ACCESS_WIFI_STATE")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Access WiFi permission")
                                .setMessage("You need to enable WiFi permissions for the app to detect nearby WiFi networks")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.ACCESS_WIFI_STATE"}, RC_PERMISSION_ACCESS_WIFI_STATE);
                                    }
                                })
                                .show();
                    }
                }
            }
            case RC_PERMISSION_CHANGE_WIFI_STATE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    if(checkLocationServices() && checkWifiService()){
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
                }
                else{
                    //denied
                    Utils.showToast(getActivity(), "You need to enable WiFi permission", true);
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.CHANGE_WIFI_STATE")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Modify WiFi permission")
                                .setMessage("You need to enable WiFi permissions for the app to configure your RonixTech device")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.CHANGE_WIFI_STATE"}, RC_PERMISSION_CHANGE_WIFI_STATE);
                                    }
                                })
                                .show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Utils.showLoading(getActivity());
                Place gPlace = PlacePicker.getPlace(data, getActivity());
                double latitude = gPlace.getLatLng().latitude;
                double longitude = gPlace.getLatLng().longitude;
                String address = gPlace.getAddress().toString();

                com.ronixtech.ronixhome.entities.Place place;

                if(MySettings.getTempPlace() != null){
                    //new place being added
                    place = MySettings.getTempPlace();
                }else{
                    //old place being edited
                    place = MySettings.getCurrentPlace();
                }

                place.setLatitude(latitude);
                place.setLongitude(longitude);
                place.setAddress(address);

                new Utils.AddressGeocoder(getActivity(), latitude, longitude, new Utils.AddressGeocoder.OnGeocodingCallback() {
                    @Override
                    public void onGeocodingSuccess(String address, String city, String state, String country, String zipCode) {
                        place.setCity(city);
                        place.setState(state);
                        place.setCountry(country);
                        place.setZipCode(zipCode);

                        if(MySettings.getTempPlace() != null){
                            MySettings.setTempPlace(place);
                        }else {
                            MySettings.setCurrentPlace(place);
                        }

                        Utils.dismissLoading();

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
                        Utils.showToast(getActivity(), errorMsg, true);
                        if(MySettings.getTempPlace() != null){
                            MySettings.setTempPlace(place);
                        }else {
                            MySettings.setCurrentPlace(place);
                        }

                        Utils.dismissLoading();

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
        }else if( requestCode == RC_ACTIVITY_WIFI_TURN_ON ) {
            if(checkLocationServices() && checkWifiService()){
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
        }else if(requestCode == RC_ACTIVITY_LOCATION_TURN_ON){
            if(checkLocationServices() && checkWifiService()){
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
