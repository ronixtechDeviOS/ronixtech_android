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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.ExportImportDB;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;

import java.io.File;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExportDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExportDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExportDataFragment extends android.support.v4.app.Fragment {
    private static final String TAG = ExportDataFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    private static final int RC_PERMISSION_WRITE_EXTERNAL_STORAGE = 1001;

    RelativeLayout exportingDataLayout, uploadingProgressLayout;

    ProgressBar progressCircle;
    TextView progressTextView;
    int totalNumberOfFiles = 3;
    int currentFile = 1;

    EditText exportNameEditText;
    Button exportButton;

    public ExportDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExportDataFragment.
     */
    public static ExportDataFragment newInstance(String param1, String param2) {
        ExportDataFragment fragment = new ExportDataFragment();
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
        View view = inflater.inflate(R.layout.fragment_export_data, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.export_data), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        checkExternalStoragePermissions();

        exportingDataLayout = view.findViewById(R.id.export_data_layout);
        uploadingProgressLayout = view.findViewById(R.id.uploading_layout);

        progressCircle = view.findViewById(R.id.progress_circle);
        progressTextView = view.findViewById(R.id.progress_textview);

        exportNameEditText = view.findViewById(R.id.export_name_edittedxt);
        exportButton = view.findViewById(R.id.export_button);

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Utils.InternetChecker(getActivity(), new Utils.InternetChecker.OnConnectionCallback() {
                    @Override
                    public void onConnectionSuccess() {
                        uploadingProgressLayout.setVisibility(View.VISIBLE);
                        exportingDataLayout.setVisibility(View.GONE);

                        String exportName = "";
                        if(exportNameEditText.getText().toString().length() >= 1){
                            exportName = exportNameEditText.getText().toString();
                        }else{
                            exportName = String.valueOf(new Date().getTime());
                        }

                        if(ExportImportDB.exportDB(Constants.DB_FILE_1) && ExportImportDB.exportDB(Constants.DB_FILE_2) && ExportImportDB.exportDB(Constants.DB_FILE_3)){
                            uploadData(Constants.DB_FILE_1, exportName);
                        }else{
                            if(getActivity() != null){
                                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.export_failed), Toast.LENGTH_SHORT).show();
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

    private void uploadData(String fileName, String exportName){
        progressTextView.setText(getActivity().getResources().getString(R.string.uploading_database_file, currentFile, totalNumberOfFiles));
        //upload 3 files to firebase-db/email/exports/timestamp
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        //create new reference for current user's personal dir
        StorageReference personalDirRef = storageRef.child(MySettings.getActiveUser().getEmail());
        //create new reference for exports dir for current user
        StorageReference exportsRef = personalDirRef.child("exports");
        /*//create new reference for current backup with current timestamp
        StorageReference backupReference = exportsRef.child(exportName);
        //create new reference for current backup file
        StorageReference dbFile1Reference = backupReference.child(fileName);*/

        //create new reference for current backup file
        StorageReference dbFile1Reference = exportsRef.child(fileName);

        UploadTask uploadTask = dbFile1Reference.putFile(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/RonixHome/" + "Databases/" + fileName)));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                currentFile++;
                if(currentFile == 2){
                    uploadData(Constants.DB_FILE_2, exportName);
                }else if(currentFile == 3){
                    uploadData(Constants.DB_FILE_3, exportName);
                }else{
                    //done uploading all files
                    /*if(getActivity() != null){
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.export_successful), Toast.LENGTH_SHORT).show();
                    }*/
                    //go to successFragment
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    SuccessFragment successFragment = new SuccessFragment();
                    successFragment.setSuccessSource(Constants.SUCCESS_SOURCE_EXPORT);
                    fragmentTransaction.replace(R.id.fragment_view, successFragment, "successFragment");
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentTransaction.commit();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                if(getActivity() != null){
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.uploading_database_file_failed), Toast.LENGTH_SHORT).show();
                }
                if(getFragmentManager() != null){
                    getFragmentManager().popBackStack();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
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
