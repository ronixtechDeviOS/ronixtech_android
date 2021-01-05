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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceTypeVerificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceTypeVerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceTypeVerificationFragment extends androidx.fragment.app.Fragment implements TypePickerDeviceDialogFragment.OnDeviceTypeSelectedListener{
    private static final String TAG = AddDeviceTypeVerificationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout deviceTypeSelectionLayout;
    TextView deviceTypeNameTextView;
    ImageView deviceTypeImageView;
    Button continueButton;

    private Device selectedDeviceType;

    public AddDeviceTypeVerificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceTypeVerificationFragment.
     */
    public static AddDeviceTypeVerificationFragment newInstance(String param1, String param2) {
        AddDeviceTypeVerificationFragment fragment = new AddDeviceTypeVerificationFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_type_verification, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_device_get_data_type_verification), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        deviceTypeSelectionLayout = view.findViewById(R.id.device_type_selection_layout);
        deviceTypeNameTextView = view.findViewById(R.id.selected_device_type_name_textview);
        deviceTypeImageView = view.findViewById(R.id.selected_device_type_imageview);
        continueButton = view.findViewById(R.id.continue_button);

        selectedDeviceType = new Device();

        deviceTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.  We also want to remove any currently showing
                // dialog, so make our own transaction and take care of that here.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                androidx.fragment.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                TypePickerDeviceDialogFragment fragment = TypePickerDeviceDialogFragment.newInstance();
                fragment.setTargetFragment(AddDeviceTypeVerificationFragment.this, 0);
                fragment.show(ft, "typePickerDialogFragment");
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedDeviceType == null || selectedDeviceType.getDeviceTypeID() == -1){
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(deviceTypeSelectionLayout);
                    return;
                }

                Device device = MySettings.getTempDevice();
                device.setDeviceTypeID(selectedDeviceType.getDeviceTypeID());
                MySettings.setTempDevice(device);

                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                        device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines) {
                    goToConfigurationFragment();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    goToPIRConfigurationFragment();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
                    goToSoundControllerConfigurationFragment();
                }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SHUTTER){
                    goToShutterControllerConfigurationFragment();
                }else{
                    Utils.showToast(getActivity(), Utils.getStringExtraInt(getActivity(), R.string.unknown_smart_controller_type, device.getDeviceTypeID()), true);
                    goToSearchFragment();
                }
            }
        });

        return view;
    }

    @Override
    public void onDeviceTypeSelected(Device deviceType){
        if(deviceType != null){
            selectedDeviceType = deviceType;
            deviceTypeNameTextView.setText(Device.getDeviceTypeString(selectedDeviceType.getDeviceTypeID()));
            /*if(selectedDeviceType.getImageUrl() != null && selectedDeviceType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedDeviceType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.device_colored_icon))
                        .into(deviceTypeImageView);
            }else {
                if(selectedDeviceType.getImageResourceName() != null && selectedDeviceType.getImageResourceName().length() >= 1) {
                    deviceTypeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedDeviceType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    deviceTypeImageView.setImageResource(selectedDeviceType.getImageResourceID());
                }
            }*/
            Utils.setButtonEnabled(continueButton, true);
        }
    }

    public void goToSearchFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack("addDeviceFragmentIntro", 0);
            }
        }
    }

    public void goToPIRConfigurationFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationPIRFragment addDeviceConfigurationPIRFragment = new AddDeviceConfigurationPIRFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationPIRFragment, "addDeviceConfigurationPIRFragment");
                //fragmentTransaction.addToBackStack("addDeviceConfigurationPIRFragment");
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    public void goToSoundControllerConfigurationFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationSoundControllerFragment addDeviceConfigurationSoundControllerFragment = new AddDeviceConfigurationSoundControllerFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationSoundControllerFragment, "addDeviceConfigurationSoundControllerFragment");
                //fragmentTransaction.addToBackStack("addDeviceConfigurationPIRFragment");
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    public void goToShutterControllerConfigurationFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationShutterFragment addDeviceConfigurationShutterFragment = new AddDeviceConfigurationShutterFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationShutterFragment, "addDeviceConfigurationShutterFragment");
                //fragmentTransaction.addToBackStack("addDeviceConfigurationPIRFragment");
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    public void goToConfigurationFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed){
            if(getFragmentManager() != null){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceConfigurationPreparingFragment addDeviceConfigurationPreparingFragment = new AddDeviceConfigurationPreparingFragment();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceConfigurationPreparingFragment, "addDeviceConfigurationPreparingFragment");
                //fragmentTransaction.addToBackStack("addDeviceConfigurationFragment");
                fragmentTransaction.commitAllowingStateLoss();
            }
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
