package cse442.courseradar;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import UtilityClass.InstructorData;
import UtilityClass.MyReviewsAdapter;
import UtilityClass.UBITValidation;

public class MentionMeActivity extends AppCompatActivity {
    private static final String TAG = MyReviewsActivity.class.getSimpleName();
    private ListView lvMyCourses;

    private static final String INSTRUCTORS= "instructors";
    private static final String EMAIL= "email";
    private DatabaseReference instructorDB;

    public String myEmail;
    String instructorName;
    String courseID;

    private boolean noResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mention_me);

        myEmail= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        instructorDB= FirebaseDatabase.getInstance().getReference(INSTRUCTORS);

        lvMyCourses= findViewById(R.id.lv_my_courses);
    }

    @Override
    protected void onStart(){
        super.onStart();
        populateMyCoursesList();

    }

    private void populateMyCoursesList(){
        instructorDB.orderByChild(EMAIL).equalTo(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InstructorData resultInstructor= dataSnapshot.getValue(InstructorData.class);
                noResult = (resultInstructor == null);
                if(!noResult){
                    if (resultInstructor.email == null) {
                        Log.d(TAG, dataSnapshot.getKey());
                        Log.d(TAG, "email is null");
                    } else {
                        Log.e(TAG, resultInstructor.email);
                        ArrayList<String> courseList= new ArrayList<>();
                        for(String course: resultInstructor.courses.keySet()){
                            courseList.add(course);
                        }
                        ArrayAdapter<String> coursesResultAdapter= new ArrayAdapter<String>(MentionMeActivity.this,
                                android.R.layout.simple_list_item_1,
                                courseList);
                        lvMyCourses.setAdapter(coursesResultAdapter);
                    }
                }
                else{
                    Log.d(TAG, "not a professor");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
