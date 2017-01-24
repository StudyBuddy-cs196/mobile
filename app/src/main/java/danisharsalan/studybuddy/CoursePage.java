package danisharsalan.studybuddy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class CoursePage extends Fragment {

    private LinearLayout mLayout;
    private RelativeLayout ll;
    private FragmentActivity fa;

    String display_name = "";
    String email ="";
    String photo_url = "";
    String provider_id = "";
    String uid = "";
    String fontpath;

    ArrayList<String> courseList = new ArrayList<String>();
    ArrayList<String> courseCode = new ArrayList<String>();
    ArrayList<String> addedCodes = new ArrayList<String>();
    ArrayList<String> addedCourses = new ArrayList<String>();

    boolean isAnonymous = false;

    TextView[] textViewArr;
    TextView tvTemp;
    TextView yourClasses;
    TextView tapMessage;

    RequestQueue queue;

    Typeface tf;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fa = super.getActivity();
        ll = (RelativeLayout) inflater.inflate(R.layout.activity_course_page, container, false);

        yourClasses = (TextView) ll.findViewById(R.id.your_classes_textview);
        tapMessage = (TextView) ll.findViewById(R.id.tap_message);

        fontpath = "fonts/HelveticaNeue Light.ttf";

        tf = Typeface.createFromAsset(getActivity().getAssets(),fontpath);

        yourClasses.setTypeface(tf);
        tapMessage.setTypeface(tf);

        Intent i = getActivity().getIntent();

        display_name = i.getStringExtra("display name");
        email =i.getStringExtra("email");
        photo_url = i.getStringExtra("photo url");
        provider_id = i.getStringExtra("provider id");
        uid = i.getStringExtra("uid");
        isAnonymous = i.getBooleanExtra("anonymity",false);
        courseList = i.getStringArrayListExtra("full course list");
        courseCode = i.getStringArrayListExtra("full code list");
        addedCourses = i.getStringArrayListExtra("added courses");
        addedCodes = i.getStringArrayListExtra("added codes");

        mLayout = (LinearLayout) ll.findViewById(R.id.linlay1);

        if(addedCourses != null) {
            // start queue
            queue = Volley.newRequestQueue(getActivity());
            String url = "http://studybuddy-backend.herokuapp.com/courses?email=" + email;

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            try {
                                Log.d("CoursePage", "parsingSelectedCourses");
                                JSONObject selectedCourses = new JSONObject(response);
                                JSONArray selectedCoursesArray = selectedCourses.getJSONArray("courses");
                                if(selectedCoursesArray.length() <= 0)
                                {
                                    movetoAddCourses();
                                    return;
                                }
                                for(int i = 0; i < selectedCoursesArray.length(); i++)
                                {
                                    mLayout.addView(createNewTextView(selectedCoursesArray.getString(i)));
                                }

                                // setTextBarAuto();
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
        return ll;
    }

    private void movetoAddCourses() {
        Intent i= new Intent(getActivity(),AfterProfileWelcome.class);

        i.putExtra("display name", display_name);
        i.putExtra("email", email);
        i.putExtra("photo url", photo_url);
        i.putExtra("provider id", provider_id);
        i.putExtra("uid", uid);
        i.putExtra("anonymity", isAnonymous);
        i.putExtra("mustregister", false);

        startActivity(i);
    }


    private TextView createNewTextView(String text) {
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(getActivity());

        fontpath = "fonts/HelveticaNeue Light.ttf";

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),fontpath);

        // Setup scrolling text view
        textView.setTypeface(tf);
        lparams.setMargins(0, 20, 0, 20);
        textView.setLayoutParams(lparams);
        textView.setTextColor(Color.argb(173,255,255,255));
        textView.setTextSize(18);
        textView.setMaxLines(1);
        textView.setPadding(20,10,20,10);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(text);
        textView.setClickable(true);

        textView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                tvTemp = (TextView)v;
                tvTemp.setTextColor(Color.WHITE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after .1s = 100ms

                        Intent intent = new Intent(getActivity(),MapsActivity.class);

                        intent.putExtra("display name", NavigationMenu.display_name);
                        intent.putExtra("email", NavigationMenu.email);
                        intent.putExtra("photo url", NavigationMenu.photo_url);
                        intent.putExtra("bio", NavigationMenu.bio);
                        intent.putExtra("anonymity", isAnonymous);
                        intent.putExtra("full course list", courseList);
                        intent.putExtra("full code list", courseCode);
                        intent.putExtra("added courses", addedCourses);
                        intent.putExtra("added codes", addedCodes);
                        intent.putExtra("selected course", tvTemp.getText().toString());

                        tvTemp.setTextColor(getResources().getColor(R.color.transparent_white));

                        startActivity(intent);
                    }
                }, 100);

            }
        });

        return textView;

    }

}
