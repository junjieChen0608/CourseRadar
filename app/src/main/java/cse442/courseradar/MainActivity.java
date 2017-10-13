package cse442.courseradar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import UtilityClass.CourseRating;
import UtilityClass.InstructorInfo;
import UtilityClass.InstructorResultAdapter;
import UtilityClass.ReviewInfo;
import UtilityClass.ReviewInfoAdapter;
import UtilityClass.courseData;


public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private ProgressBar pbWait;

    /*search overview elements*/
    private ConstraintLayout clSearchOverview;
    private ListView lvSearchResultList;
    private TextView tvNoResult;
    private boolean noResult;

    /*search detail view elements*/
    private ListView lvInstructorInfo;
    private ConstraintLayout clInstructorOverview;

    private static final String INSTRUCTORS = "instructors";
    private static final String COURSES= "courses";
    private static final String RATINGS = "ratings";

    private DatabaseReference courseDB;
    private DatabaseReference instructorDB;
    private DatabaseReference ratingsDB;

    private TextView instructorReview;

    private String lastTimeUsedModifiedCourseID;

    private int countInstructors;
    private int countReviews;

    private String currentInstructor;
    private String currentCourseID;
    private String currentInstructorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);
        unlockDrawer();
        clSearchOverview = (ConstraintLayout) findViewById(R.id.search_overview);
        svSearchBar = (SearchView) findViewById(R.id.sv_search_bar);
        svSearchBar.setIconifiedByDefault(false);
        svSearchBar.setOnQueryTextListener(this);
        pbWait = (ProgressBar) findViewById(R.id.pb_wait);

        lvSearchResultList = (ListView) findViewById(R.id.lv_instructorData);
        lvInstructorInfo = (ListView) findViewById(R.id.lv_instructor_info);

        tvNoResult = (TextView) findViewById(R.id.tv_no_result);
        noResult = true;

        courseDB= FirebaseDatabase.getInstance().getReference(COURSES);
        instructorDB = FirebaseDatabase.getInstance().getReference(INSTRUCTORS);
        ratingsDB = FirebaseDatabase.getInstance().getReference(RATINGS);

        clInstructorOverview = findViewById(R.id.instructor_overview);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf("onStart","MainActivity onStart");
        /*mark current activity as this activity*/
        currentActivity = this;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.wtf(TAG + " onStart", "user is signed out");
        }
        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        input = input.replaceAll(" ", "");
        final String keyword = input;
        showProgressBarInOverview();
        Log.d(TAG, "search this: " + input);
        //improve the illegal input detection
        if(input!= null && !input.isEmpty()&& input.matches("\\w+")){
            input= input.toUpperCase();
            final String modifiedInput = input;

            courseDB.child(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    courseData resultCourse= dataSnapshot.getValue(courseData.class);
                    /*here it gets the courseData, it has a hashmap to store instructors and their email
                    * take those to form list*/
                    //construct the instructor list for the course

                    noResult = (resultCourse == null);

                    if (!noResult) {

                        Log.d("search test", resultCourse.getCredit());
                        Log.d("search test", resultCourse.getInstructor().toString());

                        HashMap<String, String> instructors = resultCourse.getInstructor();
                        final ArrayList<InstructorInfo> instructorNames = new ArrayList<InstructorInfo>();

                        final int numInstructors = instructors.size();
                        countInstructors = 0;

                        for (String s : instructors.keySet()) {
                            final String eachInstructorName = s;
                            final String eachInstructorEmail = instructors.get(s);

                            //no need for email, instead we should have an review overview.
                            instructorDB.child(s.toUpperCase()).child("courses").child(modifiedInput).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, Long> totalRatingInfo = (HashMap<String, Long>) dataSnapshot.getValue();
                                    instructorNames.add(new InstructorInfo(eachInstructorName, eachInstructorEmail, totalRatingInfo));
                                    Log.wtf(TAG, "in search result, current instructor name is: " + eachInstructorName);
                                    Log.wtf(TAG, "instructor name size: " + instructorNames.size());
                                    countInstructors += 1;
                                    if (countInstructors == numInstructors) {
                                        displayInstructorsForThisCourse(instructorNames, modifiedInput);
                                        hideProgressBarInOverview(keyword);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            Log.wtf(TAG, "end of each iteration");
                        }
                    }else{
                        hideProgressBarInOverview(keyword);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("onCancelled", "activited");
                }
            });
        }else{
            noResult = true;
            hideProgressBarInOverview(keyword);
            Toast.makeText(this, "we only accept charecters and numbers", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void displayInstructorsForThisCourse(final ArrayList<InstructorInfo> instructorNames, final String modifiedInput) {
        Log.wtf(TAG, "found all instructors for this courses");
        InstructorResultAdapter instructorResultAdapter = new InstructorResultAdapter(MainActivity.this, instructorNames);
        lvSearchResultList.setAdapter(instructorResultAdapter);

        Log.wtf(TAG, "now all instructors for the particular course should be displayed in list view");

        lvSearchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                InstructorInfo theInstructor = instructorNames.get(i);
                currentInstructor = theInstructor.getName().toUpperCase();
                currentCourseID = modifiedInput;
                currentInstructorEmail = theInstructor.getEmail();

                Intent detailedViewIntent = new Intent(MainActivity.this, DetailedViewActivity.class);
                detailedViewIntent.putExtra("currentInstructor", currentInstructor);
                detailedViewIntent.putExtra("currentCourseID", currentCourseID);
                detailedViewIntent.putExtra("currentInstructorEmail", currentInstructorEmail);
                //TODO optimize: start a new DetailedViewActivity here
                startActivity(detailedViewIntent);
            }
        });
    }

    /**
     *  If we are looking at instructor's review, then press back goes to the search result.
     *  If we are on the search result, then press back behaves just like before.
     */
    @Override
    public void onBackPressed() {

        if(pbWait.getVisibility() == View.VISIBLE){
            return;
        }

        if (noResult) {
            lvSearchResultList.setVisibility(View.GONE);
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onQueryTextChange(String s) {
        /* track keywords change, maybe useful for search suggestion */
        return false;
    }

    /*hide previously shown result list or no result text view, then show progress bar*/
    private void showProgressBarInOverview(){
        Log.d("PBO", "show");
        if(noResult){
            tvNoResult.setVisibility(View.GONE);
        } else {
            lvSearchResultList.setVisibility(View.GONE);
        }
        pbWait.setVisibility(View.VISIBLE);
    }

    /*show no search result or result list, then hide progress bar*/
    private void hideProgressBarInOverview(String keyword){
        Log.d("PBO", "hide");
        if(noResult){
            String formatKeyword = "\"" + keyword + "\"";
            tvNoResult.setText(getString(R.string.no_result_found_for) + " " +formatKeyword);
            tvNoResult.setVisibility(View.VISIBLE);

        }else{
            lvSearchResultList.setVisibility(View.VISIBLE);
        }
        pbWait.setVisibility(View.GONE);
    }
}
