package com.ronixtech.ronixhome;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.alexa.LoginManager;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.PIRData;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.SoundDeviceData;
import com.ronixtech.ronixhome.entities.TimeUnit;
import com.ronixtech.ronixhome.entities.Type;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final int ANIMATION_TYPE_TRANSLATION = 0;
    public static final int ANIMATION_TYPE_FADE = 1;
    private final static String PATTERN = "yyyy/MM/dd hh:mm:ss";

    private static CustomProgressDialog customProgressDialog;

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static FragmentTransaction setAnimations(FragmentTransaction originalFragmentTransaction, int animationType){
        switch (animationType){
            case ANIMATION_TYPE_TRANSLATION:
                originalFragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                return originalFragmentTransaction;
            case ANIMATION_TYPE_FADE:
                originalFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                return originalFragmentTransaction;

            default:
                return originalFragmentTransaction;
        }
    }

    public static void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x + 1);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    public static void justifyListViewHeightBasedOnChildren (ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }

        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    public static void setGridViewWidthBasedOnChildren(GridView gridView) {
        ListAdapter gridViewAdapter = gridView.getAdapter();
        if (gridViewAdapter == null) {
            return;
        }

        ViewGroup vg = gridView;
        int totalWidth = 0;
        for (int i = 0; i < gridViewAdapter.getCount(); i++) {
            View gridItem = gridViewAdapter.getView(i, null, vg);
            gridItem.measure(0, 0);
            totalWidth += gridItem.getMeasuredHeight(); //Hack: use getMeasuredHeight instead of getMeasuredWidth as each grid item is square anyway, becauase of ScrollingTextviews variable width
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.width = totalWidth;
        gridView.setLayoutParams(params);
    }

    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public static String getDateString(long timestamp){
        String dateString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        dateString = day + "/" + month + "/" + year;

        return dateString;
    }

    private static SimpleDateFormat simpleDateFormatDateHoursMinute;
    public static String getTimeStringDateHoursMinutes(long timestamp){
        String timeString = "";
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String amPM = "";
        if(calendar.get(Calendar.AM_PM) == Calendar.PM){
            amPM = "PM";
        }else{
            amPM = "AM";
        }
        timeString = hour + ":" + minute + ":" + second + " " + amPM;*/
        if(simpleDateFormatDateHoursMinute == null) {
            simpleDateFormatDateHoursMinute = new SimpleDateFormat("dd/MM/yy  h:mm a");
        }
        timeString = simpleDateFormatDateHoursMinute.format(timestamp);
        return timeString;
    }

    public static String getTimeStringHoursMinutes(long timestamp){
        String timeString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        timeString = hour + ":" + minute;
        return timeString;
    }

    private static SimpleDateFormat simpleDateFormatHoursMinutesSeconds;
    public static String getTimeStringHoursMinutesSeconds(long timestamp){
        String timeString = "";
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String amPM = "";
        if(calendar.get(Calendar.AM_PM) == Calendar.PM){
            amPM = "PM";
        }else{
            amPM = "AM";
        }
        timeString = hour + ":" + minute + ":" + second + " " + amPM;*/
        if(simpleDateFormatHoursMinutesSeconds == null) {
            simpleDateFormatHoursMinutesSeconds = new SimpleDateFormat("h:mm:ss a");
        }
        timeString = simpleDateFormatHoursMinutesSeconds.format(timestamp);
        return timeString;
    }

    public static boolean validateInputs(EditText... editTexts){
        boolean inputsValid = true;

        for (EditText editText:editTexts) {
            if(editText == null || editText.getText().toString() == null || editText.getText().toString().length() < 1){
                inputsValid = false;
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .repeat(1)
                        .playOn(editText);
            }
        }

        return inputsValid;
    }

    public static boolean validateInputsWithoutYoyo(EditText... editTexts){
        boolean inputsValid = true;

        for (EditText editText:editTexts) {
            if(editText == null || editText.getText().toString() == null || editText.getText().toString().length() < 1){
                inputsValid = false;
            }
        }

        return inputsValid;
    }

    public static void setButtonEnabled(Button button, boolean enabled){
        if(enabled){
            button.setBackgroundResource(R.drawable.button_background_round_blue);
            button.setEnabled(true);
        }else{
            button.setBackgroundResource(R.drawable.button_background_round_gray);
            button.setEnabled(false);
        }
    }

    public static void showErrorIfFound(JSONObject errorMessages, String errorKey, EditText editText){
        if(errorMessages != null){
            try{
                if(errorMessages.has(errorKey)){
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(1)
                            .playOn(editText);
                    String errorMessage = errorMessages.getJSONArray(errorKey).getString(0);
                    editText.setError(errorMessage);
                }
            }catch (JSONException e){
                Utils.log(TAG, "Json exception: " + e.getMessage(), true);
            }

        }
    }

    public static void showNotification(Device device){
        /*//Create the intent that’ll fire when the user taps the notification//
        Intent intent = new Intent(MyApp.getInstance(), MainActivity.class);
        intent.putExtra("deviceID", device.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MyApp.getInstance());

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSmallIcon(R.drawable.logo_white_big)
                .setContentTitle("Your smart controller " + device.getName() + " has been updated.")
                .setContentText("Click here to view more details.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setChannelId(Constants.CHANNEL_ID)
                .setContentIntent(pendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        //Get an instance of NotificationManager//
        NotificationManager mNotificationManager = (NotificationManager) MyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one.
        mNotificationManager.notify(((int) device.getId()) *//* ID of notification *//*, mBuilder.build());*/
    }

    public static void showUpdatingNotification(){
        /*//Create the intent that’ll fire when the user taps the notification//
        Intent intent = new Intent(MyApp.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MyApp.getInstance());

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSmallIcon(R.drawable.logo_white_big)
                .setContentTitle("Your devices are being updated.")
                .setContentText("Some functionality may not be available temporarily.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setChannelId(Constants.CHANNEL_ID)
                .setContentIntent(pendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        //Get an instance of NotificationManager//
        NotificationManager mNotificationManager = (NotificationManager) MyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one.
        mNotificationManager.notify(((int) Constants.UPDATING_DEVICES_NOTIFICATION) *//* ID of notification *//*, mBuilder.build());*/
    }

    public static void hideUpdatingNotification(){
        NotificationManager mNotificationManager = (NotificationManager) MyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((int) Constants.UPDATING_DEVICES_NOTIFICATION);
    }

    public static String getDeviceInfo(Context context){
        String deviceInfo = "";
        if(context != null){
            String versionName = Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT + 1].getName();
            String[] versionNames = new String[]{
                    "ANDROID BASE", "ANDROID BASE 1.1", "CUPCAKE", "DONUT",
                    "ECLAIR", "ECLAIR_0_1", "ECLAIR_MR1", "FROYO", "GINGERBREAD",
                    "GINGERBREAD_MR1", "HONEYCOMB", "HONEYCOMB_MR1", "HONEYCOMB_MR2",
                    "ICE_CREAM_SANDWICH", "ICE_CREAM_SANDWICH_MR1",
                    "JELLY_BEAN", "JELLY_BEAN_MR1", "JELLY_BEAN_MR2", "KITKAT", "KITKAT_WATCH",
                    "LOLLIPOP", "LOLLIPOP_MR1", "MARSHMALLOW", "NOUGAT", "OREO", "OREO_MR1"
            };
            int nameIndex = Build.VERSION.SDK_INT - 1;
            if (nameIndex < versionNames.length) {
                versionName = versionNames[nameIndex];
            }

            deviceInfo = "Log Date: " + getTimeStringDateHoursMinutes(new java.util.Date().getTime())  + "\n";
            deviceInfo = deviceInfo.concat("Android version: " + versionName  + " (" + Build.VERSION.RELEASE + ")" + "\n");
            deviceInfo = deviceInfo.concat("SDK v" + Build.VERSION.SDK_INT + "\n");
            deviceInfo = deviceInfo.concat("Device Manufacturer: " + Build.MANUFACTURER + "\n");
            deviceInfo = deviceInfo.concat("Device Brand: " + Build.BRAND + "\n");
            deviceInfo = deviceInfo.concat("Device Model: " + android.os.Build.MODEL + "\n");
        }

        return deviceInfo;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        //return Runtime.getRuntime().availableProcessors();
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Utils.log(TAG, "CPU Count: "+files.length, true);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Print exception
            Utils.log(TAG, "CPU Count: Failed.", true);
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }

    public static void generatePlaceTypes(){
        List<Type> placeTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_PLACE, "Bank", "", R.drawable.place_type_bank, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_bank), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Church", "", R.drawable.place_type_church, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_church), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Cinema", "", R.drawable.place_type_cinema, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_cinema), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Clinic", "", R.drawable.place_type_clinic, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_clinic), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Coffee Shop", "", R.drawable.place_type_coffee_shop, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_coffee_shop), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Company", "", R.drawable.place_type_office, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_office),"", R.drawable.place_background_company, MyApp.getInstance().getResources().getResourceName(R.drawable.place_background_company), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Embassy", "", R.drawable.place_type_embassy, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_embassy), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hospital", "", R.drawable.place_type_hospital, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_hospital), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hotel", "", R.drawable.place_type_hotel, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_hotel), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "House", "", R.drawable.place_type_house, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_house),"", R.drawable.place_background_house_1, MyApp.getInstance().getResources().getResourceName(R.drawable.place_background_house_1), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Mosque", "", R.drawable.place_type_mosque, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_mosque), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Museum", "", R.drawable.place_type_museum, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_museum), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Pharmacy", "", R.drawable.place_type_pharmacy, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_pharmacy), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "School", "", R.drawable.place_type_school, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_school), "");
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Store", "", R.drawable.place_type_store, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_store), "");
        placeTypes.add(type);

        for (Type ty: placeTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateFloorTypes(){
        List<Type> floorTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_FLOOR, "Floor", "", R.drawable.floor_icon, MyApp.getInstance().getResources().getResourceName(R.drawable.floor_icon), "");
        floorTypes.add(type);

        for (Type ty: floorTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateRoomTypes(){
        List<Type> roomTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_ROOM, "Balcony", "", R.drawable.room_type_balcony, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_balcony),"", R.drawable.room_background_balcony, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_balcony), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Basement", "", R.drawable.room_type_basement, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_basement),"", R.drawable.room_background_basement, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_basement), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bathroom", "", R.drawable.room_type_bathroom, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bathroom),"", R.drawable.room_background_bathroom, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_bathroom), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bedroom", "", R.drawable.room_type_bedroom, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bedroom),"", R.drawable.room_background_bedroom_sample, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_bedroom_sample), "0e8c9b");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kids Bedroom", "", R.drawable.room_type_bedroom_kids, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bedroom_kids),"", R.drawable.room_type_bedroom_kids, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_bedroom_kids), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Corridor", "", R.drawable.room_type_corridor, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_corridor),"", R.drawable.room_background_corridor, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_corridor), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Dining", "", R.drawable.room_type_dining, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_dining),"", R.drawable.room_background_dining, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_dining), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Entryway", "", R.drawable.room_type_entryway, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_entryway),"", R.drawable.room_background_entryway, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_entryway), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Frontyard", "", R.drawable.room_type_frontyard, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_frontyard),"", R.drawable.room_background_frontyard, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_frontyard), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Garage", "", R.drawable.room_type_garage, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_garage),"", R.drawable.room_background_garage, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_garage), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Home Cinema", "", R.drawable.room_type_home_cinema, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_home_cinema),"", R.drawable.room_background_home_cinema, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_home_cinema), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kitchen", "", R.drawable.room_type_kitchen, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_kitchen),"", R.drawable.room_background_kitchen, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_kitchen), "7e3086");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Library", "", R.drawable.room_type_library, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_library),"", R.drawable.room_background_library, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_library), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Living Room", "", R.drawable.room_type_living_room, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_living_room),"", R.drawable.room_background_living_room, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_living_room), "6ca731");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Office", "", R.drawable.room_type_office, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_office),"", R.drawable.room_background_office, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_office), "71716f");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Terrace", "", R.drawable.room_type_terrace, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_terrace),"", R.drawable.room_background_terrace, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_terrace), "");
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Toilet", "", R.drawable.room_type_toilet, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_toilet),"", R.drawable.room_background_toilet, MyApp.getInstance().getResources().getResourceName(R.drawable.room_background_toilet), "");
        roomTypes.add(type);
        for (Type ty: roomTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateLineTypes(){
        List<Type> lineTypes = new ArrayList<>();

        /*Type type = new Type(Constants.TYPE_LINE, "Air Conditioner", "", R.drawable.line_type_air_conditioner, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_air_conditioner), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Appliance Plug", "", R.drawable.line_type_appliance_plug, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_appliance_plug), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Attic Fan", "", R.drawable.line_type_attic_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_attic_fan), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Boiler", "", R.drawable.line_type_boiler, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_boiler), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan", "", R.drawable.line_type_ceiling_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_ceiling_fan), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan Light", "", R.drawable.line_type_ceiling_fan_light, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_ceiling_fan_light), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Chandelier", "", R.drawable.line_type_chandelier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_chandelier), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Clothes Iron", "", R.drawable.line_type_clothes_iron, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_clothes_iron), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Coffee Maker", "", R.drawable.line_type_coffee_maker, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_coffee_maker), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Computer", "", R.drawable.line_type_computer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_computer), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Dishwasher", "", R.drawable.line_type_dishwasher, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_dishwasher), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Door", "", R.drawable.line_type_door, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_door), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fan", "", R.drawable.line_type_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_fan), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fluorescent Lamp", "", R.drawable.line_type_fluorescent_lamp, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_fluorescent_lamp), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Flush", "", R.drawable.line_type_flush, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_flush), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Garage", "", R.drawable.line_type_garage, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_garage), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Hair Dryer", "", R.drawable.line_type_hair_dryer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_hair_dryer), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Humidifier", "", R.drawable.line_type_humidifier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_humidifier), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Kettle", "", R.drawable.line_type_kettle, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_kettle), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "LED Lamp", "", R.drawable.line_type_led__lamp, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_led__lamp), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Light Fixture", "", R.drawable.line_type_light_fixture, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_light_fixture), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Microwave Oven", "", R.drawable.line_type_microwave_oven, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_microwave_oven), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Motion Sensor", "", R.drawable.line_type_motion_sensor, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_motion_sensor), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Oven", "", R.drawable.line_type_oven, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_oven), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Pendant", "", R.drawable.line_type_pendant, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_pendant), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Photocopier", "", R.drawable.line_type_photocopier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_photocopier), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Printer", "", R.drawable.line_type_printer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_printer), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Recessed", "", R.drawable.line_type_recessed, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_recessed), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Refrigerator", "", R.drawable.line_type_refrigerator, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_refrigerator), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Window", "", R.drawable.line_type_window, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_window), "");
        lineTypes.add(type);*/

        Type type = new Type(Constants.TYPE_LINE, "Lamp", "", R.drawable.line_type_lamp_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_lamp_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Chandelier", "", R.drawable.line_type_chandelier_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_chandelier_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Light", "", R.drawable.line_type_cieling_light_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_cieling_light_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Tube Light", "", R.drawable.line_type_tube_light_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_tube_light_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Table Light", "", R.drawable.line_type_table_light_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_table_light_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Standing Light", "", R.drawable.line_type_standing_lamp_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_standing_lamp_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan", "", R.drawable.line_type_ceiling_fan_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_ceiling_fan_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fan", "", R.drawable.line_type_fan_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_fan_white), "");
        lineTypes.add(type);



        type = new Type(Constants.TYPE_LINE_PLUG, "Appliance Plug", "", R.drawable.line_type_appliance_plug_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_appliance_plug_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE_PLUG, "Toaster", "", R.drawable.line_type_toaster_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_toaster_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE_PLUG, "TV", "", R.drawable.line_type_tv_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_tv_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE_PLUG, "Hair Dryer", "", R.drawable.line_type_hair_dryer_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_hair_dryer_white), "");
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE_PLUG, "Humidifier", "", R.drawable.line_type_humidifier_white, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_humidifier_white), "");
        lineTypes.add(type);


        for (Type ty: lineTypes) {
            MySettings.addType(ty);
        }
    }

    public static String getColorHex(String hexColor, int transparency){
        String colorString = "#";

        colorString = colorString.concat(Utils.getTransparencyHex(transparency));

        colorString = colorString.concat(hexColor.replace("#", ""));

        return colorString;
    }

    public static String getTransparencyHex(int transparency){
        transparency = 100 - transparency;
        String transparencyString = "";
        if(transparency == 0){
            transparencyString = "00";
        }else if(transparency == 5){
            transparencyString = "0D";
        }else if(transparency == 10){
            transparencyString = "1A";
        }else if(transparency == 15){
            transparencyString = "26";
        }else if(transparency == 20){
            transparencyString = "33";
        }else if(transparency == 25){
            transparencyString = "40";
        }else if(transparency == 30){
            transparencyString = "4D";
        }else if(transparency == 35){
            transparencyString = "59";
        }else if(transparency == 40){
            transparencyString = "66";
        }else if(transparency == 45){
            transparencyString = "73";
        }else if(transparency == 50){
            transparencyString = "80";
        }else if(transparency == 55){
            transparencyString = "8C";
        }else if(transparency == 60){
            transparencyString = "99";
        }else if(transparency == 65){
            transparencyString = "A6";
        }else if(transparency == 70){
            transparencyString = "B3";
        }else if(transparency == 75){
            transparencyString = "BF";
        }else if(transparency == 80){
            transparencyString = "CC";
        }else if(transparency == 85){
            transparencyString = "D9";
        }else if(transparency == 90){
            transparencyString = "E6";
        }else if(transparency == 95){
            transparencyString = "F2";
        }else if(transparency == 100){
            transparencyString = "FF";
        }
        return transparencyString;
    }

    public static List<TimeUnit> getTimeUnits(){
        List<TimeUnit> timeUnits = new ArrayList<>();
        TimeUnit timeUnit = new TimeUnit(TimeUnit.UNIT_SECONDS, "Seconds");
        timeUnits.add(timeUnit);
        timeUnit = new TimeUnit(TimeUnit.UNIT_MINUTES, "Minutes");
        timeUnits.add(timeUnit);
        timeUnit = new TimeUnit(TimeUnit.UNIT_HOURS, "Hours");
        timeUnits.add(timeUnit);
        timeUnit = new TimeUnit(TimeUnit.UNIT_DAYS, "Days");
        timeUnits.add(timeUnit);
        timeUnit = new TimeUnit(TimeUnit.UNIT_INDEFINITE, "No time limit");
        timeUnits.add(timeUnit);

        return timeUnits;
    }

    private static boolean isAppEnabled(Context context, String packageName) {
        boolean appStatus = false;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (ai != null) {
                appStatus = ai.enabled;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appStatus;
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    public static void openApp(Context context, String appName, String packageName) {
        if (isAppInstalled(context, packageName)){
            if (isAppEnabled(context, packageName)) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
            } else {
                Utils.showToast(context, appName + " is not enabled.", true);
            }
        } else {
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context)
                    //set icon
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    //set title
                    .setTitle(appName)
                    //set message
                    .setMessage(Utils.getStringExtraInt(context, R.string.upnp_app_not_available_message))
                    //set positive button
                    .setPositiveButton(Utils.getStringExtraInt(context, R.string.go_to_play_store), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            openPlayStore(context, packageName);
                        }
                    })
                    //set negative button
                    .setNegativeButton(Utils.getStringExtraInt(context, R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what should happen when negative button is clicked
                        }
                    })
                    .show();
        }
    }

    public static void openPlayStore(Context context, String packageName){
        //go to play store
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static class InternetChecker extends AsyncTask<Void, Void, Boolean>{

        private OnConnectionCallback onConnectionCallback;
        private Context context;

        public InternetChecker(Context context, OnConnectionCallback onConnectionCallback) {
            super();
            this.onConnectionCallback = onConnectionCallback;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (context == null)
                return false;

            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            if (b) {
                onConnectionCallback.onConnectionSuccess();
            } else {
                String msg = "No Internet Connection";
                if (context == null)
                    msg = "Context is null";
                onConnectionCallback.onConnectionFail(msg);
            }

        }

        public interface OnConnectionCallback {
            void onConnectionSuccess();

            void onConnectionFail(String errorMsg);
        }
    }

    public static void showLoading(Context context){
        customProgressDialog = CustomProgressDialog.show(context, "", "");
    }

    public static void dismissLoading(){
        if(customProgressDialog != null && customProgressDialog.isShowing()){
            customProgressDialog.dismiss();
        }
    }

    public static class GooglePlayAppVersion extends AsyncTask<String, Void, String> {

        private final String packageName;
        private final Listener listener;
        public interface Listener {
            void result(String version);
        }

        public GooglePlayAppVersion(String packageName, Listener listener) {
            this.packageName = packageName;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            return getPlayStoreAppVersion(String.format("https://play.google.com/store/apps/details?id=%s", packageName));
        }

        @Override
        protected void onPostExecute(String version) {
            listener.result(version);
        }

        @Nullable
        private static String getPlayStoreAppVersion(String appUrlString) {
            String
                    currentVersion_PatternSeq = "<div[^>]*?>Current\\sVersion</div><span[^>]*?>(.*?)><div[^>]*?>(.*?)><span[^>]*?>(.*?)</span>",
                    appVersion_PatternSeq = "htlgb\">([^<]*)</s";
            try {
                URLConnection connection = new URL(appUrlString).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder sourceCode = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sourceCode.append(line);

                    // Get the current version pattern sequence
                    String versionString = getAppVersion(currentVersion_PatternSeq, sourceCode.toString());
                    if (versionString == null) return null;

                    // get version from "htlgb">X.X.X</span>
                    return getAppVersion(appVersion_PatternSeq, versionString);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Nullable
        private static String getAppVersion(String patternString, String input) {
            try {
                Pattern pattern = Pattern.compile(patternString);
                if (pattern == null) return null;
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) return matcher.group(1);
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static int difference(int firstNumber, int secondNumber){
        return Math.abs(firstNumber - secondNumber);
    }

    public static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }

    public static long getTimeUnitMilliseconds(int timeUnit, int value){
        long milliseconds = 0;
        switch (timeUnit){
            case TimeUnit.UNIT_SECONDS:
                milliseconds = value * 60;
                break;
            case TimeUnit.UNIT_MINUTES:
                milliseconds = value * 60 * 60;
                break;
            case TimeUnit.UNIT_HOURS:
                milliseconds = value * 60 * 60 * 60;
                break;
            case TimeUnit.UNIT_DAYS:
                milliseconds = value * 60 * 60 * 60 * 24;
                break;
            case TimeUnit.UNIT_INDEFINITE:
                milliseconds = Long.MAX_VALUE;
                break;
        }

        return milliseconds;
    }

    public static class AddressGeocoder extends AsyncTask<Void, Void, Boolean>{

        private OnGeocodingCallback onGeocodingCallback;
        private Context context;
        double latitude, longitude;
        String errorMessage = "";

        String addressString = "";
        String city = "";
        String state = "";
        String country = "";
        String zipCode = "";

        public AddressGeocoder(Context context, double latitude, double longitude, OnGeocodingCallback onGeocodingCallback) {
            super();
            this.onGeocodingCallback = onGeocodingCallback;
            this.context = context;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (context == null) {
                errorMessage = "No Internet Connection";
                return false;
            }

            List<Address> addresses = null;

            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = Utils.getStringExtraInt(context, R.string.geocoding_error_service_not_available);
                Utils.log(TAG, errorMessage, true);
                return false;
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = Utils.getStringExtraInt(context, R.string.geocoding_error_invalid_lat_long);
                Utils.log(TAG, errorMessage + ". " + "Latitude = " + latitude + ", Longitude = " + longitude, true);
                return false;
            }catch (Exception e) {
                errorMessage = errorMessage;
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                return false;
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = Utils.getStringExtraInt(context, R.string.geocoding_error_no_address_found);
                    Utils.log(TAG, errorMessage, true);
                }
                return false;
            } else {
                Utils.log(TAG, Utils.getStringExtraInt(context, R.string.geocoding_error_address_found), true);
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                addressString = TextUtils.join(System.getProperty("line.separator"), addressFragments);
                if(address.getLocality() != null && address.getLocality().length() >= 1){
                    city = address.getLocality();
                }
                if(address.getAdminArea() != null && address.getAdminArea().length() >= 1){
                    state = address.getAdminArea();
                }
                if(address.getCountryName() != null && address.getCountryName().length() >= 1){
                    country = address.getCountryName();
                }
                if(address.getPostalCode() != null && address.getPostalCode().length() >= 1){
                    zipCode = address.getPostalCode();
                }

                return true;
            }

        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            if(b){
                onGeocodingCallback.onGeocodingSuccess(addressString, city, state, country, zipCode);
            }else{
                onGeocodingCallback.onGeocodingFail(errorMessage);
            }

        }

        public interface OnGeocodingCallback {
            void onGeocodingSuccess(String address, String city, String state, String country, String zipCode);
            void onGeocodingFail(String errorMsg);
        }
    }

    public static Bitmap resizeBitmapByDimensions(Bitmap bitmap, int width, int height, boolean recycle) {
        if (width == bitmap.getWidth() && height == bitmap.getHeight())
            return bitmap;

        //Bitmap target = Bitmap.createScaledBitmap(bitmap, width, height, false);

        float scale = Math.min(((float)height / bitmap.getWidth()), ((float)width / bitmap.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap target = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        if (recycle) bitmap.recycle();
        return target;
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
        //Bitmap target = Bitmap.createScaledBitmap(pickedBitmap, 120, 120, false);
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight())
            return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte)(value >> shift);
    }

    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for(int i = 0; i<4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // This only happens if the byte array has a bad length
            return null;
        }
    }

    public static String prefixToSubmask(short netPrefix){
        String submask = "";
        int shft = 0xffffffff<<(32-netPrefix);
        int oct1 = ((byte) ((shft&0xff000000)>>24)) & 0xff;
        int oct2 = ((byte) ((shft&0x00ff0000)>>16)) & 0xff;
        int oct3 = ((byte) ((shft&0x0000ff00)>>8)) & 0xff;
        int oct4 = ((byte) (shft&0x000000ff)) & 0xff;
        submask = oct1+"."+oct2+"."+oct3+"."+oct4;
        return submask;
    }

    public static void showToast(Context context, String text, boolean longDuration){
        if(context != null){
            if(longDuration){
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getString(Context context, int resID){
        if(context != null){
            return context.getResources().getString(resID);
        }else {
            return "";
        }
    }

    public static String getStringExtraInt(Context context, int resID, Integer... params){
        if(context != null){
            if(params != null){
                int length = params.length;
                switch (length){
                    case 1:
                        return context.getResources().getString(resID, params[0]);
                    case 2:
                        return context.getResources().getString(resID, params[0], params[1]);
                    case 3:
                        return context.getResources().getString(resID, params[0], params[1], params[2]);
                    case 4:
                        return context.getResources().getString(resID, params[0], params[1], params[2], params[3]);
                    default:
                        return context.getResources().getString(resID);
                }
            }else{
                return context.getResources().getString(resID);
            }
        }else {
            return "";
        }
    }

    public static String getStringExtraText(Context context, int resID, String... params){
        if(context != null){
            if(params != null){
                int length = params.length;
                switch (length){
                    case 1:
                        return context.getResources().getString(resID, params[0]);
                    case 2:
                        return context.getResources().getString(resID, params[0], params[1]);
                    case 3:
                        return context.getResources().getString(resID, params[0], params[1], params[2]);
                    case 4:
                        return context.getResources().getString(resID, params[0], params[1], params[2], params[3]);
                    default:
                        return context.getResources().getString(resID);
                }
            }else{
                return context.getResources().getString(resID);
            }
        }else {
            return "";
        }
    }

    public static String getDate(long timeInMills) {
        long expireTime = LoginManager.getExpireTime();
        Date date = new Date(expireTime);
        SimpleDateFormat df = new SimpleDateFormat(PATTERN);
        String expireText = df.format(date);
        return expireText;
    }

    public static void log(String tag, String message, boolean analytics){
        Log.d(tag, message);
        analytics = false;
        if(analytics){
            if(mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(MyApp.getInstance());
                Bundle bundle = new Bundle();
                if(MySettings.getActiveUser() != null) {
                    bundle.putString("user", MySettings.getActiveUser().getEmail());
                }else{
                    bundle.putString("user","unknown_user");
                }
                bundle.putString(tag, message);
                /*String versionName = Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT + 1].getName();
                String[] versionNames = new String[]{
                        "ANDROID BASE", "ANDROID BASE 1.1", "CUPCAKE", "DONUT",
                        "ECLAIR", "ECLAIR_0_1", "ECLAIR_MR1", "FROYO", "GINGERBREAD",
                        "GINGERBREAD_MR1", "HONEYCOMB", "HONEYCOMB_MR1", "HONEYCOMB_MR2",
                        "ICE_CREAM_SANDWICH", "ICE_CREAM_SANDWICH_MR1",
                        "JELLY_BEAN", "JELLY_BEAN_MR1", "JELLY_BEAN_MR2", "KITKAT", "KITKAT_WATCH",
                        "LOLLIPOP", "LOLLIPOP_MR1", "MARSHMALLOW", "NOUGAT", "OREO", "OREO_MR1"
                };
                int nameIndex = Build.VERSION.SDK_INT - 1;
                if (nameIndex < versionNames.length) {
                    versionName = versionNames[nameIndex];
                }
                bundle.putString("android_version", versionName  + " (" + Build.VERSION.RELEASE + ")");*/
                bundle.putString("timestamp", getTimeStringDateHoursMinutes(new java.util.Date().getTime()));
                bundle.putString("sdk_version", ""+Build.VERSION.SDK_INT);
                bundle.putString("device_manufacturer", ""+Build.MANUFACTURER);
                bundle.putString("device_brand", ""+Build.BRAND);
                bundle.putString("device_model", ""+android.os.Build.MODEL);
                mFirebaseAnalytics.logEvent(Constants.ANALYTICS_TAG, bundle);
            }else{
                Bundle bundle = new Bundle();
                bundle.putString(tag, message);
                /*String versionName = Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT + 1].getName();
                String[] versionNames = new String[]{
                        "ANDROID BASE", "ANDROID BASE 1.1", "CUPCAKE", "DONUT",
                        "ECLAIR", "ECLAIR_0_1", "ECLAIR_MR1", "FROYO", "GINGERBREAD",
                        "GINGERBREAD_MR1", "HONEYCOMB", "HONEYCOMB_MR1", "HONEYCOMB_MR2",
                        "ICE_CREAM_SANDWICH", "ICE_CREAM_SANDWICH_MR1",
                        "JELLY_BEAN", "JELLY_BEAN_MR1", "JELLY_BEAN_MR2", "KITKAT", "KITKAT_WATCH",
                        "LOLLIPOP", "LOLLIPOP_MR1", "MARSHMALLOW", "NOUGAT", "OREO", "OREO_MR1"
                };
                int nameIndex = Build.VERSION.SDK_INT - 1;
                if (nameIndex < versionNames.length) {
                    versionName = versionNames[nameIndex];
                }
                bundle.putString("android_version", versionName  + " (" + Build.VERSION.RELEASE + ")");*/
                bundle.putString("timestamp", getTimeStringDateHoursMinutes(new java.util.Date().getTime()));
                bundle.putString("sdk_version", ""+Build.VERSION.SDK_INT);
                bundle.putString("device_manufacturer", ""+Build.MANUFACTURER);
                bundle.putString("device_brand", ""+Build.BRAND);
                bundle.putString("device_model", ""+android.os.Build.MODEL);
                mFirebaseAnalytics.logEvent(Constants.ANALYTICS_TAG, bundle);
            }
        }
    }

    public static List<Device> getDeviceTypes(){
        List<Device> deviceTypes = new ArrayList<>();

        Device device1 = new Device();
        device1.setDeviceTypeID(Device.DEVICE_TYPE_wifi_1line);
        deviceTypes.add(device1);

        Device device2 = new Device();
        device2.setDeviceTypeID(Device.DEVICE_TYPE_wifi_2lines);
        deviceTypes.add(device2);

        Device device3 = new Device();
        device3.setDeviceTypeID(Device.DEVICE_TYPE_wifi_3lines);
        deviceTypes.add(device3);

        Device device10 = new Device();
        device10.setDeviceTypeID(Device.DEVICE_TYPE_PLUG_1lines);
        deviceTypes.add(device10);

        Device device11 = new Device();
        device11.setDeviceTypeID(Device.DEVICE_TYPE_PLUG_2lines);
        deviceTypes.add(device11);

        Device device12 = new Device();
        device12.setDeviceTypeID(Device.DEVICE_TYPE_PLUG_3lines);
        deviceTypes.add(device12);


        Device device13 = new Device();
        device13.setDeviceTypeID(Device.DEVICE_TYPE_MAGIC_SWITCH_1lines);
        deviceTypes.add(device13);

        Device device14 = new Device();
        device14.setDeviceTypeID(Device.DEVICE_TYPE_MAGIC_SWITCH_2lines);
        deviceTypes.add(device14);

        Device device15 = new Device();
        device15.setDeviceTypeID(Device.DEVICE_TYPE_MAGIC_SWITCH_3lines);
        deviceTypes.add(device15);

        /*Device device4 = new Device();
        device4.setDeviceTypeID(Device.DEVICE_TYPE_wifi_1line_old);
        deviceTypes.add(device4);

        Device device5 = new Device();
        device5.setDeviceTypeID(Device.DEVICE_TYPE_wifi_2lines_old);
        deviceTypes.add(device5);

        Device device6 = new Device();
        device6.setDeviceTypeID(Device.DEVICE_TYPE_wifi_3lines_old);
        deviceTypes.add(device6);*/

        /*Device device7 = new Device();
        device7.setDeviceTypeID(Device.DEVICE_TYPE_wifi_3lines_workaround);
        deviceTypes.add(device7);*/

        Device device8 = new Device();
        device8.setDeviceTypeID(Device.DEVICE_TYPE_PIR_MOTION_SENSOR);
        deviceTypes.add(device8);

        Device device16 = new Device();
        device16.setDeviceTypeID(Device.DEVICE_TYPE_SHUTTER);
        deviceTypes.add(device16);

        Device device9 = new Device();
        device9.setDeviceTypeID(Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER);
        deviceTypes.add(device9);

        return deviceTypes;
    }

    public static void togglePlace(Place place, int newState, int mode){
        if(mode == Line.LINE_STATE_ON) {
            Utils.log(TAG, "Toggling place: " + place.getName() + " ON", true);
        }else if(mode == Line.LINE_STATE_OFF) {
            Utils.log(TAG, "Toggling place: " + place.getName() + " OFF", true);
        }
        if(place != null && MySettings.getPlaceDevices(place) != null && MySettings.getPlaceDevices(place).size() >= 1){
            List<Device> placeDevices = new ArrayList<>();
            placeDevices.addAll(MySettings.getPlaceDevices(place));
            for (Device device : placeDevices){
                Utils.toggleDevice(device, newState, mode);
            }
        }
    }

    public static void toggleRoom(Room room, int newState, int mode){
        if(mode == Line.LINE_STATE_ON) {
            Utils.log(TAG, "Toggling room: " + room.getName() + " ON", true);
        }else if(mode == Line.LINE_STATE_OFF) {
            Utils.log(TAG, "Toggling room: " + room.getName() + " OFF", true);
        }
        if(room != null && MySettings.getRoomDevices(room.getId()) != null && MySettings.getRoomDevices(room.getId()).size() >= 1) {
            List<Device> roomDevices = new ArrayList<>();
            roomDevices.addAll(MySettings.getRoomDevices(room.getId()));
            for (Device device : roomDevices) {
                Utils.toggleDevice(device, newState, mode);
            }
        }
    }

    public static void toggleDevice(Device device, int newState, int mode){
        MySettings.setControlState(true);
        if(mode == Line.LINE_STATE_ON){
            Utils.log(TAG, "Toggling device: " + device.getName() + " ON", true);
        }else if(mode == Line.LINE_STATE_OFF){
            Utils.log(TAG, "Toggling device: " + device.getName() + " OFF", true);
        }
        if(mode == Place.PLACE_MODE_LOCAL){
            Utils.log(TAG, "Toggling device: " + device.getName() + " using LOCAL mode.", true);
            DeviceToggler deviceToggler = new DeviceToggler(device, newState);
            deviceToggler.execute();
        }else if(mode == Place.PLACE_MODE_REMOTE){
            Utils.log(TAG, "Toggling device: " + device.getName() + " using REMOTE mode.", true);
            //send command usint MQTT
            if(MainActivity.getInstance() != null){
                MqttAndroidClient mqttAndroidClient = MainActivity.getInstance().getMainMqttClient();
                if(mqttAndroidClient != null){
                    try{
                        JSONObject jsonObject = new JSONObject();
                        for (Line line : device.getLines()){
                            int position = line.getPosition();
                            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                if(newState == Line.LINE_STATE_ON){
                                    switch(position){
                                        case 0:
                                            jsonObject.put("L_0_DIM", ":");
                                            break;
                                        case 1:
                                            jsonObject.put("L_1_DIM", ":");
                                            break;
                                        case 2:
                                            jsonObject.put("L_2_DIM", ":");
                                            break;
                                    }
                                }else if(newState == Line.LINE_STATE_OFF){
                                    switch (position){
                                        case 0:
                                            jsonObject.put("L_0_DIM", "0");
                                            break;
                                        case 1:
                                            jsonObject.put("L_1_DIM", "0");
                                            break;
                                        case 2:
                                            jsonObject.put("L_2_DIM", "0");
                                            break;
                                    }
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                                if(newState == Line.LINE_STATE_ON){
                                    switch(position){
                                        case 0:
                                            jsonObject.put("L_0_STT", 1);
                                            break;
                                        case 1:
                                            jsonObject.put("L_1_STT", 1);
                                            break;
                                        case 2:
                                            jsonObject.put("L_2_STT", 1);
                                            break;
                                    }
                                }else if(newState == Line.LINE_STATE_OFF){
                                    switch(position){
                                        case 0:
                                            jsonObject.put("L_0_STT", 0);
                                            break;
                                        case 1:
                                            jsonObject.put("L_1_STT", 0);
                                            break;
                                        case 2:
                                            jsonObject.put("L_2_STT", 0);
                                            break;
                                    }
                                }
                            }
                        }
                        jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                        MqttMessage mqttMessage = new MqttMessage();
                        mqttMessage.setPayload(jsonObject.toString().getBytes());
                        Utils.log(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                        Utils.log(TAG, "MQTT publish data: " + mqttMessage, true);
                        mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);


                        for(Line line:device.getLines()){
                            line.setPowerState(newState);
                        }
                        DevicesInMemory.updateDevice(device);

                        Device localDevice = DevicesInMemory.getLocalDevice(device);
                        if(localDevice != null){
                            for(Line line:localDevice.getLines()){
                                line.setPowerState(newState);
                            }
                            DevicesInMemory.updateLocalDevice(localDevice);
                        }

                        MainActivity.getInstance().refreshDevicesListFromMemory();
                    }catch (JSONException e){
                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        MySettings.setControlState(false);
                    }catch (MqttException e){
                        Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        MySettings.setControlState(false);
                    }
                }else{
                    Utils.log(TAG, "mqttAndroidClient is null", true);
                }
                MySettings.setControlState(false);
            }
        }
    }

    public static void toggleLine(Device device, int position, int newState, int mode, LineToggler.ToggleCallback callback){
        MySettings.setControlState(true);
        if(mode == Place.PLACE_MODE_LOCAL){
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
                if(currentFirmwareVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    //old method for controls
                    LineToggler lineToggler = new LineToggler(device, position, newState, callback);
                    lineToggler.execute();
                }else{
                    LineToggler lineToggler = new LineToggler(device, position, newState, callback);
                    lineToggler.execute();

                    //new method for controls
                    Device localDevice = DevicesInMemory.getLocalDevice(device);
                    if(localDevice != null){
                        List<Line> lines = localDevice.getLines();
                        Line line = lines.get(position);

                        line.setPowerState(newState);
                        if(newState == Line.LINE_STATE_ON){
                            line.setDimmingVvalue(10);
                        }else if(newState == Line.LINE_STATE_OFF){
                            line.setDimmingVvalue(0);
                        }
                        lines.remove(line);
                        lines.add(position, line);
                        localDevice.setLines(lines);

                        DevicesInMemory.updateLocalDevice(localDevice);

                        //MySettings.setControlState(false);
                    }
                }
            }else{
                MySettings.setControlState(false);
            }
        }else if(mode == Place.PLACE_MODE_REMOTE){
            //send command usint MQTT
            if(MainActivity.getInstance().getMainMqttClient()!= null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        if(newState == Line.LINE_STATE_ON){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_DIM", ":");
                                    //jsonObject.put("L_0_STT", "1");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_DIM", ":");
                                    //jsonObject.put("L_1_STT", "1");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_DIM", ":");
                                    //jsonObject.put("L_2_STT", "1");
                                    break;
                            }
                        }else if(newState == Line.LINE_STATE_OFF){
                            switch (position){
                                case 0:
                                    jsonObject.put("L_0_DIM", "0");
                                    //jsonObject.put("L_0_STT", "0");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_DIM", "0");
                                    //jsonObject.put("L_1_STT", "0");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_DIM", "0");
                                    //jsonObject.put("L_2_STT", "0");
                                    break;
                            }
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        if(newState == Line.LINE_STATE_ON){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_STT", "1");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_STT", "1");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_STT", "1");
                                    break;
                            }
                        }else if(newState == Line.LINE_STATE_OFF){
                            switch(position){
                                case 0:
                                    jsonObject.put("L_0_STT", "0");
                                    break;
                                case 1:
                                    jsonObject.put("L_1_STT", "0");
                                    break;
                                case 2:
                                    jsonObject.put("L_2_STT", "0");
                                    break;
                            }
                        }
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Utils.log(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                    Utils.log(TAG, "MQTT publish data: " + mqttMessage, true);
                    MainActivity.getInstance().getMainMqttClient().publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                    callback.onToggleSuccess();
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onToggleFail();
                }catch (MqttException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onToggleFail();
                }
            }else{
                Utils.log(TAG, "mqttAndroidClient is null", true);
                callback.onToggleFail();
            }
            MySettings.setControlState(false);
        }
    }

    public static void toggleShutter(Device device, int action, int mode, ShutterToggler.ToggleCallback callback){
        MySettings.setControlState(true);
        if(mode == Place.PLACE_MODE_LOCAL){
            ShutterToggler shutterToggler = new ShutterToggler(device, action, callback);
            shutterToggler.execute();
        }else if(mode == Place.PLACE_MODE_REMOTE){
            //send command usint MQTT
            if(MainActivity.getInstance().getMainMqttClient()!= null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    switch(action){
                        case Device.SHUTTER_ACTION_DOWN:
                            jsonObject.put("L_0_STT", "1");
                            break;
                        case Device.SHUTTER_ACTION_STOP:
                            jsonObject.put("L_1_STT", "1");
                            break;
                        case Device.SHUTTER_ACTION_UP:
                            jsonObject.put("L_2_STT", "1");
                            break;
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Utils.log(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                    Utils.log(TAG, "MQTT publish data: " + mqttMessage, true);
                    MainActivity.getInstance().getMainMqttClient().publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                    callback.onToggleSuccess();
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onToggleFail();
                }catch (MqttException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onToggleFail();
                }
            }else{
                Utils.log(TAG, "mqttAndroidClient is null", true);
                callback.onToggleFail();
            }
            MySettings.setControlState(false);
        }
    }

    public static void controlDimming(Device device, int position, int value, int mode, DimmingController.DimmingControlCallback callback){
        MySettings.setControlState(true);
        if(mode == Place.PLACE_MODE_LOCAL){
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
                if(currentFirmwareVersion <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    //old method for controls
                    DimmingController dimmingController = new DimmingController(device, position, value, callback);
                    dimmingController.execute();
                }else{
                    DimmingController dimmingController = new DimmingController(device, position, value, callback);
                    dimmingController.execute();

                    //new method for controls
                    Device localDevice = DevicesInMemory.getLocalDevice(device);
                    if(localDevice != null){
                        List<Line> lines = localDevice.getLines();
                        Line line = lines.get(position);

                        line.setDimmingVvalue(value);
                        if(line.getDimmingVvalue() != 0){
                            line.setPowerState(Line.LINE_STATE_ON);
                        }else{
                            line.setPowerState(Line.LINE_STATE_OFF);
                        }

                        lines.remove(line);
                        lines.add(position, line);
                        localDevice.setLines(lines);

                        DevicesInMemory.updateLocalDevice(localDevice);
                    }
                }
            }else{
                MySettings.setControlState(false);
            }
        }else if(mode == Place.PLACE_MODE_REMOTE){
            //send command usint MQTT
            if(MainActivity.getInstance().getMainMqttClient()!= null){
                try{
                    JSONObject jsonObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        switch(position){
                            case 0:
                                if(value == 10){
                                    jsonObject.put("L_0_DIM", ":");
                                }else{
                                    jsonObject.put("L_0_DIM", ""+value);
                                }
                                break;
                            case 1:
                                if(value == 10){
                                    jsonObject.put("L_1_DIM", ":");
                                }else{
                                    jsonObject.put("L_1_DIM", ""+value);
                                }
                                break;
                            case 2:
                                if(value == 10){
                                    jsonObject.put("L_2_DIM", ":");
                                }else{
                                    jsonObject.put("L_2_DIM", ""+value);
                                }
                                break;
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        //no dimming controls for these device types
                    }
                    jsonObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    Utils.log(TAG, "MQTT publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                    Utils.log(TAG, "MQTT publish data: " + mqttMessage, true);
                    MainActivity.getInstance().getMainMqttClient().publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage);
                    callback.onDimmingSuccess();
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onDimmingFail();
                }catch (MqttException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    MySettings.setControlState(false);
                    callback.onDimmingFail();
                }
            }else{
                Utils.log(TAG, "mqttAndroidClient is null", true);
                callback.onDimmingFail();
            }
            MySettings.setControlState(false);
        }
    }

    public static class LineToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = Utils.LineToggler.class.getSimpleName();

        private ToggleCallback callback;

        Device device;
        int position;
        int newState;

        int statusCode;
        boolean ronixUnit = true;

        public LineToggler(Device device, int position, int state, ToggleCallback callback) {
            this.device = device;
            this.position = position;
            this.newState = state;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200) {
                if(callback != null) {
                    callback.onToggleSuccess();
                }
            }else{
                if(callback != null) {
                    callback.onToggleFail();
                }
            }
            MySettings.setControlState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Utils.log(TAG, "getStatusActive, doing nothing...", true);
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while (statusCode != 200 && numberOfRetries < Device.CONTROL_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                    Utils.log(TAG, "lineToggler URL: " + urlString, true);

                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        switch (position){
                            case 0:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_0_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_0_DIM", "0");
                                }
                                break;
                            case 1:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_1_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_1_DIM", "0");
                                }
                                break;
                            case 2:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_2_DIM", ":");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_2_DIM", "0");
                                }
                                break;
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        switch (position){
                            case 0:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_0_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_0_STT", "0");
                                }
                                break;
                            case 1:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_1_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_1_STT", "0");
                                }
                                break;
                            case 2:
                                if(newState == Line.LINE_STATE_ON){
                                    jObject.put("L_2_STT", "1");
                                }else if(newState == Line.LINE_STATE_OFF){
                                    jObject.put("L_2_STT", "0");
                                }
                                break;
                        }
                    }

                    jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "lineToggler POST data: " + jObject.toString(), true);


                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "lineToggler response: " + result.toString(), true);
                    if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                        ronixUnit = true;
                    }else{
                        ronixUnit = false;
                    }
                    if(result.length() >= 10){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null)
                        {
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if(unitStatus != null && unitStatus.has("U_W_STT")){
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if(wifiStatus != null) {
                                    if(wifiStatus.has("U_W_UID")) {
                                        String chipID = wifiStatus.getString("U_W_UID");
                                        if (device.getChipID().length() >= 1) {
                                            if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                                MySettings.updateDeviceIP(device, "");
                                                MySettings.updateDeviceErrorCount(device, 0);
                                               // MySettings.scanNetwork();
                                                MainActivity.getInstance().refreshDeviceListFromDatabase();
                                                return null;
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                    if(wifiStatus.has("U_W_FWV")) {
                                        String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                        if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                            device.setFirmwareVersion(currentFirmwareVersion);
                                            if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineVersion != currentVersion) {
                                                    device.setFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }

                                    if(wifiStatus.has("U_W_HWV")){
                                        String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                        if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                            int wifiVersion = Integer.parseInt(wifiVersionString);
                                            device.setWifiVersion(""+wifiVersion);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_DHC")){
                                        String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                        if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else{
                                            device.setStaticIPSyncedState(false);
                                        }
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }

                                    if(wifiStatus.has("R_W_IP_")){
                                        String ipAddress = wifiStatus.getString("R_W_IP_");
                                        if(ipAddress != null && ipAddress.length() >= 1){
                                            device.setIpAddress(ipAddress);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_GWY")){
                                        String getway = wifiStatus.getString("R_W_GWY");
                                        if(getway != null && getway.length() >= 1){
                                            device.setGateway(getway);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_NMK")){
                                        String subnetmask = wifiStatus.getString("R_W_NMK");
                                        if(subnetmask != null && subnetmask.length() >= 1){
                                            device.setSubnetMask(subnetmask);
                                        }
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }

                            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                    if(hardwareStatus.has("U_H_FWV")) {
                                        String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                        if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                            device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                            if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineHWVersion != currentHWVersion) {
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setHwFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }

                                    if(hardwareStatus.has("U_H_HWV")){
                                        String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                        if(hwVersionString != null && hwVersionString.length() >= 1){
                                            int hwVersion = Integer.parseInt(hwVersionString);
                                            device.setHwVersion(""+hwVersion);
                                        }
                                    }

                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }


                                    String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                    int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                    if(hardwareStatus.has("L_0_DIM")){
                                        line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                        if(line0DimmingValueString.equals(":")){
                                            line0DimmingValue = 10;
                                        }else{
                                            line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_1_DIM")){
                                        line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                        if(line1DimmingValueString.equals(":")){
                                            line1DimmingValue = 10;
                                        }else{
                                            line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_2_DIM")){
                                        line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                        if(line2DimmingValueString.equals(":")){
                                            line2DimmingValue = 10;
                                        }else{
                                            line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                        }
                                    }


                                    String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                    int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                    if(hardwareStatus.has("L_0_D_S")){
                                        line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                        line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_1_D_S")){
                                        line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                        line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_2_D_S")){
                                        line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                        line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String pirStateString;
                                    int pirState = 0;
                                    if(hardwareStatus.has("L_0_STT")){
                                        pirStateString = hardwareStatus.getString("L_0_STT");
                                        pirState = Integer.valueOf(pirStateString);
                                    }

                                    PIRData pirData = device.getPIRData();
                                    pirData.setState(pirState);

                                    device.setPIRData(pirData);

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }
                        }
                    }
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                       /* device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");*/
                        //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        /*device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        *///MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    if(!ronixUnit){
                        device.setErrorCount(device.getErrorCount() + 1);
                        //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                        if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                            device.setErrorCount(0);
                          /*  device.setIpAddress("");
                            DevicesInMemory.updateDevice(device);
                            MySettings.updateDeviceIP(device, "");
                          */  //MySettings.updateDeviceErrorCount(device, 0);
                            //MySettings.scanNetwork();
                        }
                    }else {
                        device.setFirmwareUpdateAvailable(true);
                        DevicesInMemory.updateDevice(device);
                    }
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }
            return null;
        }

        public interface ToggleCallback {
            void onToggleSuccess();

            void onToggleFail();
        }
    }

    public static class DimmingController extends AsyncTask<Void, Void, Void> {
        private final String TAG = Utils.DimmingController.class.getSimpleName();

        private DimmingControlCallback callback;

        Device device;
        int position;
        int value;

        int statusCode;
        boolean ronixUnit = true;

        public DimmingController(Device device, int position, int value, DimmingControlCallback callback) {
            this.device = device;
            this.position = position;
            this.value = value;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200) {
                if(callback != null) {
                    callback.onDimmingSuccess();
                }
            }else{
                if(callback != null) {
                    callback.onDimmingFail();
                }
            }
            MySettings.setControlState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Utils.log(TAG, "getStatusActive, doing nothing...", true);
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while (statusCode != 200 && numberOfRetries < Device.CONTROL_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                    Utils.log(TAG, "dimmingController URL: " + urlString, true);

                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        switch(position){
                            case 0:
                                if(value == 10){
                                    jObject.put("L_0_DIM", ":");
                                }else{
                                    jObject.put("L_0_DIM", ""+value);
                                }
                                break;
                            case 1:
                                if(value == 10){
                                    jObject.put("L_1_DIM", ":");
                                }else{
                                    jObject.put("L_1_DIM", ""+value);
                                }
                                break;
                            case 2:
                                if(value == 10){
                                    jObject.put("L_2_DIM", ":");
                                }else{
                                    jObject.put("L_2_DIM", ""+value);
                                }
                                break;
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        //no dimming controls for these device types
                    }

                    jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "dimmingController POST data: " + jObject.toString(), true);


                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "dimmingController response: " + result.toString(), true);
                    if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                        ronixUnit = true;
                    }else{
                        ronixUnit = false;
                    }
                    if(result.length() >= 10){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null){
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if(unitStatus != null && unitStatus.has("U_W_STT")){
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if(wifiStatus != null) {
                                    if(wifiStatus.has("U_W_UID")) {
                                        String chipID = wifiStatus.getString("U_W_UID");
                                        if (device.getChipID().length() >= 1) {
                                            if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                                MySettings.updateDeviceIP(device, "");
                                                MySettings.updateDeviceErrorCount(device, 0);
                                               // MySettings.scanNetwork();
                                                MainActivity.getInstance().refreshDeviceListFromDatabase();
                                                return null;
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                    if(wifiStatus.has("U_W_FWV")) {
                                        String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                        if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                            device.setFirmwareVersion(currentFirmwareVersion);
                                            if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineVersion != currentVersion) {
                                                    device.setFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }

                                    if(wifiStatus.has("U_W_HWV")){
                                        String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                        if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                            int wifiVersion = Integer.parseInt(wifiVersionString);
                                            device.setWifiVersion(""+wifiVersion);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_DHC")){
                                        String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                        if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else{
                                            device.setStaticIPSyncedState(false);
                                        }
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }

                                    if(wifiStatus.has("R_W_IP_")){
                                        String ipAddress = wifiStatus.getString("R_W_IP_");
                                        if(ipAddress != null && ipAddress.length() >= 1){
                                            device.setIpAddress(ipAddress);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_GWY")){
                                        String getway = wifiStatus.getString("R_W_GWY");
                                        if(getway != null && getway.length() >= 1){
                                            device.setGateway(getway);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_NMK")){
                                        String subnetmask = wifiStatus.getString("R_W_NMK");
                                        if(subnetmask != null && subnetmask.length() >= 1){
                                            device.setSubnetMask(subnetmask);
                                        }
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }

                            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                    if(hardwareStatus.has("U_H_FWV")) {
                                        String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                        if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                            device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                            if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineHWVersion != currentHWVersion) {
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setHwFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }

                                    if(hardwareStatus.has("U_H_HWV")){
                                        String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                        if(hwVersionString != null && hwVersionString.length() >= 1){
                                            int hwVersion = Integer.parseInt(hwVersionString);
                                            device.setHwVersion(""+hwVersion);
                                        }
                                    }

                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }


                                    String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                    int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                    if(hardwareStatus.has("L_0_DIM")){
                                        line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                        if(line0DimmingValueString.equals(":")){
                                            line0DimmingValue = 10;
                                        }else{
                                            line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_1_DIM")){
                                        line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                        if(line1DimmingValueString.equals(":")){
                                            line1DimmingValue = 10;
                                        }else{
                                            line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_2_DIM")){
                                        line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                        if(line2DimmingValueString.equals(":")){
                                            line2DimmingValue = 10;
                                        }else{
                                            line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                        }
                                    }


                                    String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                    int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                    if(hardwareStatus.has("L_0_D_S")){
                                        line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                        line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_1_D_S")){
                                        line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                        line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_2_D_S")){
                                        line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                        line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String pirStateString;
                                    int pirState = 0;
                                    if(hardwareStatus.has("L_0_STT")){
                                        pirStateString = hardwareStatus.getString("L_0_STT");
                                        pirState = Integer.valueOf(pirStateString);
                                    }

                                    PIRData pirData = device.getPIRData();
                                    pirData.setState(pirState);

                                    device.setPIRData(pirData);

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }
                        }
                    }
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        /*device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        *///MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                    /*    device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                    */    //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    if(!ronixUnit){
                        device.setErrorCount(device.getErrorCount() + 1);
                        //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                        if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                            device.setErrorCount(0);
                      /*      device.setIpAddress("");
                            DevicesInMemory.updateDevice(device);
                            MySettings.updateDeviceIP(device, "");
                      */      //MySettings.updateDeviceErrorCount(device, 0);
                            //MySettings.scanNetwork();
                        }
                    }else {
                        device.setFirmwareUpdateAvailable(true);
                        DevicesInMemory.updateDevice(device);
                    }
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }
            return null;
        }

        public interface DimmingControlCallback {
            void onDimmingSuccess();

            void onDimmingFail();
        }
    }

    public static class ShutterToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = Utils.LineToggler.class.getSimpleName();

        private ToggleCallback callback;

        Device device;
        int action;

        int statusCode;
        boolean ronixUnit = true;

        public ShutterToggler(Device device, int action, ToggleCallback callback) {
            this.device = device;
            this.action = action;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode == 200) {
                if(callback != null) {
                    callback.onToggleSuccess();
                }
            }else{
                if(callback != null) {
                    callback.onToggleFail();
                }
            }
            MySettings.setControlState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            boolean statusWasActive = false;
            while(MySettings.isGetStatusActive()){
                Utils.log(TAG, "getStatusActive, doing nothing...", true);
                statusWasActive = true;
            }
            if(statusWasActive) {
                try {
                    Thread.sleep(Constants.DELAY_TIME_MS);
                } catch (InterruptedException e) {
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }
            }

            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while (statusCode != 200 && numberOfRetries < Device.CONTROL_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                    Utils.log(TAG, "shutterToggler URL: " + urlString, true);

                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jObject = new JSONObject();
                    switch (action){
                        case Device.SHUTTER_ACTION_DOWN:
                            jObject.put("L_0_STT", "1");
                            //jObject.put("L_1_DIM", "0");
                            //jObject.put("L_2_DIM", "0");
                            break;
                        case Device.SHUTTER_ACTION_STOP:
                            //jObject.put("L_0_STT", "0");
                            //jObject.put("L_1_STT", "0");
                            jObject.put("L_1_STT", "1");
                            //jObject.put("L_2_DIM", "0");
                            break;
                        case Device.SHUTTER_ACTION_UP:
                            //jObject.put("L_0_DIM", "0");
                            jObject.put("L_2_STT", "1");
                            //jObject.put("L_2_DIM", ":");
                            break;
                    }

                    jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "shutterToggler POST data: " + jObject.toString(), true);


                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "shutterToggler response: " + result.toString(), true);
                    if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                        ronixUnit = true;
                    }else{
                        ronixUnit = false;
                    }
                    if(result.length() >= 10){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null){
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if(unitStatus != null && unitStatus.has("U_W_STT")){
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if(wifiStatus != null) {
                                    if(wifiStatus.has("U_W_UID")) {
                                        String chipID = wifiStatus.getString("U_W_UID");
                                        if (device.getChipID().length() >= 1) {
                                            if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                                MySettings.updateDeviceIP(device, "");
                                                MySettings.updateDeviceErrorCount(device, 0);
                                                //MySettings.scanNetwork();
                                                MainActivity.getInstance().refreshDeviceListFromDatabase();
                                                return null;
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                    if(wifiStatus.has("U_W_FWV")) {
                                        String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                        if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                            device.setFirmwareVersion(currentFirmwareVersion);
                                            if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineVersion != currentVersion) {
                                                    device.setFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }

                                    if(wifiStatus.has("U_W_HWV")){
                                        String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                        if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                            int wifiVersion = Integer.parseInt(wifiVersionString);
                                            device.setWifiVersion(""+wifiVersion);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_DHC")){
                                        String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                        if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else{
                                            device.setStaticIPSyncedState(false);
                                        }
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }

                                    if(wifiStatus.has("R_W_IP_")){
                                        String ipAddress = wifiStatus.getString("R_W_IP_");
                                        if(ipAddress != null && ipAddress.length() >= 1){
                                            device.setIpAddress(ipAddress);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_GWY")){
                                        String getway = wifiStatus.getString("R_W_GWY");
                                        if(getway != null && getway.length() >= 1){
                                            device.setGateway(getway);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_NMK")){
                                        String subnetmask = wifiStatus.getString("R_W_NMK");
                                        if(subnetmask != null && subnetmask.length() >= 1){
                                            device.setSubnetMask(subnetmask);
                                        }
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }

                            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                    if(hardwareStatus.has("U_H_FWV")) {
                                        String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                        if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                            device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                            if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineHWVersion != currentHWVersion) {
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setHwFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }

                                    if(hardwareStatus.has("U_H_HWV")){
                                        String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                        if(hwVersionString != null && hwVersionString.length() >= 1){
                                            int hwVersion = Integer.parseInt(hwVersionString);
                                            device.setHwVersion(""+hwVersion);
                                        }
                                    }

                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }


                                    String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                    int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                    if(hardwareStatus.has("L_0_DIM")){
                                        line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                        if(line0DimmingValueString.equals(":")){
                                            line0DimmingValue = 10;
                                        }else{
                                            line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_1_DIM")){
                                        line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                        if(line1DimmingValueString.equals(":")){
                                            line1DimmingValue = 10;
                                        }else{
                                            line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_2_DIM")){
                                        line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                        if(line2DimmingValueString.equals(":")){
                                            line2DimmingValue = 10;
                                        }else{
                                            line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                        }
                                    }


                                    String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                    int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                    if(hardwareStatus.has("L_0_D_S")){
                                        line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                        line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_1_D_S")){
                                        line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                        line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_2_D_S")){
                                        line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                        line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }

                                    List<Line> lines = device.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String pirStateString;
                                    int pirState = 0;
                                    if(hardwareStatus.has("L_0_STT")){
                                        pirStateString = hardwareStatus.getString("L_0_STT");
                                        pirState = Integer.valueOf(pirStateString);
                                    }

                                    PIRData pirData = device.getPIRData();
                                    pirData.setState(pirState);

                                    device.setPIRData(pirData);

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }
                        }
                    }
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        /*device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        *///MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        /*device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        *///MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    if(!ronixUnit){
                        device.setErrorCount(device.getErrorCount() + 1);
                        //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                        if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                            device.setErrorCount(0);
                          /*  device.setIpAddress("");
                            DevicesInMemory.updateDevice(device);
                            MySettings.updateDeviceIP(device, "");
                          */  //MySettings.updateDeviceErrorCount(device, 0);
                            //MySettings.scanNetwork();
                        }
                    }else {
                        device.setFirmwareUpdateAvailable(true);
                        DevicesInMemory.updateDevice(device);
                    }
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }
            return null;
        }

        public interface ToggleCallback {
            void onToggleSuccess();

            void onToggleFail();
        }
    }

    public static class DeviceToggler extends AsyncTask<Void, Void, Void> {
        private final String TAG = Utils.DeviceToggler.class.getSimpleName();

        Device device;
        int newState;

        int statusCode;
        boolean ronixUnit = true;

        public DeviceToggler(Device device, int state) {
            this.device = device;
            this.newState = state;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {

            }
            MySettings.setControlState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            int numberOfRetries = 0;
            while (statusCode != 200 && numberOfRetries < Device.CONTROL_NUMBER_OF_RETRIES){
                try{
                    String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                    //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                    Utils.log(TAG, "deviceToggler URL: " + urlString, true);

                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");

                    JSONObject jObject = new JSONObject();
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        for (Line line : device.getLines()) {
                            switch (line.getPosition()){
                                case 0:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_0_DIM", ":");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_0_DIM", "0");
                                    }
                                    break;
                                case 1:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_1_DIM", ":");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_1_DIM", "0");
                                    }
                                    break;
                                case 2:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_2_DIM", ":");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_2_DIM", "0");
                                    }
                                    break;
                            }
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        for (Line line : device.getLines()) {
                            switch (line.getPosition()){
                                case 0:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_0_STT", "1");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_0_STT", "0");
                                    }
                                    break;
                                case 1:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_1_STT", "1");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_1_STT", "0");
                                    }
                                    break;
                                case 2:
                                    if(newState == Line.LINE_STATE_ON){
                                        jObject.put("L_2_STT", "1");
                                    }else if(newState == Line.LINE_STATE_OFF){
                                        jObject.put("L_2_STT", "0");
                                    }
                                    break;
                            }
                        }
                    }

                    jObject.put(Constants.PARAMETER_ACCESS_TOKEN, Constants.DEVICE_DEFAULT_ACCESS_TOKEN);

                    Utils.log(TAG, "deviceToggler POST data: " + jObject.toString(), true);


                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                    outputStreamWriter.write(jObject.toString());
                    outputStreamWriter.flush();

                    statusCode = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String dataLine;
                    while((dataLine = bufferedReader.readLine()) != null) {
                        result.append(dataLine);
                    }
                    urlConnection.disconnect();
                    Utils.log(TAG, "deviceToggler response: " + result.toString(), true);
                    if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                        ronixUnit = true;
                    }else{
                        ronixUnit = false;
                    }
                    if(result.length() >= 10){
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if(jsonObject != null){
                            JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                            if(unitStatus != null && unitStatus.has("U_W_STT")){
                                JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                if(wifiStatus != null) {
                                    if(wifiStatus.has("U_W_UID")) {
                                        String chipID = wifiStatus.getString("U_W_UID");
                                        if (device.getChipID().length() >= 1) {
                                            if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                                MySettings.updateDeviceIP(device, "");
                                                MySettings.updateDeviceErrorCount(device, 0);
                                               // MySettings.scanNetwork();
                                                MainActivity.getInstance().refreshDeviceListFromDatabase();
                                                return null;
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                    if(wifiStatus.has("U_W_FWV")) {
                                        String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                        if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                            device.setFirmwareVersion(currentFirmwareVersion);
                                            if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineVersion != currentVersion) {
                                                    device.setFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }

                                    if(wifiStatus.has("U_W_HWV")){
                                        String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                        if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                            int wifiVersion = Integer.parseInt(wifiVersionString);
                                            device.setWifiVersion(""+wifiVersion);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_DHC")){
                                        String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                        if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                            device.setStaticIPSyncedState(true);
                                        }else{
                                            device.setStaticIPSyncedState(false);
                                        }
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }

                                    if(wifiStatus.has("R_W_IP_")){
                                        String ipAddress = wifiStatus.getString("R_W_IP_");
                                        if(ipAddress != null && ipAddress.length() >= 1){
                                            device.setIpAddress(ipAddress);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_GWY")){
                                        String getway = wifiStatus.getString("R_W_GWY");
                                        if(getway != null && getway.length() >= 1){
                                            device.setGateway(getway);
                                        }
                                    }

                                    if(wifiStatus.has("R_W_NMK")){
                                        String subnetmask = wifiStatus.getString("R_W_NMK");
                                        if(subnetmask != null && subnetmask.length() >= 1){
                                            device.setSubnetMask(subnetmask);
                                        }
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }

                            if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                    if(hardwareStatus.has("U_H_FWV")) {
                                        String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                        if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                            device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                            if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                if (onlineHWVersion != currentHWVersion) {
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(false);
                                                }
                                            }
                                        }else{
                                            device.setHwFirmwareUpdateAvailable(true);
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }

                                    if(hardwareStatus.has("U_H_HWV")){
                                        String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                        if(hwVersionString != null && hwVersionString.length() >= 1){
                                            int hwVersion = Integer.parseInt(hwVersionString);
                                            device.setHwVersion(""+hwVersion);
                                        }
                                    }

                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }


                                    String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                    int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                    if(hardwareStatus.has("L_0_DIM")){
                                        line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                        if(line0DimmingValueString.equals(":")){
                                            line0DimmingValue = 10;
                                        }else{
                                            line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_1_DIM")){
                                        line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                        if(line1DimmingValueString.equals(":")){
                                            line1DimmingValue = 10;
                                        }else{
                                            line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                        }
                                    }
                                    if(hardwareStatus.has("L_2_DIM")){
                                        line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                        if(line2DimmingValueString.equals(":")){
                                            line2DimmingValue = 10;
                                        }else{
                                            line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                        }
                                    }


                                    String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                    int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                    if(hardwareStatus.has("L_0_D_S")){
                                        line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                        line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_1_D_S")){
                                        line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                        line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                    }
                                    if(hardwareStatus.has("L_2_D_S")){
                                        line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                        line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                    }

                                    Device localDevice = DevicesInMemory.getLocalDevice(device);

                                    List<Line> lines = device.getLines();
                                    List<Line> localLines = localDevice.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }
                                    for (Line line:localLines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                        DevicesInMemory.updateLocalDevice(localDevice);
                                        MainActivity.getInstance().refreshDevicesListFromMemory();
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                    device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                    int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                    if(hardwareStatus.has("L_0_STT")){
                                        line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                        line0PowerState = Integer.valueOf(line0PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_1_STT")){
                                        line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                        line1PowerState = Integer.valueOf(line1PowerStateString);
                                    }
                                    if(hardwareStatus.has("L_2_STT")){
                                        line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                        line2PowerState = Integer.valueOf(line2PowerStateString);
                                    }

                                    Device localDevice = DevicesInMemory.getLocalDevice(device);

                                    List<Line> lines = device.getLines();
                                    List<Line> localLines = localDevice.getLines();
                                    for (Line line:lines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }
                                    for (Line line:localLines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }

                                    String temperatureString, beepString, hwLockString;
                                    int temperatureValue;
                                    boolean beep, hwLock;
                                    if(hardwareStatus.has("U_H_TMP")){
                                        temperatureString = hardwareStatus.getString("U_H_TMP");
                                        temperatureValue = Integer.parseInt(temperatureString);
                                        device.setTemperature(temperatureValue);
                                    }
                                    if(hardwareStatus.has("U_BEEP_")){
                                        beepString = hardwareStatus.getString("U_BEEP_");
                                        if(beepString != null && beepString.length() >= 1){
                                            if(Integer.parseInt(beepString) == 1){
                                                beep = true;
                                                device.setBeep(beep);
                                            }else{
                                                beep = false;
                                                device.setBeep(beep);
                                            }
                                        }
                                    }
                                    if(hardwareStatus.has("U_H_LCK")){
                                        hwLockString = hardwareStatus.getString("U_H_LCK");
                                        if(hwLockString != null && hwLockString.length() >= 1){
                                            if(Integer.parseInt(hwLockString) == 1){
                                                hwLock = true;
                                                device.setHwLock(hwLock);
                                            }else{
                                                hwLock = false;
                                                device.setHwLock(hwLock);
                                            }
                                        }
                                    }

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                        DevicesInMemory.updateLocalDevice(localDevice);
                                        MainActivity.getInstance().refreshDevicesListFromMemory();
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                                if(unitStatus != null && unitStatus.has("U_H_STT")){
                                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                    String pirStateString;
                                    int pirState = 0;
                                    if(hardwareStatus.has("L_0_STT")){
                                        pirStateString = hardwareStatus.getString("L_0_STT");
                                        pirState = Integer.valueOf(pirStateString);
                                    }

                                    PIRData pirData = device.getPIRData();
                                    pirData.setState(pirState);

                                    device.setPIRData(pirData);

                                    if(statusCode == 200) {
                                        device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                        device.setErrorCount(0);
                                        DevicesInMemory.updateDevice(device);
                                    }
                                    //MySettings.addDevice(device);
                                }else {
                                    device.setFirmwareUpdateAvailable(true);
                                }
                            }
                        }
                    }
                }catch (MalformedURLException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                       /* device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                       */ //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (IOException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    DevicesInMemory.updateDevice(device);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                        /*device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                        *///MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }catch (JSONException e){
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                    if(!ronixUnit){
                        device.setErrorCount(device.getErrorCount() + 1);
                        //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                        if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                            device.setErrorCount(0);
                          /*  device.setIpAddress("");
                            DevicesInMemory.updateDevice(device);
                            MySettings.updateDeviceIP(device, "");
                          */  //MySettings.updateDeviceErrorCount(device, 0);
                            //MySettings.scanNetwork();
                        }
                    }else {
                        device.setFirmwareUpdateAvailable(true);
                        DevicesInMemory.updateDevice(device);
                    }
                }finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    numberOfRetries++;
                }
            }

            return null;
        }
    }

    public static void getDeviceInfo(Device device){
        Utils.log(TAG, "Getting device info...", true);
       /* SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());*/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        String millisInString  = dateFormat.format(new Date());
        Utils.log(TAG,"Current Time: "+millisInString,true);
        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
            if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                Integer currentFirmwareVersion = Integer.valueOf(device.getFirmwareVersion());
                if(currentFirmwareVersion  <= Device.SYNC_CONTROLS_STATUS_FIRMWARE_VERSION){
                    Utils.StatusGetter statusGetter = new Utils.StatusGetter(device);
                    statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    Utils.DeviceSyncer deviceSyncer = new Utils.DeviceSyncer(device);
                    deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }else{
                Utils.StatusGetter statusGetter = new Utils.StatusGetter(device);
                statusGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SOUND_SYSTEM_CONTROLLER){
            Utils.ModeGetter modeGetter = new Utils.ModeGetter(device);
            modeGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
            Utils.DeviceSyncer deviceSyncer = new Utils.DeviceSyncer(device);
            deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
            Utils.DeviceSyncer deviceSyncer = new Utils.DeviceSyncer(device);
            deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SHUTTER){
            Utils.DeviceSyncer deviceSyncer = new Utils.DeviceSyncer(device);
            deviceSyncer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        /*//volley request to device to get its status
        String url = "http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS;

        Log.d(TAG,  "getDeviceStatus URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDeviceStatus response: " + response);

                HttpConnectorDeviceStatus.getInstance(getActivity()).getRequestQueue().cancelAll("getStatusRequest");
                DataParser dataParser = new DataParser(device, response);
                dataParser.execute();

                //device.setErrorCount(0);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());

                HttpConnectorDeviceStatus.getInstance(getActivity()).getRequestQueue().cancelAll("getStatusRequest");

                *//*MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    MySettings.updateDeviceIP(device, "");
                    MySettings.updateDeviceErrorCount(device, 0);
                    MySettings.scanNetwork();
                }*//*
            }
        });
        request.setTag("getStatusRequest");
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(Device.REFRESH_TIMEOUT, Device.REFRESH_NUMBER_OF_RETRIES, 0f));
        HttpConnectorDeviceStatus.getInstance(MainActivity.getInstance()).addToRequestQueue(request);*/
    }

    public static class DeviceSyncer extends AsyncTask<Void, Void, Void>{
        private final String TAG = DeviceSyncer.class.getSimpleName();

        Device device;

        int statusCode;
        boolean ronixUnit = true;

        public DeviceSyncer(Device device) {
            this.device = device;
        }

        @Override
        protected void onPreExecute(){
            Utils.log(TAG, "Enabling getStatus flag...", true);
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                  /*  device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                  */  //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                String urlString = "http://" + device.getIpAddress() + Constants.DEVICE_STATUS_CONTROL_URL;

                //urlString = urlString.concat("?json_0").concat("=").concat(jObject.toString());

                Utils.log(TAG, "statusGetter URL: " + urlString, true);

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");

                JSONObject jObject = new JSONObject();
                Device localDevice = DevicesInMemory.getLocalDevice(device);
                if(localDevice != null){
                    if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                        for (Line line : device.getLines()) {
                            switch (line.getPosition()){
                                case 0:
                                    if(line.getPowerState() != localDevice.getLines().get(0).getPowerState()){
                                        //jObject.put("L_0_STT", ""+localDevice.getLines().get(0).getPowerState());
                                        if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_ON){
                                            jObject.put("L_0_DIM", ":");
                                        }else if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                                            jObject.put("L_0_DIM", "0");
                                        }
                                    }
                                    if(line.getDimmingState() != localDevice.getLines().get(0).getDimmingState()) {
                                        jObject.put("L_0_D_S", "" + localDevice.getLines().get(0).getDimmingState());
                                    }
                                    if(line.getDimmingVvalue() != localDevice.getLines().get(0).getDimmingVvalue()){
                                        if(localDevice.getLines().get(0).getDimmingVvalue() == 10){
                                            jObject.put("L_0_DIM", ":");
                                        }else{
                                            jObject.put("L_0_DIM", ""+localDevice.getLines().get(0).getDimmingVvalue());
                                        }
                                    }
                                    break;
                                case 1:
                                    if(line.getPowerState() != localDevice.getLines().get(1).getPowerState()){
                                        //jObject.put("L_1_STT", ""+localDevice.getLines().get(1).getPowerState());
                                        if(localDevice.getLines().get(1).getPowerState() == Line.LINE_STATE_ON){
                                            jObject.put("L_1_DIM", ":");
                                        }else if(localDevice.getLines().get(1).getPowerState() == Line.LINE_STATE_OFF){
                                            jObject.put("L_1_DIM", "0");
                                        }
                                    }
                                    if(line.getDimmingState() != localDevice.getLines().get(1).getDimmingState()) {
                                        jObject.put("L_1_D_S", "" + localDevice.getLines().get(1).getDimmingState());
                                    }
                                    if(line.getDimmingVvalue() != localDevice.getLines().get(1).getDimmingVvalue()){
                                        if(localDevice.getLines().get(1).getDimmingVvalue() == 10){
                                            jObject.put("L_1_DIM", ":");
                                        }else{
                                            jObject.put("L_1_DIM", ""+localDevice.getLines().get(1).getDimmingVvalue());
                                        }
                                    }
                                    break;
                                case 2:
                                    if(line.getPowerState() != localDevice.getLines().get(2).getPowerState()){
                                        //jObject.put("L_2_STT", ""+localDevice.getLines().get(2).getPowerState());
                                        if(localDevice.getLines().get(2).getPowerState() == Line.LINE_STATE_ON){
                                            jObject.put("L_2_DIM", ":");
                                        }else if(localDevice.getLines().get(0).getPowerState() == Line.LINE_STATE_OFF){
                                            jObject.put("L_2_DIM", "0");
                                        }
                                    }
                                    if(line.getDimmingState() != localDevice.getLines().get(2).getDimmingState()) {
                                        jObject.put("L_2_D_S", "" + localDevice.getLines().get(2).getDimmingState());
                                    }
                                    if(line.getDimmingVvalue() != localDevice.getLines().get(2).getDimmingVvalue()){
                                        if(localDevice.getLines().get(2).getDimmingVvalue() == 10){
                                            jObject.put("L_2_DIM", ":");
                                        }else{
                                            jObject.put("L_2_DIM", ""+localDevice.getLines().get(2).getDimmingVvalue());
                                        }
                                    }
                                    break;
                            }
                        }
                    }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                            device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                        for (Line line : device.getLines()) {
                            switch (line.getPosition()){
                                case 0:
                                    if(line.getPowerState() != localDevice.getLines().get(0).getPowerState()){
                                        jObject.put("L_0_STT", ""+localDevice.getLines().get(0).getPowerState());
                                    }
                                    break;
                                case 1:
                                    if(line.getPowerState() != localDevice.getLines().get(1).getPowerState()){
                                        jObject.put("L_1_STT", ""+localDevice.getLines().get(1).getPowerState());
                                    }
                                    break;
                                case 2:
                                    if(line.getPowerState() != localDevice.getLines().get(2).getPowerState()){
                                        jObject.put("L_2_STT", ""+localDevice.getLines().get(2).getPowerState());
                                    }
                                    break;
                            }
                        }
                    }
                }

                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());

                if(device.getFirmwareVersion() != null && device.getFirmwareVersion().length() >= 1){
                    int currentFirmwareVersion = Integer.parseInt(device.getFirmwareVersion());
                    if(currentFirmwareVersion >= Device.DEVICE_FIRMWARE_DHCP_FIRMWARE){
                        if(device.isStaticIPAddress() && !device.isStaticIPSyncedState()){
                            WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                            jObject.put("R_W_DHC", "off");
                            jObject.put("R_W_IP_", device.getIpAddress());
                            jObject.put("R_W_GWY", Utils.intToIp(dhcpInfo.gateway));
                            if(Utils.intToIp(dhcpInfo.netmask) == null || Utils.intToIp(dhcpInfo.netmask).length() < 1 || Utils.intToIp(dhcpInfo.netmask).equalsIgnoreCase("0.0.0.0")){
                                try {
                                    InetAddress inetAddress = Utils.intToInet(dhcpInfo.ipAddress);
                                    NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                                    if(networkInterface != null){
                                        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                                            String submask = Utils.prefixToSubmask(address.getNetworkPrefixLength());
                                            Utils.log(TAG, address.toString(), true);
                                            jObject.put("R_W_NMK", submask);
                                        }
                                    }else{
                                        jObject.put("R_W_NMK", "255.255.255.0");
                                    }

                                } catch (IOException e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                    jObject.put("R_W_NMK", "255.255.255.0");
                                }
                            }else{
                                jObject.put("R_W_NMK", Utils.intToIp(dhcpInfo.netmask));
                            }
                        }
                    }
                }

                Utils.log(TAG, "statusGetter POST data: " + jObject.toString(), true);


                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jObject.toString());
                outputStreamWriter.flush();

                statusCode = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }
                urlConnection.disconnect();
                Utils.log(TAG, "statusGetter response: " + result.toString(), true);
                if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                    ronixUnit = true;
                }else{
                    ronixUnit = false;
                }
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                            if(wifiStatus != null) {
                                if(wifiStatus.has("U_W_UID")) {
                                    String chipID = wifiStatus.getString("U_W_UID");
                                    if (device.getChipID().length() >= 1) {
                                        if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                            MySettings.updateDeviceIP(device, "");
                                            MySettings.updateDeviceErrorCount(device, 0);
                                           // MySettings.scanNetwork();
                                            MainActivity.getInstance().refreshDeviceListFromDatabase();
                                            return null;
                                        }
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                                if(wifiStatus.has("U_W_FWV")) {
                                    String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                    if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                        device.setFirmwareVersion(currentFirmwareVersion);
                                        if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                            int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineVersion != currentVersion) {
                                                device.setFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }

                                if(wifiStatus.has("U_W_HWV")){
                                    String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                    if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                        int wifiVersion = Integer.parseInt(wifiVersionString);
                                        device.setWifiVersion(""+wifiVersion);
                                    }
                                }

                                if(wifiStatus.has("R_W_DHC")){
                                    String dhcpStatus = wifiStatus.getString("R_W_DHC");
                                    if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                        device.setStaticIPSyncedState(true);
                                    }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                        device.setStaticIPSyncedState(true);
                                    }else{
                                        device.setStaticIPSyncedState(false);
                                    }
                                }else{
                                    device.setStaticIPSyncedState(false);
                                }

                                if(wifiStatus.has("R_W_IP_")){
                                    String ipAddress = wifiStatus.getString("R_W_IP_");
                                    if(ipAddress != null && ipAddress.length() >= 1){
                                        device.setIpAddress(ipAddress);
                                    }
                                }

                                if(wifiStatus.has("R_W_GWY")){
                                    String getway = wifiStatus.getString("R_W_GWY");
                                    if(getway != null && getway.length() >= 1){
                                        device.setGateway(getway);
                                    }
                                }

                                if(wifiStatus.has("R_W_NMK")){
                                    String subnetmask = wifiStatus.getString("R_W_NMK");
                                    if(subnetmask != null && subnetmask.length() >= 1){
                                        device.setSubnetMask(subnetmask);
                                    }
                                }
                            }
                        }else{
                            device.setFirmwareUpdateAvailable(true);
                        }

                        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }

                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                }


                                String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                if(hardwareStatus.has("L_0_DIM")){
                                    line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                    if(line0DimmingValueString.equals(":")){
                                        line0DimmingValue = 10;
                                    }else{
                                        line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                    }
                                }
                                if(hardwareStatus.has("L_1_DIM")){
                                    line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                    if(line1DimmingValueString.equals(":")){
                                        line1DimmingValue = 10;
                                    }else{
                                        line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                    }
                                }
                                if(hardwareStatus.has("L_2_DIM")){
                                    line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                    if(line2DimmingValueString.equals(":")){
                                        line2DimmingValue = 10;
                                    }else{
                                        line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                    }
                                }


                                String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                if(hardwareStatus.has("L_0_D_S")){
                                    line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                    line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                }
                                if(hardwareStatus.has("L_1_D_S")){
                                    line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                    line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                }
                                if(hardwareStatus.has("L_2_D_S")){
                                    line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                    line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                }

                                List<Line> lines = device.getLines();
                                for (Line line:lines) {
                                    if(line.getPosition() == 0){
                                        line.setPowerState(line0PowerState);
                                        line.setDimmingState(line0DimmingState);
                                        line.setDimmingVvalue(line0DimmingValue);
                                    }else if(line.getPosition() == 1){
                                        line.setPowerState(line1PowerState);
                                        line.setDimmingState(line1DimmingState);
                                        line.setDimmingVvalue(line1DimmingValue);
                                    }else if(line.getPosition() == 2){
                                        line.setPowerState(line2PowerState);
                                        line.setDimmingState(line2DimmingState);
                                        line.setDimmingVvalue(line2DimmingValue);
                                    }
                                }
                                if(localDevice != null){
                                    List<Line> localLines = localDevice.getLines();
                                    for (Line line:localLines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                            line.setDimmingState(line0DimmingState);
                                            line.setDimmingVvalue(line0DimmingValue);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                            line.setDimmingState(line1DimmingState);
                                            line.setDimmingVvalue(line1DimmingValue);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                            line.setDimmingState(line2DimmingState);
                                            line.setDimmingVvalue(line2DimmingValue);
                                        }
                                    }
                                }

                                String temperatureString, beepString, hwLockString;
                                int temperatureValue;
                                boolean beep, hwLock;
                                if(hardwareStatus.has("U_H_TMP")){
                                    temperatureString = hardwareStatus.getString("U_H_TMP");
                                    temperatureValue = Integer.parseInt(temperatureString);
                                    device.setTemperature(temperatureValue);
                                }
                                if(hardwareStatus.has("U_BEEP_")){
                                    beepString = hardwareStatus.getString("U_BEEP_");
                                    if(beepString != null && beepString.length() >= 1){
                                        if(Integer.parseInt(beepString) == 1){
                                            beep = true;
                                            device.setBeep(beep);
                                        }else{
                                            beep = false;
                                            device.setBeep(beep);
                                        }
                                    }
                                }
                                if(hardwareStatus.has("U_H_LCK")){
                                    hwLockString = hardwareStatus.getString("U_H_LCK");
                                    if(hwLockString != null && hwLockString.length() >= 1){
                                        if(Integer.parseInt(hwLockString) == 1){
                                            hwLock = true;
                                            device.setHwLock(hwLock);
                                        }else{
                                            hwLock = false;
                                            device.setHwLock(hwLock);
                                        }
                                    }
                                }

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                    if(localDevice != null) {
                                        DevicesInMemory.updateLocalDevice(localDevice);
                                    }
                                }
                            }else{
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines ||
                                device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_MAGIC_SWITCH_3lines){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                                if(hardwareStatus.has("L_0_STT")){
                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                }
                                if(hardwareStatus.has("L_1_STT")){
                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                }
                                if(hardwareStatus.has("L_2_STT")){
                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                }

                                List<Line> lines = device.getLines();
                                for (Line line:lines) {
                                    if(line.getPosition() == 0){
                                        line.setPowerState(line0PowerState);
                                    }else if(line.getPosition() == 1){
                                        line.setPowerState(line1PowerState);
                                    }else if(line.getPosition() == 2){
                                        line.setPowerState(line2PowerState);
                                    }
                                }
                                if(localDevice != null){
                                    List<Line> localLines = localDevice.getLines();
                                    for (Line line:localLines) {
                                        if(line.getPosition() == 0){
                                            line.setPowerState(line0PowerState);
                                        }else if(line.getPosition() == 1){
                                            line.setPowerState(line1PowerState);
                                        }else if(line.getPosition() == 2){
                                            line.setPowerState(line2PowerState);
                                        }
                                    }
                                }

                                String temperatureString, beepString, hwLockString;
                                int temperatureValue;
                                boolean beep, hwLock;
                                if(hardwareStatus.has("U_H_TMP")){
                                    temperatureString = hardwareStatus.getString("U_H_TMP");
                                    temperatureValue = Integer.parseInt(temperatureString);
                                    device.setTemperature(temperatureValue);
                                }
                                if(hardwareStatus.has("U_BEEP_")){
                                    beepString = hardwareStatus.getString("U_BEEP_");
                                    if(beepString != null && beepString.length() >= 1){
                                        if(Integer.parseInt(beepString) == 1){
                                            beep = true;
                                            device.setBeep(beep);
                                        }else{
                                            beep = false;
                                            device.setBeep(beep);
                                        }
                                    }
                                }
                                if(hardwareStatus.has("U_H_LCK")){
                                    hwLockString = hardwareStatus.getString("U_H_LCK");
                                    if(hwLockString != null && hwLockString.length() >= 1){
                                        if(Integer.parseInt(hwLockString) == 1){
                                            hwLock = true;
                                            device.setHwLock(hwLock);
                                        }else{
                                            hwLock = false;
                                            device.setHwLock(hwLock);
                                        }
                                    }
                                }

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                    if(localDevice != null) {
                                        DevicesInMemory.updateLocalDevice(localDevice);
                                    }
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PIR_MOTION_SENSOR){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                String pirStateString;
                                int pirState = 0;
                                if(hardwareStatus.has("L_0_STT")){
                                    pirStateString = hardwareStatus.getString("L_0_STT");
                                    pirState = Integer.valueOf(pirStateString);
                                }

                                PIRData pirData = device.getPIRData();
                                pirData.setState(pirState);

                                device.setPIRData(pirData);

                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_SHUTTER){
                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                /*if(hardwareStatus.has("U_H_FWV")) {
                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineHWVersion != currentHWVersion) {
                                                device.setHwFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setHwFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setHwFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }

                                if(hardwareStatus.has("U_H_HWV")){
                                    String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                    if(hwVersionString != null && hwVersionString.length() >= 1){
                                        int hwVersion = Integer.parseInt(hwVersionString);
                                        device.setHwVersion(""+hwVersion);
                                    }
                                }*/


                                if(statusCode == 200) {
                                    device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                    device.setErrorCount(0);
                                    DevicesInMemory.updateDevice(device);
                                }
                                //MySettings.addDevice(device);
                            }else {
                                device.setFirmwareUpdateAvailable(true);
                            }
                        }
                    }
                }
            }catch (MalformedURLException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                 /*   device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                 */   //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                   /* device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                   */ //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                if(!ronixUnit){
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                     /*   device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                     */   //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }else {
                    device.setFirmwareUpdateAvailable(true);
                    DevicesInMemory.updateDevice(device);
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Utils.log(TAG, "Disabling getStatus flag...", true);
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static class StatusGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = StatusGetter.class.getSimpleName();

        Device device;

        int statusCode;
        boolean ronixUnit = true;

        public StatusGetter(Device device) {
            try{
                this.device = device;
            }catch (Exception e){
                Utils.log(TAG, "Json exception " + e.getMessage(), true);
            }
        }

        @Override
        protected void onPreExecute(){
            Utils.log(TAG, "Enabling getStatus flag...", true);
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    /*device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    *///MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS);
                Utils.log(TAG, "statusGetter URL: " + url, true);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                statusCode = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }
                urlConnection.disconnect();
                Utils.log(TAG, "statusGetter response: " + result.toString(), true);
                if(result.toString().contains("UNIT_STATUS") || (result.toString().startsWith("#") && result.toString().endsWith("&"))){
                    ronixUnit = true;
                }else{
                    ronixUnit = false;
                }
                if(result.length() >= 10){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                            if(wifiStatus != null) {
                                if(wifiStatus.has("U_W_UID")) {
                                    String chipID = wifiStatus.getString("U_W_UID");
                                    if (device.getChipID().length() >= 1) {
                                        if (device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                                            if(wifiStatus.getString("R_W_IP_") != "") {
                                                if (device.getIpAddress() == null || device.getIpAddress().length() < 1 || !device.getIpAddress().equals(wifiStatus.getString("R_W_IP_"))) {
                                                    Utils.log(TAG, "Updated IP(Local): " + wifiStatus.getString("R_W_IP_"), true);
                                                    MySettings.updateDeviceIP(device, wifiStatus.getString("R_W_IP_"));
                                                    device.setIpAddress(wifiStatus.getString("R_W_IP_"));
                                                    MySettings.updateDeviceErrorCount(device, 0);
                                                    DevicesInMemory.updateDevice(device);
                                                    if (MainActivity.getInstance() != null) {
                                                        MainActivity.getInstance().refreshDevicesListFromMemory();
                                                    }
                                                    // MainActivity.getInstance().refreshDeviceListFromDatabase();
                                                    //  return null;
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                                if(wifiStatus.has("U_W_FWV")) {
                                    String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                    if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                        device.setFirmwareVersion(currentFirmwareVersion);
                                        if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                            int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                            int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                            if (onlineVersion != currentVersion) {
                                                device.setFirmwareUpdateAvailable(true);
                                            }else{
                                                device.setFirmwareUpdateAvailable(false);
                                            }
                                        }
                                    }else{
                                        device.setFirmwareUpdateAvailable(true);
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }

                                if(wifiStatus.has("U_W_HWV")){
                                    String wifiVersionString = wifiStatus.getString("U_W_HWV");
                                    if(wifiVersionString != null && wifiVersionString.length() >= 1){
                                        int wifiVersion = Integer.parseInt(wifiVersionString);
                                        device.setWifiVersion(""+wifiVersion);
                                    }
                                }
                            }
                        }else{
                            device.setFirmwareUpdateAvailable(true);
                        }

                        if(unitStatus != null && unitStatus.has("U_H_STT")){
                            JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                            if(hardwareStatus.has("U_H_FWV")) {
                                String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                    device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                    if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                        int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                        int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                        if (onlineHWVersion != currentHWVersion) {
                                            device.setHwFirmwareUpdateAvailable(true);
                                        }else{
                                            device.setHwFirmwareUpdateAvailable(false);
                                        }
                                    }
                                }else{
                                    device.setHwFirmwareUpdateAvailable(true);
                                }
                            }else{
                                device.setHwFirmwareUpdateAvailable(true);
                            }

                            if(hardwareStatus.has("U_H_HWV")){
                                String hwVersionString = hardwareStatus.getString("U_H_HWV");
                                if(hwVersionString != null && hwVersionString.length() >= 1){
                                    int hwVersion = Integer.parseInt(hwVersionString);
                                    device.setHwVersion(""+hwVersion);
                                }
                            }


                            String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                            int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;

                            if(hardwareStatus.has("L_0_STT")){
                                line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                line0PowerState = Integer.valueOf(line0PowerStateString);
                            }
                            if(hardwareStatus.has("L_1_STT")){
                                line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                line1PowerState = Integer.valueOf(line1PowerStateString);
                            }
                            if(hardwareStatus.has("L_2_STT")){
                                line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                line2PowerState = Integer.valueOf(line2PowerStateString);
                            }


                            String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                            int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                            if(hardwareStatus.has("L_0_DIM")){
                                line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                if(line0DimmingValueString.equals(":")){
                                    line0DimmingValue = 10;
                                }else{
                                    line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                }
                            }
                            if(hardwareStatus.has("L_1_DIM")){
                                line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                if(line1DimmingValueString.equals(":")){
                                    line1DimmingValue = 10;
                                }else{
                                    line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                }
                            }
                            if(hardwareStatus.has("L_2_DIM")){
                                line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                if(line2DimmingValueString.equals(":")){
                                    line2DimmingValue = 10;
                                }else{
                                    line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                }
                            }


                            String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                            int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                            if(hardwareStatus.has("L_0_D_S")){
                                line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                line0DimmingState = Integer.valueOf(line0DimmingStateString);
                            }
                            if(hardwareStatus.has("L_1_D_S")){
                                line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                line1DimmingState = Integer.valueOf(line1DimmingStateString);
                            }
                            if(hardwareStatus.has("L_2_D_S")){
                                line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                line2DimmingState = Integer.valueOf(line2DimmingStateString);
                            }


                            List<Line> lines = device.getLines();
                            for (Line line:lines) {
                                if(line.getPosition() == 0){
                                    line.setPowerState(line0PowerState);
                                    line.setDimmingState(line0DimmingState);
                                    line.setDimmingVvalue(line0DimmingValue);
                                }else if(line.getPosition() == 1){
                                    line.setPowerState(line1PowerState);
                                    line.setDimmingState(line1DimmingState);
                                    line.setDimmingVvalue(line1DimmingValue);
                                }else if(line.getPosition() == 2){
                                    line.setPowerState(line2PowerState);
                                    line.setDimmingState(line2DimmingState);
                                    line.setDimmingVvalue(line2DimmingValue);
                                }
                            }

                            String temperatureString, beepString, hwLockString;
                            int temperatureValue;
                            boolean beep, hwLock;
                            if(hardwareStatus.has("U_H_TMP")){
                                temperatureString = hardwareStatus.getString("U_H_TMP");
                                temperatureValue = Integer.parseInt(temperatureString);
                                device.setTemperature(temperatureValue);
                            }
                            if(hardwareStatus.has("U_BEEP_")){
                                beepString = hardwareStatus.getString("U_BEEP_");
                                if(beepString != null && beepString.length() >= 1){
                                    if(Integer.parseInt(beepString) == 1){
                                        beep = true;
                                        device.setBeep(beep);
                                    }else{
                                        beep = false;
                                        device.setBeep(beep);
                                    }
                                }
                            }
                            if(hardwareStatus.has("U_H_LCK")){
                                hwLockString = hardwareStatus.getString("U_H_LCK");
                                if(hwLockString != null && hwLockString.length() >= 1){
                                    if(Integer.parseInt(hwLockString) == 1){
                                        hwLock = true;
                                        device.setHwLock(hwLock);
                                    }else{
                                        hwLock = false;
                                        device.setHwLock(hwLock);
                                    }
                                }
                            }

                            if(statusCode == 200) {
                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                device.setErrorCount(0);
                                DevicesInMemory.updateDevice(device);
                            }
                            //MySettings.addDevice(device);
                        }
                    }
                }
            }catch (MalformedURLException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                  /*  device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                  */  //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    /*device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    *///MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                if(!ronixUnit){
                    device.setErrorCount(device.getErrorCount() + 1);
                    //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                    if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                        device.setErrorCount(0);
                      /*  device.setIpAddress("");
                        DevicesInMemory.updateDevice(device);
                        MySettings.updateDeviceIP(device, "");
                      */  //MySettings.updateDeviceErrorCount(device, 0);
                        //MySettings.scanNetwork();
                    }
                }else{
                    device.setFirmwareUpdateAvailable(true);
                    DevicesInMemory.updateDevice(device);
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Utils.log(TAG, "Disabling getStatus flag...", true);
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static class ModeGetter extends AsyncTask<Void, Void, Void>{
        private final String TAG = ModeGetter.class.getSimpleName();

        Device device;

        int statusCode;

        public ModeGetter(Device device) {
            this.device = device;
        }

        @Override
        protected void onPreExecute(){
            Utils.log(TAG, "Enabling getStatus flag...", true);
            MySettings.setGetStatusState(true);
        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {
            if(statusCode != 200) {
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                  /*  device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                  */  //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().refreshDevicesListFromMemory();
            }
            MySettings.setGetStatusState(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            statusCode = 0;
            try{
                URL url = new URL("http://" + device.getIpAddress() + Constants.CONTROL_SOUND_DEVICE_CHANGE_MODE_URL);
                Utils.log(TAG, "modeGetter URL: " + url, true);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setReadTimeout(Device.REFRESH_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");

                JSONObject jObject = new JSONObject();
                jObject.put(Constants.PARAMETER_SOUND_CONTROLLER_MODE, "");
                jObject.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());

                if(device.isStaticIPAddress() && !device.isStaticIPSyncedState()){
                    WifiManager mWifiManager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                    jObject.put("R_W_DHC", "off");
                    jObject.put("R_W_IP_", device.getIpAddress());
                    jObject.put("R_W_GWY", Utils.intToIp(dhcpInfo.gateway));
                    if(Utils.intToIp(dhcpInfo.netmask) == null || Utils.intToIp(dhcpInfo.netmask).length() < 1 || Utils.intToIp(dhcpInfo.netmask).equalsIgnoreCase("0.0.0.0")){
                        try {
                            InetAddress inetAddress = Utils.intToInet(dhcpInfo.ipAddress);
                            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                            if(networkInterface != null){
                                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                                    String submask = Utils.prefixToSubmask(address.getNetworkPrefixLength());
                                    Utils.log(TAG, address.toString(), true);
                                    jObject.put("R_W_NMK", submask);
                                }
                            }else{
                                jObject.put("R_W_NMK", "255.255.255.0");
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Exception: " + e.getMessage());
                            jObject.put("R_W_NMK", "255.255.255.0");
                        }
                    }else{
                        jObject.put("R_W_NMK", Utils.intToIp(dhcpInfo.netmask));
                    }
                }

                Utils.log(TAG, "modeGetter POST data: " + jObject.toString(), true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jObject.toString());
                outputStreamWriter.flush();

                statusCode = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String dataLine;
                while((dataLine = bufferedReader.readLine()) != null) {
                    result.append(dataLine);
                }
                urlConnection.disconnect();
                Utils.log(TAG, "modeGetter response: " + result.toString(), true);
                if(result.length() >= 3){
                    JSONObject jsonObject = new JSONObject(result.toString());
                    if(jsonObject != null){
                        String modeString = jsonObject.getString("mode");

                        int mode = SoundDeviceData.MODE_LINE_IN;

                        if(modeString != null && modeString.length() >= 1){
                            if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN)){
                                mode = SoundDeviceData.MODE_LINE_IN;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN_2)){
                                mode = SoundDeviceData.MODE_LINE_IN_2;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_UPNP)){
                                mode = SoundDeviceData.MODE_UPNP;
                            }else if(modeString.equals(Constants.PARAMETER_SOUND_CONTROLLER_MODE_USB)){
                                mode = SoundDeviceData.MODE_USB;
                            }
                        }

                        device.getSoundDeviceData().setMode(mode);

                        if(jsonObject.has("R_W_DHC")){
                            String dhcpStatus = jsonObject.getString("R_W_DHC");
                            if(dhcpStatus.equalsIgnoreCase("on") && !device.isStaticIPAddress()){
                                device.setStaticIPSyncedState(true);
                            }else if(dhcpStatus.equalsIgnoreCase("off") && device.isStaticIPAddress()){
                                device.setStaticIPSyncedState(true);
                            }else{
                                device.setStaticIPSyncedState(false);
                            }
                        }else{
                            device.setStaticIPSyncedState(false);
                        }

                        if(jsonObject.has("R_W_IP_")){
                            String ipAddress = jsonObject.getString("R_W_IP_");
                            if(ipAddress != null && ipAddress.length() >= 1){
                                device.setIpAddress(ipAddress);
                            }
                        }

                        if(jsonObject.has("R_W_GWY")){
                            String getway = jsonObject.getString("R_W_GWY");
                            if(getway != null && getway.length() >= 1){
                                device.setGateway(getway);
                            }
                        }

                        if(jsonObject.has("R_W_NMK")){
                            String subnetmask = jsonObject.getString("R_W_NMK");
                            if(subnetmask != null && subnetmask.length() >= 1){
                                device.setSubnetMask(subnetmask);
                            }
                        }

                        if(statusCode == 200) {
                            device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                            device.setErrorCount(0);
                            DevicesInMemory.updateDevice(device);
                        }
                        //MySettings.addDevice(device);
                    }
                }
            }catch (MalformedURLException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                   /* device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                   */ //MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (IOException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    /*device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    *///MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }catch (JSONException e){
                Utils.log(TAG, "Exception: " + e.getMessage(), true);
                device.setErrorCount(device.getErrorCount() + 1);
                //MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                DevicesInMemory.updateDevice(device);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    device.setErrorCount(0);
                    /*device.setIpAddress("");
                    DevicesInMemory.updateDevice(device);
                    MySettings.updateDeviceIP(device, "");
                    *///MySettings.updateDeviceErrorCount(device, 0);
                    //MySettings.scanNetwork();
                }
            }finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                Utils.log(TAG, "Disabling getStatus flag...", true);
                MySettings.setGetStatusState(false);
            }

            return null;
        }
    }

    public static void addPlace(Place place){
        //add place to cloud firestore

        //upload place to firebase-db/email/places/place_id
        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("id", place.getId());
        map.put("name", place.getName());
        map.put("wifi_networks", place.getWifiNetworks());
        //TODO put rest of info
        map.put("db_version", AppDatabase.version);
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("places").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(MyApp.getInstance(), Utils.getString(MyApp.getInstance(), R.string.server_connection_error), true);
            }
        });
    }

    public static void addRoom(long placeID, Room room){
        //add room to place in cloud firestore

        //upload room to firebase-db/email/places/place_id/rooms/room_id
        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("id", room.getId());
        map.put("name", room.getName());
        //TODO put rest of info
        map.put("db_version", AppDatabase.version);
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("places").document(""+placeID).collection("rooms").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(MyApp.getInstance(), Utils.getString(MyApp.getInstance(), R.string.server_connection_error), true);
            }
        });
    }

    public static void addDevice(long placeID, long roomID, Device device){
        //add device to room in cloud firestore

        //upload device to firebase-db/email/places/place_id/rooms/room_id/devices/device_chip_id
        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("id", device.getId());
        map.put("name", device.getName());
        map.put("lines", device.getLines());
        //TODO put rest of info
        map.put("db_version", AppDatabase.version);
        db.collection("users").document(MySettings.getActiveUser().getEmail()).collection("places").document(""+placeID).collection("rooms").document(""+roomID).collection("devices").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(MyApp.getInstance(), Utils.getString(MyApp.getInstance(), R.string.server_connection_error), true);
            }
        });
    }
}