package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;

import java.util.ArrayList;
import java.util.List;

public class PickBackupDialogFragment extends DialogFragment {
    private static final String TAG = PickBackupDialogFragment.class.getSimpleName();
    private OnBackupSelectedListener callback;



    private List<String> backupNames;
    ListView listView;
    ArrayAdapter<String> adapter;

    Fragment placeFragment;

    public interface OnBackupSelectedListener {
        public void onBackupSelected(String backupName);
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
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, backupNames);
        listView.setAdapter(adapter);
        listView.setDivider(null);


        //get all backup names from firebase storage using the email


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedBackupName = (String) adapter.getItem(position);
                callback.onBackupSelected(selectedBackupName);
                dismiss();
            }
        });

        return listView;
    }

    public void setParentFragment(Fragment fragment){
        this.placeFragment = fragment;
    }

    public void setBackups(List<String> backups){
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
