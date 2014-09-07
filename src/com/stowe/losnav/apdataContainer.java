package com.stowe.losnav;

public class apdataContainer {
	
	String bssid;
	int signal_strength;
	int divisor;
	
	public apdataContainer(String bssid, int signal_strength, int divisor) {
		this.bssid = bssid;
		this.signal_strength = signal_strength;
		this.divisor = divisor;
	}
	
	String getBssid() {
		return bssid;
	}
	
	int getSignal_strength() {
		return signal_strength;
	}
	
	int getDivisor() {
		return divisor;
	}

	void setBssid(String bssid) {
		this.bssid = bssid;
	}
	
	void setSignal_strength(int signal_strength) {
		this.signal_strength = signal_strength;
	}
	
	void setDivisor(int divisor) {
		this.divisor = divisor;
	}
	
	void addSignal_strength(int new_signal_strength) {
		signal_strength += new_signal_strength;
		divisor += 1;
	}
	
	void averageSignal_strength() {
		signal_strength = signal_strength / divisor;
		divisor = 1;
	}
}
