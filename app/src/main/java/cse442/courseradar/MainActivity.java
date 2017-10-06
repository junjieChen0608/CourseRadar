package cse442.courseradar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import UtilityClass.CourseRating;
import UtilityClass.InstructorDataAdapter;
import UtilityClass.InstructorInfo;
import UtilityClass.InstructorResultAdapter;
import UtilityClass.ReviewInfo;
import UtilityClass.ReviewInfoAdapter;
import UtilityClass.courseData;
import UtilityClass.instructorData;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private FirebaseDatabase searchCourse;
    private ProgressBar pbWait;


    private ListView lvSearchResultList;
    private ListView lvReviewsList;

    // second frame attributes
    private ListView lvInstructorInfo;
    private Button btnClickToRate;


    private static final String INSTRUCTORS = "instructors";
    private DatabaseReference instructorDB;

    private TextView instructorReview;


    private TextView tvNoResult;
    private boolean noResult;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);
        unlockDrawer();
        svSearchBar = (SearchView) findViewById(R.id.sv_search_bar);
        svSearchBar.setIconifiedByDefault(false);
        svSearchBar.setOnQueryTextListener(this);
        pbWait = (ProgressBar) findViewById(R.id.pb_wait);

        /*TODO implement: initialize a new fragment when click listview item*/
        lvSearchResultList = (ListView) findViewById(R.id.lv_instructorData);

        lvInstructorInfo = (ListView) findViewById(R.id.lv_instructor_info);

        tvNoResult = (TextView) findViewById(R.id.tv_no_result);
        noResult = true;
        searchCourse= FirebaseDatabase.getInstance();



        instructorDB = FirebaseDatabase.getInstance().getReference().child(INSTRUCTORS);
        instructorReview = (TextView) findViewById(R.id.tv_instructor_review);
        btnClickToRate = (Button) findViewById(R.id.btn_click_to_rate);
        lvReviewsList = (ListView) findViewById(R.id.lv_reviews_list);


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
        input = input.trim();
        final String keyword = input;
        showProgressBar();
        Log.d(TAG, "search this: " + input);
        //TODO to improve the illegal input detection
        if(input!= null && !input.isEmpty()&& input.matches("\\w+")){
            input= input.toUpperCase();
            String courseAbbr= "";
            for (int i= 0; i<input.length(); i++){
                if (input.charAt(i)>=65 &&input.charAt(i)<= 90){
                    courseAbbr= input.substring(0, i+1);
                }
                else{
                    break;
                }
            }

            final String modifiedInput = input;

            searchCourse.getReference(courseAbbr).child(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    courseData resultCourse= dataSnapshot.getValue(courseData.class);
                    /*here it gets the courseData, it has a hashmap to store instructors and their email
                    * take those to form list*/
                    //TODO construct the instructor list for the course

                    noResult = (resultCourse == null);
                    hideProgressBar(keyword);

                    if (! noResult) {

                        Log.d("search test", resultCourse.getCredit());
                        Log.d("search test", resultCourse.getInstructor().toString());


                        HashMap<String, String> instructors = resultCourse.getInstructor();
                        final ArrayList<InstructorInfo> instructorNames = new ArrayList<InstructorInfo>();
                        for (String s : instructors.keySet()) {
                            instructorNames.add(new InstructorInfo(s, instructors.get(s)));
                        }
                        InstructorResultAdapter instructorResultAdapter = new InstructorResultAdapter(MainActivity.this, instructorNames);
                        lvSearchResultList.setAdapter(instructorResultAdapter);
                        lvSearchResultList.setVisibility(View.VISIBLE);
                        lvSearchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                InstructorInfo theInstructor = instructorNames.get(i);
                                Toast.makeText(MainActivity.this, theInstructor.getName() + " " + theInstructor.getEmail(), Toast.LENGTH_SHORT).show();

                                // make the search fragment invisible
                                lvSearchResultList.setVisibility(View.GONE);
                                svSearchBar.setVisibility(View.GONE);

                                // make the result fragment visible
                                showInstructorInfo(theInstructor.getName().toUpperCase(), modifiedInput);

                                //Toast.makeText(MainActivity.this, ((TextView)view.findViewById(R.id.tv_name)).getText().toString() + ((TextView)view.findViewById(R.id.tv_email)).getText(),
                                //Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("onCancelled", "activited");
                }
            });
        }
        return false;
    }


    /**
     *  After student chose course number, and click on the instructor,
     *  here we show the reviews for the chosen instructor on chosen course.
     *
     *  TODO: need a better design, instead of a single TextView
     *
     * @param instructorName, already in uppercase, eg: "CARL ALPHANCE"
     * @param courseID, uppcase, and no sapce, eg: "CSE250", "CSE115"
     */
    private void showInstructorInfo(final String instructorName, final String courseID) {
        instructorDB.child(instructorName).child("courses").child(courseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CourseRating courseRating =  dataSnapshot.getValue(CourseRating.class);
                Toast.makeText(MainActivity.this, "Inside showInstructorInfo", Toast.LENGTH_SHORT).show();

                if (courseRating != null) {

                    HashMap<String, Object> hashMap = courseRating.toMap();
                    int assignmentDifficulty = (int) hashMap.get("assignmentDifficulty");
                    int lectureQuality = (int) hashMap.get("lectureQuality");
                    int overallQuality = (int) hashMap.get("overallQuality");
                    int totalRatings = (int) hashMap.get("totalRatings");

                    instructorReview.setVisibility(View.VISIBLE);
                    instructorReview.setText(   "Course: " + courseID + "\n" +
                                                "Instructor: " + instructorName + "\n" +
                                                "Assignment Difficult: " + assignmentDifficulty + "\n" +
                                                "Lecture Quality: " + lectureQuality + "\n" +
                                                "Overall Quality: "+ overallQuality + "\n" +
                                                "Total Ratings: " + totalRatings);


                    showReviewsForThisInstructor(instructorName, courseID);

                    Log.wtf(TAG, "visibility of lvSearchResult: " + lvSearchResultList.getVisibility() + " " + View.VISIBLE + " " + View.GONE);



                    btnClickToRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MainActivity.this, RatingActivity.class);
                            intent.putExtra("instructorName", instructorName);
                            intent.putExtra("courseID", courseID);
                            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

                        }
                    });
                    btnClickToRate.setVisibility(View.VISIBLE);



                    Log.wtf(TAG, "found something for instructor: " + instructorName + " on course: " + courseID);
                    //Toast.makeText(MainActivity.this, "successfully found something", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this, "total ratings: " + totalRatings, Toast.LENGTH_SHORT).show();
                } else {
                    Log.wtf(TAG, "found nothing for instructor: " + instructorName + " on course: " + courseID);
                    //Toast.makeText(MainActivity.this, "found nothing", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this, instructorName + " " + courseID, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     *  If we are looking at instructor's review, then press back goes to the search result.
     *  If we are on the search result, then press back behaves just like before.
     */
    @Override
    public void onBackPressed() {

        if (noResult) {
            lvSearchResultList.setVisibility(View.GONE);
            super.onBackPressed();
        } else if (lvSearchResultList.getVisibility() == View.GONE) {
            instructorReview.setVisibility(View.GONE);
            btnClickToRate.setVisibility(View.GONE);
            lvReviewsList.setVisibility(View.GONE);

            lvSearchResultList.setVisibility(View.VISIBLE);
            svSearchBar.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }

    }


    /**
     * TODO: redesign the interface to make it look better
     *
     * @param instructorName, already in uppercase, eg: "CARL ALPHANCE"
     * @param courseID, uppercase, and no space, eg: "CSE250", "CSE115"
     */
    private void showReviewsForThisInstructor(final String instructorName, final String courseID) {

        Log.wtf(TAG, "on show reviews for this instructor");

        instructorDB.child(instructorName).child("reviews").child(courseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, String> reviews = (HashMap<String, String>) dataSnapshot.getValue();
                ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();
                for (HashMap.Entry<String, String> entry : reviews.entrySet()) {
                    String name = entry.getKey();
                    String review = entry.getValue();
                    Log.wtf(TAG, "name and review: " + name + ",  " + review);
                    ReviewInfo reviewInfo = new ReviewInfo(name, review);
                    reviewInfos.add(reviewInfo);
                }

                ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(MainActivity.this, reviewInfos);
                lvReviewsList.setAdapter(reviewInfoAdapter);
                lvReviewsList.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onQueryTextChange(String s) {
        /* track keywords change, maybe useful for search suggestion */
        return false;
    }

    /*hide previously shown result list or no result text view, then show progress bar*/
    private void showProgressBar(){
        if(noResult){
            tvNoResult.setVisibility(View.GONE);
        }

        else {
            lvSearchResultList.setVisibility(View.GONE);
        }
        pbWait.setVisibility(View.VISIBLE);
    }

    /*show no search result or result list, then hide progress bar*/
    private void hideProgressBar(String keyword){
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
