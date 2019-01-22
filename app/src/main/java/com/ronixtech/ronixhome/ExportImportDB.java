package com.ronixtech.ronixhome;

import android.os.Environment;

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
            File ronixDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/");
            if(!ronixDirectory.exists()) {
                if(ronixDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Utils.log(TAG, "Created directory: " + ronixDirectory.getAbsolutePath(), true);
            File databaseDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/" + "Databases/");
            if(!databaseDirectory.exists()) {
                if(databaseDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Utils.log(TAG, "Created directory: " + databaseDirectory.getAbsolutePath(), true);
        }catch (Exception e){
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            importSuccess = false;
        }


        try {
            String backupDBPath  = MyApp.getInstance().getFilesDir() + "/RonixHome/" + "Databases/" + fileName;
            String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;


            Utils.log(TAG, "Backup directory: " + backupDBPath, true);
            Utils.log(TAG, "Live Database directory: " + liveDBPath, true);

            File liveDBFile = new File(liveDBPath);
            File backupDBFile = new File(backupDBPath);

            Utils.log(TAG, "Backup file object: " + backupDBFile.getAbsolutePath(), true);
            Utils.log(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath(), true);

            FileChannel src = new FileInputStream(backupDBFile).getChannel();
            FileChannel dst = new FileOutputStream(liveDBFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            /*File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath  = Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName;
                String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;


                Utils.log(TAG, "Backup directory: " + backupDBPath, true);
                Utils.log(TAG, "Live Database directory: " + liveDBPath, true);

                File liveDBFile = new File(liveDBPath);
                File backupDBFile = new File(backupDBPath);

                Utils.log(TAG, "Backup file object: " + backupDBFile.getAbsolutePath(), true);
                Utils.log(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath(), true);

                FileChannel src = new FileInputStream(backupDBFile).getChannel();
                FileChannel dst = new FileOutputStream(liveDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else{
                Utils.showToast(MainActivity.getInstance(), "Can't write to SD", true);
                importSuccess = false;
            }*/
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
            File ronixDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/");
            if(!ronixDirectory.exists()) {
                if(ronixDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Utils.log(TAG, "Created directory: " + ronixDirectory.getAbsolutePath(), true);
            File databaseDirectory = new File(MyApp.getInstance().getFilesDir() + "/RonixHome/" + "Databases/");
            if(!databaseDirectory.exists()) {
                if(databaseDirectory.mkdir()) {
                    //directory is created;
                }
            }
            Utils.log(TAG, "Created directory: " + databaseDirectory.getAbsolutePath(), true);
        }catch (Exception e){
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            exportSuccess = false;
        }

        try {
            String backupDBPath  = MyApp.getInstance().getFilesDir() + "/RonixHome/" + "Databases/" + fileName;
            String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;

            Utils.log(TAG, "Backup directory: " + backupDBPath, true);
            Utils.log(TAG, "Live Database directory: " + liveDBPath, true);

            File backupDBFile = new File(backupDBPath);
            File liveDBFile = new File(liveDBPath);

            Utils.log(TAG, "Backup file object: " + backupDBFile.getAbsolutePath(), true);
            Utils.log(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath(), true);

            FileChannel src = new FileInputStream(liveDBFile).getChannel();
            FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            /*File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath  = Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName;
                String liveDBPath = Environment.getDataDirectory() + "//data//" + Constants.PACKAGE_NAME + "//databases//" + fileName;

                Utils.log(TAG, "Backup directory: " + backupDBPath, true);
                Utils.log(TAG, "Live Database directory: " + liveDBPath, true);

                File backupDBFile = new File(backupDBPath);
                File liveDBFile = new File(liveDBPath);

                Utils.log(TAG, "Backup file object: " + backupDBFile.getAbsolutePath(), true);
                Utils.log(TAG, "Live Database file object: " + liveDBFile.getAbsolutePath(), true);

                FileChannel src = new FileInputStream(liveDBFile).getChannel();
                FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else{
                Utils.showToast(MainActivity.getInstance(), "Can't write " + fileName + " to SD", true);
                exportSuccess = false;
            }*/
        } catch (Exception e) {
            Utils.showToast(MainActivity.getInstance(), e.toString(), true);
            exportSuccess = false;
        }
        return exportSuccess;
    }
}
