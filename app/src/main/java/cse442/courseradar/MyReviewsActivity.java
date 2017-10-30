package cse442.courseradar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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
    private DatabaseReference ratingDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        elvMyReviews = findViewById(R.id.elv_my_reviews);
        setChildIndicatorToRight();
        String userUBIT = UBITValidation.parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        ratingDB = FirebaseDatabase.getInstance().getReference(RATINGS).child(userUBIT);

        ratingDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    HashMap<String, HashMap<String, Object>> reviewsMap = (HashMap<String, HashMap<String, Object>>)dataSnapshot.getValue();
                    ArrayList<String> header = new ArrayList<String>(reviewsMap.keySet());
                    HashMap<String, ArrayList<String>> child = new HashMap<String, ArrayList<String>>();
                    Log.d("adapter", "size of header: " + header.size());
                    for(String key : header){
                        Log.d("adapter", "checking reviews for: " + key);
                        HashMap<String, Object> eachReview = reviewsMap.get(key);
                        child.put(key, convertMapToList(eachReview));

//                        Long overall = (Long)eachReview.get(OVERALL);
//                        Long lecture = (Long)eachReview.get(LECTURE);
//                        Long assignment = (Long)eachReview.get(ASSIGNMENT);
//                        String comment = (String)eachReview.get(COMMENT);
//                        Log.d("reviews", key + "\n" + overall + "\n" + lecture + "\n" + assignment + "\n" + comment);
                    }
                    MyReviewsAdapter reviewsAdapter = new MyReviewsAdapter(MyReviewsActivity.this, header, child);
                    elvMyReviews.setAdapter(reviewsAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    /* convert the given map to ArrayList<String> each index has a specific content to hold*/
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
