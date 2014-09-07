package com.stowe.losnav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vertex implements Comparable<Vertex> {

	public final Fingerprint fingerprint;
	public Edge[] adjacencies;
	public double minDistance = Double.POSITIVE_INFINITY;
	public Vertex previous;

	public Vertex(Fingerprint fingerprint) {
	   this.fingerprint = fingerprint;
	}

	public String toString() {
	   return fingerprint._label;
	}

	public int compareTo(Vertex other) {
	   return Double.compare(minDistance, other.minDistance);
	}
	  
	public static List<Vertex> getShortestPathTo(Vertex target) {
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
		path.add(vertex);
		Collections.reverse(path);
		return path;
	}
}
