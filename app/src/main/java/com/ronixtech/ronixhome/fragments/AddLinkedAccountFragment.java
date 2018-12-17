package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddLinkedAccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddLinkedAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddLinkedAccountFragment extends android.support.v4.app.Fragment {
    private static final String TAG = AddLinkedAccountFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    EditText accountNameEditText, accountEmailEditText;
    Button addButton;

    public AddLinkedAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddLinkedAccountFragment.
     */
    public static AddLinkedAccountFragment newInstance(String param1, String param2) {
        AddLinkedAccountFragment fragment = new AddLinkedAccountFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_linked_account, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.add_linked_account), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        accountNameEditText = view.findViewById(R.id.account_name_edittedxt);
        accountEmailEditText = view.findViewById(R.id.account_email_edittext);
        addButton = view.findViewById(R.id.add_button);

        accountEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(validateInputsWithoutYoyo()){
                    Utils.setButtonEnabled(addButton, true);
                }else{
                    Utils.setButtonEnabled(addButton, false);
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputs()){
                    Utils.showLoading(getActivity());

                    long timestamp = new Date().getTime();

                    User user = new User();
                    user.setFullName(accountNameEditText.getText().toString());
                    user.setEmail(accountEmailEditText.getText().toString());
                    user.setLinked(true);
                    user.setLinkTimestamp(timestamp);

                    //TODO add user to firebase DB auth users for this user and send them an email address/notification
                    //TODO the recepient must accept and update his users/email/linked_to_accounts collection
                    addLinkedAccount(user, timestamp);
                }
            }
        });

        return view;
    }

    private void addLinkedAccount(User user, long linkDate){
        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("name", user.getFullName());
        map.put("email", user.getEmail());
        map.put("link_timestamp", linkDate);
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("linked_accounts").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                //go to successFragment
                MySettings.addUser(user);

                Utils.dismissLoading();

                if(getFragmentManager() != null){
                    getFragmentManager().popBackStack();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.dismissLoading();
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.uploading_database_file_failed), true);
                if(getFragmentManager() != null){
                    getFragmentManager().popBackStack();
                }
            }
        });
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        if(!Utils.validateInputs(accountNameEditText, accountEmailEditText)){
            inputsValid = false;
        }

        return inputsValid;
    }

    private boolean validateInputsWithoutYoyo(){
        boolean inputsValid = true;

        if(!Utils.validateInputsWithoutYoyo(accountNameEditText, accountEmailEditText)){
            inputsValid = false;
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