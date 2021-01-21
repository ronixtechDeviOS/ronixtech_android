package com.ronixtech.ronixhome.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.entities.Room;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.util.ArrayList;
import java.util.List;

public class DimmingControlDialogFragment_new extends DialogFragment {
    private static final String TAG = DimmingControlDialogFragment_new.class.getSimpleName();
    Device device;
    List<Line> lines;
    int currLine;
    Croller croller;
    Handler handler;
    Runnable runnable;
    GradientDrawable dimmingButton;
    List<Button> buttons=new ArrayList<Button>();
    List<TextView> textViews=new ArrayList<TextView>();
    List<RelativeLayout> relativeLayouts=new ArrayList<RelativeLayout>();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_dimming_control_new, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

         croller = v.findViewById(R.id.dimming_croller);
        Button doneButton = v.findViewById(R.id.done_button);
        TextView roomNameTextView = v.findViewById(R.id.room_name_textview);
        TextView lineNameTextView = v.findViewById(R.id.line_name_textview);
        textViews.add(v.findViewById(R.id.line_dimmer1_name));
        textViews.add(v.findViewById(R.id.line_dimmer2_name));
        textViews.add(v.findViewById(R.id.line_dimmer3_name));
        buttons.add(v.findViewById(R.id.line_dimmer1));
        buttons.add(v.findViewById(R.id.line_dimmer2));
        buttons.add(v.findViewById(R.id.line_dimmer3));
        relativeLayouts.add(v.findViewById(R.id.line_dimmer_layout1));
        relativeLayouts.add(v.findViewById(R.id.line_dimmer_layout2));
        relativeLayouts.add(v.findViewById(R.id.line_dimmer_layout3));

        handler=new Handler();
        int mode = MySettings.getCurrentPlace().getMode();
//        Device device = MySettings.getDeviceByID2(line.getDeviceID());
        Room room = MySettings.getRoom(device.getRoomID());

        roomNameTextView.setText(""+room.getName());
        lineNameTextView.setText(""+device.getName());
        initTextview();
        initisEnabled();

        currLine=-1;
        for(int i=0;i<device.getLines().size();i++)
        {
            if(lines.get(i).getDimmingState()==Line.DIMMING_STATE_ON)
            {
                if(currLine == -1)
                {
                    isEnabled[i]=true;
                    turnButtonOn(buttons.get(i));
                    currLine= i;
                    setConProg();
                }
                else
                {
                    isEnabled[i]=true;
                    turnButtonNeutral(buttons.get(i));
                }
            }
            else
            {
                isEnabled[i]=false;
                turnButtonOff(buttons.get(i));
                textViews.get(i).setTextColor(textViews.get(i).getContext().getResources().getColor(R.color.lightGrayColor));
            }
        }

        buttons.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[0])
                {
                    if(currLine!=0)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(0));
                        currLine=0;
                        setConProg();
                    }
                }
            }
        });
        buttons.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[1])
                {
                    if(currLine!=1)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(1));
                        currLine=1;
                        setConProg();
                    }
                }
            }
        });

        buttons.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[2])
                {
                    if(currLine!=2)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(2));
                        currLine=2;
                        setConProg();
                    }
                }
            }
        });

        relativeLayouts.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[0])
                {
                    if(currLine!=0)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(0));
                        currLine=0;
                        setConProg();
                    }
                }
            }
        });


        relativeLayouts.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[1])
                {
                    if(currLine!=1)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(1));
                        currLine=1;
                        setConProg();
                    }
                }
            }
        });

        relativeLayouts.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled[2])
                {
                    if(currLine!=2)
                    {
                        turnButtonNeutral(buttons.get(currLine));
                        turnButtonOn(buttons.get(2));
                        currLine=2;
                        setConProg();
                    }
                }
            }
        });


        //croller.setLabel(""+line.getName());


        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int pro) {
                // use the progress
                MySettings.setControlState(true);

                //     double progressValue = pro/10.0; //scale might be 1-360 not 1-100
                int progress = (int) (pro);
                if(lines.get(currLine).getDimmingVvalue() != progress) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                         Utils.controlDimming(device, currLine, progress, mode, new Utils.DimmingController.DimmingControlCallback() {
                        @Override
                        public void onDimmingSuccess() {

                        }

                        @Override
                        public void onDimmingFail() {

                        }
                           });
                        }
                    };
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(r,100);

                }
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


    private void setConProg() {
        if(currLine!=-1) {
            if(croller!=null) {
                croller.setProgress(lines.get(currLine).getDimmingVvalue());
                //  croller.setProgress(lines.get(currLine).getDimmingVvalue() * 10);
            }
        }
    }

    private void initisEnabled() {
        for(int i=0;i<3;i++)
        {
            isEnabled[i]=false;
        }
    }

    private void initTextview() {
        for(int i=0;i<device.getLines().size();i++)
        {
            textViews.get(i).setText(lines.get(i).getName());
        }
    }


    public void setLine(List<Line> lines){
        this.lines = lines;
    }


    public void turnButtonOn(Button b)
    {
       /* b.setBackgroundColor(Color.parseColor("#8bc439"));
        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button));
*/
//        b.setBackgroundColor(Color.parseColor("#8bc439"));
        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button_on));

    }


    public void turnButtonNeutral(Button b)
    {
     //   b.setBackgroundColor(Color.parseColor("#e76616"));
/*        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button));



        dimmingButton.setColor(Color.parseColor("#e76616"));
        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button));
*/
        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button_neutral));

    }

    public void turnButtonOff(Button b)
    {
        b.setBackground(ContextCompat.getDrawable(MainActivity.getInstance(), R.drawable.dimming_button_off));
    }
    public void setDevice(Device device)
    {
        this.device=device;
        setLine(device.getLines());
        setConProg();
    }



}
