package danisharsalan.studybuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FacebookLogin extends Activity {
    private CallbackManager mCallbackManager;

    public static final String PROFILE_USER_ID = "USER_ID";
    public static final String PROFILE_FIRST_NAME = "PROFILE_FIRST_NAME";
    public static final String PROFILE_LAST_NAME = "PROFILE_LAST_NAME";
    public static final String PROFILE_IMAGE_URL = "PROFILE_IMAGE_URL";

    String displayName = "";
    String email = "";
    String photoUrl = "";

    RequestQueue queue;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_facebook_login);

        logo = (ImageView) findViewById(R.id.loginLogo);
        logo.setImageResource(R.drawable.sbppoommssb);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");

        mCallbackManager = CallbackManager.Factory.create();
        queue = Volley.newRequestQueue(this);

        Intent incoming = getIntent();
        boolean navFromAnotherActLogout = incoming.getBooleanExtra("logout", true);

        if(isLoggedIn() && navFromAnotherActLogout){
            final Intent i = new Intent(this, NavigationMenu.class);
            String emailTosend = AccessToken.getCurrentAccessToken().getUserId();
            i.putExtra("email", emailTosend);
            startActivity(i);
        } else if (isLoggedIn() && !navFromAnotherActLogout){
            LoginManager.getInstance().logOut();
        }

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            Profile prof = Profile.getCurrentProfile();
                            email = prof.getId();

                            if(prof.getProfilePictureUri(128,128) == null){
                                photoUrl = "https://support.plymouth.edu/kb_images/Yammer/default.jpeg";
                            } else {
                                photoUrl = prof.getProfilePictureUri(128,128).toString();
                            }

                            displayName=prof.getFirstName()+ " "+ prof.getLastName();
                            mProfileTracker.stopTracking();

                            String url = "http://studybuddy-backend.herokuapp.com/is_registered?email=" + email;

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            // Display the first 500 characters of the response string.
                                            if(response.equals("true") ){
                                                updateUI(false);
                                            } else {
                                                updateUI(true);
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
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    Profile profile = Profile.getCurrentProfile();
                    email = profile.getId();

                    if(profile.getProfilePictureUri(128,128) == null){
                        photoUrl = "https://support.plymouth.edu/kb_images/Yammer/default.jpeg";
                    } else {
                        photoUrl = profile.getProfilePictureUri(128,128).toString();
                    }

                    displayName=profile.getFirstName()+ " "+ profile.getLastName();
                    String url = "http://studybuddy-backend.herokuapp.com/is_registered?email=" + email;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.
                                    if(response.equals("true") ){
                                        updateUI(false);
                                    } else {
                                        updateUI(true);
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
            public void onCancel() {
                // Handle cancel
                Log.v("facebook - onCancel", "cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                // Handle error
                Log.v("facebook - onError", e.getMessage());
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if you don't add following block,
        // your registered `FacebookCallback` won't be called
        if (mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    // When app is resumed on device
    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    // When app is paused on device
    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    private void updateUI(boolean toMain) {
        Intent i;

        if(toMain) {
            i = new Intent(FacebookLogin.this, MainActivity.class);
        } else {
            i = new Intent(this, NavigationMenu.class);
        }

        if(photoUrl == null){
            Log.d("photo_url","it's null...");
            i.putExtra("photo url", "https://support.plymouth.edu/kb_images/Yammer/default.jpeg");
        } else {
            i.putExtra("photo url", photoUrl);
        }

        i.putExtra("display name", displayName);
        i.putExtra("email", email);

        startActivity(i);
    }

    // returns true if logged in to Facebook in this app
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

}