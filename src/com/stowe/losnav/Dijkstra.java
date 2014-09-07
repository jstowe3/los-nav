package com.stowe.losnav;

import java.util.PriorityQueue;

public class Dijkstra {
	 public static void computePaths(Vertex source) {
		 source.minDistance = 0.;
		 PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		 vertexQueue.add(source);

		 while (!vertexQueue.isEmpty()) {
			 Vertex u = vertexQueue.poll();

			 // Visit each edge exiting u
			 for (Edge e : u.adjacencies) {
				 Vertex v = e.target;
				 double weight = e.weight;
				 double distanceThroughU = u.minDistance + weight;
				 if (distanceThroughU < v.minDistance) {
					 vertexQueue.remove(v);
					 v.minDistance = distanceThroughU;
					 v.previous = u;
					 vertexQueue.add(v);
				 }
			 }
		 }
	 }
}
