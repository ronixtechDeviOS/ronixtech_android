package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.github.clans.fab.FloatingActionButton;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.adapters.LinkedAccountsAdapter;
import com.ronixtech.ronixhome.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LinkedAccountsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LinkedAccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkedAccountsFragment extends androidx.fragment.app.Fragment {
    private static final String TAG = LinkedAccountsFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    FloatingActionButton addAccountFab;
    RelativeLayout addAccountLayout;

    List<User> accounts;
    ListView accountsListView;
    LinkedAccountsAdapter accountsAdapter;

    public LinkedAccountsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LinkedAccountsFragment.
     */
    public static LinkedAccountsFragment newInstance(String param1, String param2) {
        LinkedAccountsFragment fragment = new LinkedAccountsFragment();
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
        View view = inflater.inflate(R.layout.fragment_linked_accounts, container, false);
        MainActivity.setActionBarTitle(Utils.getString(getActivity(), R.string.linked_account), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        addAccountFab = view.findViewById(R.id.add_linked_account_fab);

        addAccountLayout = view.findViewById(R.id.add_new_linked_account_layout);

        accountsListView = view.findViewById(R.id.linked_accounts_listview);
        accounts = new ArrayList<>();
        accountsAdapter = new LinkedAccountsAdapter(getActivity(), accounts, new LinkedAccountsAdapter.AccountActionListener() {
            @Override
            public void onUserDeleted() {
                accounts.clear();
                if(MySettings.getAllLinkedAccounts() != null && MySettings.getAllLinkedAccounts().size() >= 1) {
                    accounts.addAll(MySettings.getAllLinkedAccounts());
                }
                accountsAdapter.notifyDataSetChanged();
                setLayoutVisibility();
            }
        });
        accountsListView.setAdapter(accountsAdapter);

        if(MySettings.getAllLinkedAccounts() != null && MySettings.getAllLinkedAccounts().size() >= 1) {
            accounts.addAll(MySettings.getAllLinkedAccounts());
        }
        accountsAdapter.notifyDataSetChanged();
        setLayoutVisibility();

        addAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                AddLinkedAccountFragment addLinkedAccountFragment = new AddLinkedAccountFragment();
                fragmentTransaction.replace(R.id.fragment_view, addLinkedAccountFragment, "addLinkedAccountFragment");
                fragmentTransaction.addToBackStack("addLinkedAccountFragment");
                fragmentTransaction.commit();
            }
        });

        addAccountFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getAllRooms() == null || MySettings.getAllRooms().size() < 1){
                    Utils.showToast(getActivity(), Utils.getString(getActivity(), R.string.add_room_first), true);
                }else{
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction = Utils.setAnimations(fragmentTransaction, Utils.ANIMATION_TYPE_TRANSLATION);
                    AddLinkedAccountFragment addLinkedAccountFragment = new AddLinkedAccountFragment();
                    fragmentTransaction.replace(R.id.fragment_view, addLinkedAccountFragment, "addLinkedAccountFragment");
                    fragmentTransaction.addToBackStack("addLinkedAccountFragment");
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
    }

    private void setLayoutVisibility(){
        boolean showAddAccountLayout = false;
        if(MySettings.getAllLinkedAccounts() == null || MySettings.getAllLinkedAccounts().size() < 1){
            showAddAccountLayout = true;
        }

        if(showAddAccountLayout){
            addAccountLayout.setVisibility(View.VISIBLE);
        }else{
            addAccountLayout.setVisibility(View.GONE);
        }

        if(showAddAccountLayout){
            addAccountFab.setVisibility(View.GONE);
            accountsListView.setVisibility(View.GONE);
        }else{
            addAccountFab.setVisibility(View.VISIBLE);
            accountsListView.setVisibility(View.VISIBLE);
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
