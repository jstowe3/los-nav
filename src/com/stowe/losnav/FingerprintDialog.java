package com.stowe.losnav;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FingerprintDialog extends DialogFragment {
	
	String message, positive_label, negative_label;
	
	public interface FingerprintDialogListener {
		void onFinishedFingerprintDialog(boolean proceed, boolean abort);
	}
	
	
	/* Pass in the dialog message and labels to display on the fragment */
	public static FingerprintDialog newInstance(String message, String positive_label, String negative_label) {
		FingerprintDialog new_dialog = new FingerprintDialog();
		Bundle args = new Bundle();
		args.putString("message", message);
		args.putString("positive_label", positive_label);
		args.putString("negative_label", negative_label);
		new_dialog.setArguments(args);
		return new_dialog;
	}
	
	public String getMessage() {
		return getArguments().getString("message");
	}
	public String getPositive_label() {
		return getArguments().getString("positive_label");
	}
	public String getNegative_label() {
		return getArguments().getString("negative_label");
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getMessage())
               .setPositiveButton(getPositive_label(), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Start process to create the fingerprint
                	   FingerprintDialogListener activity = (FingerprintDialogListener) getActivity();
                	   dismiss();
                	   activity.onFinishedFingerprintDialog(true, false);
                   }
               })
               .setNegativeButton(getNegative_label(), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   FingerprintDialogListener activity = (FingerprintDialogListener) getActivity();
                	   if(getPositive_label() == "Continue"){
                		   activity.onFinishedFingerprintDialog(false, true);
                	   }
                	   else{
                		   activity.onFinishedFingerprintDialog(false, false);
                	   }
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
