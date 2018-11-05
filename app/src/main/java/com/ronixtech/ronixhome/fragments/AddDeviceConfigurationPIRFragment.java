package com.ronixtech.ronixhome.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.LinePIRConfigurationAdapter;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceConfigurationPIRFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceConfigurationPIRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceConfigurationPIRFragment extends Fragment implements PickLineDialogFragment.OnLineSelectedListener {
    private static final String TAG = AddDeviceConfigurationPIRFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout lineSelectionLayout;
    TextView lineNameTextView;
    ImageView lineImageView;

    LinePIRConfigurationAdapter adapter;
    ListView selectedLinesListView;
    List<Line> selectedLines;
    Button continueButton;

    Device device;

    public AddDeviceConfigurationPIRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDeviceConfigurationPIRFragment.
     */
    public static AddDeviceConfigurationPIRFragment newInstance(String param1, String param2) {
        AddDeviceConfigurationPIRFragment fragment = new AddDeviceConfigurationPIRFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_device_configuration_pir, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.configure_device), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        lineSelectionLayout = view.findViewById(R.id.line_selection_layout);
        lineNameTextView = view.findViewById(R.id.selected_line_name_textview);
        lineImageView = view.findViewById(R.id.selected_line_image_view);
        selectedLinesListView = view.findViewById(R.id.selected_lines_listview);
        selectedLines = new ArrayList<>();
        adapter = new LinePIRConfigurationAdapter(getActivity(), selectedLines);
        selectedLinesListView.setAdapter(adapter);
        continueButton = view.findViewById(R.id.continue_button);

        device = MySettings.getTempDevice();
        if(device == null){
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_adding_smart_controller), Toast.LENGTH_LONG).show();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }

        lineSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickLineDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    PickLineDialogFragment fragment = PickLineDialogFragment.newInstance();
                    fragment.setTargetFragment(AddDeviceConfigurationPIRFragment.this, 0);
                    fragment.show(ft, "pickLineDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.add_devices_first), Toast.LENGTH_SHORT).show();
                    goToSearchFragment();
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create the lines and device.setLines then MySettings.setDevice()
                List<Line> lines = new ArrayList<>();

                /*MySettings.addDevice(device);
                device = MySettings.getDeviceByMAC(device.getMacAddress(), device.getDeviceTypeID());

                if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                    lines.addAll(device.getLines());
                }*/
                lines.addAll(selectedLines);

                device.setLines(lines);
                MySettings.setTempDevice(device);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentSendData addDeviceFragmentSendData = new AddDeviceFragmentSendData();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentSendData, "addDeviceFragmentSendData");
                fragmentTransaction.addToBackStack("addDeviceFragmentSendData");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onLineSelected(Line line){
        if(line != null && !selectedLines.contains(line)){
            this.selectedLines.add(line);
            adapter.notifyDataSetChanged();
            /*lineNameTextView.setText(line.getName());
            if(line.getType().getImageUrl() != null && line.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(line.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.device_colored_icon))
                        .into(lineImageView);
            }else {
                lineImageView.setImageResource(line.getType().getImageResourceID());
            }*/
        }
        if(validateInputs()){
            Utils.setButtonEnabled(continueButton, true);
        }else{
            Utils.setButtonEnabled(continueButton, false);
        }
    }

    public void goToSearchFragment(){
        if(MainActivity.getInstance() != null && MainActivity.isResumed) {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack("addDeviceFragmentIntro", 0);
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        if(selectedLines == null || selectedLines.size() <= 0){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(lineSelectionLayout);
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
