package com.ronixtech.ronixhome.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ronixtech.ronixhome.MySettings;
import com.ronixtech.ronixhome.R;
import com.ronixtech.ronixhome.Utils;
import com.ronixtech.ronixhome.entities.Floor;

import java.util.List;

public class FloorAdapterEditable extends ArrayAdapter {
    Activity activity ;
    List<Floor> floors;
    ViewHolder vHolder = null;

    private FloorsListener floorsListener;

    public interface FloorsListener{
        public void onFloorDeleted();
    }

    public FloorAdapterEditable(Activity activity, List floors, FloorsListener floorsListener){
        super(activity, R.layout.list_item_floor_editable, floors);
        this.activity = activity;
        this.floors = floors;
        this.floorsListener = floorsListener;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return floors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item_floor_editable, null);
            vHolder = new ViewHolder();
            vHolder.floorNameTextView = rowView.findViewById(R.id.floor_name_textview);
            vHolder.floorRemoveImageView = rowView.findViewById(R.id.floor_remove_imageview);
            vHolder.floorEditImageView = rowView.findViewById(R.id.floor_edit_imageview);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        Floor item = floors.get(position);

        vHolder.floorNameTextView.setText(""+item.getLevel() + " - " + item.getName());
        vHolder.floorRemoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setTitle(Utils.getString(activity, R.string.remove_floor_question))
                        .setMessage(Utils.getString(activity, R.string.remove_floor_description))
                        //set positive button
                        .setPositiveButton(Utils.getString(activity, R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what would happen when positive button is clicked
                                if(floors.size() > 1){
                                    MySettings.removeFloor(item);
                                    floors.remove(item);
                                    floorsListener.onFloorDeleted();
                                }else{
                                    Utils.showToast(activity, Utils.getString(activity, R.string.place_floors_error), true);
                                }
                            }
                        })
                        //set negative button
                        .setNegativeButton(Utils.getString(activity, R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //set what should happen when negative button is clicked
                            }
                        })
                        .show();
            }
        });
        vHolder.floorEditImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(activity).create();
                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r = activity.getResources();
                float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                TextView floorNameTextView = new TextView(activity);
                floorNameTextView.setText(Utils.getString(activity, R.string.floor_name));
                floorNameTextView.setTextSize(20);
                floorNameTextView.setGravity(Gravity.CENTER);
                floorNameTextView.setLayoutParams(layoutParams);

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                Resources r2 = activity.getResources();
                float pxLeftMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxRightMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxTopMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r2.getDisplayMetrics());
                float pxBottomMargin2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r2.getDisplayMetrics());
                layoutParams2.setMargins(Math.round(pxLeftMargin2), Math.round(pxTopMargin2), Math.round(pxRightMargin2), Math.round(pxBottomMargin2));
                layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

                final EditText floorNameEditText = new EditText(activity);
                floorNameEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                floorNameEditText.setHint(Utils.getString(activity, R.string.floor_name));
                floorNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                floorNameEditText.setText(item.getName());
                floorNameEditText.setLayoutParams(layoutParams2);

                Button saveButton = new Button(activity);
                saveButton.setText(Utils.getString(activity, R.string.done));
                saveButton.setTextColor(activity.getResources().getColor(R.color.whiteColor));
                saveButton.setBackgroundColor(activity.getResources().getColor(R.color.blueColor));
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(floorNameEditText.getText().toString() != null && floorNameEditText.getText().toString().length() >= 1) {
                            item.setName(floorNameEditText.getText().toString());
                            notifyDataSetChanged();
                            dialog.dismiss();
                        }else{
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(floorNameEditText);
                        }
                    }
                });

                layout.addView(floorNameTextView);
                layout.addView(floorNameEditText);
                layout.addView(saveButton);

                dialog.setView(layout);

                dialog.show();
            }
        });

        return rowView;
    }

    public static class ViewHolder{
        TextView floorNameTextView;
        ImageView floorRemoveImageView, floorEditImageView;
    }
}
