package com.stowe.losnav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stowe.losnav.ConfirmationDialog.ConfirmationDialogListener;
import com.stowe.losnav.FingerprintDialog.FingerprintDialogListener;
import com.stowe.losnav.LocatorDialog.LocatorDialogListener;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements FingerprintDialogListener, ConfirmationDialogListener, LocatorDialogListener {
	
	private static final String TAG = "MainActivity";
	private FingerprintProvider db4oHelper = null;
	WifiManager wifi;
	BroadcastReceiver receiver;
	WebAppInterface webappinterface;
	WebView myWebView;
	boolean scanRequested = false;
	ArrayList<apdataContainer> data_list = new ArrayList<apdataContainer>();
	int scan_count = 0;
	int direction_set_count = 0;
	ArrayList<ArrayList<apdataContainer>> fingerprint_list = new ArrayList<ArrayList<apdataContainer>>();
	Point coordinates = new Point();
	String label = "";
	List<Fingerprint> fingerprint_db_list = new ArrayList<Fingerprint>();
	boolean scan_iteration_requested = false;
	ArrayList<apdataContainer> locator_ap_data_list = new ArrayList<apdataContainer>();
	Fingerprint currentFingerprint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myWebView = (WebView) findViewById(R.id.webview);
		
		myWebView.setWebChromeClient(new WebChromeClient() {
			  public boolean onConsoleMessage(ConsoleMessage cm) {
			    Log.d("LosNav", cm.message() + " -- From line "
			                         + cm.lineNumber() + " of "
			                         + cm.sourceId() );
			    return true;
			  }
		});
		
		webappinterface = new WebAppInterface(this);
		myWebView.addJavascriptInterface(webappinterface, "Android");
		
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
		
		myWebView.loadUrl("file://mnt/sdcard/floorplans/webview.html");
		
		//Setup WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		
		//Register Broadcast Receiver
		if(receiver == null)
			receiver = new WiFiScanReceiver(this);
		
		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		dbHelper();
		
		Log.d(TAG, "onCreate ()");
		
		//modifyNeighborList();
		
		//printDBToLog(); //Print the contents of the DB
		
		//db4oHelper.deleteAllFingerprints();
		
		//printDBToLog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch(item.getItemId()) {
		case R.id.menu_settings:
			return true;
		case R.id.quit:
			//db4oHelper.close();
			finish();
			return true;
		case R.id.clear:
			clearMap();
			return true;
		case R.id.view_all:
			displayAllFingerprints();
			return true;
		case R.id.delete_request:
			requestDeletion();
		//case R.id.delete_all:
			//db4oHelper.deleteAllFingerprints();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
     * Close database before exiting the application
     */
     @Override
     protected void onPause() {
    	 		// This line is causing problems. Commenting out.
                   //super.onDestroy();
    	 		   super.onPause();
    	 		   
    	 		   try {
    	 		   unregisterReceiver(receiver);//Unregister the WiFi receiver.
    	 		   }
    	 		   catch(Exception e) {
    	 			   Log.e(TAG, e.getMessage());
    	 		   }
    	 		   
                   dbHelper().close();
                   db4oHelper = null;
     }
     
     @Override
     protected void onResume() {
    	 super.onResume();
    	 
    	//Register Broadcast Receiver
 		if(receiver == null)
 			receiver = new WiFiScanReceiver(this);
 		
 		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		
 		//Check database
 		dbHelper();
     }
	
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
     
	public void scanWifi() {
		Log.d(TAG, "scanWifi()");
		scanRequested = true;
		wifi.startScan();
	}
	
	public boolean getScanIterationRequested() {
		return scan_iteration_requested;
	}
	
	public boolean getScanRequested() {
		return scanRequested;
	}
	
	public void setScanRequested(boolean newVal) {
		scanRequested = newVal;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void displayCreateDialog() {
		FingerprintDialog dialog = FingerprintDialog.newInstance(this.getString(R.string.fingerprint_dialog), this.getString(R.string.create), this.getString(R.string.cancel));
		android.app.FragmentManager fm = getFragmentManager();
		dialog.show(fm, "Display Fragment");
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void displayContinueDialog() {
		FingerprintDialog dialog = FingerprintDialog.newInstance("Ready for next scan?", "Continue", "Cancel");
		android.app.FragmentManager fm = getFragmentManager();
		dialog.show(fm, "Display Fragment");
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void displayConfirmationDialog() {
		ConfirmationDialog dialog = new ConfirmationDialog();
		android.app.FragmentManager fm = getFragmentManager();
		dialog.show(fm, "Display Fragment");
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void displayLocatorDialog(String message, int x, int y, String label) {
		LocatorDialog dialog = LocatorDialog.newInstance(message, x, y, label);
		android.app.FragmentManager fm = getFragmentManager();
		dialog.show(fm, "Display Fragment");
	}

	public void onFinishedFingerprintDialog(boolean proceed, boolean abort) {
		// Check whether we need to create a new fingerprint or not.
		if(proceed){
			Log.d(TAG, "onFinishedFingerprintDialog()");
			scan_iteration_requested = true;
			scanIteration();
		}
		else if(abort) {
			//Destroy the current fingerprint data stored locally
			//This means the user wants to abort creating the current fingerprint
			scan_count = 0;
			direction_set_count = 0;
			data_list.clear();
			fingerprint_list.clear();
			scan_iteration_requested = false;
			//Clear the map
			myWebView.loadUrl("javascript:clearMap()");
			//Redraw all the fingerprints
			displayAllFingerprints();
		}
	}
	
	public void onFinishedConfirmationDialog(boolean proceed, String label) {
		if(proceed) {
			this.label = label;
			Fingerprint new_fingerprint = new Fingerprint(label);
			ArrayList<ArrayList<apdataContainer>> new_ap_list = new ArrayList<ArrayList<apdataContainer>>();
			for(int i = 0; i < fingerprint_list.size(); i ++) {
				new_ap_list.add(i, fingerprint_list.get(i));
			}
			fingerprint_list.clear();
			Log.d(TAG, "FINGERPRINT_LIST CLEARED.");
			new_fingerprint.setAP_list(new_ap_list);
			new_fingerprint.setCoordinates(coordinates.x, coordinates.y);
			//TODO: prompt the user to select all neighboring fingerprints
			displayAllFingerprints(); //Display the fingerprints on the map
			
			//Create a quick alert dialog:
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage("Tap on a Fingerprint to select it.")
			       .setTitle("Select all neighboring Fingerprints:");
			
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		        	   myWebView.loadUrl("javascript:findNeighbors(true)");
		           }
		       });

			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			
			dialog.show();
			
			// TODO: Possible problem ocurring here!
			
			currentFingerprint = new Fingerprint(label);
			currentFingerprint.setAP_list(new_ap_list);
			currentFingerprint.setCoordinates(coordinates.x, coordinates.y);
			Log.d(TAG, "SIZE OF CURRENTFINGERPRINT'S AP_LIST: "+currentFingerprint.getAPData().size());
			Log.d(TAG, "THIS NUMBER SHOULD BE 4!");
			// Store the fingerprint in the database
			// Make sure we have an instance open before saving
			/*
			db4oHelper.store(new_fingerprint);
			Log.d(TAG, "Storing new Fingerprint to DB.");
			fingerprint_db_list = db4oHelper.findAll();
			Log.d(TAG, "Current # of Fingerprints in DB: "+fingerprint_db_list.size());
			fingerprint_list.clear();
			Log.d(TAG, "fingerprint_list cleared");
			*/
			
			
		}
		else { //Don't store the Fingerprint and clear the list
			fingerprint_list.clear();
			Log.d(TAG, "fingerprint_list cleared");
		}
	}
	
	//Returns an ArrayList of apdataContainer objects after averaging the signal level data for 5 WiFi scans.
	public void scanIteration() {
		
		if((scan_count ++) < 5) {
			Log.d(TAG, "scan_count: "+scan_count);
			scanWifi();
		}
		
		else{
			scan_count = 0;
			
			// Examine the data_list and average the signal levels.
			for(apdataContainer container : data_list) {
				container.setSignal_strength(container.getSignal_strength()/container.getDivisor());
			}
			
			ArrayList<apdataContainer> return_list = (ArrayList<apdataContainer>) data_list.clone();
			Log.d(TAG, "New return_list generated: ");
			for(apdataContainer ctr : return_list) {
				Log.d(TAG, "BSSID: "+ctr.getBssid()+" Strength: "+ctr.getSignal_strength());
			}
			data_list.clear();
			
			if(scan_iteration_requested) {
			
				if(direction_set_count < 3) {
					direction_set_count ++;
					fingerprint_list.add(return_list);
					Log.d(TAG, "return_list added to fingerprint_list");
					Log.d(TAG, "CURRENT SIZE OF FINGERPRINT_LIST: "+fingerprint_list.size());
					//Prompt the user to change direction and take another scan
					displayContinueDialog();
				}
				else {
					direction_set_count = 0;
					scan_iteration_requested = false;
					fingerprint_list.add(return_list);
					Log.d(TAG, "return_list added to fingerprint_list");
					Log.d(TAG, "CURRENT SIZE OF FINGERPRINT_LIST: "+fingerprint_list.size());
					// Prompt the user for a label for the fingerprint
					displayConfirmationDialog();
				}
			
			}
			else {  // Feed this data to our locator method
				Log.d(TAG, "locator_ap_data_list set to return_list.");
				locator_ap_data_list = return_list;
			}
			
		}
		
	}
	
	public void parseScan(List<ScanResult> results) {
		Log.d(TAG, "Entering parseScan...");
		boolean item_found = false;
		for(ScanResult result : results) {
			for(apdataContainer data : data_list) {
				if(data.bssid.equals(result.BSSID)) {
					//Log.d(TAG, "BSSIDs MATCHED!: "+data.bssid);
					item_found = true;
					data.addSignal_strength(result.level);
				}
				else {
					//Log.d(TAG ,"No Match?: data.bssid: "+data.bssid+" != result.bssid: "+result.BSSID);
				}
			}
			if(!item_found) {
				// Add a new apdataContainer to data_list.
				//Log.d(TAG, "Adding apdataContainer # "+data_list.size()+", BSSID: "+result.BSSID);
				data_list.add(new apdataContainer(result.BSSID, result.level, 1));
			}
			item_found = false;
		}
	}
	
	public void setCoordinates(int x, int y) {
		coordinates.set(x, y);
	}
	
	/**
     * Create Db4oHelper instance
     */
     private Db4oHelper dbHelper() {
                   if (db4oHelper == null) {
                                 db4oHelper = new FingerprintProvider(this);
                                 db4oHelper.db();
                   }
                   return db4oHelper;
     }  
	
     public Fingerprint findLocation() {
    	 fingerprint_db_list = db4oHelper.findAll();
    	 Log.d(TAG, "Fingerprints retrieved from DB: "+fingerprint_db_list.size()+" fingerprints found.");
    	 double minDistance = -1;
    	 Fingerprint minFingerprint = null;
    	 this.scanWifi(); //Start scanning (5 times total)
    	 //Log.d(TAG, "size of locator_ap_data_list: "+locator_ap_data_list.size());
    	 while(locator_ap_data_list.size() < 1);
    	 Log.d(TAG, "Checking for closest matching Fingerprint...");
    	 for(Fingerprint fingerprint : fingerprint_db_list) {
    		 double next_dist = fingerprint.distanceTo(locator_ap_data_list);
    		 Log.d(TAG, "Distance to "+fingerprint._label+": "+next_dist);
    		 if(next_dist < minDistance || minDistance < 0) {
    			 Log.d(TAG, "New Min Dist found: "+next_dist+", Was: "+minDistance);
    			 minDistance = next_dist;
    			 minFingerprint = fingerprint;
    		 }
    	 }
    	 fingerprint_db_list = null;
    	 locator_ap_data_list.clear();
    	 Log.d(TAG, "locator_ap_data_list cleared.");
    	 String location_message = "Location Matched: X: "+minFingerprint.coordinates.x+" Y: "+minFingerprint.coordinates.y+" Label: "+minFingerprint._label;
    	 Log.d(TAG, location_message);
    	 //myWebView.loadUrl("javascript:drawLocation("+minFingerprint.coordinates.x+","+minFingerprint.coordinates.y+","+minFingerprint._label+")");
    	 displayLocatorDialog(location_message, minFingerprint.coordinates.x, minFingerprint.coordinates.y, minFingerprint._label);
    	 return minFingerprint;
     }
     
     //TODO: Finish this method.
	public void onFinishedLocatorDialog(boolean proceed, int x, int y, String label) {
		
		if(proceed) {
			String color = "#FF00FF";//Fuchsia
			myWebView.loadUrl("javascript:drawLocationWithText("+x+", "+y+", '"+color+"', '"+label+"')");
		}
		
	}
	
	public void printDBToLog() {
		List<Fingerprint> tempFingerprintList = db4oHelper.findAll();
		for(Fingerprint fingerprint : tempFingerprintList) {
			Log.d("DB4OHELP", "Fingerprint: "+fingerprint._label);
			for(ArrayList<apdataContainer> container_list : fingerprint.getAPData()) {
				Log.d("DB4OHELP", "NEXT LIST");
				for(apdataContainer container : container_list) {
					Log.d("DB4OHELP", "BSSID: "+container.bssid+" Signal: "+container.getSignal_strength());
				}
			}
		}
		tempFingerprintList = null;
		Log.d("TAG", "printDBToLog()");
	}
	
	public void clearMap() {
		myWebView.loadUrl("javascript:clearMap()");
	}
	
	public void displayAllFingerprints() {
		Log.d(TAG, "displayAllFingerprints()");
		List<Fingerprint> display_list = db4oHelper.findAll();
		for(Fingerprint fingerprint : display_list) {
			int x = fingerprint.coordinates.x;
			int y = fingerprint.coordinates.y;
			String text = fingerprint._label;
			String color = "#0000FF";//Blue
			myWebView.loadUrl("javascript:drawLocationWithText("+x+", "+y+", '"+color+"', '"+text+"')");
		}
		display_list = null;
	}
	
	/*Called by the JavaScript when the user clicks on the map trying to select a neighbor Fingerprint.*/
	public void findNeighbors() {
		Log.d(TAG, "Entering findNeighbors()");
		//Create a quick alert dialog:
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // Store the finished Fingerprint object
	        	   db4oHelper.store(currentFingerprint);
	        	   Log.d(TAG, "currentFingerprint stored in DB.");
	        	   currentFingerprint = null;
	        	   myWebView.loadUrl("javascript:findNeighbors(false)"); // This needs to be called from the UI thread
	        	   myWebView.loadUrl("javascript:clearMap()");
	        	   //displayAllFingerprints();
	        	   Log.d(TAG, "Current size of DB: "+db4oHelper.findAll().size());
	           }
	       });
		builder.setNegativeButton("Not Finished", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.d(TAG, "Not done finding neighbors yet.");
			}
		});
		
		//Load all Fingerprints from the DB
		List<Fingerprint> fingerprints = db4oHelper.findAll();
		//Run boundsMatched for each Fingerprint object
		boolean fingerprintFound = false;
		for(Fingerprint fingerprint : fingerprints) {
			if(fingerprint.boundsMatched(coordinates.x, coordinates.y)) {
				fingerprintFound = true;
				// Add Fingerprint to list of neighbors for currentFingerprint
				currentFingerprint.getNeighbors().add(fingerprint);
				if(fingerprint.neighbors == null) {
					Log.d(TAG, "For some reason the neighbors list was NULL!");
					fingerprint.neighbors = new ArrayList<Fingerprint>();
				}
				fingerprint.getNeighbors().add(currentFingerprint);
				//db4oHelper.store(fingerprint); //Store the fingerprint since we updated its neighbors
				int x = fingerprint.coordinates.x;
				int y = fingerprint.coordinates.y;
				String color = "#F88017"; //Orange
				myWebView.loadUrl("javascript:drawLocationWithColor("+x+", "+y+", '"+color+"')"); //Draw an orange circle on the location
				
				//Show dialog here
				// 2. Chain together various setter methods to set the dialog characteristics
				builder.setMessage("Fingerprint selected: "+fingerprint._label)
				       .setTitle("Done selecting all neighbors?");
				
				// 3. Get the AlertDialog from create()
				AlertDialog dialog = builder.create();
				
				dialog.show(); //Show the dialog
				break;
			}
		}
		fingerprints = null; //Clear out the list
		db4oHelper.dbCommit();
		db4oHelper.dbPurge();
		Log.d(TAG, "DB List Cleared!");
		if(!fingerprintFound) {
			//Show dialog here
			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage("No Fingerprint Found!")
			       .setTitle("Done selecting all neighbors?");
				
			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
				
			dialog.show(); //Show the dialog
		}
	}
	
	/*Generate an ArrayList of Vertex objects for every Fingerprint in the database*/
	public ArrayList<Vertex> generateVerticies() {
		ArrayList<Vertex> verticies = new ArrayList<Vertex>();
		
		List<Fingerprint> finger_list = db4oHelper.findAll();
		Log.d(TAG, "Inside generateVerticies(): Number of fingerprints in DB: "+finger_list.size());
		
		for(Fingerprint fingerprint : finger_list) {
			Log.d(TAG, "Adding fingerprint: ["+fingerprint._label+"] to the vertex list.");
			Vertex vertex = new Vertex(fingerprint);
			verticies.add(vertex);
		}
		
		//finger_list = null; //Clear the DB list
		
		for(Vertex vertex : verticies) {
			ArrayList<Fingerprint> n_list = vertex.fingerprint.getNeighbors();
			boolean temp_bool = false;
			ArrayList<Fingerprint> temp_fingers = new ArrayList<Fingerprint>();
			for(Fingerprint f : n_list){
				if(!(finger_list.contains(f))){
					temp_bool = true;
					temp_fingers.add(f);
					Log.d(TAG, "FOUND EXTRANEOUS NEIGHBOR!");
				}
			}
			if(temp_bool){
				Log.d(TAG, "REMOVING EXTRANEOUS NEIGHBOR!");
				vertex.fingerprint.getNeighbors().removeAll(temp_fingers);
			}
			int edge_count = vertex.fingerprint.getNeighbors().size();
			Log.d(TAG, "edge_count for ["+vertex.fingerprint._label+"]: "+edge_count);
			Edge[] edges = new Edge[edge_count];
			ArrayList<Fingerprint> neighbors = vertex.fingerprint.getNeighbors();
			for(int i=0; i<edge_count; i++) {
				Vertex v = findVertexByFingerprint(neighbors.get(i), verticies);
				Log.d(TAG, "Vertex ["+i+"]: "+v.fingerprint._label);
				//problem on this line vv
				edges[i] = new Edge(v, neighbors.get(i).dirDistTo(vertex.fingerprint));
			}
			vertex.adjacencies = edges;
		}
		
		return verticies;
	}
	
	public Vertex findVertexByFingerprint(Fingerprint fingerprint, ArrayList<Vertex> verticies) {
		Vertex return_vertex = null;
		for(Vertex vertex : verticies) {
			if(vertex.fingerprint == fingerprint) {
				return vertex;
			}
		}
		return return_vertex;
	}
	
	/*Navigate to the current coordinates*/
	public void navigate() {
		int source_x = coordinates.x;
		int source_y = coordinates.y;
		Fingerprint dest_loc = findClosestFingerprint(source_x, source_y);
		Fingerprint current_loc = findLocation();
		//Generate the verticies for the graph of Fingerprints
		ArrayList<Vertex> verticies = generateVerticies();
		Vertex start_vertex = null, dest_vertex = null;
		for(Vertex vertex : verticies) {
			if(vertex.fingerprint == current_loc) {
				start_vertex = vertex;
			}
			else if(vertex.fingerprint == dest_loc) {
				dest_vertex = vertex;
			}
			if(dest_vertex != null && start_vertex != null)
				break;
		}
		Log.d(TAG, "navigate(): attempting to find route from "+current_loc._label);
		Log.d(TAG, "Running Dijkstra's!");
		Dijkstra.computePaths(start_vertex);
		Log.d(TAG, "Done running Dijkstra's!");
		List<Vertex> path = Vertex.getShortestPathTo(dest_vertex);
		Log.d(TAG, "Shortest Path Generated: ");
		for(Vertex vert : path) {
			Log.d(TAG, "PATH: "+vert.fingerprint._label);
		}
		String state = "BEGIN";
		String color = "#00FF00";
		for(int i=0; i<path.size(); i++) {
			if((i > 0) && (i < path.size()-1)){
				state = "CONTINUE";
				color = "#0000FF";
			}
			else if(i == path.size() -1){
				state = "END";
				color = "#FF0000";
			}
			Vertex vert = path.get(i);
			int x = vert.fingerprint.coordinates.x;
			int y = vert.fingerprint.coordinates.y;
			Log.d(TAG, "NEXT FINGERPRINT: "+vert.fingerprint._label);
			myWebView.loadUrl("javascript:drawPath("+x+" , "+y+", '"+color+"', \""+state+"\")");
		}
		myWebView.loadUrl("javascript:drawPath("+source_x+" , "+source_y+", '"+color+"', \""+state+"\")");
		//color = "#00FF00";
		//int start_x = current_loc.coordinates.x;
		//int start_y = current_loc.coordinates.y;
		//Log.d(TAG, "TRYING TO DRAW GREEN START CIRCLE @ X: "+start_x+" Y: "+start_y);
		//myWebView.loadUrl("javascript:drawLocationWithColor("+start_x+", "+start_y+", '"+color+"')");
		
	}
	
	/*Use this to find the closest fingerprint when the user clicks on the map for a destination.*/
	public Fingerprint findClosestFingerprint(int x, int y) {
		Fingerprint found = null;
		List<Fingerprint> fingerprints = db4oHelper.findAll();
		double minDist = -1;
		for(Fingerprint fingerprint : fingerprints) {
			double next_dist = fingerprint.dirDistToCoor(x, y);
			if(minDist < 0 || next_dist < minDist) {
				minDist = next_dist;
				found = fingerprint;
			}
		}
		fingerprints = null; //Clear the DB List
		return found;
	}
	
	public void deleteFingerprint(String label) {
		List<Fingerprint> fingerprints = db4oHelper.findByLabel(label);
		Log.d(TAG, "Number of fingerprints to delete: "+fingerprints.size());
		db4oHelper.dbCommit();
		Log.d(TAG, "Purging DB...");
		db4oHelper.dbPurge();
		for(Fingerprint fingerprint : fingerprints) {
			if(fingerprint._label.equals(label)) {
				removeFromNeighbors(fingerprint);
				db4oHelper.delete(fingerprint);
				db4oHelper.dbCommit();
				db4oHelper.dbPurge();
				Log.d(TAG, "Purging DB...");
				String message = "Fingerprint: ["+fingerprint._label+"] was deleted.";
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
				Log.d(TAG, message);
				break;
			}
		}
		fingerprints = null;
		List<Fingerprint> fingerprints2 = db4oHelper.findAll();
		Log.d(TAG, "Current Number of Fingerprints in DB: "+fingerprints2.size());
		fingerprints2 = null;
	}
	
	public void removeFromNeighbors(Fingerprint fingerprint){
		ArrayList<Fingerprint> neighbors = fingerprint.getNeighbors();
		for(Fingerprint neighbor : neighbors) {
			if(neighbor != null){
				neighbor.removeNeighbor(fingerprint);
				db4oHelper.store(neighbor);
			}
		}
	}
	
	public void requestDeletion() {
		Log.d(TAG, "Initial size of DB: "+db4oHelper.findAll().size());
		//Create a quick alert dialog:
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Find the matching Fingerprint and delete it from the DB
				String value = input.getText().toString();
				Log.d(TAG, "Current size of DB: "+db4oHelper.findAll().size());
				deleteFingerprint(value);
				Log.d(TAG, "Deletion completed: "+value);
				myWebView.loadUrl("javascript:clearMap()");
				displayAllFingerprints();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
					Log.d(TAG, "Deletion cancelled");
			}
		});
		
		//Show dialog here
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage("Delete a Fingerprint:")
		       .setTitle("Enter the label for the Fingerprint to delete.");
		
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		
		dialog.show(); //Show the dialog
	}
	
	public void modifyNeighborList() {
		Fingerprint temp1 = null;
		List<Fingerprint> fingerprints = db4oHelper.findByLabel("Rm 3138");
		for(Fingerprint f : fingerprints){
			if(f._label.equals("Rm 3138")){
				Log.d(TAG, "findByLabel worked!");
				temp1 = f;
			}
		}
		fingerprints = db4oHelper.findByLabel("Rm 3132");
		for(Fingerprint fingerprint : fingerprints){
			if(fingerprint._label.equals("Rm 3132")){
				if(!(fingerprint.getNeighbors().contains(temp1))){
					fingerprint.getNeighbors().add(temp1);
					db4oHelper.store(fingerprint);
				}
			}
		}
	}
}
