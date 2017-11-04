package cse442.courseradar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import UtilityClass.InstructorData;

public class MentionedMeActivity extends AppCompatActivity {
    private static final String TAG = MyReviewsActivity.class.getSimpleName();
    private static final String INSTRUCTORS= "instructors";

    private static final String MENTIONED_ME= "mentioned me";
    private static final String EMAIL= "email";
    private DatabaseReference instructorDB;
    private String myEmail;

    private String instructorName;
    private String courseID;

    private boolean isInstructor;
    private ListView lvMyCourses;
    private ProgressBar pbMentionedMeWait;
    private TextView tvMentionedMeNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentioned_me);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myEmail= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        instructorDB= FirebaseDatabase.getInstance().getReference(INSTRUCTORS);

        lvMyCourses= findViewById(R.id.lv_mentioned_me_my_courses);
        pbMentionedMeWait = findViewById(R.id.pb_mentioned_me_wait);
        tvMentionedMeNoReviews = findViewById(R.id.tv_mentioned_me_no_reviews);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("lv", "onStart");
        pbMentionedMeWait.setVisibility(View.VISIBLE);
        lvMyCourses.setVisibility(View.GONE);
        tvMentionedMeNoReviews.setVisibility(View.GONE);
        populateMyCoursesList();
    }

    private void populateMyCoursesList(){
        instructorDB.orderByChild(EMAIL).equalTo(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d("lv", "is professor");
                    Iterable<DataSnapshot> professorList= dataSnapshot.getChildren();
                    for (final DataSnapshot professor: professorList){
                        final InstructorData resultInstructor= professor.getValue(InstructorData.class);
                        if(resultInstructor != null){
                            Log.d("lv", "is a professor");
                            final ArrayList<String> courseList= new ArrayList<>();
                            for(String course: resultInstructor.courses.keySet()){
                                courseList.add(course);
                            }
                            ArrayAdapter<String> coursesResultAdapter= new ArrayAdapter<String>(MentionedMeActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    courseList);
                            lvMyCourses.setAdapter(coursesResultAdapter);

                            lvMyCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent detailedViewIntent = new Intent(MentionedMeActivity.this, DetailedViewActivity.class);
                                    detailedViewIntent.putExtra("currentInstructor", professor.getKey());
                                    detailedViewIntent.putExtra("currentCourseID", courseList.get(i));
                                    detailedViewIntent.putExtra("currentInstructorEmail", resultInstructor.email+ MENTIONED_ME);
                                /*
                                    start a new DetailedViewActivity here, SINGLE_TOP flag will prevent
                                    fast click invoke multiple instances
                                 */
                                    Log.d(TAG,professor.getKey()+ "\n"+ courseList.get(i)+ "\n"+ resultInstructor.email+ MENTIONED_ME);
                                    startActivity(detailedViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                                }
                            });
                        }
                    }
                    isInstructor = true;
                }else{
                    isInstructor = false;
                    Log.d("lv", "not professor");
                }
                loadComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void loadComplete() {
        pbMentionedMeWait.setVisibility(View.GONE);
        if(isInstructor){
            lvMyCourses.setVisibility(View.VISIBLE);
        }else{
            tvMentionedMeNoReviews.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
