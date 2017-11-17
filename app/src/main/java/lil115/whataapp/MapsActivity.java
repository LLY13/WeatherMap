package lil115.whataapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    RequestQueue queue = null;



    public RequestQueue getRequestQueue(Context context)
    {
        if(queue == null)
        {
            queue = Volley.newRequestQueue(this);
        }

        return queue;
    }

    //location (name or coordination)
    String location;
    String temp;
    String weather;

    TextView txtWeather = null;
    TextView txtTemp = null;
    EditText txtLocation = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        try {

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            Task<Location> located = client.getLastLocation();
            located.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    displayLocation(task.getResult().getLatitude(),task.getResult().getLongitude());
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ) );
                    displayWeather(task.getResult().getLatitude(),task.getResult().getLongitude());
                    String locate = "";
                    locate += (Double.toString(task.getResult().getLatitude()));
                    locate = locate + "," + (Double.toString(task.getResult().getLongitude()));
                    txtLocation.setText(locate);
                }
            });
        }
        catch(SecurityException ex)
        {
            ex.printStackTrace();
        }


        //edit text(location)
        txtLocation = (EditText) findViewById(R.id.location);
        // text view
        txtWeather = (TextView) findViewById(R.id.txtWeather);
        txtTemp = (TextView) findViewById(R.id.txtTemp);

        //button
        final Button btnGo = (Button) findViewById(R.id.btnGo);
        final Button btnI = (Button) findViewById(R.id.btnI);

        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        //btnI method to get my location
        btnI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mMap.clear();
                    //ask permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                        checkPermission();
                    }
                    //client to get my location

                    Task<Location> located = client.getLastLocation();
                    located.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            String locate = "";
                            locate += (Double.toString(task.getResult().getLatitude()));
                            locate = locate + "," + (Double.toString(task.getResult().getLongitude()));
                            txtLocation.setText(locate);
                            displayLocation(task.getResult().getLatitude(),task.getResult().getLongitude());
                            displayWeather(task.getResult().getLatitude(),task.getResult().getLongitude());
                        }
                    });
                }catch(SecurityException ex)
                {
                    ex.printStackTrace();
                }

            }
        });

        //btnGo method
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //display location
                mMap.clear();
                location = txtLocation.getText().toString();
                try {
                    double lat = Double.parseDouble(location.split(",")[0]);
                    double lon = Double.parseDouble(location.split(",")[1]);
                    displayLocation(lat,lon);
                    displayWeather(lat,lon);
                }catch (Exception e){

                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //function to display location
    public void displayLocation(double lat, double lon){
        LatLng place = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(place).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );
    }

    //function to dispaly weather
    public void displayWeather(double lat, double lon){
        //display weather
        //weather  yahoo
        String YQL = "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"(" + lat + "," + lon + ")\")";

        final String endPoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

        RequestQueue q = Volley.newRequestQueue(getApplicationContext());

        queue = getRequestQueue(getApplicationContext());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, endPoint, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject query = response.getJSONObject("query");
                    JSONObject results = query.getJSONObject("results");
                    JSONObject channel = results.getJSONObject("channel");
                    JSONObject item = channel.getJSONObject("item");
                    JSONObject condition = item.getJSONObject("condition");


                    temp = condition.getString("temp");
                    temp += "F";
                    weather = condition.getString("text");
                    txtWeather.setTextColor(Color.RED);
                    txtTemp.setTextColor(Color.RED);
                    txtWeather.setText(weather);
                    txtTemp.setText(temp);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        queue.add(jsObjRequest);


    }

    //permission
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }



}
