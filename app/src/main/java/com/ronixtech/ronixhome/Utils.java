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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
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
}