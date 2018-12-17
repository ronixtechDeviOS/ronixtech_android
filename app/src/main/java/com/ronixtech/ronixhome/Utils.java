package com.ronixtech.ronixhome;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.TimeUnit;
import com.ronixtech.ronixhome.entities.Type;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final int ANIMATION_TYPE_TRANSLATION = 0;
    public static final int ANIMATION_TYPE_FADE = 1;

    private static CustomProgressDialog customProgressDialog;


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

    public static String getDateString(long timestamp){
        String dateString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
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
                Log.d("Utils", "Json exception: " + e.getMessage());
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
            Log.d("Utils", "CPU Count: "+files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Print exception
            Log.d("Utils", "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }

    public static void generatePlaceTypes(){
        List<Type> placeTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_PLACE, "Bank", "", R.drawable.place_type_bank, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_bank));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Church", "", R.drawable.place_type_church, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_church));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Cinema", "", R.drawable.place_type_cinema, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_cinema));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Clinic", "", R.drawable.place_type_clinic, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_clinic));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Coffee Shop", "", R.drawable.place_type_coffee_shop, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_coffee_shop));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Embassy", "", R.drawable.place_type_embassy, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_embassy));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hospital", "", R.drawable.place_type_hospital, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_hospital));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hotel", "", R.drawable.place_type_hotel, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_hotel));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "House", "", R.drawable.place_type_house, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_house));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Mosque", "", R.drawable.place_type_mosque, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_mosque));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Museum", "", R.drawable.place_type_museum, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_museum));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Office", "", R.drawable.place_type_office, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_office));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Pharmacy", "", R.drawable.place_type_pharmacy, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_pharmacy));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "School", "", R.drawable.place_type_school, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_school));
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Store", "", R.drawable.place_type_store, MyApp.getInstance().getResources().getResourceName(R.drawable.place_type_store));
        placeTypes.add(type);

        for (Type ty: placeTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateFloorTypes(){
        List<Type> floorTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_FLOOR, "Floor", "", R.drawable.floor_icon, MyApp.getInstance().getResources().getResourceName(R.drawable.floor_icon));
        floorTypes.add(type);

        for (Type ty: floorTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateRoomTypes(){
        List<Type> roomTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_ROOM, "Balcony", "", R.drawable.room_type_balcony, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_balcony));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Basement", "", R.drawable.room_type_basement, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_basement));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bathroom", "", R.drawable.room_type_bathroom, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bathroom));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bedroom", "", R.drawable.room_type_bedroom, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bedroom));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kids Bedroom", "", R.drawable.room_type_bedroom_kids, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_bedroom_kids));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Corridor", "", R.drawable.room_type_corridor, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_corridor));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Dining", "", R.drawable.room_type_dining, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_dining));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Entryway", "", R.drawable.room_type_entryway, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_entryway));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Frontyard", "", R.drawable.room_type_frontyard, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_frontyard));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Garage", "", R.drawable.room_type_garage, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_garage));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Home Cinema", "", R.drawable.room_type_home_cinema, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_home_cinema));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kitchen", "", R.drawable.room_type_kitchen, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_kitchen));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Library", "", R.drawable.room_type_library, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_library));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Living Room", "", R.drawable.room_type_living_room, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_living_room));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Office", "", R.drawable.room_type_office, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_office));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Terrace", "", R.drawable.room_type_terrace, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_terrace));
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Toilet", "", R.drawable.room_type_toilet, MyApp.getInstance().getResources().getResourceName(R.drawable.room_type_toilet));
        roomTypes.add(type);
        for (Type ty: roomTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateLineTypes(){
        List<Type> lineTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_LINE, "Air Conditioner", "", R.drawable.line_type_air_conditioner, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_air_conditioner));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Appliance Plug", "", R.drawable.line_type_appliance_plug, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_appliance_plug));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Attic Fan", "", R.drawable.line_type_attic_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_attic_fan));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Boiler", "", R.drawable.line_type_boiler, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_boiler));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan", "", R.drawable.line_type_ceiling_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_ceiling_fan));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan Light", "", R.drawable.line_type_ceiling_fan_light, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_ceiling_fan_light));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Chandelier", "", R.drawable.line_type_chandelier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_chandelier));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Clothes Iron", "", R.drawable.line_type_clothes_iron, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_clothes_iron));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Coffee Maker", "", R.drawable.line_type_coffee_maker, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_coffee_maker));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Computer", "", R.drawable.line_type_computer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_computer));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Dishwasher", "", R.drawable.line_type_dishwasher, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_dishwasher));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Door", "", R.drawable.line_type_door, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_door));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fan", "", R.drawable.line_type_fan, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_fan));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fluorescent Lamp", "", R.drawable.line_type_fluorescent_lamp, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_fluorescent_lamp));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Flush", "", R.drawable.line_type_flush, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_flush));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Garage", "", R.drawable.line_type_garage, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_garage));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Hair Dryer", "", R.drawable.line_type_hair_dryer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_hair_dryer));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Humidifier", "", R.drawable.line_type_humidifier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_humidifier));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Kettle", "", R.drawable.line_type_kettle, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_kettle));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "LED Lamp", "", R.drawable.line_type_led__lamp, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_led__lamp));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Light Fixture", "", R.drawable.line_type_light_fixture, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_light_fixture));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Microwave Oven", "", R.drawable.line_type_microwave_oven, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_microwave_oven));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Motion Sensor", "", R.drawable.line_type_motion_sensor, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_motion_sensor));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Oven", "", R.drawable.line_type_oven, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_oven));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Pendant", "", R.drawable.line_type_pendant, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_pendant));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Photocopier", "", R.drawable.line_type_photocopier, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_photocopier));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Printer", "", R.drawable.line_type_printer, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_printer));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Recessed", "", R.drawable.line_type_recessed, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_recessed));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Refrigerator", "", R.drawable.line_type_refrigerator, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_refrigerator));
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Window", "", R.drawable.line_type_window, MyApp.getInstance().getResources().getResourceName(R.drawable.line_type_window));
        lineTypes.add(type);

        for (Type ty: lineTypes) {
            MySettings.addType(ty);
        }
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
                            //go to play store
                            try {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                            }
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
                Log.d(TAG, "Exception: " + e.getMessage());
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
                Log.d(TAG, errorMessage, ioException);
                return false;
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = Utils.getStringExtraInt(context, R.string.geocoding_error_invalid_lat_long);
                Log.d(TAG, errorMessage + ". " + "Latitude = " + latitude + ", Longitude = " + longitude, illegalArgumentException);
                return false;
            }catch (Exception e) {
                errorMessage = errorMessage;
                Log.d(TAG, "Exception: " + e.getMessage());
                return false;
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = Utils.getStringExtraInt(context, R.string.geocoding_error_no_address_found);
                    Log.d(TAG, errorMessage);
                }
                return false;
            } else {
                Log.d(TAG, Utils.getStringExtraInt(context, R.string.geocoding_error_address_found));
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
}