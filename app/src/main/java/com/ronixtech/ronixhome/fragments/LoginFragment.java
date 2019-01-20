package com.ronixtech.ronixhome.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Backup;
import com.ronixtech.ronixhome.entities.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    private static final String EMAIL = "email";
    CallbackManager callbackManager;

    ImageView logoImageView;
    EditText emailEditText, passwordEditText;
    ImageView togglePasswordVisibilityImageView;
    Button loginButton, registerButton, resetPasswordButton;
    LoginButton facebookLoginButton;

    boolean passwordVisible = false;

    private FirebaseAuth mAuth;

    private String resetPassEmail;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        logoImageView = view.findViewById(R.id.logo_imageview);
        emailEditText = view.findViewById(R.id.login_email_edittext);
        passwordEditText = view.findViewById(R.id.login_password_edittext);
        togglePasswordVisibilityImageView = view.findViewById(R.id.toggle_password_visibility_imageview);
        loginButton = view.findViewById(R.id.login_button);
        resetPasswordButton = view.findViewById(R.id.forgot_password_button);
        facebookLoginButton = view.findViewById(R.id.facebook_login_button);
        registerButton = view.findViewById(R.id.register_button);

        mAuth = FirebaseAuth.getInstance();

        facebookLoginButton.setReadPermissions(Arrays.asList(EMAIL));
        facebookLoginButton.setFragment(this);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        loginFacebook(loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
;
        togglePasswordVisibilityImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        //show password
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        togglePasswordVisibilityImageView.setImageResource(R.drawable.password_on);
                        passwordVisible = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        //hide password
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        togglePasswordVisibilityImageView.setImageResource(R.drawable.password_off);
                        passwordVisible = false;
                        passwordEditText.setSelection(passwordEditText.getText().toString().length() >= 1 ? passwordEditText.getText().toString().length() : 0);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_CANCEL:
                        // RELEASED
                        //hide password
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        togglePasswordVisibilityImageView.setImageResource(R.drawable.password_off);
                        passwordVisible = false;
                        passwordEditText.setSelection(passwordEditText.getText().toString().length() >= 1 ? passwordEditText.getText().toString().length() : 0);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

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
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                RegistrationFragment registrationFragment = new RegistrationFragment();
                fragmentTransaction.replace(R.id.fragment_view, registrationFragment, "registrationFragment");
                fragmentTransaction.addToBackStack("registrationFragment");
                fragmentTransaction.commit();
            }
        });
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.validateInputs(emailEditText)){
                    resetPassword(emailEditText.getText().toString());
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.validateInputs(emailEditText, passwordEditText)){
                    login(emailEditText.getText().toString(), passwordEditText.getText().toString());
                }
            }
        });
        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //getCurrentFocus().
                    if(Utils.validateInputs(emailEditText, passwordEditText)){
                        login(emailEditText.getText().toString(), passwordEditText.getText().toString());
                    }
                }
                return false;
            }
        });

        if(resetPassEmail != null && resetPassEmail.length() >= 1){
            emailEditText.setText(resetPassEmail);
        }

        logoImageView.requestFocus();

        return view;
    }

    public void setResetPassEmail(String email){
        this.resetPassEmail = email;
        if(emailEditText != null){
            emailEditText.setText(resetPassEmail);
        }
    }

    private void login(final String email, final String password){
        Utils.showLoading(getActivity());
        if(mAuth != null){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser fbUser = mAuth.getCurrentUser();
                        if(fbUser != null){
                            User user = new User();
                            user.setEmail(fbUser.getEmail());
                            user.setPassword(password);
                            user.setFirstName(fbUser.getDisplayName());
                            MySettings.setActiveUser(user);
                            Utils.dismissLoading();

                            MySettings.clearNonUserData();

                            getBackupsIfFound();
                        }else{
                            Utils.dismissLoading();
                            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.login_failed), true);
                        }
                    }else {
                        // If sign in fails, display a message to the user.
                        Utils.log(TAG, "signInWithEmailAndPassword failure: " + task.getException(), true);
                        if(task.getException() != null){
                            Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                        }
                        //Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.login_failed), Toast.LENGTH_SHORT).show();
                        Utils.dismissLoading();
                    }
                }
            });
        }else{
            Utils.dismissLoading();
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.login_failed), true);
        }

        /*final CustomProgressDialog customProgressDialog = CustomProgressDialog.show(getActivity(), "", "");

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(Constants.PARAMETER_EMAIL, email);
            jsonObject.put(Constants.PARAMETER_PASSWORD, password);
        }catch (JSONException e){

        }

        Log.d(TAG,  "login URL: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginButton.setEnabled(true);
                Log.d(TAG, "login response: " + response);

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
                loginButton.setEnabled(true);
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

    private void resetPassword(String email){
        Utils.showLoading(getActivity());
        if(mAuth != null){
            ActionCodeSettings actionCodeSettings =
                    ActionCodeSettings.newBuilder()
                            // URL you want to redirect back to. The domain (www.example.com) for this
                            // URL must be whitelisted in the Firebase Console.
                            .setUrl(Constants.FIREBASE_DYNAMIC_LINK_RESET_PASSWORD_URL + "?" + Constants.PARAMETER_EMAIL + "=" + email)
                            // This must be true
                            .setHandleCodeInApp(false)
                            .setAndroidPackageName(
                                    Constants.PACKAGE_NAME,
                                    true, /* installIfNotAvailable */
                                    Constants.FIREBASE_DYNAMIC_LINKS_MIN_VERSION    /* minimumVersion */)
                            .build();
            mAuth.sendPasswordResetEmail(email, actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Utils.dismissLoading();
                        Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.password_reset_mail_sent_successfully), true);
                    }else{
                        Utils.log(TAG, "sendPasswordResetEmail failure: " + task.getException(), true);
                        if(task.getException() != null){
                            Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                        }
                        Utils.dismissLoading();
                    }
                }
            });
        }else{
            Utils.dismissLoading();
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.reset_password_failed), true);
        }
    }

    private void getBackupsIfFound(){
        Utils.showLoading(getActivity());

        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("exports").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Backup> backups = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Utils.log(TAG, document.getId() + " => " + document.getData(), true);
                    Backup backup = new Backup();
                    backup.setName((String)document.getData().get("name"));
                    backup.setTimestamp((long)document.getData().get("timestamp"));
                    if(document.getData().get("db_version") != null){
                        backup.setDbVersion((long)document.getData().get("db_version"));
                    }
                    backups.add(backup);
                }

                Utils.dismissLoading();

                if(backups.size() >= 1){
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    ImportDataFirstTimeFragment importDataFirstTimeFragment = new ImportDataFirstTimeFragment();
                    fragmentTransaction.replace(R.id.fragment_view, importDataFirstTimeFragment, "importDataFirstTimeFragment");
                    //fragmentTransaction.addToBackStack("importDataFirstTimeFragment");
                    fragmentTransaction.commit();
                }else{
                    Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mainIntent);
                    getActivity().finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }
        });
    }

    private void loginFacebook(final String accessToken){
        String url = Constants.LOGIN_URL;

        Utils.showLoading(getActivity());

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(Constants.PARAMETER_FACEBOOK_ACCESS_TOKEN, accessToken);
        }catch (JSONException e){

        }

        Utils.log(TAG, "loginFacebook URL: " + url, true);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginButton.setEnabled(true);
                Utils.log(TAG, "login response: " + response, true);

                Utils.dismissLoading();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loginButton.setEnabled(true);
                Utils.dismissLoading();
                Utils.log(TAG, "Volley Error: " + error.getMessage(), true);
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.server_connection_error), true);
            }
        }){
            /*@Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put(Constants.PARAMETER_FACEBOOK_ACCESS_TOKEN, accessToken);

                return params;
            }*/
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
        HttpConnector.getInstance(getActivity()).addToRequestQueue(request);
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
