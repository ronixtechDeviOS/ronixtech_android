package com.ronixtech.ronixhome.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

public class DimmingControlDialogFragment extends DialogFragment {
    private static final String TAG = DimmingControlDialogFragment.class.getSimpleName();

    Device device;
    Line line;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static DimmingControlDialogFragment newInstance() {
        DimmingControlDialogFragment f = new DimmingControlDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_dimming_control, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        Croller croller = v.findViewById(R.id.dimming_croller);
        Button doneButton = v.findViewById(R.id.done_button);
        TextView roomNameTextView = v.findViewById(R.id.room_name_textview);
        TextView lineNameTextView = v.findViewById(R.id.line_name_textview);

        int mode = MySettings.getCurrentPlace().getMode();
        Device device = MySettings.getDeviceByID2(line.getDeviceID());
        Room room = MySettings.getRoom(device.getRoomID());

        roomNameTextView.setText(""+room.getName());
        lineNameTextView.setText(""+line.getName());

        //croller.setLabel(""+line.getName());
        croller.setProgress(line.getDimmingVvalue()*10);

        /*croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int pro) {
                MySettings.setControlState(true);
                double progressValue = pro/10.0; //scale might be 1-360 not 1-100
                int progress = (int) (progressValue);
                Utils.controlDimming(device, item.getPosition(), progress, mode, new Utils.DimmingController.DimmingControlCallback() {
                    @Override
                    public void onDimmingSuccess() {

                    }

                    @Override
                    public void onDimmingFail() {

                    }
                });
            }
        });*/

        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int pro) {
                // use the progress
                MySettings.setControlState(true);
                double progressValue = pro/10.0; //scale might be 1-360 not 1-100
                int progress = (int) (progressValue);
                Utils.controlDimming(device, line.getPosition(), progress, mode, new Utils.DimmingController.DimmingControlCallback() {
                    @Override
                    public void onDimmingSuccess() {

                    }

                    @Override
                    public void onDimmingFail() {

                    }
                });
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                // tracking started
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                // tracking stopped
                //getDialog().dismiss();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return v;
    }

    public void setLine(Line line){
        this.line = line;
    }
}
