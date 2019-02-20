package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.LoginActivity;
import com.ronixtech.ronixhome.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImportDataFirstTimeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImportDataFirstTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImportDataFirstTimeFragment extends android.support.v4.app.Fragment {
    private static final String TAG = ImportDataFirstTimeFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    public ImportDataFirstTimeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImportDataFirstTimeFragment.
     */
    public static ImportDataFirstTimeFragment newInstance(String param1, String param2) {
        ImportDataFirstTimeFragment fragment = new ImportDataFirstTimeFragment();
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
        View view = inflater.inflate(R.layout.fragment_import_data_first_time, container, false);
        LoginActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.import_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        //MySettings.clearNonUserData();

        Button importData = view.findViewById(R.id.restore_data_button);
        TextView skipTextView= view.findViewById(R.id.skip_textview);

        skipTextView.setPaintFlags(skipTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        importData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                mainIntent.putExtra("action", "import_data");
                startActivity(mainIntent);
                getActivity().finish();
            }
        });

        skipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
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
