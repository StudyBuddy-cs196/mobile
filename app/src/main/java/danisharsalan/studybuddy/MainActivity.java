package danisharsalan.studybuddy;

import android.content.Intent;
import java.io.IOException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    String display_name = "";
    String email = "";
    String photoUrl = "";
    public static String profilePicURL = "";
    public static String lastName = "";
    public static String firstName = "";
    ImageView profilePicSetterImageView;
    EditText firstNameSetterEditText;
    EditText lastNameSetterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        display_name = i.getStringExtra("display name") + "";
        if(display_name.split("\\w+").length > 1){
            lastName = display_name.substring(display_name.lastIndexOf(" ")+1);
            firstName = display_name.substring(0, display_name.lastIndexOf(' '));
        } else {
            firstName = display_name;
            lastName = "";
        }
        firstNameSetterEditText = (EditText) findViewById(R.id.first_name);
        lastNameSetterEditText = (EditText) findViewById(R.id.last_name);
        firstNameSetterEditText.setText(firstName);
        lastNameSetterEditText.setText(lastName);

        email = i.getStringExtra("email");

        photoUrl = i.getStringExtra("photo url");
        if(photoUrl+"" == null){
            profilePicURL = "http://www.clker.com/cliparts/S/0/B/R/c/g/united-nations-logo-transparent-background.svg";
        } else {
            profilePicURL = photoUrl + "";
            //profilePicSetterImageView.findViewById(R.id.profilePicSetter);
        }

        findViewById(R.id.logOutButton).setOnClickListener(this);
        Button doneButton = (Button) findViewById(R.id.donebutton);
        doneButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                moveToNextActivity(true);
            }
        });
    }

    public void moveToNextActivity(boolean mustRegister) {
        Intent i;
        if(!mustRegister){
            i = new Intent(MainActivity.this,CoursePage.class);
        } else {
            i = new Intent(MainActivity.this,AfterProfileWelcome.class);
        }
        i.putExtra("display name", display_name);
        i.putExtra("email", email);
        i.putExtra("photo url", photoUrl);
        i.putExtra("mustregister", mustRegister);
        startActivity(i);
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
    public void onClick(View view) {
        if(view.getId() == R.id.logOutButton){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.putExtra("sign out", true);
            startActivity(i);
        }
    }

}
