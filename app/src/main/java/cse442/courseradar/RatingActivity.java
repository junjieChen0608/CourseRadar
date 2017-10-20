package cse442.courseradar;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import UtilityClass.CourseRating;
import UtilityClass.ReviewInfo;

public class RatingActivity extends AppCompatActivity {

    private static final String TAG = RatingActivity.class.getSimpleName();
    private static final String RATINGS = "ratings";
    private static final String INSTRUCTORS = "instructors";
    private static final String RETURN_DATA = "new rating";
    private static final int RESULT_NO_NEW_RATING = 100, RESULT_NEW_RATING = 200;

    private EditText etComment;
    private TextView tvInstructorName, tvCourseID;
    private RatingBar rbOverallQuality, rbLectureQuality, rbAssignmentDiff;
    private Button btnSubmit;
    private DatabaseReference instructorDB, ratingDB;
    private String userUBIT, instructorName, courseID, comment;
    private int overallQuality, lectureQuality, assignmentDiff;
    private ReviewInfo reviewToUpdate;

    /*rating database related strings*/
    private int userOverallQuality, userLectureQuality, userAssignmentDiff,
                instructorOverallQuality, instructorLectureQuality, instructorAssignmentDiff, instructorTotalRatings;

    @Override
    public void onBackPressed() {
        // indicate no new review is made
        setResult(RESULT_NO_NEW_RATING);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        /*initialize UI elements*/
        tvInstructorName = (TextView) findViewById(R.id.tv_instructor_name);
        tvInstructorName.setText(getIntent().getStringExtra("instructorName"));
        tvCourseID = (TextView) findViewById(R.id.tv_course_id);
        tvCourseID.setText(getIntent().getStringExtra("courseID"));
        etComment = (EditText) findViewById(R.id.et_comment);


        rbOverallQuality = (RatingBar) findViewById(R.id.rb_overall_quality);
        rbLectureQuality = (RatingBar) findViewById(R.id.rb_lecture_quality);
        rbAssignmentDiff = (RatingBar) findViewById(R.id.rb_assignment_diff);

        userUBIT = parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        instructorDB = FirebaseDatabase.getInstance().getReference().child(INSTRUCTORS);
        ratingDB = FirebaseDatabase.getInstance().getReference().child(RATINGS);

        /*initialize user made ratings to -1 to indicate whether this user has rated before*/
        userOverallQuality = -1;
        userLectureQuality = -1;
        userAssignmentDiff = -1;

        btnSubmit = (Button) findViewById(R.id.btn_submit_rate);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructorName = tvInstructorName.getText().toString();
                courseID = tvCourseID.getText().toString();
                comment = etComment.getText().toString();

                overallQuality = (int) rbOverallQuality.getRating();
                lectureQuality = (int) rbLectureQuality.getRating();
                assignmentDiff = (int) rbAssignmentDiff.getRating();

                Log.d(TAG, "Instructor name: " + instructorName + "\n" +
                            "Course ID: " + courseID + "\n" +
                            "Overall: " + overallQuality + "\n" +
                            "Lecture: " + lectureQuality + "\n" +
                            "Assignment Difficulty: " + assignmentDiff + "\n" +
                            "Comment: " + comment);
                /*fetch rating data from both user's and instructor's rating database*/
                getRatingDatabase(RATINGS);
                getRatingDatabase(INSTRUCTORS);
            }
        });
    }

    /*update rating to each database*/
    private void setRatingDatabase(final String whichDB){

        if(whichDB.equals(RATINGS)){
            // update this user's rating database
            HashMap<String, Object> newRating = new HashMap<>();
            newRating.put("assignmentDifficulty", assignmentDiff);
            newRating.put("lectureQuality", lectureQuality);
            newRating.put("overallQuality", overallQuality);
            ratingDB.child(userUBIT).child(instructorName+"-"+courseID).updateChildren(newRating);
            HashMap<String, Object> newComment = new HashMap<>();
            newComment.put("comment", comment);
            ratingDB.child(userUBIT).child(instructorName+"-"+courseID).updateChildren(newComment);
            // instantiate a new ReviewInfo object to return back to DetailViewActivity
            reviewToUpdate = new ReviewInfo(userUBIT, newRating);
            Log.d("setter", "setter called at " + whichDB);
        }else{
            // update instructor's rating database
            if(userAssignmentDiff < 0){
                /*this user's rating is negative, which means it has no previous rating
                * just update it to the instructor's rating database, and increment total number of ratings*/
                instructorOverallQuality += overallQuality;
                instructorAssignmentDiff += assignmentDiff;
                instructorLectureQuality += lectureQuality;
                instructorTotalRatings++;
            }else{
                /*this user has previous rating
                * need to calculate the rating difference to update the instructor's rating database*/
                instructorOverallQuality = instructorOverallQuality - userOverallQuality + overallQuality;
                instructorAssignmentDiff = instructorAssignmentDiff - userAssignmentDiff + assignmentDiff;
                instructorLectureQuality = instructorLectureQuality - userLectureQuality + lectureQuality;
            }
            updateInstructorRating();
        }
    }

    /*helper function to update instructor's rating fields*/
    private void updateInstructorRating(){
        CourseRating newRating = new CourseRating(instructorAssignmentDiff, instructorLectureQuality,instructorOverallQuality, instructorTotalRatings);
        instructorDB.child(instructorName).child("courses").child(courseID).updateChildren(newRating.toMap());

        HashMap<String, Object> newComment = new HashMap<>();
        newComment.put(userUBIT, comment);

        instructorDB.child(instructorName).child("reviews").child(courseID).updateChildren(newComment);
        Log.d("setter", "setter called at " + INSTRUCTORS);
        Toast.makeText(RatingActivity.this, "Your rating is submitted!!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "closing activity");
        /*
            set result code to indicate new review is made,
            then send this piece of new review back to DetailedViewActivity
          */
        Intent resultIntent = new Intent();
        Bundle dataBundle = new Bundle();
        dataBundle.putParcelable(RETURN_DATA, reviewToUpdate);
        resultIntent.putExtras(dataBundle);
        setResult(RESULT_NEW_RATING, resultIntent);
        finish();

    }

    /*get rating data from designated database reference*/
    private void getRatingDatabase(final String whichDB){

        ValueEventListener valueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //this user has rated before
                    CourseRating rating = dataSnapshot.getValue(CourseRating.class);
                    Log.d("onDataChange", "whichDB " + whichDB);
                    if(rating != null){
                        if(whichDB.equals(RATINGS)){
                            /*get this user's previous rating*/
                            Log.d("getter", "getter called at " + whichDB);
                            userOverallQuality = rating.overallQuality;
                            userLectureQuality = rating.lectureQuality;
                            userAssignmentDiff = rating.assignmentDifficulty;
                            Log.d("getRatingDatabase", "user previous rating:\n" + userOverallQuality + "\n" +
                                                            userLectureQuality + "\n" +
                                                            userAssignmentDiff + "\n");
                            /*update this user's rating*/
                            setRatingDatabase(RATINGS);
                        }else{
                            /*get this instructor's rating*/
                            Log.d("getter", "getter called at " + whichDB);
                            instructorOverallQuality = rating.overallQuality;
                            instructorLectureQuality = rating.lectureQuality;
                            instructorAssignmentDiff = rating.assignmentDifficulty;
                            instructorTotalRatings = rating.totalRatings;
                            Log.d("getRatingDatabase", "instructor previous rating:\n" + instructorOverallQuality + "\n" +
                                    instructorLectureQuality + "\n" +
                                    instructorAssignmentDiff + "\n" +
                                    rating.totalRatings + "\n");
                            /*update this instructor's rating*/
                            setRatingDatabase(INSTRUCTORS);
                        }
                    }else{
                        Log.d("onDataChange", "rating is null");
                    }
                }else{
                    /*this user has no previous rating on this instructor's course
                    * just update this user's rating database*/
                    Log.d(TAG, "current user has no previous rating on it");
                    setRatingDatabase(RATINGS);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };

        if (whichDB.equals(RATINGS)){
            /*get this user's rating from database*/
            Log.d(TAG, "checking student rating DB");
            ratingDB.child(userUBIT).child(instructorName + "-" + courseID).addListenerForSingleValueEvent(valueEventListener);
        }else {
            /*get this instructor's rating from database*/
            Log.d(TAG, "checking instructor rating DB");
            instructorDB.child(instructorName).child("courses").child(courseID).addListenerForSingleValueEvent(valueEventListener);
        }
    }

    private String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

}
