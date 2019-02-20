package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationFragment extends android.support.v4.app.Fragment {
    private static final String TAG = VerificationFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    TextView verificationMessageTextView;
    Button resendEmailButton, cancelButton;
    TextView wrongEmailTextView;

    FirebaseAuth mAuth;

    public VerificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VerificationFragment.
     */
    public static VerificationFragment newInstance(String param1, String param2) {
        VerificationFragment fragment = new VerificationFragment();
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
        View view = inflater.inflate(R.layout.fragment_verification, container, false);

        verificationMessageTextView = view.findViewById(R.id.verification_message_hint_textview);
        resendEmailButton = view.findViewById(R.id.resend_email_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        wrongEmailTextView = view.findViewById(R.id.wrong_email_textview);

        wrongEmailTextView.setPaintFlags(wrongEmailTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        if(MySettings.getActiveUser() != null) {
            verificationMessageTextView.setText(Utils.getStringExtraText(getActivity(), R.string.verification_message_hint, MySettings.getActiveUser().getEmail()));
        }else{
            verificationMessageTextView.setText(Utils.getStringExtraText(getActivity(), R.string.verification_message_hint, ""));
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
                getActivity().finish();
            }
        });

        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MySettings.getActiveUser() != null) {
                    sendVerificationEmail();
                }
            }
        });

        wrongEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
                getActivity().finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    private void sendVerificationEmail(){
        Utils.showLoading(getActivity());

        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl(Constants.FIREBASE_DYNAMIC_LINK_VERIFICATION_URL)
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                Constants.PACKAGE_NAME,
                                true, /* installIfNotAvailable */
                                Constants.FIREBASE_DYNAMIC_LINKS_MIN_VERSION    /* minimumVersion */)
                        .build();

        if(mAuth != null){
            FirebaseUser fbUser = mAuth.getCurrentUser();
            if(fbUser != null){
                fbUser.sendEmailVerification(actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Utils.dismissLoading();
                            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.verification_mail_sent_successfully), true);
                            Utils.log(TAG, "sendEmailVerification Email sent.", true);
                        } else {
                            // If sending mail fails, display a message to the user.
                            Utils.log(TAG, "sendEmailVerification failure: " + task.getException(), true);
                            if (task.getException() != null) {
                                Utils.showToast(getActivity(), "" + task.getException().getMessage(), true);
                            }
                            //Toast.makeText(getActivity(), getActivity().getResources().getStringExtraInt(R.string.login_failed), Toast.LENGTH_SHORT).show();
                            Utils.dismissLoading();
                        }
                    }
                });
            }else{
                Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.verification_email_failed), true);
                Utils.dismissLoading();
            }
        }else{
            Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.verification_email_failed), true);
            Utils.dismissLoading();
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
