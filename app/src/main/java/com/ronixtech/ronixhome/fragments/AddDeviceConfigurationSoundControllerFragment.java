package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.entities.Speaker;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationSoundControllerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationSoundControllerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationSoundControllerFragment extends androidx.fragment.app.Fragment {
    private static final String TAG = AddDeviceConfigurationSoundControllerFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    EditText deviceNameEditText;

    Spinner numberOfSpeakersSpinner;
    Button continueButton;

    int selectedNumberOfSpeakers = 1;

    Device device;

    public AddDeviceConfigurationSoundControllerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationSoundControllerFragment.
     */
    public static AddDeviceConfigurationSoundControllerFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationSoundControllerFragment fragment = new AddDeviceConfigurationSoundControllerFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_sound_controller, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.configure_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        deviceNameEditText = view.findViewById(R.id.device_name_edittext);

        numberOfSpeakersSpinner = view.findViewById(R.id.number_of_speakers_spinner);
        continueButton = view.findViewById(R.id.continue_button);

        List<Integer> numberOfSpeakers = new ArrayList<>();
        for(int x = 1; x <= SoundDeviceData.MAX_NUMBER_OF_SPEAKERS; x++){
            numberOfSpeakers.add(x);
        }

        ArrayAdapter<Integer> speakersAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, numberOfSpeakers);
        speakersAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        numberOfSpeakersSpinner.setAdapter(speakersAdapter);

        numberOfSpeakersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNumberOfSpeakers = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        device = MySettings.getTempDevice();
        if(device == null){
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.error_adding_smart_controller), true);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }



        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initialize the souddevicedata configuration for the device
                Device device = MySettings.getTempDevice();

                if(deviceNameEditText.getText().toString().length() > 1){
                    device.setName(deviceNameEditText.getText().toString());
                }else{
                    device.setName(Utils.getString(getActivity(), R.string.sound_controller_name_hint));
                }

                Device dbDevice = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                if(dbDevice == null){
                    MySettings.addDevice(device);
                    device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());
                }

                if(deviceNameEditText.getText().toString().length() > 1){
                    device.setName(deviceNameEditText.getText().toString());
                }else{
                    device.setName(Utils.getString(getActivity(), R.string.sound_controller_name_hint));
                }

                SoundDeviceData soundDeviceData = new SoundDeviceData();
                soundDeviceData.setDeviceID(device.getId());
                //soundDeviceData.setSpeakers(speakers);
                device.setSoundDeviceData(soundDeviceData);

                MySettings.updateDeviceSoundData(device, soundDeviceData);
                device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                soundDeviceData = device.getSoundDeviceData();

                List<Speaker> speakers = new ArrayList<>();
                for(int x = 0; x < selectedNumberOfSpeakers; x++){
                    Speaker speaker = new Speaker();
                    speaker.setId(x);
                    speaker.setName("Speaker #"+x);
                    speaker.setVolume(1);
                    speaker.setSoundDeviceID(soundDeviceData.getId());
                    speakers.add(speaker);
                }

                soundDeviceData.setSpeakers(speakers);
                device.setSoundDeviceData(soundDeviceData);
                MySettings.updateSoundDeviceDataSpeakers(soundDeviceData, speakers);


                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceSelectLocationFragment addDeviceSelectLocationFragment = new AddDeviceSelectLocationFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceSelectLocationFragment, "addDeviceSelectLocationFragment");
                //fragmentTransaction.addToBackStack("addDeviceSelectLocationFragment");
                fragmentTransaction.commit();
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
