package com.mapastar.map;

import java.util.ArrayList;

/**
 * Created by nguyenhuuthanh on 4/3/18.
 */
public class Astar {

    public static class Node{
        public int id;
        public double lon,lat;
        public double g_scores = 0;
        public double h_scores = 0;
        public double f_scores = 0;
        public ArrayList<Edge> adjacencies= new ArrayList<Edge>();
        public Node parent;

        public Node(int id, double lon,double lat, double hVal){
            this.id = id;
            this.h_scores = hVal;
            this.lon = lon;
            this.lat = lat;
        }

        public String toString(){
            return id+"";
        }

    }

    public static class Edge{
        public final double cost;
        public final Node target;
        public String name="";

        public Edge(Node targetNode,String name,double costVal){
            target = targetNode;
            cost = costVal;
            this.name=name;
        }
    }
}
