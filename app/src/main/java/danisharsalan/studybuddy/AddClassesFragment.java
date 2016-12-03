package danisharsalan.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AddClassesFragment extends Fragment {
    String display_name = "";
    String email = "";
    String photo_url = "";
    ArrayList<String> courseList = new ArrayList<String>();
    ArrayList<String> courseCode = new ArrayList<String>();
    ArrayList<String> addedCodes = new ArrayList<String>();
    ArrayList<String> addedCourses = new ArrayList<String>();
    boolean isAnonymous = false;
    private LinearLayout mLayout;
    RequestQueue queue;
    Context context;
    private CoordinatorLayout ll;
    private FragmentActivity fa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fa = super.getActivity();
        ll = (CoordinatorLayout) inflater.inflate(R.layout.activity_after_profile_welcome, container, false);

        Intent i = getActivity().getIntent();
        display_name = i.getStringExtra("display name");
        email = i.getStringExtra("email");
        photo_url = i.getStringExtra("photo url");
        mLayout = (LinearLayout) ll.findViewById(R.id.linlay);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        String fontpath = "fonts/HelveticaNeue Light.ttf";
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),fontpath);
        TextView hello_name = (TextView)ll.findViewById(R.id.textViewName);
        hello_name.setTypeface(tf);
        TextView welcomeMessage = (TextView)ll.findViewById(R.id.textViewWelcomeMessage);
        welcomeMessage.setTypeface(tf);
        AutoCompleteTextView searchBar = (AutoCompleteTextView)ll.findViewById(R.id.editText1);
        searchBar.setTypeface(tf);
        Button addButton = (Button)ll.findViewById(R.id.addClassButton);
        addButton.setTypeface(tf);
        Button nButton = (Button)ll.findViewById(R.id.nextButton);
        nButton.setTypeface(tf);



        hello_name.setText("Hello "+display_name.substring(0,display_name.indexOf(' '))+ "!");


        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getActivity());

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
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
//        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setIcon(R.drawable.ic_action_search);

        Button addClassButton = (Button) ll.findViewById(R.id.addClassButton);
        addClassButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AutoCompleteTextView source = (AutoCompleteTextView) ll.findViewById(editText1);

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


        Button nextButton = (Button) ll.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NavigationMenu.class);
                intent.putExtra("display name", display_name);
                intent.putExtra("email", email);
                intent.putExtra("photo url", photo_url);
                intent.putExtra("full course list", courseList);
                intent.putExtra("full code list", courseCode);
                intent.putExtra("added courses", addedCourses);
                intent.putExtra("added codes", addedCodes);
                //i.putExtra("display name", auth.getCurrentUser().getDisplayName());

                startActivity(intent);
            }
        });
        return ll;

    }

    public void setTextBarAuto() {


        //actionBar.setCustomView(v);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, courseCode);
        AutoCompleteTextView textView = (AutoCompleteTextView) ll.findViewById(editText1);
        textView.setAdapter(adapter);
        textView.setEnabled(true);
    }


    private TextView createNewTextView(String text) {
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(getActivity());
        String fontpath = "fonts/HelveticaNeue Light.ttf";
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),fontpath);
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
