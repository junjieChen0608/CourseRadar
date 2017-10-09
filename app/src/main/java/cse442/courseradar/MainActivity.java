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


/**
 * TODO: UI design
 * TODO: update reviews when user finishes review
 *
 */



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
    private ConstraintLayout clSearchDetailedView, clInstructorOverview;
    private TextView tvInstructorName, tvCourseID, tvOverallQuality, tvLectureQuality, tvAssignmentDifficulty;
    private ImageView ivInstructorPhoto;
    private Button btnClickToRate;
    private ListView lvReviewsList;

    private static final String INSTRUCTORS = "instructors";
    private static final String COURSES= "courses";
    private static final String RATINGS = "ratings";
    private DatabaseReference courseDB;

    private DatabaseReference instructorDB;
    private DatabaseReference ratingsDB;

    private TextView instructorReview;

    private AlertDialog alertDialog;

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
        clSearchDetailedView = (ConstraintLayout) findViewById(R.id.search_detailed_view);
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

        /*detailed view UI elements initialization*/
        clInstructorOverview = findViewById(R.id.instructor_overview);
        tvInstructorName = findViewById(R.id.tv_instructor_name);
        tvCourseID = findViewById(R.id.tv_course_id);
        tvOverallQuality = findViewById(R.id.tv_overall_rating);
        tvLectureQuality = findViewById(R.id.tv_lecture_rating);
        tvAssignmentDifficulty = findViewById(R.id.tv_assignment_difficulty);
        ivInstructorPhoto = findViewById(R.id.iv_instructor_photo);
        btnClickToRate = (Button) findViewById(R.id.btn_click_to_rate);
        lvReviewsList = (ListView) findViewById(R.id.lv_reviews_list);

        //

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to sign to rate?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.hide();
            }
        });

        alertDialog = builder.create();

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

        if (clSearchOverview.getVisibility() == View.GONE) {
            showInstructorInfo(currentInstructor, currentCourseID);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        input = input.trim();
        input = input.replaceAll(" ", "");
        final String keyword = input;
        showProgressBarInOverview();
        Log.d(TAG, "search this: " + input);
        //TODO to improve the illegal input detection
        if(input!= null && !input.isEmpty()&& input.matches("\\w+")){
            input= input.toUpperCase();
            final String modifiedInput = input;

            courseDB.child(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    courseData resultCourse= dataSnapshot.getValue(courseData.class);
                    /*here it gets the courseData, it has a hashmap to store instructors and their email
                    * take those to form list*/
                    //TODO construct the instructor list for the course

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

                            // TODO: no need for email, instead we should have an review overview.
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
                showProgressBarInDetailedView();

                clSearchOverview.setVisibility(View.GONE);
                clSearchDetailedView.setVisibility(View.VISIBLE);


                InstructorInfo theInstructor = instructorNames.get(i);

                                /*TODO optimize: make the search overview gone*/
//                                lvSearchResultList.setVisibility(View.GONE);
//                                svSearchBar.setVisibility(View.GONE);

                                /*TODO optimize: make the detailed view visible*/
                currentInstructor = theInstructor.getName().toUpperCase();
                currentCourseID = modifiedInput;
                currentInstructorEmail = theInstructor.getEmail();
                showInstructorInfo(currentInstructor, currentCourseID);

            }
        });
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

                if (courseRating != null) {

                    HashMap<String, Object> hashMap = courseRating.toMap();
                    int totalRatings = (int) hashMap.get("totalRatings");
                    double overallQuality = calculateAvgScore((int) hashMap.get("overallQuality"), totalRatings);
                    double lectureQuality = calculateAvgScore((int) hashMap.get("lectureQuality"), totalRatings);
                    double assignmentDifficulty = calculateAvgScore((int) hashMap.get("assignmentDifficulty"), totalRatings);

                    /*TODO optimize: overhaul the UI design, and make it visible*/
                    /*update UI element in detailed view */
                    tvInstructorName.setText(instructorName);
                    tvCourseID.setText(courseID);
                    tvOverallQuality.setText(getString(R.string.overall_quality) + " " + overallQuality);
                    tvLectureQuality.setText(getString(R.string.lecture_quality) + " " + lectureQuality);
                    tvAssignmentDifficulty.setText(getString(R.string.assignment_difficulty) + " " + assignmentDifficulty);
                    /*check firebase storage to show this instructor's profile photo*/
                    Log.d(TAG, "current instructor UBIT: " + parseUBIT(currentInstructorEmail));
                    FirebaseStorage.getInstance().getReference().child("avatar/"+parseUBIT(currentInstructorEmail))
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Picasso", "found photo for" + instructorName);
                            Picasso.with(MainActivity.this).load(uri).into(ivInstructorPhoto);
                        }
                    });

                    showReviewsForThisInstructor(instructorName, courseID);

                    Log.wtf(TAG, "visibility of lvSearchResult: " + lvSearchResultList.getVisibility() + " " + View.VISIBLE + " " + View.GONE);

                    btnClickToRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                                alertDialog.show();
                            } else {
                                Intent intent = new Intent(MainActivity.this, RatingActivity.class);
                                intent.putExtra("instructorName", instructorName);
                                intent.putExtra("courseID", courseID);
                                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                            }
                        }
                    });

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

    /*helper function to calculate average score*/
    private double calculateAvgScore(int totalScore, int numOfScores){
        double ret = (numOfScores == 0) ? 0.0 : (totalScore * 1.0)/ numOfScores;
        ret = Double.parseDouble(String.format("%.1f", ret));
        return ret;
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
                final ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();

                // because there is always a dummy node, so the real size is 1 lesser
                final int numReviews = reviews.size() - 1;

                if (numReviews == 0) {
                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(MainActivity.this, reviewInfos);
                    lvReviewsList.setAdapter(reviewInfoAdapter);
                    hideProgressBarInDetailedView();
                } else {
                    countReviews = 0;
                    for (HashMap.Entry<String, String> entry : reviews.entrySet()) {
                        final String name = entry.getKey();
                        if (name.equals("dummy")) {
                            continue;
                        }
                        ratingsDB.child(name).child(instructorName + "-" + courseID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                countReviews += 1;
                                HashMap<String, Object> theStudentReviewDetail = (HashMap<String, Object>) dataSnapshot.getValue();
                                ReviewInfo reviewInfo = new ReviewInfo(name, theStudentReviewDetail);
                                reviewInfos.add(reviewInfo);
                                Log.wtf(TAG, "counter reviews is: " + countReviews +" instructor name is: " + name);
                                //TODO if the last element, then show this!
                                if (numReviews == countReviews) {
                                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(MainActivity.this, reviewInfos);
                                    lvReviewsList.setAdapter(reviewInfoAdapter);
                                    hideProgressBarInDetailedView();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
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

        if(pbWait.getVisibility() == View.VISIBLE){
            return;
        }

        if (noResult) {
            lvSearchResultList.setVisibility(View.GONE);
            super.onBackPressed();
        } else if (clSearchOverview.getVisibility() == View.GONE) {
            /*TODO optimize: make search detailed view gone*/
            ivInstructorPhoto.setImageResource(R.drawable.pic_holder);
            Log.d("vis", "onBackPressed: clSearchDetailedView GONE");
            clSearchDetailedView.setVisibility(View.GONE);
//            instructorReview.setVisibility(View.GONE);
//            btnClickToRate.setVisibility(View.GONE);
//            lvReviewsList.setVisibility(View.GONE);

            /*TODO optimize: make search overview visible*/
//            lvSearchResultList.setVisibility(View.VISIBLE);
//            svSearchBar.setVisibility(View.VISIBLE);

            onQueryTextSubmit(currentCourseID);
            clSearchOverview.setVisibility(View.VISIBLE);

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

    private void showProgressBarInDetailedView(){
        Log.d("PBD", "show");
        clInstructorOverview.setVisibility(View.GONE);
        lvReviewsList.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarInDetailedView(){
        Log.d("PBD", "hide");
        clInstructorOverview.setVisibility(View.VISIBLE);
        lvReviewsList.setVisibility(View.VISIBLE);
        pbWait.setVisibility(View.GONE);
    }
}
