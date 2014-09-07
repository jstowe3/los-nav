package com.stowe.losnav;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;


public class Fingerprint {
	
	//private variables
	
	String _label; //The name for this fingerprint
	
	// Four lists of AP readings for four different directions
	ArrayList<ArrayList<apdataContainer>> AP_list; //List of AP's detected for the fingerprint
	
	Point coordinates = new Point();
	
	ArrayList<Fingerprint> neighbors = new ArrayList<Fingerprint>();  // List of neighboring fingerprints
	
	
	public Fingerprint(String _label){
		
		this._label = _label;
	}
	
	public void setAP_list(ArrayList<ArrayList<apdataContainer>> AP_list){
		this.AP_list = AP_list;
	}
	
	public void setCoordinates(int x, int y) {
		coordinates.set(x, y);
	}
	
	public ArrayList<ArrayList<apdataContainer>> getAPData() {
		return AP_list;
	}
	
	public ArrayList<Fingerprint> getNeighbors() {
		return neighbors;
	}
	
	public void addNeighbor(Fingerprint neighbor) {
		neighbors.add(neighbor);
	}
	
	/*Returns the minimum distance between this Fingerprint and the input apdataContainer list.*/
	public double distanceTo(ArrayList<apdataContainer> apdata_list) {
		double minDistance = -1;
		double tempDistance = 0;
		
		Log.d("Fingerprint", "Comparing Location Read: ");
		for(apdataContainer container : apdata_list) {
			Log.d("Fingerprint", "BSSID: "+container.getBssid()+", Strength: "+container.getSignal_strength());
		}
		Log.d("Fingerprint", "To "+this._label+": ");
		for(ArrayList<apdataContainer> from_list : this.AP_list) {
			Log.d("Fingerprint", "NEXT LIST: ");
			for(apdataContainer ctr : from_list) {
				Log.d("Fingerprint", "BSSID: "+ctr.getBssid()+", Strength: "+ctr.getSignal_strength());
			}
		}
		
		// For each list of AP data in the source Fingerprint
		for(ArrayList<apdataContainer> from_list : this.AP_list) {
			// For each apdataContainer in the destination Fingerprint
			for(apdataContainer container : apdata_list) {
				tempDistance += Math.pow(apDiff(container, from_list), 2);
			}
			tempDistance = Math.sqrt(tempDistance);
			if(minDistance < 0)
				minDistance = tempDistance;
			else if(minDistance > tempDistance)
				minDistance = tempDistance;
			tempDistance = 0;
		}
		Log.d("Fingerprint", "Min distance to: "+this._label+": "+minDistance);
		return minDistance;
	}
	
	/*Returns whether the ap_container was found in the list of apdataContainers.*/
	public boolean containsAP(apdataContainer ap_container, ArrayList<apdataContainer> list) {
		boolean found = false;
		String bssid = ap_container.getBssid();
		for(apdataContainer container : list) {
			if(container.getBssid().equals(bssid)) {
				return true;
			}
		}
		return found;
	}
	
	/*Returns the difference between an AP signal strength and its matching AP signal strength if found.
	  If no AP in the list matches the ap_container given, return the ap_container strength.*/
	public int apDiff(apdataContainer ap_container, ArrayList<apdataContainer> list) {
		int diff = Math.abs(ap_container.getSignal_strength());
		String bssid = ap_container.getBssid();
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).getBssid().equals(bssid)) {
				int num1 = Math.abs(list.get(i).getSignal_strength());
				int num2 = Math.abs(ap_container.getSignal_strength());
				return Math.abs(num1 - num2);
			}
		}
		return diff;
	}
	
	/* Detect whether a point on the map is close to this fingerprint's coordinates. */
	public boolean boundsMatched(int x, int y) {
		boolean matched = false;
		
		double x_diff = Math.abs(x - this.coordinates.x);
		double y_diff = Math.abs(y - this.coordinates.y);
		
		if(x_diff <= 4 && y_diff <= 4) {
			matched = true;
		}
		return matched;
	}
	
	/*Find the euclidean distance between two Fingerprints*/
	public double dirDistTo(Fingerprint neighbor) {
		
		int x1 = this.coordinates.x;
		int x2 = neighbor.coordinates.x;
		int y1 = this.coordinates.y;
		int y2 = neighbor.coordinates.y;
		
		double diff1 = x2 - x1;
		double diff2 = y2 - y1;
		
		diff1 = Math.pow(diff1, 2);
		diff2 = Math.pow(diff2, 2);
		double sum = diff1 + diff2;
		sum = Math.sqrt(sum);
		
		return sum;
	}
	
	public double dirDistToCoor(int x, int y) {
		
		int x1 = this.coordinates.x;
		int x2 = x;
		int y1 = this.coordinates.y;
		int y2 = y;
		
		double diff1 = x2 - x1;
		double diff2 = y2 - y1;
		
		diff1 = Math.pow(diff1, 2);
		diff2 = Math.pow(diff2, 2);
		double sum = diff1 + diff2;
		sum = Math.sqrt(sum);
		
		return sum;
	}
	
	public void removeNeighbor(Fingerprint neighbor){
		neighbors.remove(neighbor);
	}
}
