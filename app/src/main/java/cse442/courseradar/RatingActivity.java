package cse442.courseradar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RatingActivity extends AppCompatActivity {

    private static final String TAG = RatingActivity.class.getSimpleName();
    private EditText etInstructorName, etCouserID, etComment;
    private RatingBar rbOverall, rbLectureQuality, rbAssignmentDiff;
    private Button btnSubmit;
    private DatabaseReference instructorDB, ratingDB;

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

        instructorDB = FirebaseDatabase.getInstance().getReference().child("instructors");
        ratingDB = FirebaseDatabase.getInstance().getReference().child("ratings");

        btnSubmit = (Button) findViewById(R.id.btn_submit_rate);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String instructorName = etInstructorName.getText().toString();
                String courseID = etCouserID.getText().toString();
                String comment = etComment.getText().toString();
                int overallRating = (int) rbOverall.getRating();
                int lectureRating = (int) rbLectureQuality.getRating();
                int assignmentDiff = (int)rbAssignmentDiff.getRating();

                Log.d(TAG, "Instructor name: " + instructorName + "\n" +
                            "Course ID: " + courseID + "\n" +
                            "Overall: " + overallRating + "\n" +
                            "Lecture: " + lectureRating + "\n" +
                            "Assignment Difficulty: " + assignmentDiff + "\n" +
                            "Comment: " + comment);
            }
        });
    }

}
