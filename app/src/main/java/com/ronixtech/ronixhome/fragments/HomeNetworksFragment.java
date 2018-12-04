package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.WifiNetworkItemAdapterEditable;
import com.ronixtech.ronixhome.entities.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeNetworksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeNetworksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeNetworksFragment extends android.support.v4.app.Fragment {
    private static final String TAG = HomeNetworksFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    List<WifiNetwork> networks;
    ListView networksListView;
    WifiNetworkItemAdapterEditable networksAdapter;

    public HomeNetworksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeNetworksFragment.
     */
    public static HomeNetworksFragment newInstance(String param1, String param2) {
        HomeNetworksFragment fragment = new HomeNetworksFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_networks, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.home_networks), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        networksListView = view.findViewById(R.id.networks_listview);
        networks = new ArrayList<>();
        networksAdapter = new WifiNetworkItemAdapterEditable(getActivity(), networks, new WifiNetworkItemAdapterEditable.WifiNetworksListener() {
            @Override
            public void onNetworkDeleted() {
                //selectedWifiNetworks.clear();
                //selectedWifiNetworks.addAll(MySettings.getPlaceWifiNetworks(place.getId()));
                networksAdapter.notifyDataSetChanged();
            }
        });
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_wifi_network_footer, null, false);
        networksListView.addFooterView(footerView);
        networksListView.setAdapter(networksAdapter);

        networks.addAll(MySettings.getAllWifiNetworks());
        networksAdapter.notifyDataSetChanged();

        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualNetworkDialog();
            }
        });

        return view;
    }

    private void showManualNetworkDialog(){
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getActivity()).create();
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r = getActivity().getResources();
        float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        TextView ssidTextView = new TextView(getActivity());
        ssidTextView.setText(getActivity().getResources().getString(R.string.ssid_colon));
        ssidTextView.setTextSize(20);
        ssidTextView.setGravity(Gravity.CENTER);
        ssidTextView.setLayoutParams(layoutParams);

        TextView passwordTextView = new TextView(getActivity());
        passwordTextView.setText(getActivity().getResources().getString(R.string.password_colon));
        passwordTextView.setTextSize(20);
        passwordTextView.setGravity(Gravity.CENTER);
        passwordTextView.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        Resources r2 = getActivity().getResources();
        float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
        float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
        layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
        layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

        final EditText ssidEditText = new EditText(getActivity());
        ssidEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ssidEditText.setHint(getActivity().getResources().getString(R.string.ssid_hint));
        ssidEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        ssidEditText.setLayoutParams(layoutParams2);

        final EditText passwordEditText = new EditText(getActivity());
        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEditText.setHint(getActivity().getResources().getString(R.string.password_hint));
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        passwordEditText.setLayoutParams(layoutParams2);

        Button submitButton = new Button(getActivity());
        submitButton.setText(getActivity().getResources().getString(R.string.done));
        submitButton.setTextColor(getActivity().getResources().getColor(R.color.whiteColor));
        submitButton.setBackgroundColor(getActivity().getResources().getColor(R.color.blueColor));
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ssidEditText.getText().toString() != null && ssidEditText.getText().toString().length() >= 1){
                    if(passwordEditText.getText().toString() != null && passwordEditText.getText().toString().length() >= 4) {
                        WifiNetwork network = new WifiNetwork();
                        network.setSsid(ssidEditText.getText().toString());
                        network.setPassword(passwordEditText.getText().toString());
                        MySettings.addWifiNetwork(network);

                        networks.clear();
                        networks.addAll(MySettings.getAllWifiNetworks());
                        networksAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }else{
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(passwordEditText);
                    }
                }else{
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(ssidEditText);
                }
            }
        });

        layout.addView(ssidTextView);
        layout.addView(ssidEditText);
        layout.addView(passwordTextView);
        layout.addView(passwordEditText);
        layout.addView(submitButton);

        dialog.setView(layout);

        dialog.show();
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
