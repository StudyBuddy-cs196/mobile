package danisharsalan.studybuddy;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.vision.text.Text;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

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
    String bio;
    private float myHue = BitmapDescriptorFactory.HUE_AZURE;
    private float matchHue = BitmapDescriptorFactory.HUE_BLUE;
    private SlidingUpPanelLayout slidingLayout;
    private TextView slideUpNameTextView;
    private ImageView slideUpPreviewPic;
    private ImageView expandedBuddyPic;
    private TextView expandedBuddyName;
    private TextView buddyBio;
    private ArrayList<JSONObject> matches;
    private Button chatWithBuddy;
    protected LocationManager mLocationManager;

    Marker userLocation;

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
        bio = i.getStringExtra("bio");
        selectedCourse = i.getStringExtra("selected course");
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Check if permissions are given
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)// && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        {
            // Get permissions
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
        matches = new ArrayList<JSONObject>();
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
                                    final JSONObject newUserLocation = userLocationsArray.getJSONObject(i);
                                    placeMarker(newUserLocation.getDouble("lat"), newUserLocation.getDouble("lng"), newUserLocation.getString("email"), matchHue);
                                }

                                if(userLocationsArray != null){
                                    for(int i = 0; i < userLocationsArray.length(); i++){
                                        matches.add(userLocationsArray.getJSONObject(i));
                                    }
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

        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(onSlideListener());
        slideUpNameTextView = (TextView) findViewById(R.id.username);
        slideUpNameTextView.setText("You!");
        slideUpPreviewPic = (ImageView) findViewById(R.id.buddyPicPreview);
        expandedBuddyPic = (ImageView) findViewById(R.id.expandedBuddyPic);
        expandedBuddyName = (TextView) findViewById(R.id.expandedBuddyName);
        expandedBuddyName.setText(display_name);
        Picasso.with(slideUpPreviewPic.getContext()).load(photo_url).transform(new RoundedTransformation(160, 13)).resize(320,320).centerCrop().into(slideUpPreviewPic);
        buddyBio = (TextView) findViewById(R.id.buddyBio);
        buddyBio.setText(bio);//bio
        expandedBuddyPic.setVisibility(View.INVISIBLE);
        chatWithBuddy = (Button) findViewById(R.id.chatButton);
        chatWithBuddy.setVisibility(View.INVISIBLE);
        Picasso.with(slideUpPreviewPic.getContext()).load(photo_url).transform(new RoundedTransformation(160, 13)).resize(320,320).centerCrop().into(expandedBuddyPic);

    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocationLat = location.getLatitude();
            currentLocationLong = location.getLongitude();
            Toast.makeText(MapsActivity.this,  "Location changed!",
                    Toast.LENGTH_SHORT).show();
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

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                //textView.setText("panel is sliding");
                slideUpNameTextView.setVisibility(View.INVISIBLE);
                slideUpPreviewPic.setVisibility(View.INVISIBLE);
                expandedBuddyName.setVisibility(View.VISIBLE);
                expandedBuddyPic.setVisibility(View.VISIBLE);
                buddyBio.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelCollapsed(View view) {
                //textView.setText("panel Collapse");
                slideUpNameTextView.setVisibility(View.VISIBLE);
                slideUpPreviewPic.setVisibility(View.VISIBLE);
                expandedBuddyName.setVisibility(View.INVISIBLE);
                expandedBuddyPic.setVisibility(View.INVISIBLE);
                buddyBio.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPanelExpanded(View view) {
                //textView.setText("panel expand");
                slideUpNameTextView.setVisibility(View.INVISIBLE);
                slideUpPreviewPic.setVisibility(View.INVISIBLE);
                expandedBuddyName.setVisibility(View.VISIBLE);
                expandedBuddyPic.setVisibility(View.VISIBLE);
                buddyBio.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelAnchored(View view) {
                //textView.setText("panel anchored");
            }

            @Override
            public void onPanelHidden(View view) {
                //textView.setText("panel is Hidden");
            }
        };
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

    public void placeMarker(double lat, double lng, String name, float hue) {
        Log.d("Location", "Current Real location: " + currentLocationLat + "," + currentLocationLong);
        if (mMap != null) {

            LatLng markerLoc = new LatLng(lat, lng);
            if (name.equals("Your Location!")) {
                if (userLocation == null) {
                    userLocation = mMap.addMarker(new MarkerOptions().position(markerLoc).title(name).icon(BitmapDescriptorFactory.defaultMarker(hue)));
                } else {
                    userLocation.remove();
                    userLocation = mMap.addMarker(new MarkerOptions().position(markerLoc).title(name).icon(BitmapDescriptorFactory.defaultMarker(hue)));
                }
            } else {
                mMap.addMarker(new MarkerOptions().position(markerLoc).title(name).icon(BitmapDescriptorFactory.defaultMarker(hue)));
            }

            if (hue == myHue) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLoc, 17));
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
                    if (marker.getTitle().equals("Your Location!")) {
                        Log.d("email", email);
                        slideUpNameTextView.setText("You!");
                        expandedBuddyName.setText(display_name);
                        Picasso.with(slideUpPreviewPic.getContext()).load(photo_url).transform(new RoundedTransformation(160, 13)).resize(320, 320).centerCrop().into(slideUpPreviewPic);
                        Picasso.with(slideUpPreviewPic.getContext()).load(photo_url).transform(new RoundedTransformation(160, 13)).resize(320, 320).centerCrop().into(expandedBuddyPic);
                        buddyBio.setText(bio);
                        chatWithBuddy.setEnabled(false);
                        chatWithBuddy.setVisibility(View.INVISIBLE);
                    } else {
                        final JSONObject buddy = getBuddy(marker.getTitle());
                        try {
                            slideUpNameTextView.setText(buddy.getString("name")); //use getTitle() to get buddy's info from server like name, pic, and bio
                            expandedBuddyName.setText(buddy.getString("name")); //getTitle() should either give email or display name to the server so it can find the Buddy's user on the database
                            Picasso.with(slideUpPreviewPic.getContext()).load(buddy.getString("picture")).transform(new RoundedTransformation(160, 13)).resize(320, 320).centerCrop().into(slideUpPreviewPic);
                            Picasso.with(expandedBuddyPic.getContext()).load(buddy.getString("picture")).transform(new RoundedTransformation(160, 13)).resize(320, 320).centerCrop().into(expandedBuddyPic);
                            buddyBio.setText(buddy.getString("bio"));
                            chatWithBuddy.setEnabled(true);
                            chatWithBuddy.setVisibility(View.VISIBLE);
                            chatWithBuddy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        String userId = buddy.getString("email");
                                        Log.d("uid", userId);
                                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + userId));

                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("fb://profile/" + userId)));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    return true;
                }
            });
        }
    }

    public JSONObject getBuddy(String email){
        for(int i = 0; i < matches.size(); i++)
        {
            JSONObject match = matches.get(i);
            try {
                if(match.getString("email").equals(email))
                {
                    return match;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
