package com.ronixtech.ronixhome;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.ronixtech.ronixhome.entities.Type;

public class MyApp extends MultiDexApplication {
    private static MyApp mInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        MySettings.initDB();

        /*if(MySettings.getAppFirstStart()) {
            Utils.generatePlaceTypes();
            Utils.generateFloorTypes();
            Utils.generateRoomTypes();
            Utils.generateLineTypes();
            MySettings.setAppFirstStart(false);
        }*/

        for (Type type:MySettings.getTypes(Constants.TYPE_PLACE)) {
            MySettings.initDB().typeDAO().removeType(type.getId());
        }
        for (Type type:MySettings.getTypes(Constants.TYPE_FLOOR)) {
            MySettings.initDB().typeDAO().removeType(type.getId());
        }
        for (Type type:MySettings.getTypes(Constants.TYPE_ROOM)) {
            MySettings.initDB().typeDAO().removeType(type.getId());
        }
        for (Type type:MySettings.getTypes(Constants.TYPE_LINE)) {
            MySettings.initDB().typeDAO().removeType(type.getId());
        }
        for (Type type:MySettings.getTypes(Constants.TYPE_LINE_PLUG)) {
            MySettings.initDB().typeDAO().removeType(type.getId());
        }

        Utils.generatePlaceTypes();
        Utils.generateFloorTypes();
        Utils.generateRoomTypes();
        Utils.generateLineTypes();

        //MySettings.scanNetwork();


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static synchronized MyApp getInstance() {
        return mInstance;
    }

    public static SharedPreferences getShardPrefs(){
        SharedPreferences prefs = mInstance.getSharedPreferences(Constants.PACKAGE_NAME, Context.MODE_PRIVATE);
        return prefs;
    }
}
