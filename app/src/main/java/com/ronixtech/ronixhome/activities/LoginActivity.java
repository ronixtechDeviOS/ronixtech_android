package com.ronixtech.ronixhome.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.fragments.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(isGooglePlayServicesAvailable(this)) {
            Toast.makeText(this, "Google Play Services is available", Toast.LENGTH_SHORT).show();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LoginFragment loginFragment = new LoginFragment();
            fragmentTransaction.replace(R.id.fragment_view, loginFragment, "loginFragment");
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }else{
            Toast.makeText(this, "Google Play Services not available, app won't start", Toast.LENGTH_SHORT).show();
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
