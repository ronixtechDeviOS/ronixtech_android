package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.AppDatabase;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.adapters.BackupsAdapter;
import com.ronixtech.ronixhome.entities.Backup;

import java.util.ArrayList;
import java.util.List;

public class PickBackupDialogFragment extends DialogFragment {
    private static final String TAG = PickBackupDialogFragment.class.getSimpleName();
    private OnBackupSelectedListener callback;



    private List<Backup> backupNames;
    ListView listView;
    BackupsAdapter adapter;

    Fragment placeFragment;

    public interface OnBackupSelectedListener {
        public void onBackupSelected(Backup backup);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickBackupDialogFragment newInstance() {
        PickBackupDialogFragment f = new PickBackupDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (OnBackupSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnBackupSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_backup_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        listView = new ListView(getActivity());
        if(backupNames == null) {
            backupNames = new ArrayList<>();
        }
        adapter = new BackupsAdapter(getActivity(), backupNames);
        listView.setAdapter(adapter);
        //listView.setDivider(null);


        //get all backup names from firebase storage using the email


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Backup selectedBackup = (Backup) adapter.getItem(position);
                if(selectedBackup != null){
                    if(selectedBackup.getDbVersion() == AppDatabase.version) {
                        callback.onBackupSelected(selectedBackup);
                    }else{
                        Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.backup_not_compatible), true);
                    }
                }

                dismiss();
            }
        });

        return listView;
    }

    public void setParentFragment(Fragment fragment){
        this.placeFragment = fragment;
    }

    public void setBackups(List<Backup> backups){
        if(backupNames == null){
            backupNames = new ArrayList<>();
        }
        backupNames.addAll(backups);
        if(adapter != null) {
            adapter.notifyDataSetChanged();
            Utils.justifyListViewHeightBasedOnChildren(listView);
        }
    }
}
