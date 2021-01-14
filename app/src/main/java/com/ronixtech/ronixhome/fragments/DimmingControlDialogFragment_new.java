package com.ronixtech.ronixhome.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.util.List;

public class DimmingControlDialogFragment_new extends DialogFragment {
    private static final String TAG = DimmingControlDialogFragment_new.class.getSimpleName();
    Device device;
    List<Line> lines;
    boolean isClicked[]=new boolean[3];
    boolean isEnabled[]=new boolean[3];

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static DimmingControlDialogFragment_new newInstance() {
        DimmingControlDialogFragment_new f = new DimmingControlDialogFragment_new();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_dimming_control_new, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        Croller croller = v.findViewById(R.id.dimming_croller);
        Button doneButton = v.findViewById(R.id.done_button);
        TextView roomNameTextView = v.findViewById(R.id.room_name_textview);
        TextView lineNameTextView = v.findViewById(R.id.line_name_textview);
        TextView lineName1TextView = v.findViewById(R.id.line_dimmer1_name);
        TextView lineName2TextView = v.findViewById(R.id.line_dimmer2_name);
        TextView lineName3TextView = v.findViewById(R.id.line_dimmer3_name);
        Button lineButton1 = v.findViewById(R.id.line_dimmer1);
        Button lineButton2 = v.findViewById(R.id.line_dimmer2);
        Button lineButton3 = v.findViewById(R.id.line_dimmer3);

        initClick();

        int mode = MySettings.getCurrentPlace().getMode();
//        Device device = MySettings.getDeviceByID2(line.getDeviceID());
        Room room = MySettings.getRoom(device.getRoomID());

        roomNameTextView.setText(""+room.getName());

        lineName1TextView.setText(lines.get(0).getName());

        lineName2TextView.setText(lines.get(1).getName());

        lineName3TextView.setText(lines.get(2).getName());

        if(lines.get(0).getDimmingState() == Line.DIMMING_STATE_ON)
        {
            lineButton1.setBackgroundColor(lineButton1.getContext().getResources().getColor(R.color.greenColor));
            isClicked[0]=true;
            isEnabled[0]=true;
        }
        else
        {
            lineButton1.setBackgroundColor(lineButton1.getContext().getResources().getColor(R.color.lightGrayColor));

        }

        if(lines.get(1).getDimmingState() == Line.DIMMING_STATE_ON)
        {
            lineButton2.setBackgroundColor(lineButton2.getContext().getResources().getColor(R.color.orangeColor));
            isEnabled[1]=true;
        }
        else
        {
            lineButton2.setBackgroundColor(lineButton2.getContext().getResources().getColor(R.color.lightGrayColor));
        }

        if(lines.get(2).getDimmingState() == Line.DIMMING_STATE_ON)
        {
            lineButton3.setBackgroundColor(lineButton3.getContext().getResources().getColor(R.color.orangeColor));
            isEnabled[2]=true;
        }
        else
        {
            lineButton3.setBackgroundColor(lineButton3.getContext().getResources().getColor(R.color.lightGrayColor));
        }
/*

        lineButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[0]) {
                    if (!isClicked[0]) {
                        lineButton1.setBackgroundColor(lineButton1.getContext().getResources().getColor(R.color.greenColor));
                        isClicked[0]=true;
                    }
                    else
                    {
                        lineButton2.setBackgroundColor(lineButton2.getContext().getResources().getColor(R.color.orangeColor));
                    }
                }
            }
        });

        lineButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
*/

     //   lineNameTextView.setText(""+line.getName());

        //croller.setLabel(""+line.getName());
//        croller.setProgress(line.getDimmingVvalue()*10);



        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int pro) {
                // use the progress
                MySettings.setControlState(true);
                double progressValue = pro/10.0; //scale might be 1-360 not 1-100
                int progress = (int) (progressValue);
            /*    Utils.controlDimming(device, line.getPosition(), progress, mode, new Utils.DimmingController.DimmingControlCallback() {
                    @Override
                    public void onDimmingSuccess() {

                    }

                    @Override
                    public void onDimmingFail() {

                    }
                });
            */}

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

    private void initClick() {
        for(int i=0;i<3;i++)
        {
            isClicked[i]=false;
        }
    }

    public void setLine(List<Line> lines){
        this.lines = lines;
    }

    public void setDevice(Device device)
    {
        this.device=device;
        setLine(device.getLines());
    }

}
