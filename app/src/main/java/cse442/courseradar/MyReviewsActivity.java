package cse442.courseradar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import UtilityClass.MyReviewsAdapter;
import UtilityClass.UBITValidation;

public class MyReviewsActivity extends AppCompatActivity {

    private static final String TAG = MyReviewsActivity.class.getSimpleName();
    private static final String RATINGS = "ratings";
    private static final String OVERALL = "overallQuality";
    private static final String LECTURE = "lectureQuality";
    private static final String ASSIGNMENT = "assignmentDifficulty";
    private static final String COMMENT = "comment";

    private ExpandableListView elvMyReviews;
    private MyReviewsAdapter reviewsAdapter;
    private AlertDialog changeReviewsDialog;
    private DatabaseReference ratingDB;
    String instructorName;
    String courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        /*
            build a YES/NO dialog, so when user click on child item
            it will show up to ask user if he/she wants to change reviews
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to change review to this instructor?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO implement: launch a new RatingActivity
                Intent changeReviewIntent = new Intent(MyReviewsActivity.this, RatingActivity.class);
                changeReviewIntent.putExtra("instructorName", instructorName);
                changeReviewIntent.putExtra("courseID", courseID);
                startActivity(changeReviewIntent);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeReviewsDialog.hide();
            }
        });
        changeReviewsDialog = builder.create();

        elvMyReviews = findViewById(R.id.elv_my_reviews);
        // set onClickListener for each child item, so user can click on child item to modify each review
        elvMyReviews.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPos, int childPos, long l) {
                String parentHeader = (String)reviewsAdapter.getGroup(groupPos);
                instructorName = parentHeader.substring(0, parentHeader.indexOf("-"));
                courseID = parentHeader.substring(parentHeader.indexOf("-")+1);
                Log.d("ELV", "instructor: " + instructorName + "\ncourseID: " + courseID);
                changeReviewsDialog.show();
                return false;
            }
        });
        setChildIndicatorToRight();
        String userUBIT = UBITValidation.parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        ratingDB = FirebaseDatabase.getInstance().getReference(RATINGS).child(userUBIT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ELV", "onStart");
        populateMyReviewsList();
    }

    /* populate the my reviews listview with this user's review on firebase */
    private void populateMyReviewsList(){
        ratingDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // fetch and cast the review from firebase to a HashMap
                    HashMap<String, HashMap<String, Object>> reviewsMap = (HashMap<String, HashMap<String, Object>>)dataSnapshot.getValue();
                    // this header ArrayList is a list of user's review heading (i.e. CALR ALPHONCE-CSE115, it will then split by '-')
                    ArrayList<String> header = new ArrayList<String>(reviewsMap.keySet());
                    // this child HashMap maps each heading to a detailed review (i.e. scoring and comment)
                    HashMap<String, ArrayList<String>> child = new HashMap<String, ArrayList<String>>();
                    Log.d("adapter", "size of header: " + header.size());
                    // iterate on the heading, populate the child map
                    for(String key : header){
                        Log.d("adapter", "checking reviews for: " + key);
                        HashMap<String, Object> eachReview = reviewsMap.get(key);
                        /*
                            need to convert the detailed review map to list
                            because in the Adapter, all data are indexed by number, not string
                         */
                        child.put(key, convertMapToList(eachReview));
                    }
                    // initialize the ExpandableListView adapter
                    reviewsAdapter = new MyReviewsAdapter(MyReviewsActivity.this, header, child);
                    elvMyReviews.setAdapter(reviewsAdapter);
                } else {
                    Log.d("ELV", "this user has no review");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    /* convert the given map to ArrayList<String> each index has a specific content to hold */
    private ArrayList<String> convertMapToList(HashMap<String, Object> map){
        ArrayList<String> ret = new ArrayList<>(Arrays.asList("", "", "", ""));
        for(String key : map.keySet()){
            // 0: overall, 1: lecture, 2: assignment, 3: comment
            switch (key){
                case OVERALL:
                    ret.set(0, String.valueOf(map.get(key)));
                    break;
                case LECTURE:
                    ret.set(1, String.valueOf(map.get(key)));
                    break;
                case ASSIGNMENT:
                    ret.set(2, String.valueOf(map.get(key)));
                    break;
                case COMMENT:
                    ret.set(3, (String)map.get(key));
                    break;
            }
        }

        return ret;
    }

    /* set the child indicator to the right */
    private void setChildIndicatorToRight(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        elvMyReviews.setIndicatorBoundsRelative(width - getDipsFromPixel(35), width - getDipsFromPixel(5));
    }

    private int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }
}
