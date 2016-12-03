package danisharsalan.studybuddy;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import java.io.IOException;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
    private static int RESULT_LOAD_IMAGE = 1;
    private static int PICK_IMAGE = 100;
    public boolean needProfilePic = true;
    private static final int REQUEST_EXTERNAL_STORAGE_RESULT = 1;

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
        profilePicSetterImageView = (ImageButton) findViewById(R.id.profilePicSetter);
        email = i.getStringExtra("email");
        photoUrl = i.getStringExtra("photo url");
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
        Button doneButton = (Button) findViewById(R.id.donebutton);
        doneButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                moveToNextActivity(true);
            }
        });
    }

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
        Intent i;
        if(!mustRegister){
            i = new Intent(MainActivity.this,CoursePage.class);
        } else {
            i = new Intent(MainActivity.this,AfterProfileWelcome.class);
        }
        i.putExtra("display name", display_name);
        i.putExtra("email", email);
        i.putExtra("photo url", profilePicURL);
        i.putExtra("mustregister", mustRegister);
        Log.d("Display Name:", display_name);
        startActivity(i);
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
