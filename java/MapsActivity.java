package com.example.setu4.final_project_3;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    JSONArray routesArray;

    String jsonArray;

    int k = 0;

    double stp_strt_lat, stp_strt_lng, stp_end_lat, stp_end_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Intent intent = getIntent();

        jsonArray = intent.getStringExtra("jsonArray");

        String tmp = intent.getStringExtra("selRouteNum");
        k = Integer.parseInt(tmp);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        LatLng source = null;
        LatLng destination = null;

        try {
            routesArray = new JSONArray(jsonArray);
            String journey_start_lat, journey_start_lng;
            List path = new ArrayList<HashMap<String, String>>();


            double jrny_strt_lt, jrny_strt_ln;

            double jrny_end_lt, jrny_end_ln;

            JSONObject routeObject = routesArray.getJSONObject(k);
            JSONArray stepArray = routeObject.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

            journey_start_lat = stepArray.getJSONObject(0).getJSONObject("start_location").getString("lat");
            journey_start_lng = stepArray.getJSONObject(0).getJSONObject("start_location").getString("lng");

            jrny_strt_lt = Double.parseDouble(journey_start_lat);
            jrny_strt_ln = Double.parseDouble(journey_start_lng);

            source = new LatLng(jrny_strt_lt, jrny_strt_ln);

            mMap.addMarker(new MarkerOptions().position(source).title("Marker at start"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 3));

            for (int j = 0; j < stepArray.length(); j++) {
                //JSONObject stepObject = stepArray.getJSONObject(j);

                String tmp;
                tmp = stepArray.getJSONObject(j).getJSONObject("start_location").getString("lat");
                stp_strt_lat = Double.parseDouble(tmp);
                tmp = stepArray.getJSONObject(j).getJSONObject("start_location").getString("lng");
                stp_strt_lng = Double.parseDouble(tmp);
                tmp = stepArray.getJSONObject(j).getJSONObject("end_location").getString("lat");
                stp_end_lat = Double.parseDouble(tmp);
                tmp = stepArray.getJSONObject(j).getJSONObject("end_location").getString("lng");
                stp_end_lng = Double.parseDouble(tmp);

                String polyline = "";
                polyline = (String)(stepArray.getJSONObject(j).getJSONObject("polyline").get("points"));
                List<LatLng> list = decodePoly(polyline);

                /** Traversing all points */
                for(int l=0;l<list.size();l++){
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                    hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                    path.add(hm);
                }

                /*
                Polyline line = mMap.addPolyline(new PolylineOptions()

                        .add(new LatLng(stp_strt_lat, stp_strt_lng), new LatLng(stp_end_lat, stp_end_lng))
                        .width(5)
                        .color(Color.RED));
                         */
            }

            jrny_end_lt = stp_end_lat;
            jrny_end_ln = stp_end_lng;

            destination = new LatLng(jrny_end_lt, jrny_end_ln);

            mMap.addMarker(new MarkerOptions().position(destination).title("Marker at start"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));

            routes.add(path);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for(int i=0;i<routes.size();i++)
        {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.BLUE);
        }

        // Drawing polyline in the Google Map for the i-th route
        mMap.addPolyline(lineOptions);
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
