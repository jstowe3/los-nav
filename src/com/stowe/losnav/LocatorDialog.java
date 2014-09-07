package com.stowe.losnav;

import com.stowe.losnav.ConfirmationDialog.ConfirmationDialogListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LocatorDialog extends DialogFragment {
	
	String message;
	int x, y;
	
	public interface LocatorDialogListener {
		void onFinishedLocatorDialog(boolean proceed, int x, int y, String label);
	}
	
	/* Pass in the dialog message and coordinates to display on the fragment */
	public static LocatorDialog newInstance(String message, int x, int y, String label) {
		LocatorDialog new_dialog = new LocatorDialog();
		Bundle args = new Bundle();
		args.putString("message", message);
		args.putInt("x", x);
		args.putInt("y", y);
		args.putString("label", label);
		new_dialog.setArguments(args);
		return new_dialog;
	}
	
	public String getMessage() {
		return getArguments().getString("message");
	}
	public int getX() {
		return getArguments().getInt("x");
	}
	public int getY() {
		return getArguments().getInt("y");
	}
	public String getLabel() {
		return getArguments().getString("label");
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Location Matched");
		alert.setMessage(getMessage());

		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			LocatorDialogListener activity = (LocatorDialogListener) getActivity();
   	   		dismiss();
   	   		activity.onFinishedLocatorDialog(true, getX(), getY(), getLabel());
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
