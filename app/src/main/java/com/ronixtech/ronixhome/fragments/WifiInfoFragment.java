package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.WifiNetwork;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiInfoFragment extends Fragment implements WifiListFragment.OnNetworkSelectedListener{
    private static final String TAG = WifiInfo.class.getSimpleName();

    private OnNetworkAddedListener callback;

    public interface OnNetworkAddedListener {
        public void onNetworkAdded(WifiNetwork network);
    }

    TextView ssidTextView, passwordTextView;
    LinearLayout ssidEditLayout, passwordEditLayout;
    ImageView ssidEditImageView, passwordEditImageView;
    EditText ssidEditText, passwordEditText;
    Button continueButton;

    String ssid, password;
    boolean ssidRetrieved = false;
    boolean passwordRetrieved = false;

    private int source = Constants.SOURCE_NAV_DRAWER;

    public WifiInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiInfoFragment.
     */
    public static WifiInfoFragment newInstance(String param1, String param2) {
        WifiInfoFragment fragment = new WifiInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if(getTargetFragment() != null){
                Log.d("AAAA", "WifiInfoFragment onCreate - getTargetFragment(): " + getTargetFragment().getClass().getName());
                callback = (OnNetworkAddedListener) getTargetFragment();
                Log.d("AAAA", "WifiInfoFragment onCreate - callback is found!");
            }else{
                Log.d("AAAA", "WifiInfoFragment onCreate - getTargetFragment() IS NULL");
                Log.d("AAAA", "WifiInfoFragment onCreate - Trying findFragmentByTag()");
                AddDeviceSelectLocationFragment fragment = (AddDeviceSelectLocationFragment) getFragmentManager().findFragmentByTag("addDeviceSelectLocationFragment");
                if(fragment != null){
                    Log.d("AAAA", "WifiInfoFragment onCreate - findFragmentByTag() - " + fragment.getClass().getName());
                    callback = fragment;
                    Log.d("AAAA", "WifiInfoFragment onCreate - callback is found!");
                }else{
                    Log.d("AAAA", "WifiInfoFragment onCreate - findFragmentByTag() IS NULL");
                }
            }
        } catch (ClassCastException e) {
            Log.d("AAAA", "WifiInfoFragment onCreate - Calling Fragment must implement OnNetworkAddedListener!");
            throw new ClassCastException("Calling Fragment must implement OnNetworkAddedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wifi_info, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.home_network), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        ssidTextView = view.findViewById(R.id.ssid_textview);
        passwordTextView = view.findViewById(R.id.password_textview);
        ssidEditLayout = view.findViewById(R.id.ssid_edit_layout);
        passwordEditLayout = view.findViewById(R.id.password_edit_layout);
        ssidEditImageView = view.findViewById(R.id.ssid_edit_imageview);
        passwordEditImageView = view.findViewById(R.id.password_edit_imageview);
        ssidEditText = view.findViewById(R.id.ssid_edittext);
        passwordEditText = view.findViewById(R.id.password_edittext);

        continueButton = view.findViewById(R.id.continue_button);

        if(MySettings.getHomeNetwork() != null){
            ssid = MySettings.getHomeNetwork().getSsid();
            password = MySettings.getHomeNetwork().getPassword();
            ssidTextView.setText(ssid);
            passwordTextView.setText(password);
        }

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiNetwork wifiNetwork = new WifiNetwork();
                wifiNetwork.setSsid(ssid);
                wifiNetwork.setPassword(password.trim());
                //MySettings.setHomeNetwork(wifiNetwork);
                MySettings.addWifiNetwork(wifiNetwork);
                if(source == Constants.SOURCE_NAV_DRAWER) {
                    if(callback != null) {
                        callback.onNetworkAdded(wifiNetwork);
                    }
                    getFragmentManager().popBackStack();
                }else if(source == Constants.SOURCE_NEW_DEVICE){
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                    fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                    fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                    fragmentTransaction.commit();
                }else if(source == Constants.SOURCE_NEW_PLACE){
                    if(callback != null) {
                        callback.onNetworkAdded(wifiNetwork);
                    }
                    getFragmentManager().popBackStack();
                }
            }
        });

        ssidTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUnfinishedChanges();
                ssidEditLayout.setVisibility(View.VISIBLE);
                ssidTextView.setVisibility(View.INVISIBLE);
                ssidEditText.setText(ssid);
                ssidEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ssidEditText, InputMethodManager.SHOW_IMPLICIT);
                ssidEditText.setSelection(ssidEditText.getText().length());
            }
        });
        passwordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUnfinishedChanges();
                passwordEditLayout.setVisibility(View.VISIBLE);
                passwordTextView.setVisibility(View.INVISIBLE);
                passwordEditText.setText(password);
                passwordEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(passwordEditText, InputMethodManager.SHOW_IMPLICIT);
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        ssidEditImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ssidEditText.getText() != null && ssidEditText.getText().toString().length() >= 1){
                    ssidEditLayout.setVisibility(View.INVISIBLE);
                    ssidTextView.setVisibility(View.VISIBLE);
                    ssid = ssidEditText.getText().toString();
                    ssidTextView.setText(ssid);
                    //MySettings.setSSID(ssid);
                    ssidRetrieved = true;
                    if(ssidRetrieved && passwordRetrieved){
                        Utils.setButtonEnabled(continueButton, true);
                    }
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ssidEditText.getWindowToken(), 0);
                }
            }
        });
        passwordEditImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordEditText.getText() != null && passwordEditText.getText().toString().length() >= 1){
                    passwordEditLayout.setVisibility(View.INVISIBLE);
                    passwordTextView.setVisibility(View.VISIBLE);
                    password = passwordEditText.getText().toString().trim();
                    passwordTextView.setText(password);
                    //MySettings.setPassword(password);
                    passwordRetrieved = true;
                    if(ssidRetrieved && passwordRetrieved){
                        Utils.setButtonEnabled(continueButton, true);
                    }
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                }
            }
        });

        ssidEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(ssidEditText.getText() != null && ssidEditText.getText().toString().length() >= 1){
                        ssidEditLayout.setVisibility(View.INVISIBLE);
                        ssidTextView.setVisibility(View.VISIBLE);
                        ssid = ssidEditText.getText().toString();
                        ssidTextView.setText(ssid);
                        //MySettings.setSSID(ssid);
                        ssidRetrieved = true;
                        if(ssidRetrieved && passwordRetrieved){
                            Utils.setButtonEnabled(continueButton, true);
                        }
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(ssidEditText.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(passwordEditText.getText() != null && passwordEditText.getText().toString().length() >= 1){
                        passwordEditLayout.setVisibility(View.INVISIBLE);
                        passwordTextView.setVisibility(View.VISIBLE);
                        password = passwordEditText.getText().toString().trim();
                        passwordTextView.setText(password);
                        //MySettings.setPassword(password);
                        passwordRetrieved = true;
                        if(ssidRetrieved && passwordRetrieved){
                            Utils.setButtonEnabled(continueButton, true);
                        }
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });


        WifiListFragment wifiListFragment = new WifiListFragment();
        //wifiListFragment.setTargetFragment(WifiInfoFragment.this, 0);
        wifiListFragment.setNetworkSelectedListener(this);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, wifiListFragment).commit();

        return view;
    }

    public void setSource(int source){
        this.source = source;
    }

    @Override
    public void onNetworkSelected(WifiNetwork network){
        if(network != null) {
            Log.d("AAAA", "WifiInfoFragment onNetworkSelected - ID: " + network.getId() + " - SSID: " + network.getSsid());
            ssid = network.getSsid();
            password = network.getPassword();
            ssidTextView.setText("" + ssid);
            passwordTextView.setText("" + password.trim());
            ssidRetrieved = true;
            passwordRetrieved = true;
            Utils.setButtonEnabled(continueButton, true);


            //as if continue button is clicked
            WifiNetwork wifiNetwork = new WifiNetwork();
            wifiNetwork.setSsid(ssid);
            wifiNetwork.setPassword(password.trim());
            //MySettings.setHomeNetwork(wifiNetwork);
            MySettings.addWifiNetwork(wifiNetwork);
            wifiNetwork = MySettings.getWifiNetworkBySSID(wifiNetwork.getSsid());
            Log.d("AAAA", "WifiInfoFragment onNetworkSelected after adding to DB - ID: " + wifiNetwork.getId() + " - SSID: " + wifiNetwork.getSsid());
            if(source == Constants.SOURCE_NAV_DRAWER) {
                if(callback != null) {
                    callback.onNetworkAdded(wifiNetwork);
                }
                getFragmentManager().popBackStack();
            }else if(source == Constants.SOURCE_NEW_DEVICE){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddDeviceFragmentIntro addDeviceFragmentIntro = new AddDeviceFragmentIntro();
                fragmentTransaction.replace(R.id.fragment_view, addDeviceFragmentIntro, "addDeviceFragmentIntro");
                fragmentTransaction.addToBackStack("addDeviceFragmentIntro");
                fragmentTransaction.commit();
            }else if(source == Constants.SOURCE_NEW_PLACE){
                if(callback != null) {
                    Log.d("AAAA", "WifiInfoFragment onNetworkSelected CALLBACK IS NOT NULL");
                    callback.onNetworkAdded(wifiNetwork);
                }else{
                    Log.d("AAAA", "WifiInfoFragment onNetworkSelected CALLBACK IS NULL");
                }
                getFragmentManager().popBackStack();
            }
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_FADE);
            DashboardRoomsFragment dashboardRoomsFragment = new DashboardRoomsFragment();
            fragmentTransaction.replace(R.id.fragment_view, dashboardRoomsFragment, "dashboardRoomsFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void saveUnfinishedChanges(){
        if(ssidEditLayout.getVisibility() == View.VISIBLE){
            if(ssidEditText.getText() != null && ssidEditText.getText().toString().length() >= 1){
                ssidEditLayout.setVisibility(View.INVISIBLE);
                ssidTextView.setVisibility(View.VISIBLE);
                ssid = ssidEditText.getText().toString();
                ssidTextView.setText(ssid);
                //MySettings.setSSID(ssid);
                ssidRetrieved = true;
                if(ssidRetrieved && passwordRetrieved){
                    Utils.setButtonEnabled(continueButton, true);
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ssidEditText.getWindowToken(), 0);
            }
        }

        if(passwordEditLayout.getVisibility() == View.VISIBLE){
            if(passwordEditText.getText() != null && passwordEditText.getText().toString().length() >= 1){
                passwordEditLayout.setVisibility(View.INVISIBLE);
                passwordTextView.setVisibility(View.VISIBLE);
                password = passwordEditText.getText().toString();
                passwordTextView.setText(password);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
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
