package com.ronixtech.ronixhome.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.ronixtech.ronixhome.entities.Type;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPlaceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPlaceFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddPlaceFragment extends Fragment implements TypePickerDialogFragment.OnTypeSelectedListener {
    private static final String TAG = AddPlaceFragment.class.getSimpleName();


    private OnFragmentInteractionListener mListener;


    TextView addPlaceTitleTextView, addPlaceDescriptionTextView, numberOfFloorsTextView;
    EditText placeNameEditText;
    RelativeLayout placeTypeSelectionLayout;
    ImageView placeTypeImageView;
    TextView placeTypeNameTextView;
    Button incrementFloorsButton, decrementFloorsButton;
    Button addPlaceButton;

    Type selectedPlaceType;
    int numberOfFloors = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPlaceFragment.
     */
    public static AddPlaceFragment newInstance(String param1, String param2) {
        AddPlaceFragment fragment = new AddPlaceFragment();
        return fragment;
    }
    public AddPlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_place, container, false);
        MainActivity.setActionBarTitle(getActivity().getResources().getString(R.string.add_new_place), getResources().getColor(R.color.whiteColor));
        setHasOptionsMenu(true);

        addPlaceTitleTextView = view.findViewById(R.id.add_place_title_textview);
        addPlaceDescriptionTextView = view.findViewById(R.id.add_place_description_textview);
        numberOfFloorsTextView = view.findViewById(R.id.selected_number_of_floors_textview);
        placeNameEditText= view.findViewById(R.id.place_name_edittedxt);
        placeTypeSelectionLayout = view.findViewById(R.id.place_type_selection_layout);
        placeTypeImageView = view.findViewById(R.id.type_imageview);
        placeTypeNameTextView = view.findViewById(R.id.type_name_textview);
        incrementFloorsButton = view.findViewById(R.id.increment_button);
        decrementFloorsButton = view.findViewById(R.id.decrement_button);
        addPlaceButton= view.findViewById(R.id.add_place_button);

        placeNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(validateInputs()){
                    Utils.setButtonEnabled(addPlaceButton, true);
                }else{
                    Utils.setButtonEnabled(addPlaceButton, false);
                }
            }
        });

        placeTypeSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MySettings.getTypes(Constants.TYPE_PLACE) != null && MySettings.getTypes(Constants.TYPE_LINE).size() >= 1){
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
                    fragment.setTypesCategory(Constants.TYPE_PLACE);
                    fragment.setTargetFragment(AddPlaceFragment.this, 0);
                    fragment.show(ft, "typePickerDialogFragment");
                }else{
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_types_available), Toast.LENGTH_SHORT).show();
                }

            }
        });

        numberOfFloorsTextView.setText(""+numberOfFloors);

        incrementFloorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberOfFloors++;
                numberOfFloorsTextView.setText(""+numberOfFloors);
            }
        });

        decrementFloorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(numberOfFloors > 1){
                    numberOfFloors--;
                    numberOfFloorsTextView.setText(""+numberOfFloors);
                }
            }
        });

        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    Place oldPlace = MySettings.getPlaceByName(placeNameEditText.getText().toString());
                    if(oldPlace != null){
                        placeNameEditText.setError(getActivity().getResources().getString(R.string.place_already_exists_error));
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .repeat(1)
                                .playOn(placeNameEditText);
                    }else{
                        Place place = new Place();
                        place.setName(placeNameEditText.getText().toString());
                        place.setTypeID(selectedPlaceType.getId());
                        MySettings.addPlace(place);
                        Place dbPlace = MySettings.getPlaceByName(place.getName());
                        for(int x = 1; x <= numberOfFloors; x++){
                            Floor floor = new Floor();
                            floor.setName("Floor #" + x);
                            floor.setLevel(x);
                            floor.setPlaceID(dbPlace.getId());
                            MySettings.addFloor(floor);
                        }
                        MySettings.setCurrentPlace(dbPlace);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(placeNameEditText.getWindowToken(), 0);
                        getFragmentManager().popBackStack();
                    }
                }
            }
        });

        selectedPlaceType = MySettings.getTypeByName("House");
        if(selectedPlaceType != null){
            placeTypeNameTextView.setText(selectedPlaceType.getName());
            if(selectedPlaceType.getImageUrl() != null && selectedPlaceType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlaceType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeTypeImageView);
            }else {
                placeTypeImageView.setImageResource(selectedPlaceType.getImageResourceID());
            }
        }

        return view;
    }

    @Override
    public void onTypeSelected(Type type){
        if(type != null){
            selectedPlaceType = type;
            placeTypeNameTextView.setText(selectedPlaceType.getName());
            if(selectedPlaceType.getImageUrl() != null && selectedPlaceType.getImageUrl().length() >= 1){
                GlideApp.with(getActivity())
                        .load(selectedPlaceType.getImageUrl())
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.place_type_house))
                        .into(placeTypeImageView);
            }else {
                placeTypeImageView.setImageResource(selectedPlaceType.getImageResourceID());
            }
            if(validateInputs()){
                Utils.setButtonEnabled(addPlaceButton, true);
            }else{
                Utils.setButtonEnabled(addPlaceButton, false);
            }
        }
    }

    private boolean validateInputs(){
        boolean inputsValid = true;

        if(!Utils.validateInputs(placeNameEditText)){
            inputsValid = false;
        }

        if(selectedPlaceType == null){
            inputsValid = false;
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .repeat(1)
                    .playOn(placeTypeSelectionLayout);
        }

        return inputsValid;
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
    }
*/
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
