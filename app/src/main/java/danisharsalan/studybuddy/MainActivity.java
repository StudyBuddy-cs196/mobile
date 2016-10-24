package danisharsalan.studybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import java.io.IOException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int RC_SIGN_IN = 0;
    private FirebaseAuth auth;
    public static String profilePicURL = "";
    public static String lastName = "";
    public static String firstName = "";
    public static String fullName = "";
    ImageView profilePicSetterImageView;
    EditText firstNameSetterEditText;
    EditText lastNameSetterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            //User already signed in
            Log.d("AUTH", auth.getCurrentUser().getEmail());
            if(auth.getCurrentUser().getPhotoUrl()+"" == null){
                profilePicURL = "http://www.clker.com/cliparts/S/0/B/R/c/g/united-nations-logo-transparent-background.svg";
            } else {
                profilePicURL = auth.getCurrentUser().getPhotoUrl() + "";
            }
            profilePicSetterImageView = (ImageView) findViewById(R.id.profilePicSetter);
            fullName = auth.getCurrentUser().getDisplayName();
            if(fullName.split("\\w+").length > 1){
                lastName = fullName.substring(fullName.lastIndexOf(" ")+1);
                firstName = fullName.substring(0, fullName.lastIndexOf(' '));
            } else {
                firstName = fullName;
                lastName = "";
            }
            GetXMLTask task = new GetXMLTask();
            task.execute(new String[] { profilePicURL });
            firstNameSetterEditText = (EditText) findViewById(R.id.first_name);
            lastNameSetterEditText = (EditText) findViewById(R.id.last_name);
            firstNameSetterEditText.setText(firstName);
            lastNameSetterEditText.setText(lastName);
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER,
                            AuthUI.EMAIL_PROVIDER,
                            AuthUI.GOOGLE_PROVIDER)
                    .build(), RC_SIGN_IN);
        }
        findViewById(R.id.logOutButton).setOnClickListener(this);
    }

    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            profilePicSetterImageView.setImageBitmap(result);
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.
                        decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //User logged in
                Log.d("AUTH", auth.getCurrentUser().getEmail());
                Log.d("AUTH", auth.getCurrentUser().getDisplayName());
                Log.d("AUTH", auth.getCurrentUser().getPhotoUrl()+"");
                fullName = auth.getCurrentUser().getDisplayName();
                if(fullName.split("\\w+").length > 1){
                    lastName = fullName.substring(fullName.lastIndexOf(" ")+1);
                    firstName = fullName.substring(0, fullName.lastIndexOf(' '));
                } else {
                    firstName = fullName;
                    lastName = "";
                }
                if(auth.getCurrentUser().getPhotoUrl()+"" == null){
                    profilePicURL = "http://www.clker.com/cliparts/S/0/B/R/c/g/united-nations-logo-transparent-background.svg";
                } else {
                    profilePicURL = auth.getCurrentUser().getPhotoUrl() + "";
                }
                firstNameSetterEditText = (EditText) findViewById(R.id.first_name);
                lastNameSetterEditText = (EditText) findViewById(R.id.last_name);
                firstNameSetterEditText.setText(firstName);
                lastNameSetterEditText.setText(lastName);
            } else {
                //User not authenticated
                Log.d("AUTH", "NOT AUTHENTICATED");
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.FACEBOOK_PROVIDER,
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER)
                        .build(), RC_SIGN_IN);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.logOutButton){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("AUTH", "USER LOGGED OUT!");
                            if(auth.getCurrentUser() != null){
                                //User alread signed in
                                Log.d("AUTH", auth.getCurrentUser().getEmail());
                                Log.d("AUTH", auth.getCurrentUser().getEmail());
                                Log.d("AUTH", auth.getCurrentUser().getDisplayName());
                                Log.d("AUTH", auth.getCurrentUser().getPhotoUrl()+"");
                                fullName = auth.getCurrentUser().getDisplayName();
                                if(fullName.split("\\w+").length > 1){
                                    lastName = fullName.substring(fullName.lastIndexOf(" ")+1);
                                    firstName = fullName.substring(0, fullName.lastIndexOf(' '));
                                } else {
                                    firstName = fullName;
                                    lastName = "";
                                }
                                if(auth.getCurrentUser().getPhotoUrl()+"" == null){
                                    profilePicURL = "http://www.clker.com/cliparts/S/0/B/R/c/g/united-nations-logo-transparent-background.svg";
                                } else {
                                    profilePicURL = auth.getCurrentUser().getPhotoUrl() + "";
                                }
                                firstNameSetterEditText = (EditText) findViewById(R.id.first_name);
                                lastNameSetterEditText = (EditText) findViewById(R.id.last_name);
                                firstNameSetterEditText.setText(firstName);
                                lastNameSetterEditText.setText(lastName);
                            } else {
                                startActivityForResult(AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setProviders(
                                                AuthUI.FACEBOOK_PROVIDER,
                                                AuthUI.EMAIL_PROVIDER,
                                                AuthUI.GOOGLE_PROVIDER)
                                        .build(), RC_SIGN_IN);
                            }
                        }
                    });
        }
    }
}
