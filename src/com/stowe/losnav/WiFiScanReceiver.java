package com.stowe.losnav;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.Toast;

public class WiFiScanReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WiFiScanReceiver";
	MainActivity mainactivity;
	
	public WiFiScanReceiver(MainActivity mainactivity) {
		super();
		this.mainactivity = mainactivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(mainactivity.getScanRequested()) {
			
			mainactivity.setScanRequested(false);
			
			List<ScanResult> results = mainactivity.wifi.getScanResults();
			if(results.size() > 15) {
				Log.d("WiFiScanReceiver", "ABBREVIATING SCAN RESULTS TO SIZE 15!");
				List<ScanResult> abbreviated_results = new ArrayList<ScanResult>();
				for(int i=0; i<15; i++) {
					abbreviated_results.add(results.get(i));
				}
				results = abbreviated_results;
			}
			// Parse the results of the scan and save the parts we need
			mainactivity.parseScan(results);
			
			// Create a string for the scan
			String message = "AP DATA("+results.size()+"): \n";
			/*
			for(ScanResult result : results) {
				message = message.concat("BSSID: "+result.BSSID + "\n SSID: "+result.SSID + "\n LEVEL: "+ result.level + "\n");
			}
			*/
			//Toast.makeText(mainactivity, message, Toast.LENGTH_LONG).show();
			
			Log.d(TAG, "onReceive() message: "+message);
			// Hand back control to MainActivity for more scanning
			mainactivity.scanIteration();
		}
	}

}
