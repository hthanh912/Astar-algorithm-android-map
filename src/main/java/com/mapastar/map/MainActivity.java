package com.mapastar.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    MapView map;
    Astar.Node x = null;
    Astar.Node y = null;
    ImageButton timduong, reset;
    TextView lotrinh;

    int source, goal;

    GeoPoint startPoint = new GeoPoint(10.762622, 106.660172);

    ItemizedIconOverlay<OverlayItem> start = null;
    ItemizedIconOverlay<OverlayItem> end = null;
    Overlay touchOverlay;
    boolean findpath = false;
    PriorityQueue<Astar.Node> queue;
    Set<Astar.Node> explored = new HashSet<Astar.Node>();
    ArrayList<Astar.Node> path = new ArrayList<Astar.Node>();
    List<Overlay> mapOverlays;
    ArrayList<GeoPoint> l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Comparator<Astar.Node> comparator = new Comparator<Astar.Node>() {
            //override compare method
            public int compare(Astar.Node i, Astar.Node j) {
                if (i.f_scores > j.f_scores) {
                    return 1;
                } else if (i.f_scores < j.f_scores) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        queue = new PriorityQueue<Astar.Node>(100000, comparator);
        timduong = (ImageButton) findViewById(R.id.timduong);
        lotrinh =(TextView)findViewById(R.id.lotrinh);
        reset = (ImageButton) findViewById(R.id.reset);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        ////setup mapview
        final IMapController mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(startPoint);
        startPoint = new GeoPoint(10.762622, 106.660172);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);


        ////recognize single tap or long press on map
        ////single tap to choose start node, double tap to choose goal node
        touchOverlay = new Overlay(this) {
            @Override
            public void draw(Canvas arg0, MapView arg1, boolean arg2) {
            }

            @Override
            public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
                if (!findpath) {
                    final Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.blue);
                    Projection proj = mapView.getProjection();
                    GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                    String longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
                    String latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
                    x = DataHolder.findNode(Double.parseDouble(longitude), Double.parseDouble(latitude));
                    if (x != null) {
                        source = x.id;
                        System.out.println(x.id + "");

                        /// draw marker
                        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
                        OverlayItem mapItem = new OverlayItem("", "", new GeoPoint(x.lat, x.lon));
                        mapItem.setMarker(marker);
                        overlayArray.add(mapItem);
                        if (start == null) {
                            start = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(0, start);
                            mapView.invalidate();
                        } else {
                            mapView.getOverlays().remove(start);
                            mapView.invalidate();
                            start = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(0, start);
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean onLongPress(MotionEvent e, MapView mapView) {
                if (!findpath) {
                    final Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.red);
                    Projection proj = mapView.getProjection();
                    GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                    String longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
                    String latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
                    y = DataHolder.findNode(Double.parseDouble(longitude), Double.parseDouble(latitude));
                    if (y != null) {
                        goal = y.id;


                        /////update h value to all node after chose goal node
                        DataHolder.updateH(y.lon, y.lat);

                        System.out.println(y.id + "");

                        /// draw marker
                        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
                        OverlayItem mapItem = new OverlayItem("", "", new GeoPoint(y.lat, y.lon));
                        mapItem.setMarker(marker);
                        overlayArray.add(mapItem);
                        if (end == null) {
                            end = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(0, end);
                            mapView.invalidate();
                        } else {
                            mapView.getOverlays().remove(end);
                            mapView.invalidate();
                            end = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                            mapView.getOverlays().add(0, end);
                        }
                    }
                }
                return true;
            }
        };

        map.getOverlays().add(touchOverlay);

        ///setup button timduong
        timduong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!findpath && x != null && y != null) {
                    mapOverlays = map.getOverlays();

                    //Astar algorithm
                    AstarSearch(DataHolder.getArrayNode()[source], DataHolder.getArrayNode()[goal]);

                    //get path after run Astar algorithm
                    path = printPath(DataHolder.getArrayNode()[goal]);

                    ///draw path on map
                    if (path.size() > 0) {
                        l = new ArrayList<GeoPoint>();
                        for (int i = 0; i < path.size(); i++)
                            l.add(new GeoPoint(path.get(i).lat, path.get(i).lon));

                        Road road = new Road(l);
                        Polyline roadPolyline = RoadManager.buildRoadOverlay(road, Color.parseColor("#4872b5"), 9);
                        roadPolyline.getPaint().setStrokeJoin(Paint.Join.ROUND);
                        roadPolyline.getPaint().setStrokeCap(Paint.Cap.ROUND);
                        mapOverlays.add(roadPolyline);
                        map.invalidate();
                        findpath = true;
                        lotrinh.setText(route(path));
                        lotrinh.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
    }

    ////clear current source node, goal node and path
    public void reset() {

        ///after run astar algorithm, reset database becasue database was modified during running astar algorithm
        new loadData().execute();
        lotrinh.setVisibility(View.GONE);
        queue.clear();
        explored.clear();
        map.getOverlays().clear();
        map.getOverlays().add(touchOverlay);
        map.invalidate();
        path.clear();
        l.clear();

        ///set nodes to default
        x = null;
        y = null;
        start = null;
        end = null;
        findpath = false;
    }

    public static ArrayList<Astar.Node> printPath(Astar.Node target) {
        ArrayList<Astar.Node> path = new ArrayList<Astar.Node>();
        for (Astar.Node node = target; node != null; node = node.parent) {
            path.add(node);
        }

        ///reverse the path
        Collections.reverse(path);
        return path;
    }

    ///get route from path
    public String route(ArrayList<Astar.Node> path){
        ArrayList<String> r = new ArrayList<String>();
        for(int i=0; i<path.size()-1; i++){
            for(int j=0; j<path.get(i).adjacencies.size(); j++){
                if(path.get(i).adjacencies.get(j).target == path.get(i+1)){
                    if(path.get(i).adjacencies.get(j).name!="") {
                        r.add(path.get(i).adjacencies.get(j).name);
                    }
                }
            }
        }
        String a="";
        ArrayList<String> r2 = new ArrayList<String>();
        r2.add(r.get(0));
        for(int i =1; i<r.size();i++){
            if(r.get(i).equals(r.get(i-1))==false) {
                r2.add(r.get(i));
            }
        }

        if(r2.size()-3>0) {
            for (int i = 0; i < r2.size() - 3; i++) {
                if (r2.get(i).equals(r2.get(i + 2))) r2.set(i + 1, "(Quay đầu)");
            }
        }

        for (int i=0; i< r2.size(); i++){
            a+= r2.get(i);
            if(i!=r2.size()-1) a+= " -> ";
        }


        return a;
    }

    ///Astar Algorithm
    public void AstarSearch(Astar.Node source, Astar.Node goal) {
        source.g_scores = 0;
        queue.add(source);
        boolean found = false;
        while ((!queue.isEmpty()) && (!found)) {
            Astar.Node current = queue.poll();
            explored.add(current);
            if (current.id == goal.id) {
                found = true;
            }
            for (Astar.Edge e : current.adjacencies) {
                Astar.Node child = e.target;
                double cost = e.cost;
                double temp_g_scores = current.g_scores + cost;
                double temp_f_scores = temp_g_scores + child.h_scores;

                if ((explored.contains(child)) &&
                        (temp_f_scores >= child.f_scores)) {
                    continue;
                } else if ((!queue.contains(child)) ||
                        (temp_f_scores < child.f_scores)) {

                    child.parent = current;
                    child.g_scores = temp_g_scores;
                    child.f_scores = temp_f_scores;

                    if (queue.contains(child)) {
                        queue.remove(child);
                    }
                    queue.add(child);
                }
            }
        }
    }

    ///class to read database(nodes,edges) from file in asset (numberOfNodeEdge.txt,node.txt,edge.txt,edge_name.txt)
    public class loadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            BufferedReader reader = null;
            BufferedReader name = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("numberOfNodeEdge.txt"), "UTF-8"));
                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    String[] str= mLine.split(" ");
                    DataHolder.setArrayNode(new Astar.Node[Integer.parseInt(str[0])]);
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }

            reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("node.txt"), "UTF-8"));
                String mLine;
                int i=0;
                String[] str;
                while ((mLine = reader.readLine()) != null) {
                    str = mLine.split(" ");

                    ///parameters Node(node id, double longtitude, double latitude, double h_value)
                    ///default h_value = 0.0
                    DataHolder.getArrayNode()[i] = new Astar.Node(Integer.parseInt(str[0]), Double.parseDouble(str[2]), Double.parseDouble(str[1]), 0.0);
                    i++;
                }
            } catch (IOException e) {
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }

            reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("edge.txt"), "UTF-8"));
                name = new BufferedReader(
                        new InputStreamReader(getAssets().open("edge_name.txt"), "UTF-8"));
                String mLine;
                String[] str;
                while ((mLine = reader.readLine()) != null) {
                    str = mLine.split(" ");

                    ///add adjacencies to node
                    ///parameters Edge(Node targetNode,String street_name,double costVal)
                    DataHolder.getArrayNode()[Integer.parseInt(str[0])].adjacencies.add(new Astar.Edge(DataHolder.arrayNode[Integer.parseInt(str[1])], name.readLine()+"",Double.parseDouble(str[2])));
                }
            } catch (IOException e) {
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }

    }
}
