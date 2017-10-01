package cse442.courseradar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import UtilityClass.CourseRating;

public class RatingActivity extends AppCompatActivity {

    private static final String TAG = RatingActivity.class.getSimpleName();
    private static final String RATINGS = "ratings";
    private static final String INSTRUCTORS = "instructors";

    private EditText etInstructorName, etCouserID, etComment;
    private RatingBar rbOverall, rbLectureQuality, rbAssignmentDiff;
    private Button btnSubmit;
    private DatabaseReference instructorDB, ratingDB;
    private String userUBIT, instructorName, courseID, comment;
    private int overallQuality, lectureQuality, assignmentDiff;

    /*rating database related strings*/
    private int userOverallQuality, userLectureQuality, userAssignmentDiff,
                    instructorOverallQuality, instructorLectureQuality, instructorAssignmentDiff, totalRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        etInstructorName = (EditText) findViewById(R.id.et_instructor_name);
        etCouserID = (EditText) findViewById(R.id.et_course_id);
        etComment = (EditText) findViewById(R.id.et_comment);

        rbOverall = (RatingBar) findViewById(R.id.rb_overall_quality);
        rbLectureQuality = (RatingBar) findViewById(R.id.rb_lecture_quality);
        rbAssignmentDiff = (RatingBar) findViewById(R.id.rb_assignment_diff);

        userUBIT = parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        instructorDB = FirebaseDatabase.getInstance().getReference().child(INSTRUCTORS);
        ratingDB = FirebaseDatabase.getInstance().getReference().child(RATINGS);

        userOverallQuality = -1;
        userLectureQuality = -1;
        userAssignmentDiff = -1;

        btnSubmit = (Button) findViewById(R.id.btn_submit_rate);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructorName = etInstructorName.getText().toString();
                courseID = etCouserID.getText().toString();
                comment = etComment.getText().toString();

                overallQuality = (int) rbOverall.getRating();
                lectureQuality = (int) rbLectureQuality.getRating();
                assignmentDiff = (int)rbAssignmentDiff.getRating();

                Log.d(TAG, "Instructor name: " + instructorName + "\n" +
                            "Course ID: " + courseID + "\n" +
                            "Overall: " + overallQuality + "\n" +
                            "Lecture: " + lectureQuality + "\n" +
                            "Assignment Difficulty: " + assignmentDiff + "\n" +
                            "Comment: " + comment);
                getRatingDatabase(RATINGS);
                getRatingDatabase(INSTRUCTORS);
                setRatingDatabase(RATINGS);
                setRatingDatabase(INSTRUCTORS);
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
        }else{
            // update instructor's rating database
            if(userAssignmentDiff < 0){
                instructorOverallQuality += overallQuality;
                instructorAssignmentDiff += assignmentDiff;
                instructorLectureQuality += lectureQuality;
                totalRatings++;
            }else{
                instructorOverallQuality = instructorOverallQuality - userOverallQuality + overallQuality;
                instructorAssignmentDiff = instructorAssignmentDiff - userAssignmentDiff + assignmentDiff;
                instructorLectureQuality = instructorLectureQuality - userLectureQuality + lectureQuality;
            }
            updateInstructorRating();
        }
    }

    private void updateInstructorRating(){
        CourseRating newRating = new CourseRating(instructorAssignmentDiff, instructorLectureQuality,instructorOverallQuality, totalRatings);
        instructorDB.child(instructorName).child("courses").child(courseID).updateChildren(newRating.toMap());
    }

    /*get rating from each database*/
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
                            userOverallQuality = rating.overallQuality;
                            userLectureQuality = rating.lectureQuality;
                            userAssignmentDiff = rating.assignmentDifficulty;
                            Log.d("getRatingDatabase", "user rating:\n" + userOverallQuality + "\n" +
                                                            userLectureQuality + "\n" +
                                                            userAssignmentDiff + "\n");
                        }else{
                            instructorOverallQuality = rating.overallQuality;
                            instructorLectureQuality = rating.lectureQuality;
                            instructorAssignmentDiff = rating.assignmentDifficulty;
                            totalRatings = rating.totalRatings;
                            Log.d("getRatingDatabase", "instructor rating:\n" + instructorOverallQuality + "\n" +
                                    instructorLectureQuality + "\n" +
                                    instructorAssignmentDiff + "\n");
                        }
                    }else{
                        Log.d("onDataChange", "rating is null");
                    }
                }else{
                    //this user never rated this course before
                    Log.d(TAG, "current user has no previous rating");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };

        if (whichDB.equals(RATINGS)){
            Log.d(TAG, "checking student rating DB");
            ratingDB.child(userUBIT).child(instructorName + "-" + courseID).addListenerForSingleValueEvent(valueEventListener);
        }else {
            Log.d(TAG, "checking instructor rating DB");
            instructorDB.child(instructorName).child("courses").child(courseID).addListenerForSingleValueEvent(valueEventListener);
        }
    }

    private String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

}
