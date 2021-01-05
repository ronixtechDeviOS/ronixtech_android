package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends android.support.v4.app.Fragment {
    private static final String TAG = UserProfileFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ImageView logoImageView;
    EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    ImageView togglePasswordVisibilityImageView;
    Button saveButton;
    User user;

    boolean passwordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.profile), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        logoImageView = view.findViewById(R.id.logo_imageview);
        firstNameEditText = view.findViewById(R.id.registration_first_name_edittext);
        lastNameEditText = view.findViewById(R.id.registration_last_name_edittext);
        emailEditText = view.findViewById(R.id.registration_email_edittext);
        passwordEditText = view.findViewById(R.id.registration_password_edittext);
        togglePasswordVisibilityImageView = view.findViewById(R.id.toggle_password_visibility_imageview);
        saveButton = view.findViewById(R.id.save_button);
        user = new User();
        //get current user
        user = MySettings.getActiveUser();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        togglePasswordVisibilityImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        //show password
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        //togglePasswordVisibilityImageView.setImageResource(R.drawable.password_on);
                        passwordVisible = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        //hide password
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        //togglePasswordVisibilityImageView.setImageResource(R.drawable.password_off);
                        passwordVisible = false;
                        passwordEditText.setSelection(passwordEditText.getText().toString().length() >= 1 ? passwordEditText.getText().toString().length() : 0);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_CANCEL:
                        // RELEASED
                        //hide password
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        //togglePasswordVisibilityImageView.setImageResource(R.drawable.password_off);
                        passwordVisible = false;
                        passwordEditText.setSelection(passwordEditText.getText().toString().length() >= 1 ? passwordEditText.getText().toString().length() : 0);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        if (user != null) {
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            emailEditText.setText(user.getEmail());
            passwordEditText.setText(user.getPassword());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utils.validateInputs(firstNameEditText,emailEditText,passwordEditText)){
                    if(firebaseUser!=null)
                    {

                    }


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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}

