package com.mapastar.map;


import android.location.Location;

/**
 * Created by nguyenhuuthanh on 3/29/18.
 */


////class to holder data in whole app
public class DataHolder {
    public static Astar.Node[] arrayNode;
    private static Location loc1, loc2;

    public static Astar.Node[] getArrayNode() {
        return arrayNode;
    }

    public static void setArrayNode(Astar.Node[] arrayNode) {
        DataHolder.arrayNode = arrayNode;
    }


    ////find node by longtitue and latitude
    public static Astar.Node findNode(double lon, double lat){
        for (int i =0; i< arrayNode.length; i++) {
            if ((Math.abs(arrayNode[i].lon - lon) < 0.0002 && Math.abs(arrayNode[i].lat - lat) < 0.0002)) {
                return arrayNode[i];
            }
        }
        return null;
    }


    ////calculate h value (bee-line)
    public static void updateH(double lon, double lat){

        ///calculate h value to all nodes
        for(int i=0; i<arrayNode.length; i++){
            loc1 = new Location("");
            loc1.setLatitude(lat);
            loc1.setLongitude(lon);
            loc2 = new Location("");
            loc2.setLatitude(arrayNode[i].lat);
            loc2.setLongitude(arrayNode[i].lon);

            ////h value = distance between 2 nodes
            arrayNode[i].h_scores = loc1.distanceTo(loc2);
        }
    }

}
