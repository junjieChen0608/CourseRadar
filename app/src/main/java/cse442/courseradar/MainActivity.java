package cse442.courseradar;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

import UtilityClass.InstructorInfo;
import UtilityClass.InstructorResultAdapter;
import UtilityClass.CourseData;


public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private ProgressBar pbWait;

    /*search overview elements*/
    private ListView lvSearchResultList;
    private TextView tvNoResult;
    private boolean noResult;

    /*search detail view elements*/
    private static final String INSTRUCTORS = "instructors";
    private static final String COURSES= "courses";
    private static final String RATINGS = "ratings";

    private DatabaseReference courseDB, instructorDB;

    private String checkedCourseID;

    private int countInstructors;

    private String currentInstructor, currentCourseID, currentInstructorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);

        svSearchBar = (SearchView) findViewById(R.id.sv_search_bar);
        svSearchBar.setIconifiedByDefault(false);
        svSearchBar.setOnQueryTextListener(this);
        pbWait = (ProgressBar) findViewById(R.id.pb_wait);

        lvSearchResultList = (ListView) findViewById(R.id.lv_instructorData);

        tvNoResult = (TextView) findViewById(R.id.tv_no_result);
        noResult = true;

        courseDB= FirebaseDatabase.getInstance().getReference(COURSES);
        instructorDB = FirebaseDatabase.getInstance().getReference(INSTRUCTORS);
        unlockDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf("avatar","MainActivity onStart");
        /*mark this activity as current activity*/
        currentActivity = this;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.wtf(TAG + " onStart", "user is signed out");
        }
//        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
        if(checkedCourseID != null){
            onQueryTextSubmit(checkedCourseID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.wtf("avatar","MainActivity onResume");
        /* update drawer UI from here to wait for the setFromLocal flag to be set */
        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        input = input.replaceAll(" ", ""); // remove all spaces in the keyword
        final String keyword = input;
        showProgressBarInOverview();
        Log.d(TAG, "search this: " + input);
        //improve the illegal input detection
        if(input != null && !input.isEmpty() && input.matches("\\w+")){
            input = input.toUpperCase();
            final String capitalizedCourseID = input;
            checkedCourseID = capitalizedCourseID;

            /* lookup course databse for this course */
            courseDB.child(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CourseData resultCourse = dataSnapshot.getValue(CourseData.class);

                    noResult = (resultCourse == null);

                    if (!noResult) {
                        // found result for given keyword
                        Log.d("search test", resultCourse.getCredit());
                        Log.d("search test", resultCourse.getInstructor().toString());

                        HashMap<String, String> instructors = resultCourse.getInstructor(); // this map has all the instructors that teach this course
                        final ArrayList<InstructorInfo> instructorNames = new ArrayList<InstructorInfo>(); // list that used in search result ListView

                        final int numInstructors = instructors.size();
                        countInstructors = 0;

                        /* now iterate on the instructor map to find each instructor's review of this course*/
                        for (String s : instructors.keySet()) {
                            final String eachInstructorName = s;
                            final String eachInstructorEmail = instructors.get(s);

                            //no need for email, instead we should have an review overview.
                            instructorDB.child(s.toUpperCase()).child("courses").child(capitalizedCourseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, Long> totalRatingInfo = (HashMap<String, Long>) dataSnapshot.getValue();
                                    instructorNames.add(new InstructorInfo(eachInstructorName, eachInstructorEmail, totalRatingInfo));
                                    Log.wtf(TAG, "in search result, current instructor name is: " + eachInstructorName);
                                    Log.wtf(TAG, "instructor name size: " + instructorNames.size());
                                    countInstructors += 1;
                                    if (countInstructors == numInstructors) {
                                        /*
                                            iteration complete, display search result
                                            the reason why use a counter to identify end of iteration is due to asynchronous nature of firebase callback
                                         */
                                        displayInstructorsForThisCourse(instructorNames, capitalizedCourseID);
                                        hideProgressBarInOverview(keyword);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                        }
                    }else{
                        // legal keyword, no result found
                        hideProgressBarInOverview(keyword);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("onCancelled", "activited");
                }
            });
        }else{
            // illegal keyword, no result found
            noResult = true;
            hideProgressBarInOverview(keyword);
            Toast.makeText(this, "we only accept charecters and numbers", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /*
        display instructors in the ListView
        set up click listener for each ListView item
    */
    private void displayInstructorsForThisCourse(final ArrayList<InstructorInfo> instructorNames, final String capitalizedCourseID) {
        Log.wtf(TAG, "found all instructors for this courses");
        InstructorResultAdapter instructorResultAdapter = new InstructorResultAdapter(MainActivity.this, instructorNames);
        lvSearchResultList.setAdapter(instructorResultAdapter);

        Log.wtf(TAG, "now all instructors for the particular course should be displayed in list view");

        lvSearchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                InstructorInfo theInstructor = instructorNames.get(i);
                currentInstructor = theInstructor.getName().toUpperCase();
                currentCourseID = capitalizedCourseID;
                currentInstructorEmail = theInstructor.getEmail();

                Intent detailedViewIntent = new Intent(MainActivity.this, DetailedViewActivity.class);
                detailedViewIntent.putExtra("currentInstructor", currentInstructor);
                detailedViewIntent.putExtra("currentCourseID", currentCourseID);
                detailedViewIntent.putExtra("currentInstructorEmail", currentInstructorEmail);
                /*
                    start a new DetailedViewActivity here, SINGLE_TOP flag will prevent
                    fast click invoke multiple instances
                 */
                startActivity(detailedViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(pbWait.getVisibility() == View.VISIBLE){
            // while loading, press back will do nothing
            return;
        }
        super.onBackPressed();
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
