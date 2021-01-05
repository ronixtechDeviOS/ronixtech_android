package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.R;

public class DuplicateDeviceActionDialogFragment extends DialogFragment {
    private static final String TAG = DuplicateDeviceActionDialogFragment.class.getSimpleName();
    private DuplicateDeviceActionDialogFragment.OnActionPickedListener callback;

    public interface OnActionPickedListener {
        public void onActionPicked(int action);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PickPlaceDialogFragment newInstance() {
        PickPlaceDialogFragment f = new PickPlaceDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (OnActionPickedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnActionPickedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_duplicate_device_action, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Button deleteControllerButton, keepControllerButton, cancelButton;

        callback.onActionPicked(Constants.ACTION_YES);
        callback.onActionPicked(Constants.ACTION_NO);
        callback.onActionPicked(Constants.ACTION_CANCEL);
        dismiss();

        return view;
    }
}
