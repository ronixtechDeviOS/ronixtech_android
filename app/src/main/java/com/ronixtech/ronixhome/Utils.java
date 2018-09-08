package com.ronixtech.ronixhome;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Type;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final int ANIMATION_TYPE_TRANSLATION = 0;
    public static final int ANIMATION_TYPE_FADE = 1;


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

    public static String getTimeString(long timestamp){
        String timeString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        timeString = hour + ":" + minute;
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

        Type type = new Type(Constants.TYPE_PLACE, "Bank", "", R.drawable.place_type_bank);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Church", "", R.drawable.place_type_church);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Cinema", "", R.drawable.place_type_cinema);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Clinic", "", R.drawable.place_type_clinic);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Coffee Shop", "", R.drawable.place_type_coffee_shop);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Embassy", "", R.drawable.place_type_embassy);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hospital", "", R.drawable.place_type_hospital);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Hotel", "", R.drawable.place_type_hotel);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "House", "", R.drawable.place_type_house);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Mosque", "", R.drawable.place_type_mosque);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Museum", "", R.drawable.place_type_museum);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Office", "", R.drawable.place_type_office);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Pharmacy", "", R.drawable.place_type_pharmacy);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "School", "", R.drawable.place_type_school);
        placeTypes.add(type);
        type = new Type(Constants.TYPE_PLACE, "Store", "", R.drawable.place_type_store);
        placeTypes.add(type);

        for (Type ty: placeTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateFloorTypes(){
        List<Type> floorTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_FLOOR, "Floor", "", R.drawable.floor_icon);
        floorTypes.add(type);

        for (Type ty: floorTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateRoomTypes(){
        List<Type> roomTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_ROOM, "Balcony", "", R.drawable.room_type_balcony);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Basement", "", R.drawable.room_type_basement);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bathroom", "", R.drawable.room_type_bathroom);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Bedroom", "", R.drawable.room_type_bedroom);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kids Bedroom", "", R.drawable.room_type_bedroom_kids);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Corridor", "", R.drawable.room_type_corridor);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Dining", "", R.drawable.room_type_dining);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Entryway", "", R.drawable.room_type_entryway);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Frontyard", "", R.drawable.room_type_frontyard);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Garage", "", R.drawable.room_type_garage);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Home Cinema", "", R.drawable.room_type_home_cinema);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Kitchen", "", R.drawable.room_type_kitchen);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Library", "", R.drawable.room_type_library);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Living Room", "", R.drawable.room_type_living_room);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Office", "", R.drawable.room_type_office);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Terrace", "", R.drawable.room_type_terrace);
        roomTypes.add(type);
        type = new Type(Constants.TYPE_ROOM, "Toilet", "", R.drawable.room_type_toilet);
        roomTypes.add(type);

        for (Type ty: roomTypes) {
            MySettings.addType(ty);
        }
    }

    public static void generateLineTypes(){
        List<Type> lineTypes = new ArrayList<>();

        Type type = new Type(Constants.TYPE_LINE, "Air Conditioner", "", R.drawable.line_type_air_conditioner);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Appliance Plug", "", R.drawable.line_type_appliance_plug);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Attic Fan", "", R.drawable.line_type_attic_fan);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Boiler", "", R.drawable.line_type_boiler);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan", "", R.drawable.line_type_ceiling_fan);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Ceiling Fan Light", "", R.drawable.line_type_ceiling_fan_light);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Chandelier", "", R.drawable.line_type_chandelier);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Clothes Iron", "", R.drawable.line_type_clothes_iron);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Coffee Maker", "", R.drawable.line_type_coffee_maker);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Computer", "", R.drawable.line_type_computer);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Dishwasher", "", R.drawable.line_type_dishwasher);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Door", "", R.drawable.line_type_door);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fan", "", R.drawable.line_type_fan);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Fluorescent Lamp", "", R.drawable.line_type_fluorescent_lamp);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Flush", "", R.drawable.line_type_flush);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Garage", "", R.drawable.line_type_garage);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Hair Dryer", "", R.drawable.line_type_hair_dryer);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Humidifier", "", R.drawable.line_type_humidifier);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Kettle", "", R.drawable.line_type_kettle);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "LED Lamp", "", R.drawable.line_type_led__lamp);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Light Fixture", "", R.drawable.line_type_light_fixture);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Microwave Oven", "", R.drawable.line_type_microwave_oven);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Motion Sensor", "", R.drawable.line_type_motion_sensor);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Oven", "", R.drawable.line_type_oven);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Pendant", "", R.drawable.line_type_pendant);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Photocopier", "", R.drawable.line_type_photocopier);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Printer", "", R.drawable.line_type_printer);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Recessed", "", R.drawable.line_type_recessed);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Refrigerator", "", R.drawable.line_type_refrigerator);
        lineTypes.add(type);
        type = new Type(Constants.TYPE_LINE, "Window", "", R.drawable.line_type_window);
        lineTypes.add(type);

        for (Type ty: lineTypes) {
            MySettings.addType(ty);
        }
    }
}