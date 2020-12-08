package com.mapastar.map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ///load database in background app to avoid lag in UI
        loadData loadData = new loadData();
        loadData.execute();
    }

    ///class to read database(nodes,edges) from file in asset (numberOfNodeEdge.txt,node.txt,edge.txt,edge_name.txt)
    public class loadData extends AsyncTask<Void, Void, Void>{
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ////After loading database, starting main activity of map

            Intent main = new Intent(Splash.this, MainActivity.class);
            startActivity(main);
            overridePendingTransition(0,0);

            ////stop Splash activity
            finish();
        }
    }


}
