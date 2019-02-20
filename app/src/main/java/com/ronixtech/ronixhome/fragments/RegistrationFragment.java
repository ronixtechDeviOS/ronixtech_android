package com.ronixtech.ronixhome.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.LoginActivity;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {
    private static final String TAG = RegistrationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    ImageView logoImageView;
    EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    ImageView togglePasswordVisibilityImageView;
    Button registerButton;

    boolean passwordVisible = false;

    private FirebaseAuth mAuth;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistrationFragment.
     */
    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
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
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        LoginActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.register), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        logoImageView = view.findViewById(R.id.logo_imageview);
        firstNameEditText = view.findViewById(R.id.registration_first_name_edittext);
        lastNameEditText = view.findViewById(R.id.registration_last_name_edittext);
        emailEditText = view.findViewById(R.id.registration_email_edittext);
        passwordEditText = view.findViewById(R.id.registration_password_edittext);
        togglePasswordVisibilityImageView = view.findViewById(R.id.toggle_password_visibility_imageview);
        registerButton = view.findViewById(R.id.register_button);

        mAuth = FirebaseAuth.getInstance();

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

        Drawable drawable = firstNameEditText.getBackground(); // get current EditText drawable
        drawable.setColorFilter(getActivity().getResources().getColor(R.color.darkGrayColor), PorterDuff.Mode.SRC_ATOP); // change the drawable color
        if(Build.VERSION.SDK_INT > 16) {
            firstNameEditText.setBackground(drawable); // set the new drawable to EditText
        }else{
            firstNameEditText.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }
        drawable = lastNameEditText.getBackground(); // get current EditText drawable
        drawable.setColorFilter(getActivity().getResources().getColor(R.color.darkGrayColor), PorterDuff.Mode.SRC_ATOP); // change the drawable color
        if(Build.VERSION.SDK_INT > 16) {
            lastNameEditText.setBackground(drawable); // set the new drawable to EditText
        }else{
            lastNameEditText.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }
        drawable = emailEditText.getBackground(); // get current EditText drawable
        drawable.setColorFilter(getActivity().getResources().getColor(R.color.darkGrayColor), PorterDuff.Mode.SRC_ATOP); // change the drawable color
        if(Build.VERSION.SDK_INT > 16) {
            emailEditText.setBackground(drawable); // set the new drawable to EditText
        }else{
            emailEditText.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }
        drawable = passwordEditText.getBackground(); // get current EditText drawable
        drawable.setColorFilter(getActivity().getResources().getColor(R.color.darkGrayColor), PorterDuff.Mode.SRC_ATOP); // change the drawable color
        if(Build.VERSION.SDK_INT > 16) {
            passwordEditText.setBackground(drawable); // set the new drawable to EditText
        }else{
            passwordEditText.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }

        /*togglePasswordVisibilityImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordVisible){
                    //hide password
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePasswordVisibilityImageView.setImageResource(R.drawable.password_off);
                    passwordVisible = false;
                }else{
                    //show password
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    togglePasswordVisibilityImageView.setImageResource(R.drawable.password_on);
                    passwordVisible = true;
                }
            }
        });*/

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.validateInputs(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                    register(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString());
                }
            }
        });

        /*firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                    Utils.setButtonEnabled(registerButton, true);
                }else{
                    Utils.setButtonEnabled(registerButton, false);
                }
            }
        });
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                    Utils.setButtonEnabled(registerButton, true);
                }else{
                    Utils.setButtonEnabled(registerButton, false);
                }
            }
        });
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                    Utils.setButtonEnabled(registerButton, true);
                }else{
                    Utils.setButtonEnabled(registerButton, false);
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(Utils.validateInputsWithoutYoyo(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                    Utils.setButtonEnabled(registerButton, true);
                }else{
                    Utils.setButtonEnabled(registerButton, false);
                }
            }
        });*/

        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //getCurrentFocus().
                    if(Utils.validateInputs(firstNameEditText, lastNameEditText, emailEditText, passwordEditText)){
                        register(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString());
                    }
                }
                return false;
            }
        });

        logoImageView.requestFocus();

        return view;
    }

    private void register(final String firstName, final  String lastName, final String email, final String password){

        Utils.showLoading(getActivity());

        if(mAuth != null){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign up success, update database with the signed-up user's information
                                Utils.log(TAG, "createUserWithEmail success", true);
                                FirebaseUser fbUser = mAuth.getCurrentUser();
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(firstName + "" + lastName)
                                        .build();

                                if(fbUser != null){
                                    fbUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                User user = new User();
                                                user.setEmail(email);
                                                user.setPassword(password);
                                                user.setFirstName(firstName);
                                                user.setLastName(lastName);
                                                MySettings.setActiveUser(user);
                                                ActionCodeSettings actionCodeSettings =
                                                        ActionCodeSettings.newBuilder()
                                                                // URL you want to redirect back to. The domain (www.example.com) for this
                                                                // URL must be whitelisted in the Firebase Console.
                                                                .setUrl(Constants.FIREBASE_DYNAMIC_LINK_VERIFICATION_URL)
                                                                // This must be true
                                                                .setHandleCodeInApp(true)
                                                                .setAndroidPackageName(
                                                                        Constants.PACKAGE_NAME,
                                                                        true, /* installIfNotAvailable */
                                                                        Constants.FIREBASE_DYNAMIC_LINKS_MIN_VERSION    /* minimumVersion */)
                                                                .build();

                                                FirebaseUser fbUser = mAuth.getCurrentUser();
                                                if(fbUser != null){
                                                    fbUser.sendEmailVerification(actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Utils.dismissLoading();
                                                                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.verification_mail_sent_successfully), true);
                                                                Utils.log(TAG, "sendEmailVerification Email sent.", true);
                                                                if(getActivity() != null) {
                                                                    Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    getActivity().finish();
                                                                }
                                                            } else {
                                                                // If sending mail fails, display a message to the user.
                                                                Utils.log(TAG, "sendEmailVerification failure: " + task.getException(), true);
                                                                if (task.getException() != null) {
                                                                    Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                                                                }
                                                                //Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.login_failed), Toast.LENGTH_SHORT).show();
                                                                Utils.dismissLoading();
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.registration_failed), true);
                                                    Utils.dismissLoading();
                                                }
                                            }else{
                                                Utils.log(TAG, "userProfileChangeRequest failure: " + task.getException(), true);
                                                if(task.getException() != null){
                                                    Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                                                }
                                                //Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.registration_failed), Toast.LENGTH_SHORT).show();
                                                Utils.dismissLoading();
                                            }
                                        }
                                    });
                                }else{
                                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.registration_failed), true);
                                    Utils.dismissLoading();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Utils.log(TAG, "createUserWithEmail failure: " + task.getException(), true);
                                if(task.getException() != null){
                                    Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                                }
                                //Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.registration_failed), Toast.LENGTH_SHORT).show();
                                Utils.dismissLoading();
                            }
                        }
                    });
        }else{
            Utils.dismissLoading();
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.registration_failed), true);
        }

        /*final CustomProgressDialog customProgressDialog = CustomProgressDialog.show(getActivity(), "", "");

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(Constants.PARAMETER_FIRST_NAME, firstName);
            jsonObject.put(Constants.PARAMETER_LAST_NAME, lastName);
            jsonObject.put(Constants.PARAMETER_EMAIL, email);
            jsonObject.put(Constants.PARAMETER_PASSWORD, password);
        }catch (JSONException e){

        }

        Log.d(TAG,  "register URL: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                registerButton.setEnabled(true);
                Log.d(TAG, "register response: " + response);

                if(response != null && response.has(Constants.PARAMETER_ERROR)){
                    try{
                        JSONObject errorMessages = jsonObject.getJSONObject(Constants.PARAMETER_ERROR);
                        Utils.showErrorIfFound(errorMessages, Constants.PARAMETER_EMAIL, emailEditText);
                        Utils.showErrorIfFound(errorMessages, Constants.PARAMETER_PASSWORD, passwordEditText);
                    }catch (JSONException e){

                    }
                }else{
                    Gson gson = new Gson();
                    Type type = new TypeToken<User>(){}.getType();
                    User user;
                    user = gson.fromJson(response.toString(), type);
                    MySettings.setActiveUser(user);
                }

                if (customProgressDialog != null) customProgressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                registerButton.setEnabled(true);
                if (customProgressDialog != null) customProgressDialog.dismiss();
                Log.d(TAG, "Volley Error: " + error.getMessage());
                Toast.makeText(getActivity(), getStringExtraInt(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        }){
            *//*@Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put(Constants.PARAMETER_FACEBOOK_ACCESS_TOKEN, accessToken);

                return params;
            }*//*
            @Override
            public Map<String,String> getHeaders(){
                Map<String,String> params = new HashMap<String, String>();
                //params.put("X-Requested-With", "XMLHttpRequest");
                //params.put(Constants.PARAMETER_LANGUAGE, MySettings.getActiveLanguage());

                return params;
            }
        };
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);*/
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
