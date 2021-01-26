package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.alexa.ApiResponse;
import com.ronixtech.ronixhome.alexa.AudioPlayer;
import com.ronixtech.ronixhome.alexa.AvsItem;
import com.ronixtech.ronixhome.alexa.AvsSpeakItem;
import com.ronixtech.ronixhome.alexa.AvsTemplateItem;
import com.ronixtech.ronixhome.alexa.ConnectManager;
import com.ronixtech.ronixhome.alexa.LoginManager;
import com.ronixtech.ronixhome.entities.Device;

import org.json.JSONObject;

import java.util.List;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;

public class AlexaFragment extends Fragment {
    private static final String TAG = AlexaFragment.class.getSimpleName();
    private static final String KEY_CODE_VERIFIER ="code_verifier";
    private static SharedPreferences mPref;

    TextView loginButton;
    View mPressButton,mPulseView,mProcessingView;

    private RawAudioRecorder mRecorder;
    private AudioPlayer mAudioPlayer;
    private static final int AUDIO_RATE = 16000;
    private boolean isRecording = false;

    RequestContext requestContext;
    final JSONObject scopeData = new JSONObject();
    final JSONObject productInstanceAttributes = new JSONObject();
    private ConnectManager mConnectManager;
    private Device device;

    public AlexaFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AlexaFragment newInstance(String param1, String param2) {
        AlexaFragment fragment = new AlexaFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestContext=RequestContext.create(MainActivity.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alexa, container, false);
        loginButton=view.findViewById(R.id.login_button);
        mPressButton = view.findViewById(R.id.press_button);
        mPulseView = view.findViewById(R.id.avi);
        mProcessingView = view.findViewById(R.id.processing_view);

        device= new Device();
        mConnectManager = new ConnectManager(MainActivity.getInstance());
        mAudioPlayer = new AudioPlayer(MainActivity.getInstance());
        mPref = MainActivity.getInstance().getSharedPreferences("ronixtech", Context.MODE_PRIVATE);

        loginButton.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
        mPressButton.setVisibility(LoginManager.isLogin() ? View.VISIBLE : View.GONE);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LoginManager.doLogin(requestContext, new LoginManager.LoginCallback() {
                    @Override
                    public void onSuccess() {
                        setLoginStatus();

                        long expireTime = LoginManager.getExpireTime();
                        Toast.makeText(MainActivity.getInstance(), "Login Success, Token Expires At " + Utils.getDateString(expireTime), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(MainActivity.getInstance(), "Login Fail", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        mPressButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //long press to listen
                        startListening();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        //release to stop recording and send request to alexa
                        stopListening();
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        setLoginStatus();
        return view;
    }

    private void setLoginStatus() {
        //token expired
        if (LoginManager.isLogin()  && System.currentTimeMillis() > LoginManager.getExpireTime()) {
            Toast.makeText(MainActivity.getInstance(), "Token Expired, Refreshing Token..", Toast.LENGTH_LONG).show();
            LoginManager.doRefreshToken(new LoginManager.LoginCallback() {
                @Override
                public void onSuccess() {
                    long expireTime = LoginManager.getExpireTime();
                    Toast.makeText(MainActivity.getInstance(), "Token Refreshed, Token Expires At " + Utils.getDate(expireTime), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFail() {
                    LoginManager.logout();
                    setLoginStatus();
                    Toast.makeText(MainActivity.getInstance(), "Refresh Token Failed", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        loginButton.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
        mPressButton.setVisibility(LoginManager.isLogin() ? View.VISIBLE : View.GONE);
    }

    private void startListening() {
        mPulseView.setVisibility(View.VISIBLE);
        if (!isRecording) {
            if (mRecorder == null) {
                mRecorder = new RawAudioRecorder(AUDIO_RATE);
            }
            mRecorder.start();
            isRecording = true;
        }
    }


    private void stopListening() {
        mPulseView.setVisibility(View.GONE);
       // mProcessingView.setVisibility(View.VISIBLE);
        if (mRecorder != null) {
                    final byte[] recordBytes = mRecorder.getCompleteRecording();
                    Log.v("Recorder: "," "+recordBytes.length);
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    isRecording = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mConnectManager.sendRequest(recordBytes, new ConnectManager.Callback() {
                                @Override
                                public void onResponse(final ApiResponse res) {
                                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (res.getResponseCode() != 200) {
                                            Toast.makeText(MainActivity.getInstance(), res.getResponseCode() + " " + res.getMessage(), Toast.LENGTH_LONG).show();
                                        } else {
                                           // Toast.makeText(MainActivity.getInstance(), res.getResponseCode() + " " + res.getMessage(), Toast.LENGTH_LONG).show();
                                              onAlexaResponse(res.getAvsItems());
                                        }
                                     //  mProcessingView.setVisibility(View.GONE);
                                    }
                                });
                                }
                            });
                        }
                    }).start();

                }
        }

    private void onAlexaResponse(List<AvsItem> res) {
        if (res != null) {
            for (AvsItem item : res) {
                Log.v("MAIN","Response: "+ item);
                if (item instanceof AvsSpeakItem) {
                    mAudioPlayer.play((AvsSpeakItem) item);

                }
                if (item instanceof AvsTemplateItem) {
                    final AvsTemplateItem templateItem = (AvsTemplateItem) item;
                    if (templateItem.isBodyType()) {
                            String txt = ((AvsTemplateItem) item).getPayLoad().getTextField();
                            if(MySettings.getCurrentPlace() != null) {
                                int mode=MySettings.getCurrentPlace().getMode();
                                if(txt.contains("on")) {
                                    if(txt.contains("device"))
                                    {
                                      String deviceName =  txt.substring(txt.indexOf("device "+1));
                                       device= MySettings.getDeviceByName(deviceName);
                                       if(device != null)
                                       {
                                          // Utils.toggleLine();
                                       }
                                       else
                                       {
                                           Utils.showToast(MainActivity.getInstance(),"No Device Found",false);
                                       }
                                    }
                                    else
                                    {
                                        Utils.showToast(MainActivity.getInstance(),"Incorrect Command",false);
                                    }
                                }
                                else if(txt.contains("all off"))
                                {
                                    Utils.toggleDevice(MySettings.getDeviceByChipID2("e3ca40"),0,mode);
                                }
                            }
                        }
                    }

                }
            }
        }
    }



