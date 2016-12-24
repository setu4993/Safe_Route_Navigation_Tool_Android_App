package com.example.setu4.final_project_3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;
import static java.lang.Math.random;
import static java.lang.Math.tanh;

public class MainActivity extends AppCompatActivity {

    int input_nodes = 15;
    int hidden_nodes = 6;
    int output_nodes = 2;

    Button fndRte;

    ImageButton gps_location;

    EditText src, dst;

    TextView txt;

    StringBuffer map_url = new StringBuffer(1000);
    StringBuffer weather_url = new StringBuffer(1000);

    String[] summary = new String[3];
    String[] duration = new String[3];
    boolean[] bad_weather = new boolean[3];
    double[] route_fatal_rate = new double[3];
    double[] route_fatal_ratio = new double[3];
    double[] route_run_times = new double[3];


    double[] inp = new double[input_nodes];
    double[] hid = new double[hidden_nodes];
    double[] op = new double[output_nodes];
    //wih = new double[input_nodes][hidden_nodes];
    double[][] who = new double[][]{
            {-0.750123, -0.497043},
            {0.915349, -0.678874},
            {-0.654927, 0.146259},
            {-0.505615, 0.112282},
            {0.868924, -0.297115},
            {0.610024, 0.421366},
    };

    double[][] wih = new double[][]{
            {-0.561637, -0.253991, 0.819873, -0.25971, 0.572855},
            {-0.533829, 0.654406, 0.931391, -0.344783, 0.410525},
            {0.114898, -0.367054, 0.27816, -0.553507, 0.0245071},
            {0.707431, -0.0112928, -0.472734, -0.793755, 0.261714},
            {0.0364817, 0.553587, -0.268991, -0.0209633, -0.572661},
            {-0.0741651, -0.329272, 0.659618, 0.171692, -0.30354},
            {0.535172, 1.19705, 0.543737, 0.646956, 0.277101},
            {1.0377, 0.96999, 0.0556309, -0.793415, -0.560348},
            {0.0614614, 0.491456, 0.399289, -0.526572, -0.469879},
            {-0.129515, -0.21533, 0.491429, 0.713054, -0.180044},
            {0.612165, -0.365329, -0.134128, -0.657588, 0.628338},
            {0.331338, 0.513023, 0.15748, 0.0360183, 0.266197},
            {0.600015, -0.578222, 0.446647, 0.312744, -0.308215},
            {0.181697, 0.473083, -0.237502, -0.224479, 0.717235},
            {0.588548, -0.238637, 0.628652, -0.0556734, 0.189249}
    };

    long[] route_time = new long[3];    //route time in seconds

    long current;

    int sel = 0;

    double stp_strt_lat, stp_strt_lng, stp_end_lat, stp_end_lng, cur_lat, cur_lng;

    JSONArray routesArray;

    int id;
    long stp_sunrise, stp_sunset;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fndRte = (Button) findViewById(R.id.buttonFndRte);

        gps_location = (ImageButton) findViewById(R.id.gpsButton);

        txt = (TextView) findViewById(R.id.textViewMain);

        gps_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(MainActivity.this, "GPS unavailable!", Toast.LENGTH_LONG).show();
                }
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                String provider = locationManager.getBestProvider(new Criteria(), true);
                //String provider = LocationManager.NETWORK_PROVIDER;
                //Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                Location locations = locationManager.getLastKnownLocation(provider);
                List<String> providerList = locationManager.getAllProviders();
                if (null != locations && null != providerList && providerList.size() > 0)
                {
                    Toast.makeText(MainActivity.this, "Getting GPS location...", Toast.LENGTH_LONG).show();
                    double longitude = locations.getLongitude();
                    double latitude = locations.getLatitude();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try
                    {
                        List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (null != listAddresses && listAddresses.size() > 0)
                        {
                            String _Location = listAddresses.get(0).getAddressLine(0);
                            src = (EditText) findViewById(R.id.editTextSrc);
                            src.setText(_Location);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });

        fndRte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clear_values();

                Toast.makeText(MainActivity.this, "Calculating best route...", Toast.LENGTH_LONG).show();

                src = (EditText) findViewById(R.id.editTextSrc);
                dst = (EditText) findViewById(R.id.editTextDst);

                for (int l = 0; l < input_nodes; l++) {
                    inp[l] = 1;
                }

                hid[hidden_nodes - 1] = 1;

                map_url.append("https://maps.googleapis.com/maps/api/directions/json?alternatives=true&key=YOUR_GOOGLE_MAPS_API_KEY&origin=");
                map_url.append(src.getText().toString().replaceAll("\\s", "+"));
                map_url.append("&destination=");
                map_url.append(dst.getText().toString().replaceAll("\\s", "+"));
                new MapsXML().execute(map_url.toString());
                map_url.delete(0, map_url.length());

            }
        });

        //map_url.delete(0, map_url.length());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public class MapsXML extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            int i;

            String tmp = null;

            URL url = null;
            try {
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String mapsJSON = buffer.toString();

                JSONObject mapObject = new JSONObject(mapsJSON);
                routesArray = mapObject.getJSONArray("routes");

                for (i = 0; i < routesArray.length(); i++) {
                    JSONObject routeObject = routesArray.getJSONObject(i);
                    summary[i] = routeObject.getString("summary");

                    //JSONObject durationObject = routeObject.getJSONArray("legs").getJSONObject(0).getJSONObject("duration");

                    duration[i] = routeObject.getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");

                    JSONArray stepArray = routeObject.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");


                    Calendar calendar = Calendar.getInstance();
                    current = calendar.getTimeInMillis();

                    //set time and date related inputs

                    inp[0] = calendar.get(Calendar.DATE);
                    inp[1] = 1 + calendar.get(Calendar.MONTH);
                    inp[2] = 1 + calendar.get(Calendar.DAY_OF_WEEK);
                    inp[3] = calendar.get(Calendar.HOUR_OF_DAY);
                    inp[4] = calendar.get(Calendar.MINUTE);

                    inp[0] = tanh((inp[i] - 15) / 8);
                    inp[1] = tanh((inp[i] - 6) / 3);
                    inp[2] = tanh((inp[i] - 3.5) / 2);
                    inp[3] = tanh((inp[i] - 12) / 6);
                    inp[4] = tanh((inp[i] - 30) / 15);

                    /*
                    System.out.println(inp[0]);
                    System.out.println(inp[1]);
                    System.out.println(inp[2]);
                    System.out.println(inp[3]);
                    System.out.println(inp[4]);

                    */

                    for (int j = 0; j < stepArray.length(); j++) {
                        //JSONObject stepObject = stepArray.getJSONObject(j);

                        tmp = stepArray.getJSONObject(j).getJSONObject("start_location").getString("lat");
                        stp_strt_lat = Double.parseDouble(tmp);
                        tmp = stepArray.getJSONObject(j).getJSONObject("start_location").getString("lng");
                        stp_strt_lng = Double.parseDouble(tmp);
                        tmp = stepArray.getJSONObject(j).getJSONObject("end_location").getString("lat");
                        stp_end_lat = Double.parseDouble(tmp);
                        tmp = stepArray.getJSONObject(j).getJSONObject("end_location").getString("lng");
                        stp_end_lng = Double.parseDouble(tmp);

                        tmp = stepArray.getJSONObject(j).getJSONObject("duration").getString("value");
                        route_time[i] += Long.parseLong(tmp);

                        cur_lat = stp_strt_lat;
                        cur_lng = stp_strt_lng;

                        inp[5] = tanh((cur_lat - 44) / 13);
                        inp[6] = tanh((cur_lng + 115.0) / 13);

                        double n = (abs(stp_strt_lat - stp_end_lat)) + (abs(stp_strt_lng - stp_end_lng));

                        double diff_lat = (abs(stp_strt_lat - stp_end_lat)) / n;
                        double diff_lng = (abs(stp_strt_lng - stp_end_lng)) / n;


                        //if((abs(stp_strt_lat - stp_end_lat) > 1) || (abs(stp_strt_lng - stp_end_lng) > 1) || j==0)
                        for (int m = 0; m < n; m++) {
                            //get weather
                            weather_url.append("http://api.openweathermap.org/data/2.5/weather?appid=YOUR_OPENWEATHERMAP_API_KEY&origin&lat=");
                            weather_url.append(Double.toString(cur_lat));
                            weather_url.append("&lon=");
                            weather_url.append(Double.toString(cur_lng));

                            inp[5] = tanh((cur_lat - 44) / 13);
                            inp[6] = tanh((cur_lng + 115.0) / 13);

                            //System.out.println(weather_url);

                            //new WeatherXML().execute(weather_url.toString());

                            String w_url = weather_url.toString();
                            weather_url.delete(0, weather_url.length());

                            //update the new lat, lng
                            cur_lat = stp_strt_lat + (m * diff_lat);
                            cur_lng = stp_strt_lng + (m * diff_lng);

                            HttpURLConnection weather_connection = null;
                            BufferedReader weather_reader = null;

                            URL url2 = null;
                            url2 = new URL(w_url);
                            weather_connection = (HttpURLConnection) url2.openConnection();
                            weather_connection.connect();
                            InputStream inputStream2 = weather_connection.getInputStream();
                            weather_reader = new BufferedReader(new InputStreamReader(inputStream2));
                            StringBuffer weather_buffer = new StringBuffer();
                            String weather_line = "";

                            while ((weather_line = weather_reader.readLine()) != null) {
                                weather_buffer.append(weather_line);
                            }

                            String weatherJSON = weather_buffer.toString();
                            JSONObject weatherObject = new JSONObject(weatherJSON);

                            tmp = weatherObject.getJSONArray("weather").getJSONObject(0).getString("id");
                            id = Integer.parseInt(tmp);

                            tmp = weatherObject.getJSONObject("sys").getString("sunrise");
                            stp_sunrise = Long.parseLong(tmp);
                            tmp = weatherObject.getJSONObject("sys").getString("sunset");
                            stp_sunset = Long.parseLong(tmp);

                            set_weather_inputs(id, i);
                            set_light_inputs(stp_sunrise, stp_sunset, i);
                            ///set_other_inputs();
                        }
                        runNetwork(i);
                    }
                }

                sel = getBestRoute(i);
                return ("Route " + Integer.toString(sel + 1) + " is the best route.");
                //return (duration[0] + " " + duration[1] + " " + duration[2] + " " + routesArray.length())
                //return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();

            txt.setText(result);
            //Intent intent = new Intent(this.getContext(), MapsActivity.class);

            //intent.putExtra("json", routesArray.toString());
            //startActivity(intent);
            //MainActivity.startActivity(new Intent(MainActivity, MapsActivity.class));

            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("jsonArray", routesArray.toString());
            intent.putExtra("selRouteNum", Integer.toString(sel));
            startActivity(intent);
            //finish();
            //context.startActivity(intent);
            //((Activity)context).finish();
        }
    }

    public void set_weather_inputs(int id, int i) {

        switch (id) {
            case 800:
            case 801:
            case 904:
            case 951:
            case 952:
            case 953:
            case 954:
            case 955:
                //inp[9] = 1;	//clear sky
                inp[9] = 0.75 + (random() / 4);
                set_input_random(10);
                set_input_random(11);
                set_input_random(12);
                set_input_random(13);
                break;

            case 500:
            case 501:
            case 300:
            case 301:
            case 310:
            case 311:
            case 313:
            case 321:
            case 520:
                //inp[10] = 0.5;
                //inp[11] = 0.5;	//drizzle = rain 0.5
                inp[10] = 0.25 + (random() / 4);
                inp[11] = 0.25 + (random() / 4);
                set_input_random(9);
                set_input_random(12);
                set_input_random(13);
                break;
            case 511:
            case 615:
            case 616:
                //inp[10] = 0.5;
                //inp[11] = 0.5;	//freezing rain
                inp[10] = 0.25 + (random() / 4);
                inp[11] = 0.25 + (random() / 4);
                set_input_random(9);
                set_input_random(12);
                set_input_random(13);
                break;
            case 502:
            case 503:
            case 504:
            case 521:
            case 522:
            case 531:
            case 302:
            case 312:
            case 314:
                //inp[10] = 1;	//rain 1.0
                inp[10] = 0.75 + (random() / 4);
                set_input_random(9);
                set_input_random(11);
                set_input_random(12);
                set_input_random(13);
                break;
            case 906:
            case 611:
            case 612:
                //inp[11] = 1;	//sleet hail
                inp[11] = 0.75 + (random() / 4);
                set_input_random(9);
                set_input_random(10);
                set_input_random(12);
                set_input_random(13);
                bad_weather[i] = true;
                break;
            case 600:
            case 620:
            case 903:
                //inp[11] = 0.5;	//snow 0.5
                inp[11] = 0.25 + (random() / 4);
                set_input_random(9);
                set_input_random(10);
                set_input_random(12);
                set_input_random(13);
                break;
            case 601:
            case 602:
            case 621:
            case 622:
                //inp[11] = 1;	//snow 1.0
                inp[11] = 0.75 + (random() / 4);
                set_input_random(9);
                set_input_random(10);
                set_input_random(12);
                set_input_random(13);
                break;
            case 701:
            case 711:
            case 721:
            case 741:
                //inp[12] = 1;	//fog, smog, smoke
                inp[12] = 0.75 + (random() / 4);
                set_input_random(9);
                set_input_random(11);
                set_input_random(10);
                set_input_random(13);
                break;
            case 802:
            case 803:
            case 804:
                //inp[12] = 0.5;	//cloudy
                inp[12] = 0.25 + (random() / 4);
                set_input_random(9);
                set_input_random(11);
                set_input_random(10);
                set_input_random(13);
                break;
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
            case 731:
            case 751:
            case 761:
            case 762:
            case 771:
            case 781:
            case 900:
            case 901:
            case 902:
            case 905:
            case 956:
            case 957:
            case 958:
            case 959:
            case 960:
            case 961:
            case 962:
                //inp[13] = 1;
                inp[13] = 0.75 + (random() / 4);    //Severe Crosswinds / Blowing sand, soil, dirt / Blowing snow
                set_input_random(9);
                set_input_random(11);
                set_input_random(12);
                set_input_random(10);
                bad_weather[i] = true;
                break;
            default:
                break;
        }
    }
/*
    public void set_other_inputs()
    {
        int i;
        for (i = 0; i < 5; i++)
        {
            switch (i)
            {
                case 0:
                    inp[i] = tanh((inp[i] - 15)/8);
                    break;
                case 1:
                    inp[i] = tanh((inp[i] - 6)/3);
                    break;
                case 2:
                    inp[i] = tanh((inp[i] - 3.5)/2);
                    break;
                case 3:
                    inp[i] = tanh((inp[i] - 12)/6);
                    break;
                case 4:
                    inp[i] = tanh((inp[i] - 30)/15);
                    break;
                default:
                    break;
            }
        }
        inp[input_nodes - 1] = 1;
        hid[hidden_nodes - 1] = 1;
    }
    */

    void set_light_inputs(long sunrise, long sunset, int i) {


        if ((abs(current - sunrise)) < 360000) {
            inp[7] = (random() / 4);
            inp[8] = 0.50 + (random() / 4); //dusk
        } else if ((abs(current - sunset)) < 360000) {
            inp[7] = 0.25 + (random() / 4); //dawn
            inp[8] = 0.25 + (random() / 4);
        } else if ((current > sunrise) || (current < sunset)) {
            inp[7] = 0.75 + (random() / 4); //daylight
            set_input_random(8);
        } else if ((current < sunrise) || (current > sunset)) {
            inp[8] = 0.75 + (random() / 4); //night
            set_input_random(7);
        }
    }


    public void runNetwork(int k) {

        int i, j;

        //calculation of hidden layer
        for (j = 0; j < (hidden_nodes - 1); j++) {
            for (i = 0; i < input_nodes; i++) {
                hid[j] = hid[j] + (wih[i][j] * inp[i]);
            }
        }
        for (j = 0; j < (hidden_nodes - 1); j++) {
            //double expValue = (-2 * ( hid[j]) / 10);
            hid[j] = tanh(hid[j] / 10);    //tanh to hidden value

        }


        //calculation of output layer
        for (j = 0; j < hidden_nodes; j++) {
            for (i = 0; i < output_nodes; i++) {
                op[i] = op[i] + (who[j][i] * hid[j]);
            }
        }

        for (j = 0; j < output_nodes; j++) {

            op[j] = tanh((3.5 * op[j]) - 1); //tanh to hidden value

            //System.out.println(op[j]);


            if ((op[j]) <= 0.2)
            {
                op[j] = 0.0;
            }
            else
            {
                op[j] = 1.0;
            }
        }

        route_fatal_rate[k] += op[1];
        route_run_times[k]++;
        //return op[1]; //Return the output value
    }

    public void set_input_random(int i) {
        inp[i] = random() - 1;
    }

    public void clear_values() {
        int i;

        for (i = 0; i < hidden_nodes; i++) {
            hid[i] = 0;
        }
        for (i = 0; i < 3; i++) {
            route_time[i] = 0;
            route_fatal_rate[i] = 0;
            route_run_times[i] = 0;
            bad_weather[i] = false;
        }
    }

    public int getBestRoute(int n) {
        int i, j;
        for (i = 0; i < n; i++) {
            route_fatal_ratio[i] = route_fatal_rate[i] / route_run_times[i];
        }

        double route_fatal_ratio_sort[] = route_fatal_ratio;
        double route_multiplication[] = new double[]{0, 0, 0};

        double route_fatal_ratio_selected = 100;
        double route_time_selected = 0;

        long route_time_sort[] = route_time;

        for (i = 0; i < n; i++) {
            if(bad_weather[i] == false) {
                for (j = 0; j < n; j++) {
                    if (route_fatal_ratio_sort[j] < route_fatal_ratio_sort[i]) {
                        double temp = route_fatal_ratio_sort[i];
                        route_fatal_ratio_sort[i] = route_fatal_ratio_sort[j];
                        route_fatal_ratio_sort[j] = temp;

                        long tmp = route_time_sort[i];
                        route_time_sort[i] = route_time_sort[j];
                        route_time_sort[j] = tmp;
                    }
                }
            }
        }

        if ((route_fatal_ratio_sort[0] - route_fatal_ratio_sort[1]) < 0.01) {
            if ((route_fatal_ratio_sort[2] - route_fatal_ratio_sort[1]) < 0.01) {
                route_fatal_ratio_selected = route_fatal_ratio_sort[0];
            }
        } else if (((route_fatal_ratio_sort[0] - route_fatal_ratio_sort[1]) < 0.03) && ((route_fatal_ratio_sort[0] - route_fatal_ratio_sort[1]) > 0.01)) {
            for (i = 0; i < n; i++) {
                route_multiplication[i] = route_time_sort[i] * route_fatal_ratio_sort[i];
            }
            route_time_selected = route_multiplication[0];
            route_fatal_ratio_selected = route_fatal_ratio_sort[0];


            for (i = 0; i < route_multiplication.length; i++) {
                if (route_multiplication[i] > route_time_selected) {
                    route_time_selected = route_multiplication[i];
                    route_fatal_ratio_selected = route_fatal_ratio_sort[i];
                }
            }
        } else {
            route_fatal_ratio_selected = route_fatal_ratio_sort[0];
        }

        for (i = 0; i < n; i++) {
            if (route_fatal_ratio_selected == route_fatal_ratio[i]) {
                return i;
            }
        }

        return 0;
    }
}