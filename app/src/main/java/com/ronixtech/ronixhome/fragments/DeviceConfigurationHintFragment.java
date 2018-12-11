package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Device;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceConfigurationHintFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceConfigurationHintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceConfigurationHintFragment extends android.support.v4.app.Fragment {
    private static final String TAG = DeviceConfigurationHintFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    GifImageView instructionsGifImageView;
    TextView instructionsTextView;

    private int deviceType;

    public DeviceConfigurationHintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceConfigurationHintFragment.
     */
    public static DeviceConfigurationHintFragment newInstance(String param1, String param2) {
        DeviceConfigurationHintFragment fragment = new DeviceConfigurationHintFragment();
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
        View view = inflater.inflate(R.layout.fragment_device_configuration_hint, container, false);

        instructionsGifImageView = view.findViewById(R.id.instructions_gif_imageview);
        instructionsTextView = view.findViewById(R.id.instructions_textview);

        if(deviceType == Device.DEVICE_TYPE_wifi_1line_old || deviceType == Device.DEVICE_TYPE_wifi_2lines_old || deviceType == Device.DEVICE_TYPE_wifi_3lines_old ||
                deviceType == Device.DEVICE_TYPE_wifi_1line || deviceType == Device.DEVICE_TYPE_wifi_2lines || deviceType == Device.DEVICE_TYPE_wifi_3lines ||
                deviceType == Device.DEVICE_TYPE_wifi_3lines_workaround){
            instructionsGifImageView.setImageResource(R.drawable.access_point_activation_final);
            instructionsTextView.setText(getActivity().getResources().getString(R.string.add_device_hint_switch_1) + "\n\n");
            instructionsTextView.append(getActivity().getResources().getString(R.string.add_device_hint_switch_2) + "\n\n");
            instructionsTextView.append(getActivity().getResources().getString(R.string.add_device_hint_switch_3) + "\n\n");
        }else if(deviceType == Device.DEVICE_TYPE_PLUG_1lines || deviceType == Device.DEVICE_TYPE_PLUG_2lines || deviceType == Device.DEVICE_TYPE_PLUG_3lines){
            instructionsGifImageView.setImageResource(R.drawable.access_point_activation_final);
            instructionsTextView.setText(getActivity().getResources().getString(R.string.add_device_hint_plug_1) + "\n\n");
            instructionsTextView.append(getActivity().getResources().getString(R.string.add_device_hint_plug_2) + "\n\n");
        }else if(deviceType == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            instructionsGifImageView.setImageResource(R.drawable.access_point_activation_final);
            instructionsTextView.setText(getActivity().getResources().getString(R.string.add_device_hint_pir_1) + "\n\n");
            instructionsTextView.append(getActivity().getResources().getString(R.string.add_device_hint_pir_2) + "\n\n");
        }else if(deviceType == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            instructionsGifImageView.setImageResource(R.drawable.access_point_activation_final);
            instructionsTextView.setText(getActivity().getResources().getString(R.string.add_device_hint_sound_controller_1) + "\n\n");
            instructionsTextView.append(getActivity().getResources().getString(R.string.add_device_hint_sound_controller_2) + "\n\n");
        }

        return view;
    }

    public void setDeviceType(int deviceType){
        this.deviceType = deviceType;
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
