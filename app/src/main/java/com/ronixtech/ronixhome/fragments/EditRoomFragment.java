package com.ronixtech.ronixhome.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.GlideApp;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Floor;
import com.ronixtech.ronixhome.entities.Place;
import com.ronixtech.ronixhome.entities.Room;
import com.ronixtech.ronixhome.entities.Type;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditRoomFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditRoomFragment extends android.support.v4.app.Fragment implements PickPlaceDialogFragment.OnPlaceSelectedListener,
        TypePickerDialogFragment.OnTypeSelectedListener{
    private static final String TAG = EditRoomFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    RelativeLayout placeSelectionLayout, selectedFloorLayout;
    TextView placeNameTextView;
    ImageView placeImageView;
    TextView selectedFloorTextView;
    Button incrementFloorButton, decremetnFloorButton;
    EditText roomNameEditText;
    RelativeLayout roomTypeSelectionLayout;
    TextView roomTypeNameTextView;
    ImageView roomTypeImageView;
    Button saveRoomButton;

    Place selectedPlace;
    Floor selectedFloor;
    int selectedFloorIndex = 0;

    Type selectedRoomType;

    private Room room;

    public EditRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditRoomFragment.
     */
    public static EditRoomFragment newInstance(String param1, String param2) {
        EditRoomFragment fragment = new EditRoomFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_room, container, false);
        if(room != null){
            MainActivity.setActionBarTitle(room.getName(), getResources().getColor(R.color.whiteColor));
        }else{
            MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.edit_room), getResources().getColor(R.color.whiteColor));
        }
        setHasOptionsMenu(true);

        placeSelectionLayout = view.findViewById(R.id.place_selection_layout);
        placeNameTextView = view.findViewById(R.id.selected_place_name_textview);
        placeImageView = view.findViewById(R.id.selected_place_image_view);
        selectedFloorLayout = view.findViewById(R.id.floor_layout);
        selectedFloorTextView = view.findViewById(R.id.selected_floor_textview);
        incrementFloorButton = view.findViewById(R.id.increment_button);
        decremetnFloorButton = view.findViewById(R.id.decrement_button);
        roomNameEditText = view.findViewById(R.id.room_name_edittedxt);
        roomTypeSelectionLayout = view.findViewById(R.id.room_type_selection_layout);
        roomTypeNameTextView = view.findViewById(R.id.room_type_textview);
        roomTypeImageView = view.findViewById(R.id.room_type_imageview);
        saveRoomButton = view.findViewById(R.id.save_room_button);

        if(room != null){
            roomNameEditText.setText(room.getName());

            if(selectedPlace == null) {
                selectedFloor = MySettings.getFloor(room.getFloorID());
                selectedPlace = MySettings.getPlace(selectedFloor.getPlaceID());
            }
            if(selectedPlace != null){
                placeNameTextView.setText(selectedPlace.getName());
                if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                    GlideApp.with(getActivity())
                            .load(selectedPlace.getType().getImageUrl())
                            .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                            .into(placeImageView);
                }else {
                    if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1) {
                        placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                    }
                }
                selectedFloorTextView.setText(""+selectedFloor.getName());
            }

            if(selectedRoomType == null) {
                selectedRoomType = room.getType();
            }
            if(selectedRoomType != null){
                roomTypeNameTextView.setText(selectedRoomType.getName());
                if(selectedRoomType.getImageUrl() != null && selectedRoomType.getImageUrl().length() >= 1){
                    GlideApp.with(getActivity())
                            .load(selectedRoomType.getImageUrl())
                            .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                            .into(roomTypeImageView);
                }else {
                    if(selectedRoomType.getImageResourceName() != null && selectedRoomType.getImageResourceName().length() >= 1) {
                        roomTypeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoomType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                    }else{
                        roomTypeImageView.setImageResource(selectedRoomType.getImageResourceID());
                    }
                }
            }
        }

        placeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.  We also want to remove any currently showing
                // dialog, so make our own transaction and take care of that here.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("pickPlaceDialogFragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                PickPlaceDialogFragment fragment = PickPlaceDialogFragment.newInstance();
                fragment.setTargetFragment(EditRoomFragment.this, 0);
                fragment.show(ft, "pickPlaceDialogFragment");
            }
        });

        incrementFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex < selectedPlace.getFloors().size() - 1){
                        selectedFloorIndex++;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                    }
                }
            }
        });

        decremetnFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPlace != null){
                    if(selectedFloorIndex >= 1){
                        selectedFloorIndex--;
                        selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
                        selectedFloorTextView.setText(""+selectedFloor.getName());
                    }
                }
            }
        });

        roomTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_ROOM) != null && MySettings.getTypes(Constants.TYPE_ROOM).size() >= 1){
                    // DialogFragment.show() will take care of adding the fragment
                    // in a transaction.  We also want to remove any currently showing
                    // dialog, so make our own transaction and take care of that here.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getFragmentManager().findFragmentByTag("typePickerDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TypePickerDialogFragment fragment = TypePickerDialogFragment.newInstance();
                    fragment.setTypesCategory(Constants.TYPE_ROOM);
                    fragment.setTargetFragment(EditRoomFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                    Utils.generateRoomTypes();
                }
            }
        });

        roomNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(validateInputs()){
                    Utils.setButtonEnabled(saveRoomButton, true);
                }else{
                    Utils.setButtonEnabled(saveRoomButton, false);
                }
            }
        });

        saveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    boolean roomNameDuplicate = false;
                    List<Room> placeRooms = MySettings.getPlaceRooms(selectedPlace);
                    if(placeRooms != null && placeRooms.size() >= 1){
                        for (Room tempRoom : placeRooms) {
                            if(tempRoom.getName().equals(roomNameEditText.getText().toString()) && ! (tempRoom.getId() == room.getId())){
                                roomNameDuplicate = true;
                            }
                        }
                    }
                    if(roomNameDuplicate){
                        roomNameEditText.setError(getActivity().getResources().getString(R.string.room_already_exists_error));
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(roomNameEditText);
                    }else{
                        room.setName(roomNameEditText.getText().toString());
                        room.setFloorID(selectedFloor.getId());
                        room.setTypeID(selectedRoomType.getId());

                        MySettings.updateRoomName(room, roomNameEditText.getText().toString());
                        MySettings.updateRoomType(room, selectedRoomType.getId());
                        MySettings.updateRoomFloor(room, selectedFloor.getId());

                        MySettings.setCurrentRoom(room);

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(roomNameEditText.getWindowToken(), 0);

                        getFragmentManager().popBackStack();
                    }
                }
            }
        });

        return view;
    }

    public void setRoom(Room room){
        this.room = room;
    }

    @Override
    public void onPlaceSelected(Place place){
        if(place != null){
            this.selectedPlace = MySettings.getPlace(place.getId());
            placeNameTextView.setText(selectedPlace.getName());
            if(selectedPlace.getType().getImageUrl() != null && selectedPlace.getType().getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlace.getType().getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeImageView);
            }else {
                if(selectedPlace.getType().getImageResourceName() != null && selectedPlace.getType().getImageResourceName().length() >= 1) {
                    placeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedPlace.getType().getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    placeImageView.setImageResource(selectedPlace.getType().getImageResourceID());
                }
            }
            selectedFloorIndex = 0;
            selectedFloor = selectedPlace.getFloors().get(selectedFloorIndex);
            selectedFloorTextView.setText(""+selectedFloor.getName());
        }
    }

    @Override
    public void onTypeSelected(Type type){
        if(type != null){
            selectedRoomType = type;
            roomTypeNameTextView.setText(selectedRoomType.getName());
            if(selectedRoomType.getImageUrl() != null && selectedRoomType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedRoomType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(roomTypeImageView);
            }else {
                if(selectedRoomType.getImageResourceName() != null && selectedRoomType.getImageResourceName().length() >= 1) {
                    roomTypeImageView.setImageResource(getActivity().getResources().getIdentifier(selectedRoomType.getImageResourceName(), "drawable", Constants.PACKAGE_NAME));
                }else{
                    roomTypeImageView.setImageResource(selectedRoomType.getImageResourceID());
                }
            }
            if(validateInputs()){
                Utils.setButtonEnabled(saveRoomButton, true);
            }else{
                Utils.setButtonEnabled(saveRoomButton, false);
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;
        if(selectedPlace == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(placeSelectionLayout);
        }

        if(selectedFloor == null){
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(selectedFloorLayout);
        }

        if(!Utils.validateInputs(roomNameEditText)){
            inputsValid = false;
        }

        if(selectedRoomType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(roomTypeSelectionLayout);
        }
        return inputsValid;
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