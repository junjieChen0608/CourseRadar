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
    private static final String COURSES= "courses";
    private static final String RATINGS = "ratings";

    private DatabaseReference courseDB;
    private DatabaseReference instructorDB;
    private DatabaseReference ratingsDB;

    private TextView tvInstructorName, tvCourseID, tvOverallQuality, tvLectureQuality, tvAssignmentDifficulty;
    private ImageView ivInstructorPhoto;
    private AlertDialog signInAlertDialog;
    private Button btnClickToRate;
    private ListView lvReviewsList;
    private String currentInstructor;
    private String currentCourseID;
    private String currentInstructorEmail;
    private int countInstructors, countReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        courseDB= FirebaseDatabase.getInstance().getReference(COURSES);
        instructorDB = FirebaseDatabase.getInstance().getReference(INSTRUCTORS);
        ratingsDB = FirebaseDatabase.getInstance().getReference(RATINGS);

        /*detailed view UI elements initialization*/
        //TODO optimized: pass these information to the new DetailedViewActivity
        tvInstructorName = findViewById(R.id.tv_instructor_name);
        tvCourseID = findViewById(R.id.tv_course_id);
        tvOverallQuality = findViewById(R.id.tv_overall_rating);
        tvLectureQuality = findViewById(R.id.tv_lecture_rating);
        tvAssignmentDifficulty = findViewById(R.id.tv_assignment_difficulty);
        ivInstructorPhoto = findViewById(R.id.iv_instructor_photo);
        btnClickToRate = (Button) findViewById(R.id.btn_click_to_rate);
        lvReviewsList = (ListView) findViewById(R.id.lv_reviews_list);

        AlertDialog.Builder builder = new AlertDialog.Builder(DetailedViewActivity.this);
        builder.setMessage("Do you want to sign to rate?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent landingIntent = new Intent(DetailedViewActivity.this, LandingActivity.class);
                landingIntent.putExtra("source", TAG);
                startActivity(landingIntent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signInAlertDialog.hide();
            }
        });
        signInAlertDialog = builder.create();

        Bundle extra = this.getIntent().getExtras();
        if(extra != null){
            currentInstructor = extra.getString("currentInstructor");
            currentCourseID = extra.getString("currentCourseID");
            currentInstructorEmail = extra.getString("currentInstructorEmail");
            showInstructorInfo(currentInstructor, currentCourseID);
        }

    }

    /**
     *  After student chose course number, and click on the instructor,
     *  here we show the reviews for the chosen instructor on chosen course.
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

                    btnClickToRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                                signInAlertDialog.show();
                            } else {
                                Intent intent = new Intent(DetailedViewActivity.this, RatingActivity.class);
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
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    /*helper function to calculate average score*/
    private double calculateAvgScore(int totalScore, int numOfScores){
        double ret = (numOfScores == 0) ? 0.0 : (totalScore * 1.0)/ numOfScores;
        ret = Double.parseDouble(String.format("%.1f", ret));
        return ret;
    }

    /**
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
                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(DetailedViewActivity.this, reviewInfos);
                    lvReviewsList.setAdapter(reviewInfoAdapter);
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
                                //if the last element, then show this!
                                if (numReviews == countReviews) {
                                    ReviewInfoAdapter reviewInfoAdapter = new ReviewInfoAdapter(DetailedViewActivity.this, reviewInfos);
                                    lvReviewsList.setAdapter(reviewInfoAdapter);
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
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }
}
