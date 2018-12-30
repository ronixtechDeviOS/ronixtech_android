package com.ronixtech.ronixhome.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.fragments.LoginFragment;
import com.ronixtech.ronixhome.fragments.VerificationFragment;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.showLoading(this);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Utils.log(TAG, "getDynamicLink - onSuccess", true);
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        Utils.log(TAG, "getDynamicLink - onSuccess - deepLink: " + deepLink, true);

                        if(deepLink != null && deepLink.toString().contains("oobCode")){
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            VerificationFragment verificationFragment = new VerificationFragment();
                            fragmentTransaction.replace(R.id.fragment_view, verificationFragment, "verificationFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commit();

                            Utils.log(TAG, "getDynamicLink - onSuccess - oobCode: " + deepLink.getQueryParameter("oobCode"), true);
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            if(mAuth != null){
                                mAuth.applyActionCode(deepLink.getQueryParameter("oobCode")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    FirebaseUser fbUser = mAuth.getCurrentUser();
                                                    if(fbUser != null){
                                                        if(fbUser.isEmailVerified()){
                                                            Utils.log(TAG, "fbUser is verified", true);
                                                            Utils.dismissLoading();
                                                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        }else{
                                                            Utils.log(TAG, "fbUser is not verified", true);
                                                            Utils.dismissLoading();
                                                        }
                                                    }else{
                                                        Utils.log(TAG, "fbUser is null", true);
                                                        Utils.dismissLoading();
                                                    }
                                                }
                                            });
                                        }else{
                                            Utils.log(TAG, "task failed: " + task.getException(), true);
                                            Utils.dismissLoading();
                                        }
                                    }
                                });
                            }else{
                                Utils.log(TAG, "mAuth is null", true);
                                Utils.dismissLoading();
                            }
                        }else if(deepLink != null && deepLink.toString().contains(Constants.PARAMETER_EMAIL)){
                            String email = deepLink.getQueryParameter(Constants.PARAMETER_EMAIL);
                            Utils.dismissLoading();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            LoginFragment loginFragment = new LoginFragment();
                            loginFragment.setResetPassEmail(email);
                            fragmentTransaction.replace(R.id.fragment_view, loginFragment, "loginFragment");
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragmentTransaction.commit();
                        }else{
                            Utils.dismissLoading();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.dismissLoading();
                        Utils.log(TAG, "getDynamicLink - onFailure: " + e.getMessage(), true);
                    }
                });

        if(isGooglePlayServicesAvailable(this)) {
            //Toast.makeText(this, "Google Play Services is available", Toast.LENGTH_SHORT).show();
            if(getIntent() != null && getIntent().getStringExtra("action") != null && getIntent().getStringExtra("action").equals("verify")){
                //go to VerificationFragment and wait for user verification
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                VerificationFragment verificationFragment = new VerificationFragment();
                fragmentTransaction.replace(R.id.fragment_view, verificationFragment, "verificationFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }else{
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                LoginFragment loginFragment = new LoginFragment();
                fragmentTransaction.replace(R.id.fragment_view, loginFragment, "loginFragment");
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.commit();
            }
        }else{
            Utils.showToast(this, "Google Play Services not available, app won't start", true);
        }
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
