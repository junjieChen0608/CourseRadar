package UtilityClass;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cse442.courseradar.R;

/**
 * Created by yang on 10/5/17.
 */

// TODO debug: LIKE feature is not fully implemented
// Transaction is not responding to click correctly
// LIKE buttons is not initialized properly

public class ReviewInfoAdapter extends ArrayAdapter<ReviewInfo> {

    /**
     *
     * @param context
     * @param pps
     *
     * parameter needed to manipulate database
     *
     * DatabaseReference to ratings database -> reviewerUBIT -> instructorName-courseID -> likes
     * DatabaseReference to likes database -> userUBIT -> reviewerUBIT-instructorName-courseID
     */
    private static final String DEBUG_LIKES_TAG = "likes";
    private static final long INTERVAL = 2000;

    private DatabaseReference ratingsDB;
    private DatabaseReference likesDB;
    private String userUBIT, instructorName, courseID;
    private long lastTimeClicked;

    public ReviewInfoAdapter(Activity context, ArrayList<ReviewInfo> pps
                            , DatabaseReference ratingsDB, DatabaseReference likesDB
                            , String userUBIT, String instructorName, String courseID) {
        super(context, 0, pps);
        this.ratingsDB = ratingsDB;
        this.likesDB = likesDB;
        this.userUBIT = userUBIT;
        this.instructorName = instructorName;
        this.courseID = courseID;
        lastTimeClicked = -1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("getView", "called");
        View listItemView = convertView;

        // we have to inflate a reviews_list_item when there is no view available
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.reviews_list_item, parent, false);
        }

        ReviewInfo currentReview = getItem(position);

        // show three scores
        TextView overallQuality = (TextView) listItemView.findViewById(R.id.tv_overall_quality);
        overallQuality.setText("Overall: "+ currentReview.getOverallQuality());

        TextView lectureQuality = (TextView) listItemView.findViewById(R.id.tv_lecture_quality);
        lectureQuality.setText("Lecture Quality: "+ currentReview.getLectureQuality());

        TextView diff = (TextView) listItemView.findViewById(R.id.tv_assignment_difficulty);
        diff.setText("Assignment Difficulty: "+ currentReview.getAssignmentDifficulty());

        // show the comment from this student for tor this particular course
        TextView commentView = (TextView) listItemView.findViewById(R.id.tv_comment);
        String comment = "Comment: " + currentReview.getComment();
        commentView.setText(comment);

        // show the LIKE ImageView and total likes TextView
        TextView tvTotalLikes = listItemView.findViewById(R.id.tv_total_likes);
        formatTextView(currentReview.getTotalLikes(), tvTotalLikes);

        ImageView ivLike = listItemView.findViewById(R.id.iv_thumbs_up);
        // set a tag for this ImageView, it is used to determined if the LIKE is clicked
        // 99 = not clicked, 100 = clicked
        // check if this user liked this review
        // TODO debug: like button cannot initialize properly
        Log.d(DEBUG_LIKES_TAG, currentReview.getName() + " is at " + position);
        likesDB.child(userUBIT).child(currentReview.getName()+"-"+instructorName+"-"+courseID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_clicked));
                    ivLike.setTag(100);
                    Log.d(DEBUG_LIKES_TAG, "LIKE button at " + position + " which is " + currentReview.getName() + ", set as blue");
                }else{
                    ivLike.setTag(99);
                    ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_unclicked));
                    Log.d(DEBUG_LIKES_TAG, "LIKE button at " + position + " which is " + currentReview.getName() + ", set as grey");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });



        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastTimeClicked > 0 && System.currentTimeMillis() - lastTimeClicked < INTERVAL){
                    Toast.makeText(getContext(), "take a break, dude.", Toast.LENGTH_SHORT).show();
                    return;
                }

                lastTimeClicked = System.currentTimeMillis();

                Log.d(DEBUG_LIKES_TAG, "I clicked on " + position);
                String reviewerUBIT = currentReview.getName();
                Integer imgTag = (Integer) ivLike.getTag();
                imgTag = (imgTag == null) ? 0 : imgTag;
                // compose the ratings database path
                DatabaseReference ratingsDBPath = ratingsDB.child(reviewerUBIT).child(instructorName+"-"+courseID).child("likes");

                if(imgTag == 99){
                    Log.d(DEBUG_LIKES_TAG, userUBIT + " liked " + reviewerUBIT + "'s rating on " + instructorName + "'s " + courseID);
                    /**
                     * like clicked logic
                     */
                    // 1 increment that review's number of likes by 1
                    ratingsDBPath.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                // read the current number of likes then increment by 1
                                // use Transaction to modify likes counter
                                ratingsDBPath.runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        // 4 increment the TextView counter by 1
                                        long currentTotalLikes = (long) mutableData.getValue();
                                        Log.d(DEBUG_LIKES_TAG, "LIKE clicked logic, before click: " + currentTotalLikes);
                                        mutableData.setValue(currentTotalLikes+1);
                                        Log.d(DEBUG_LIKES_TAG, "LIKE clicked logic, after click: " + (currentTotalLikes+1));
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                        // 4 format the TextView according to the number of likes fixed as length 4, set as 999+ if exceeds 999
                                        // causal model
                                        if(dataSnapshot.exists()){
                                            Log.d(DEBUG_LIKES_TAG, "dataSnapshot is not null");
                                            formatTextView((long)dataSnapshot.getValue(), tvTotalLikes);
                                        }else{
                                            Log.d(DEBUG_LIKES_TAG, "dataSnapshot is null");
                                        }
                                    }
                                });

                                // 2 create a new node in likes database, organize in the format of userUBIT -> (reviewerUBIT-instructorName-courseID)
                                likesDB.child(userUBIT).child(reviewerUBIT+"-"+instructorName+"-"+courseID).setValue(1);
                                // 3 set the LIKE ImageView as clicked
                                ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_clicked));
                                ivLike.setTag(100);

                            }else{
                                Log.d(DEBUG_LIKES_TAG, "LIKE clicked logic dataSnapshot does not exist");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });

                }else{
                    Log.d(DEBUG_LIKES_TAG, userUBIT + " unliked " + reviewerUBIT + "'s rating on " + instructorName + "'s " + courseID);
                    /**
                     * like unclicked logic
                     */

                    // 1 decrement that review's number of likes by 1
                    ratingsDBPath.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // read the current number of likes then decrement by 1
                            if(dataSnapshot.exists()){

                                ratingsDBPath.runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        // 4 decrement the TextView counter by 1
                                        long currentTotalLikes = (long) mutableData.getValue();
                                        Log.d(DEBUG_LIKES_TAG, "LIKE unclicked logic, before click: " + currentTotalLikes);
                                        mutableData.setValue(currentTotalLikes-1);
                                        Log.d(DEBUG_LIKES_TAG, "LIKE unclicked logic, after click: " + (currentTotalLikes-1));
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        // 4 format the TextView according to the number of likes fixed as length 4, set as 999+ if exceeds 999
                                        // causal model
                                        if(dataSnapshot.exists()){
                                            Log.d(DEBUG_LIKES_TAG, "dataSnapshot is not null");
                                            formatTextView((long)dataSnapshot.getValue(), tvTotalLikes);
                                        }else{
                                            Log.d(DEBUG_LIKES_TAG, "dataSnapshot is null");
                                        }

                                    }
                                });

                                // 2 delete the node in likes database, organize in the format of userUBIT -> (reviewerUBIT-instructorName-courseID)
                                likesDB.child(userUBIT).child(reviewerUBIT+"-"+instructorName+"-"+courseID).removeValue();
                                // 3 set the LIKE ImageView as unclicked
                                ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_unclicked));
                                ivLike.setTag(99);
                            }else{
                                Log.d(DEBUG_LIKES_TAG, "LIKE unclicked logic dataSnapshot does not exist");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                }

            }
        });

        // show a face expression according to the overall quality
        // note that 0 and 1 point in overall quality both will be setted to face awful
        ImageView profileImage = (ImageView) listItemView.findViewById(R.id.iv_rating_face);
        switch (String.valueOf(currentReview.getOverallQuality())) {
            case "0":
            case "1":
                profileImage.setImageResource(R.drawable.faceawful);
                break;
            case "2":
                profileImage.setImageResource(R.drawable.facepoor);
                break;
            case "3":
                profileImage.setImageResource(R.drawable.faceaverage);
                break;
            case "4":
                profileImage.setImageResource(R.drawable.facegood);
                break;
            case "5":
                profileImage.setImageResource(R.drawable.faceexcellent);
                break;
            default:
                // this should never happen, it is only used for debug in developing
                profileImage.setImageResource(R.drawable.pic_holder);
        }

        return listItemView;
    }

    private void formatTextView(Long totalLikesFromTextView, TextView tvTotalLikes){
        StringBuilder sb = new StringBuilder(String.valueOf(totalLikesFromTextView));
        if(sb.length() >= 4){
            // the length exceeds 4, just set it as 999
            tvTotalLikes.setText("999+");
        }else{
            // the length is less than 4, pad the TextView with spaces
            int paddingSpaces = 4 - sb.length();
            for (int i = 0; i < paddingSpaces; i++){
                sb.insert(0, " ");
            }
            tvTotalLikes.setText(sb.toString());
        }
    }
}
