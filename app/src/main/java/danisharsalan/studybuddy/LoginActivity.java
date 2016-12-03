package danisharsalan.studybuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;


public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private GoogleSignInAccount acct;
    private boolean signOutTrue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent i = getIntent();
        signOutTrue = i.getBooleanExtra("sign out", false);
        loadActivity();
    }

    private void loadActivity() {
        Log.d("loadActivity","1");
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        Log.d("loadActivity","2");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        Log.d("loadActivity","3");
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        Log.d("loadActivity","4");
        mGoogleApiClient.connect();
        Log.d("loadActivity","5");
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        Log.d("loadActivity","6");
        signInButton.setSize(SignInButton.SIZE_WIDE);
        Log.d("loadActivity","7");
        signInButton.setScopes(gso.getScopeArray());
        Log.d("loadActivity","8");
    }

    private void loadActivityNoNew() {
        Log.d("loadActivity","1");
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        Log.d("loadActivity","2");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        Log.d("loadActivity","3");
        //mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        Log.d("loadActivity","4");
        mGoogleApiClient.connect();
        Log.d("loadActivity","5");
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        Log.d("loadActivity","6");
        signInButton.setSize(SignInButton.SIZE_WIDE);
        Log.d("loadActivity","7");
        signInButton.setScopes(gso.getScopeArray());
        Log.d("loadActivity","8");
        Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("appruns","before if statement");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d("appruns","beginning of if statement");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("appruns","middle of if statement");
            handleSignInResult(result);
            Log.d("appruns","end of if statement");
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            Log.d("handleSignIn","isSuccess");
            acct = result.getSignInAccount();
            updateUI(true);
        } else {
            mGoogleApiClient.disconnect();
            Log.d("handleSignIn","isNotSuccess");
            loadActivityNoNew();
        }
    }


    private void updateUI(boolean b) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("display name", acct.getDisplayName());
        i.putExtra("email", acct.getEmail());
        if(acct.getPhotoUrl() == null){
            Log.d("photo_url","it's null...");
            i.putExtra("photo url", "https://support.plymouth.edu/kb_images/Yammer/default.jpeg");
        } else {
            i.putExtra("photo url", acct.getPhotoUrl().toString());
        }
        startActivity(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case 2:
                if(signOutTrue){
                    signOut();
                    break;
                }
                break;
            // ...
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        loadActivity();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}