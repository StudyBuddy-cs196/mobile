package danisharsalan.studybuddy;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1003;
    double currentLocationLat;
    double currentLocationLong;
    RequestQueue queue;
    String display_name = "";
    String email ="";
    String photo_url = "";
    String provider_id = "";
    String uid = "";
    String selectedCourse = "";
    private float myHue = BitmapDescriptorFactory.HUE_AZURE;
    private float matchHue = BitmapDescriptorFactory.HUE_BLUE;

    protected LocationManager mLocationManager;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("Location", String.valueOf(location));
            currentLocationLat = location.getLatitude();
            currentLocationLong = location.getLongitude();
            Log.d("Latitude", String.valueOf(currentLocationLat));
            placeMarker(currentLocationLat, currentLocationLong, "Your Location!", myHue);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Status", "Status Changed: " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent i = getIntent();
        display_name = i.getStringExtra("display name");
        email =i.getStringExtra("email");
        photo_url = i.getStringExtra("photo url");
        provider_id = i.getStringExtra("provider id");
        uid = i.getStringExtra("uid");
        selectedCourse = i.getStringExtra("selected course");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)// && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
        else
        {
            forceLocationUpdate();
        }
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);
        String[] course = selectedCourse.split(" ");

        String url = "http://studybuddy-backend.herokuapp.com/matches?email=" + email + "&course="+ course[0] + "%20" + course[1];
        String encodedUrl = url;

        Log.d("Post Request", encodedUrl);
        if(encodedUrl != null){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, encodedUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            try {
                                Log.d("CoursePage", "parsingOtherUserLocations");
                                JSONObject userLocations = new JSONObject(response);
                                JSONArray userLocationsArray = userLocations.getJSONArray("matches");
                                if(userLocationsArray.length() <= 0)
                                {
                                    //TO-DO
                                    //Display Toast Message
                                    return;
                                }
                                for(int i = 0; i < userLocationsArray.length(); i++)
                                {
                                    JSONObject newUserLocation = userLocationsArray.getJSONObject(i);
                                    placeMarker(newUserLocation.getDouble("lat"), newUserLocation.getDouble("lng"), newUserLocation.getString("email"), matchHue);
                                }

//                            setTextBarAuto();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        forceLocationUpdate();
    }

    public void forceLocationUpdate(){
        Log.d("Permissions", "Permissions have been done");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            Log.d("Location", "Got Location!");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, mLocationListener);
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
            Log.d("Location", "Got Location!");
            Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null)
            {
                currentLocationLat = loc.getLatitude();
                currentLocationLong = loc.getLongitude();

                placeMarker(loc.getLatitude(), loc.getLongitude(), "Your Location", myHue);
            }

        }
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Log.d("Location", "Current location: " + currentLocationLat + "," + currentLocationLong);
        placeMarker(currentLocationLat, currentLocationLong, "Your Location!", myHue);
    }

    public void placeMarker(double lat, double lng, String name, float hue)
    {
        Log.d("Location", "Current Real location: " + currentLocationLat + "," + currentLocationLong);
        if(mMap != null) {

            LatLng markerLoc = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(markerLoc).title(name).icon(BitmapDescriptorFactory.defaultMarker(hue)));
            if(hue == myHue)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLoc, 17));
            }

        }
    }


}
