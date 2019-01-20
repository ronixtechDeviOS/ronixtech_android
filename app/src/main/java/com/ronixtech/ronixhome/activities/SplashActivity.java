package com.ronixtech.ronixhome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.HttpConnector;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        //ActionBar actionBar = getActionBar();
        //actionBar.hide();

        ImageView view = findViewById(R.id.splash_animating_view);
        view.setImageResource(R.drawable.logo_white_big);
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_animation));
        //int widthInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        //int heightInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());

         //New Handler to start the Menu-Activity
         //and close this Splash-Screen after some seconds.
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                goToMainActivity();
            }
        }, SPLASH_DISPLAY_LENGTH);*/

        getLatestFirmwareVersion();
    }

    private void goToMainActivity(){
        /* Create an Intent that will start the MainActivity. */
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void getLatestFirmwareVersion(){
        String url = Constants.DEVICE_LATEST_FIRMWARE_VERSIONS_URL;

        Utils.log(TAG, "getLatestFirmwareVersions URL: " + url, true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Utils.log(TAG, "getLatestFirmwareVersions response: " + response, true);
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    for(int x = 0; x < length; x++){
                        JSONObject jsonObject = jsonArray.getJSONObject(x);
                        if(jsonObject.has("unit_type_id")){
                            String deviceTypeString = jsonObject.getString("unit_type_id");
                            int deviceType = Integer.valueOf(deviceTypeString);
                            if(jsonObject.has("latest_firmware_version")){
                                String latestVersionString = jsonObject.getString("latest_firmware_version");
                                MySettings.setDeviceLatestWiFiFirmwareVersion(deviceType, latestVersionString);
                            }
                            if(jsonObject.has("latest_hw_firmware_version")){
                                String latestHWVersionString = jsonObject.getString("latest_hw_firmware_version");
                                MySettings.setDeviceLatestHWFirmwareVersion(deviceType, latestHWVersionString);
                            }
                        }
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Json exception: " + e.getMessage(), true);
                }finally {
                    goToMainActivity();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.log(TAG, "Volley error: " + error.getMessage(), true);
                goToMainActivity();
            }
        });
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.SERVER_TIMEOUT, Constants.SERVER_NUMBER_OF_RETRIES, 0f));
        HttpConnector.getInstance(this).addToRequestQueue(stringRequest);
    }
}