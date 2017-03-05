package danisharsalan.studybuddy;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String profilePicURL = "";
    public static String lastName = "";
    public static String firstName = "";
    public boolean needProfilePic = true;

    private static int PICK_IMAGE = 100;
    private static final int REQUEST_EXTERNAL_STORAGE_RESULT = 1;

    EditText firstNameSetterEditText;
    EditText lastNameSetterEditText;
    EditText bio;
    ImageView profilePicSetterImageView;
    Button doneButton;

    String display_name = "";
    String email = "";
    String photoUrl = "";
    String bioFromIntent;

    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        Log.d("da","sd");

        // Get intent from previous view
        Intent i = getIntent();
        display_name = i.getStringExtra("display name") + "";
        bioFromIntent = i.getStringExtra("bio");
        email = i.getStringExtra("email");
        photoUrl = i.getStringExtra("photo url");

        firstNameSetterEditText = (EditText) findViewById(R.id.first_name);
        lastNameSetterEditText = (EditText) findViewById(R.id.last_name);
        bio = (EditText) findViewById(R.id.desc_edittext);
        profilePicSetterImageView = (ImageButton) findViewById(R.id.profilePicSetter);
        doneButton = (Button) findViewById(R.id.donebutton);

        // Formatting full name from text fields
        if(display_name.split("\\w+").length > 1){
            lastName = display_name.substring(display_name.lastIndexOf(" ")+1);
            firstName = display_name.substring(0, display_name.lastIndexOf(' '));
        } else {
            firstName = display_name;
            lastName = "";
        }

        firstNameSetterEditText.setText(firstName);
        lastNameSetterEditText.setText(lastName);

        // If there is a bio on db, set that as db else, empty
        if(bioFromIntent!=null) {
            bio.setText(bioFromIntent);
        }

        // If no profile pic on db, set to default
        if(needProfilePic){
            if(photoUrl == null){
                profilePicURL = "https://i.vimeocdn.com/portrait/58832_300x300";
            } else {
                profilePicURL = photoUrl;
            }
            Picasso.with(profilePicSetterImageView.getContext()).load(profilePicURL)
                    .transform(new RoundedTransformation(160, 13)).resize(320,320).centerCrop().into(profilePicSetterImageView);
        }

        profilePicSetterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(v);
            }
        });

        findViewById(R.id.logOutButton).setOnClickListener(this);

        doneButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Register/update user on db after tapping the done button
                String url = "http://studybuddy-backend.herokuapp.com/is_registered?email=" + email;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                if(response.equals("true") ){
                                    moveToNextActivity(false);
                                } else {
                                    moveToNextActivity(true);
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
        });

    }

    // If permission is given, allow user to take picture to set as profile pic
    @TargetApi(Build.VERSION_CODES.M)
    private void takePhoto(View v) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                startActivityForResult(i, PICK_IMAGE);
        } else {
            if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this,
                        "External storage permission required to pick images from Gallery",
                        Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE_RESULT);
        }
    }

    // Get permissions for device gallery/camera
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == REQUEST_EXTERNAL_STORAGE_RESULT){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                startActivityForResult(i, PICK_IMAGE);
            } else {
                Toast.makeText(this,
                        "Cannot change profile picture without permission to Gallery",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // After user has selected a picture from gallery/camera, set as profile pic
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            profilePicURL = selectedImage.toString();
            Picasso.with(profilePicSetterImageView.getContext()).load(selectedImage)
                    .transform(new RoundedTransformation(160, 13)).resize(320,320).centerCrop().into(profilePicSetterImageView);
            Log.d("transformation", "complete");
            needProfilePic = false;
        }
    }

    public void moveToNextActivity(boolean mustRegister) {
        display_name = firstNameSetterEditText.getText() + " " + lastNameSetterEditText.getText();

        final Intent i;

        if(!mustRegister){
            i = new Intent(MainActivity.this,NavigationMenu.class);
        } else {
            i = new Intent(MainActivity.this,AfterProfileWelcome.class);
        }

        // Queue Start
        queue = Volley.newRequestQueue(this);
        String url = "http://studybuddy-backend.herokuapp.com/register";
        StringRequest sr = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //mPostCommentResponse.requestCompleted();
                startActivity(i);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mPostCommentResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("name", display_name);
                params.put("bio", bio.getText().toString());
                params.put("picture", profilePicURL);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                return params;
            }
        };

        queue.add(sr);

        // add info for next intent
        i.putExtra("display name", display_name);
        i.putExtra("email", email);
        i.putExtra("photo url", profilePicURL);
        i.putExtra("bio", bio.getText());
        i.putExtra("mustregister", mustRegister);

        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        // logout facebook - can be implemented for other login apis
        if(view.getId() == R.id.logOutButton){
            Intent i = new Intent(MainActivity.this, FacebookLogin.class);
            i.putExtra("logout", false);
            startActivity(i);
        }
    }
}
