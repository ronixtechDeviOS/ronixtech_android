package com.ronixtech.ronixhome;

import android.os.Environment;
import android.util.Log;

import com.ronixtech.ronixhome.activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class ExportImportDB {
    private static final String TAG = ExportImportDB.class.getSimpleName();

    //importing database
    public static boolean importDB(String fileName) {
        boolean importSuccess = true;
        try{
            //creating a new folder for the database to be backuped from
            File ronixDirectory = new File(Environment.getExternalStorageDirectory() + "/RonixHome/");
            if(!ronixDirectory.exists()) {
                if(ronixDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Log.d(TAG, "Created directory: " + ronixDirectory.getAbsolutePath());
            File databaseDirectory = new File(Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/");
            if(!databaseDirectory.exists()) {
                if(databaseDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Log.d(TAG, "Created directory: " + databaseDirectory.getAbsolutePath());
        }catch (Exception e){
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            importSuccess = false;
        }


        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath  = Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName;
                String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;

                Log.d(TAG, "Backup directory: " + backupDBPath);
                Log.d(TAG, "Live Database directory: " + liveDBPath);

                File liveDBFile = new File(liveDBPath);
                File backupDBFile = new File(backupDBPath);

                Log.d(TAG, "Backup file object: " + backupDBFile.getAbsolutePath());
                Log.d(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath());

                FileChannel src = new FileInputStream(backupDBFile).getChannel();
                FileChannel dst = new FileOutputStream(liveDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else{
                Utils.showToast(MainActivity.getInstance(), "Can't write to SD", true);
                importSuccess = false;
            }
        } catch (Exception e) {
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            importSuccess = false;
        }
        return importSuccess;
    }

    //exporting database
    public static boolean exportDB(String fileName) {
        boolean exportSuccess = true;
        try{
            //creating a new folder for the database to be backuped to
            File ronixDirectory = new File(Environment.getExternalStorageDirectory() + "/RonixHome/");
            if(!ronixDirectory.exists()) {
                if(ronixDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Log.d(TAG, "Created directory: " + ronixDirectory.getAbsolutePath());
            File databaseDirectory = new File(Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/");
            if(!databaseDirectory.exists()) {
                if(databaseDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Log.d(TAG, "Created directory: " + databaseDirectory.getAbsolutePath());
        }catch (Exception e){
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            exportSuccess = false;
        }

        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath  = Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName;
                String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;

                Log.d(TAG, "Backup directory: " + backupDBPath);
                Log.d(TAG, "Live Database directory: " + liveDBPath);

                File backupDBFile = new File(backupDBPath);
                File liveDBFile = new File(liveDBPath);

                Log.d(TAG, "Backup file object: " + backupDBFile.getAbsolutePath());
                Log.d(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath());

                FileChannel src = new FileInputStream(liveDBFile).getChannel();
                FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else{
                Utils.showToast(MainActivity.getInstance(), "Can't write " + fileName + " to SD", true);
                exportSuccess = false;
            }
        } catch (Exception e) {
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            exportSuccess = false;
        }
        return exportSuccess;
    }
}
