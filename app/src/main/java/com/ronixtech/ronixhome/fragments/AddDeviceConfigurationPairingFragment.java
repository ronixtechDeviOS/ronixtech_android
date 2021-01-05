package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationPairingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationPairingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationPairingFragment extends Fragment {
    private static final String TAG = AddDeviceConfigurationPairingFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    public AddDeviceConfigurationPairingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationPairingFragment.
     */
    public static AddDeviceConfigurationPairingFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationPairingFragment fragment = new AddDeviceConfigurationPairingFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_pairing, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.select_paired_devices), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

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
