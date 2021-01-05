package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationModeSelectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationModeSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationModeSelectionFragment extends Fragment {
    private static final String TAG = AddDeviceConfigurationModeSelectionFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RadioGroup modeRadioGroup;
    Button continueButton;

    int selectedMode = Device.MODE_PRIMARY;

    public AddDeviceConfigurationModeSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationModeSelectionFragment.
     */
    public static AddDeviceConfigurationModeSelectionFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationModeSelectionFragment fragment = new AddDeviceConfigurationModeSelectionFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_mode_selection, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.unit_mode_title), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        modeRadioGroup = view.findViewById(R.id.device_mode_radiogroup);
        continueButton = view.findViewById(R.id.continue_button);

        modeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.device_mode_primary_mode_radiobutton:
                        selectedMode = Device.MODE_PRIMARY;
                        break;
                    case R.id.device_mode_secondary_mode_radiobutton:
                        selectedMode = Device.MODE_SECONDARY;
                        break;
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMode == Device.MODE_PRIMARY) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceConfigurationFragment addDeviceConfigurationFragment = new AddDeviceConfigurationFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationFragment, "addDeviceConfigurationFragment");
                    fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                    fragmentTransaction.commitAllowingStateLoss();
                }else if(selectedMode == Device.MODE_SECONDARY){
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceConfigurationPairingFragment addDeviceConfigurationPairingFragment = new AddDeviceConfigurationPairingFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationPairingFragment, "addDeviceConfigurationPairingFragment");
                    fragmentTransaction.addToBackStack("addDeviceConfigurationPairingFragment");
                    fragmentTransaction.commitAllowingStateLoss();
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
