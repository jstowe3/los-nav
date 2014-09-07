package com.stowe.losnav;


import com.stowe.losnav.FingerprintDialog.FingerprintDialogListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ConfirmationDialog extends DialogFragment {
	
	public interface ConfirmationDialogListener {
		void onFinishedConfirmationDialog(boolean proceed, String label);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Done Scanning");
		alert.setMessage("Enter a label for this Fingerprint:");

		// Set an EditText view to get user input 
		final EditText input = new EditText(getActivity());
		alert.setView(input);

		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();
			ConfirmationDialogListener activity = (ConfirmationDialogListener) getActivity();
   	   		dismiss();
   	   		if(value == null) {
   	   			Log.e("ConfirmationDialog", "Error retrieving value for label!");
   	   		}
   	   		activity.onFinishedConfirmationDialog(true, value);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		return alert.create();
	}

}
