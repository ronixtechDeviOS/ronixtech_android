package com.ronixtech.ronixhome;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class CustomProgressDialog extends Dialog {
    public static CustomProgressDialog show(Context context, CharSequence title,
                                            CharSequence message) {
        return show(context, title, message, false);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
                                            CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
                                            CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
                                            CharSequence message, boolean indeterminate,
                                            boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        CustomProgressDialog dialog = new CustomProgressDialog(context);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.setCanceledOnTouchOutside(false);
        /* The next line will add the ProgressBar to the dialog. */
        ProgressBar progressBar = new ProgressBar(context);
        //progressBar.getIndeterminateDrawable().setColorFilter(context.getColor(R.color.blue_color), android.graphics.PorterDuff.Mode.MULTIPLY);
        //progressBar.setIndeterminate(true);
        //here is the trick:
        //progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.logo_animation, null));
        dialog.addContentView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();

        return dialog;
    }

    public CustomProgressDialog(Context context) {
        super(context, R.style.CustomDialog);
    }
}
