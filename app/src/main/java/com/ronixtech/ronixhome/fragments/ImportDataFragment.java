package com.ronixtech.ronixhome.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.ExportImportDB;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImportDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImportDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImportDataFragment extends android.support.v4.app.Fragment implements PickBackupDialogFragment.OnBackupSelectedListener{
    private static final String TAG = ImportDataFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    private static final int RC_PERMISSION_WRITE_EXTERNAL_STORAGE = 1001;

    RelativeLayout importingDataLayout, downloadingProgressLayout;

    ProgressBar progressCircle;
    TextView progressTextView;
    int totalNumberOfFiles = 3;
    int currentFile = 1;

    RelativeLayout backupSelectionLayout;
    Button importButton;

    String selectedBackupName;

    public ImportDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImportDataFragment.
     */
    public static ImportDataFragment newInstance(String param1, String param2) {
        ImportDataFragment fragment = new ImportDataFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_import_data, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.import_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        checkExternalStoragePermissions();

        importingDataLayout = view.findViewById(R.id.import_data_layout);
        downloadingProgressLayout = view.findViewById(R.id.downloading_layout);

        progressCircle = view.findViewById(R.id.progress_circle);
        progressTextView = view.findViewById(R.id.progress_textview);

        backupSelectionLayout = view.findViewById(R.id.import_name_selection_layout);
        importButton = view.findViewById(R.id.import_button);

        backupSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MySettings.getActiveUser() != null && MySettings.getActiveUser().getEmail().length() >= 1){
                    getBackups();
                }
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Utils.InternetChecker(getActivity(), new Utils.InternetChecker.OnConnectionCallback() {
                    @Override
                    public void onConnectionSuccess() {
                        downloadingProgressLayout.setVisibility(View.VISIBLE);
                        importingDataLayout.setVisibility(View.GONE);

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

                            downloadFile(Constants.DB_FILE_1);
                        }catch (Exception e){
                            Toast.makeText(MainActivity.getInstance(), e.toString(), Toast.LENGTH_LONG).show();
                            if(getFragmentManager() != null){
                                getFragmentManager().popBackStack();
                            }
                        }
                    }

                    @Override
                    public void onConnectionFail(String errorMsg) {
                        if(getActivity() != null){
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_internet_connection_try_later), Toast.LENGTH_SHORT).show();
                        }
                        if(getFragmentManager() != null){
                            getFragmentManager().popBackStack();
                        }
                    }
                }).execute();
            }
        });

        return view;
    }

    @Override
    public void onBackupSelected(String backupName){
        this.selectedBackupName = backupName;
    }

    private void getBackups(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        //create new reference for current user's personal dir
        StorageReference personalDirRef = storageRef.child(MySettings.getActiveUser().getEmail());
        //create new reference for exports dir for current user
        StorageReference exportsRef = personalDirRef.child("exports");

        //TODO firebase storage client-api doesn't have a -ls dir option, research this

        List<String> backups = new ArrayList<>();

        if(backups.size() >= 1){
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickBackupDialogFragment");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            PickBackupDialogFragment fragment = PickBackupDialogFragment.newInstance();
            fragment.setBackups(backups);
            fragment.setTargetFragment(ImportDataFragment.this, 0);
            fragment.setParentFragment(ImportDataFragment.this);
            fragment.show(ft, "pickBackupDialogFragment");
        }else{
            if(getActivity() != null) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.downloading_database_file_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile(String fileName){
        //download 3 files to firebase-db/email/exports/timestamp (show picker for timestamp if more than 1 entry)
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        //create new reference for current user's personal dir
        StorageReference personalDirRef = storageRef.child(MySettings.getActiveUser().getEmail());
        //create new reference for exports dir for current user
        StorageReference exportsRef = personalDirRef.child("exports");
        //create new reference for exports dir for current file
        StorageReference fileRef = exportsRef.child(fileName);


        File file = new File(Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName);

        fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                currentFile++;
                if(currentFile == 2){
                    downloadFile(Constants.DB_FILE_2);
                }else if(currentFile == 3){
                    downloadFile(Constants.DB_FILE_3);
                }else{
                    //done downloading all files
                    //if success
                    if(ExportImportDB.importDB(Constants.DB_FILE_1) && ExportImportDB.importDB(Constants.DB_FILE_2) && ExportImportDB.importDB(Constants.DB_FILE_3)){
                        //go to successFragment
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                        SuccessFragment successFragment = new SuccessFragment();
                        successFragment.setSuccessSource(Constants.SUCCESS_SOURCE_IMPORT);
                        fragmentTransaction.replace(R.id.fragment_view, successFragment, "successFragment");
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commit();

                        /*if(getActivity() != null){
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.import_successful), Toast.LENGTH_SHORT).show();
                        }*/
                    }else{
                        if(getActivity() != null){
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.import_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("AAAA", "Exception: " + exception.getMessage());
                Log.d("AAAA", "Exception: " + exception.getStackTrace());
                // Handle any errors
                if(getActivity() != null){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.downloading_database_file_failed), Toast.LENGTH_SHORT).show();
                }
                if(getFragmentManager() != null){
                    getFragmentManager().popBackStack();
                }
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressCircle.setProgress(progress.intValue());
            }
        });
    }

    private void checkExternalStoragePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
            }else{
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, RC_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }else{
            //no need to show runtime permission stuff
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //allowed
                } else{
                    //denied
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), "You need to enable external storage permission", Toast.LENGTH_SHORT).show();
                    }
                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("External Storage permission")
                                .setMessage("You need to enable external storage permissions for the app to export/import data")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, RC_PERMISSION_WRITE_EXTERNAL_STORAGE);
                                    }
                                })
                                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(getFragmentManager() != null) {
                                            getFragmentManager().popBackStack();
                                        }
                                    }
                                })
                                .show();
                    }else{
                        if(getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_gym, menu);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
