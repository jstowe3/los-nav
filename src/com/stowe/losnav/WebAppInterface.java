package com.stowe.losnav;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
	MainActivity mainactivity;

    /** Instantiate the interface and set the context */
    WebAppInterface(MainActivity mainactivity) {
        this.mainactivity = mainactivity;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mainactivity, toast, Toast.LENGTH_SHORT).show();
    }
    
    /** Scan WiFi and display result of scan */
    @JavascriptInterface
    public void scanWifi() {
    	mainactivity.scanWifi();
    }
    
    /** Display the dialog for fingerprint creation */
    @JavascriptInterface
    public void displayDialog() {
    	mainactivity.displayCreateDialog();
    }
    
    /** Update the current coordinates the user clicked on */
    @JavascriptInterface
    public void updateCoordinates(int x, int y) {
    	mainactivity.setCoordinates(x, y);
    }
    
    /** Search for the user's current location. */
    @JavascriptInterface
    public void findLocation() {
    	mainactivity.findLocation();
    }
    
    /** Search for the user's current location. */
    @JavascriptInterface
    public void findNeighbors() {
    	mainactivity.findNeighbors();
    }
    
    /** Search for the user's current location. */
    @JavascriptInterface
    public void navigate() {
    	mainactivity.navigate();
    }
}
