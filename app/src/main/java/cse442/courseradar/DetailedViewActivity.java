package cse442.courseradar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import UtilityClass.ReviewInfo;
import UtilityClass.ReviewInfoAdapter;

public class DetailedViewActivity extends AppCompatActivity {

    private static final String TAG = DetailedViewActivity.class.getSimpleName();
    private static final String INSTRUCTORS = "instructors";
    private static final String RATINGS = "ratings";
    private static final String MENTIONED_ME= "mentioned me";

    private DatabaseReference instructorDB;
    private DatabaseReference ratingsDB;

    private TextView tvInstructorName, tvCourseID, tvOverallQuality, tvLectureQuality, tvAssignmentDifficulty;
    private ImageView ivInstructorPhoto;
    private AlertDialog signInAlertDialog;
    private Button btnClickToRate;
    private ProgressBar pbReviewListWait;
    private ListView lvReviewsList;
    private String currentInstructor, currentCourseID, currentInstructorEmail, userUBIT;
    private int countReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        pbReviewListWait = findViewById(R.id.pb_review_list_wait);

        instructorDB = FirebaseDatabase.getInstance().getReference(INSTRUCTORS);
        ratingsDB = FirebaseDatabase.getInstance().getReference(RATINGS);

        /*detailed view UI elements initialization*/
        tvInstructorName = findViewById(R.id.tv_instructor_name);
        tvCourseID = findViewById(R.id.tv_course_id);
        tvOverallQuality = findViewById(R.id.tv_overall_rating);
        tvLectureQuality = findViewById(R.id.tv_lecture_rating);
        tvAssignmentDifficulty = findViewById(R.id.tv_assignment_difficulty);
        ivInstructorPhoto = findViewById(R.id.iv_instructor_photo);
        btnClickToRate = (Button) findViewById(R.id.btn_click_to_rate);
        lvReviewsList = (ListView) findViewById(R.id.lv_reviews_list);

        /*
            get this instructor's information from MainActivity intent extra
         */
        Bundle extra = this.getIntent().getExtras();
        if(extra != null){
            currentInstructor = extra.getString("currentInstructor");
            currentCourseID = extra.getString("currentCourseID");
            currentInstructorEmail = extra.getString("currentInstructorEmail");
        }

        if(currentInstructorEmail.endsWith(MENTIONED_ME)){
            btnClickToRate.setVisibility(View.GONE);
            ivInstructorPhoto.setVisibility(View.GONE);
//            currentInstructorEmail = currentInstructorEmail.substring(0, currentInstructorEmail.indexOf(MENTIONED_ME));
        }

        /*
            build the sign in alert dialog for "RATE ME" button
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailedViewActivity.this);
        builder.setMessage("Do you want to sign in to rate?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*
                    if the user decide to log in to rate, pass these extra information
                    to LandingActivity, so that after LandingActivity handled log in logic,
                    it has enough information to start RatingActivity
                 */
                Intent signInIntent = new Intent(DetailedViewActivity.this, LandingActivity.class);
                signInIntent.putExtra("source", TAG);
                signInIntent.putExtra("instructorName", currentInstructor);
                signInIntent.putExtra("courseID", currentCourseID);
                startActivity(signInIntent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signInAlertDialog.hide();
            }
        });
        signInAlertDialog = builder.create();

        /*
            "RATE ME!" button logic
         */
        btnClickToRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    // if the user is not logged in, prompt to ask for log in
                    signInAlertDialog.show();
                } else {
                    // if the user logged in, pass extra information to start a new RatingActivity
                    Intent ratingIntent = new Intent(DetailedViewActivity.this, RatingActivity.class);
                    ratingIntent.putExtra("instructorName", currentInstructor);
                    ratingIntent.putExtra("courseID", currentCourseID);
                    startActivity(ratingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInstructorInfo(currentInstructor, currentCourseID);
    }

    /**
     *
     * Display detailed view for the specific instructor's course,
     * It will initialize the instructor's rating overview
     * then populate the reviews ListView
     *
     * @param instructorName, already in uppercase, eg: "CARL ALPHONCE"
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
                            Picasso.with(DetailedViewActivity.this).load(uri).into(ivInstructorPhoto);
                        }
                    });

                    showReviewsForThisInstructor(instructorName, courseID);

                    Log.wtf(TAG, "found something for instructor: " + instructorName + " on course: " + courseID);
                } else {
                    Log.wtf(TAG, "found nothing for instructor: " + instructorName + " on course: " + courseID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    /*calculate average score*/
    private double calculateAvgScore(int totalScore, int numOfScores){
        double ret = (numOfScores == 0) ? 0.0 : (totalScore * 1.0)/ numOfScores;
        ret = Double.parseDouble(String.format("%.1f", ret));
        return ret;
    }

    /**
     * display all reviews and ratings for this instructor's this course
     *
     * @param instructorName, already in uppercase, eg: "CARL ALPHONCE"
     * @param courseID, uppercase, and no space, eg: "CSE250", "CSE115"
     */
    private void showReviewsForThisInstructor(final String instructorName, final String courseID) {

        Log.wtf(TAG, "on show reviews for this instructor");

        // grab all reviews in this instructor's this course's review section
        instructorDB.child(instructorName).child("reviews").child(courseID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, String> reviews = (HashMap<String, String>) dataSnapshot.getValue(); // all students review
                final ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();

                // because there is always a dummy review, so the real size is 1 lesser
                final int numReviews = reviews.size() - 1;

                if (numReviews == 0) {
                    // no ratings and reviews
                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(DetailedViewActivity.this, reviewInfos);
                    lvReviewsList.setAdapter(reviewInfoAdapter);
                    reviewListReady();
                } else {
                    countReviews = 0;
                    for (HashMap.Entry<String, String> entry : reviews.entrySet()) {
                        final String name = entry.getKey();
                        if (name.equals("dummy")) {
                            continue;
                        }

                        /*
                            lookup this student's detailed review for this instructor-course combination,
                            use it to instantiate a ReviewInfo object,
                            then add this object to reviewInfos list for ListView usage
                         */
                        ratingsDB.child(name).child(instructorName + "-" + courseID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                countReviews += 1;
                                HashMap<String, Object> theStudentReviewDetail = (HashMap<String, Object>) dataSnapshot.getValue();
                                ReviewInfo reviewInfo = new ReviewInfo(name, theStudentReviewDetail);
                                reviewInfos.add(reviewInfo);
                                Log.wtf(TAG, "counter reviews is: " + countReviews +" instructor name is: " + name);

                                /*
                                    use a local counter to detect end of iteration,
                                    a workaround of asynchronous firebase callback
                                 */
                                if (numReviews == countReviews) {
                                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(DetailedViewActivity.this, reviewInfos);
                                    lvReviewsList.setAdapter(reviewInfoAdapter);
                                    reviewListReady();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    // hide the progress bar when the ListView content is available
    private void reviewListReady(){
        pbReviewListWait.setVisibility(View.GONE);
        lvReviewsList.setVisibility(View.VISIBLE);
    }

    private String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }
}
