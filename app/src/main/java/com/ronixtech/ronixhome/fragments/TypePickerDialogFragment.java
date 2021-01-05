package com.ronixtech.ronixhome.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronixtech.ronixhome.Constants;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.adapters.TypeAdapter;
import com.ronixtech.ronixhome.entities.Type;

import java.util.List;

public class TypePickerDialogFragment extends DialogFragment {
    private static final String TAG = PickPlaceDialogFragment.class.getSimpleName();
    private TypePickerDialogFragment.OnTypeSelectedListener callback;

    List<Type> types;
    TypeAdapter adapter;

    private int typesCategory = Constants.TYPE_PLACE; //default value

    public interface OnTypeSelectedListener {
        public void onTypeSelected(Type type);
    }
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static TypePickerDialogFragment newInstance() {
        TypePickerDialogFragment f = new TypePickerDialogFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback = (TypePickerDialogFragment.OnTypeSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnTypeSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_type_selection, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ListView listView = new ListView(getActivity());
        types = MySettings.getTypes(typesCategory);


        adapter = new TypeAdapter(getActivity(), types);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Type selectedType = (Type) adapter.getItem(position);
                callback.onTypeSelected(selectedType);
                dismiss();
            }
        });

        return listView;
    }

    public void setTypesCategory(int typesCategory){
        this.typesCategory = typesCategory;
    }
}
