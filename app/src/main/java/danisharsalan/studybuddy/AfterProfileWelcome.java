package danisharsalan.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static danisharsalan.studybuddy.R.id.editText1;

public class AfterProfileWelcome extends AppCompatActivity {

    private LinearLayout mLayout;

    String display_name = "";
    String email = "";
    String photo_url = "";

    ArrayList<String> courseList = new ArrayList<String>();
    ArrayList<String> courseCode = new ArrayList<String>();
    ArrayList<String> addedCodes = new ArrayList<String>();
    ArrayList<String> addedCourses = new ArrayList<String>();

    boolean isAnonymous = false;

    RequestQueue queue;

    Context context;

    String fontpath;

    TextView welcomeMessage;
    TextView hello_name;

    Button addButton;
    Button nButton;

    Typeface tf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_profile_welcome);
        context = this;

        Intent i = getIntent();

        display_name = i.getStringExtra("display name");
        email = i.getStringExtra("email");
        photo_url = i.getStringExtra("photo url");
        mLayout = (LinearLayout) findViewById(R.id.linlay);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        fontpath = "fonts/HelveticaNeue Light.ttf";
        tf = Typeface.createFromAsset(getAssets(),fontpath);

        hello_name = (TextView)findViewById(R.id.textViewName);
        hello_name.setTypeface(tf);

        welcomeMessage = (TextView)findViewById(R.id.textViewWelcomeMessage);
        welcomeMessage.setTypeface(tf);

        AutoCompleteTextView searchBar = (AutoCompleteTextView)findViewById(R.id.editText1);
        searchBar.setTypeface(tf);

        addButton = (Button)findViewById(R.id.addClassButton);
        addButton.setTypeface(tf);

        nButton = (Button)findViewById(R.id.nextButton);
        nButton.setTypeface(tf);

        hello_name.setText("Hello "+display_name.substring(0,display_name.indexOf(' '))+ "!");

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

        if(i.getBooleanExtra("mustregister", false))
        {
            //Start Register
            StringRequest sr = new StringRequest(Request.Method.POST,"https://studybuddy-backend.herokuapp.com/register", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Register Response", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    mPostCommentResponse.requestEndedWithError(error);
                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("email",email);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    return params;
                }
            };
            queue.add(sr);
        }

        String url = "http://studybuddy-backend.herokuapp.com/all_courses";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            Log.d("AfterProfileWelcome", "parsingJSON");
                            JSONArray courseArray = new JSONArray(response);
                            int len = courseArray.length();
                            for (int i = 0; i < len; i++) {
                                String formattedCourse = "";
                                String formattedCode = "";
                                JSONObject course = courseArray.getJSONObject(i);
                                String[] splitCourse = course.toString().split(":");
                                formattedCourse += splitCourse[0].substring(1) + " " + splitCourse[1].substring(0, splitCourse[1].length() - 1);
                                formattedCourse = formattedCourse.replace("\"", "");
                                formattedCode += splitCourse[0].substring(1);
                                formattedCode = formattedCode.replace("\"", "");
                                courseList.add(formattedCourse);
                                courseCode.add(formattedCode);
                            }
                            setTextBarAuto();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // start queue
        queue.add(stringRequest);

        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AutoCompleteTextView source = (AutoCompleteTextView) findViewById(editText1);

                int index = courseCode.indexOf(source.getText().toString());


                if (index >= 0) {
                    String chosen = courseList.get(index);
                    if (!addedCourses.contains(chosen)) {
                        final String code = source.getText().toString();
                        addedCodes.add(code);
                        addedCourses.add(chosen);

                        mLayout.addView(createNewTextView(chosen));
                        StringRequest sr = new StringRequest(Request.Method.POST, "http://studybuddy-backend.herokuapp.com/courses", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("response", response);
//                                mPostCommentResponse.requestCompleted();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
//                                mPostCommentResponse.requestEndedWithError(error);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("course", code);
                                params.put("status", "True");
                                params.put("email", email);
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
                                return params;
                            }
                        };
                        queue.add(sr);
                    }

                } else {
                    Toast.makeText(context,"Course: " + source.getText().toString() + " does not exist!",Toast.LENGTH_SHORT).show();
                }
                source.setText("");
            }
        });

        nButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AfterProfileWelcome.this, NavigationMenu.class);

                intent.putExtra("display name", display_name);
                intent.putExtra("email", email);
                intent.putExtra("photo url", photo_url);
                intent.putExtra("full course list", courseList);
                intent.putExtra("full code list", courseCode);
                intent.putExtra("added courses", addedCourses);
                intent.putExtra("added codes", addedCodes);

                startActivity(intent);
            }
        });
    }

    public void setTextBarAuto() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, courseCode);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(editText1);
        textView.setAdapter(adapter);
        textView.setEnabled(true);
    }


    private TextView createNewTextView(String text) {
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(this);

        fontpath = "fonts/HelveticaNeue Light.ttf";

        tf = Typeface.createFromAsset(getAssets(),fontpath);

        textView.setTypeface(tf);
        textView.setLayoutParams(lparams);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        textView.setMaxLines(1);
        textView.setPadding(20, 10, 20, 10);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(text);

        return textView;
    }

}
