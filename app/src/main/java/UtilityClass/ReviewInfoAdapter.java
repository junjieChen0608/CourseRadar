package UtilityClass;

import android.app.Activity;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cse442.courseradar.R;

/**
 * Created by yang on 10/5/17.
 */

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

    private DatabaseReference ratingsDB;
    private DatabaseReference likesDB;
    private String userUBIT, instructorName, courseID;

    public ReviewInfoAdapter(Activity context, ArrayList<ReviewInfo> pps
                            , DatabaseReference ratingsDB, DatabaseReference likesDB
                            , String userUBIT, String instructorName, String courseID) {
        super(context, 0, pps);
        this.ratingsDB = ratingsDB;
        this.likesDB = likesDB;
        this.userUBIT = userUBIT;
        this.instructorName = instructorName;
        this.courseID = courseID;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

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
        tvTotalLikes.setText(String.valueOf(currentReview.getTotalLikes()));

        ImageView ivLike = listItemView.findViewById(R.id.iv_thumbs_up);
        // set a tag for this ImageView, it is used to determined if the LIKE is clicked
        // 99 = not clicked, 100 = clicked
        // check if this user liked this review
        likesDB.child(userUBIT).child(getItem(position).getName()+"-"+instructorName+"-"+courseID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_clicked));
                    ivLike.setTag(100);
                }else{
                    ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_unclicked));
                    ivLike.setTag(99);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        // show a face expression according to the overall quality
        // note that 0 and 1 point in overall quality both will be setted to face awful
        ImageView profileImage = (ImageView) listItemView.findViewById(R.id.iv_rating_face);
        switch (currentReview.getOverallQuality().toString()) {
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

        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(DEBUG_LIKES_TAG, "I clicked on " + position);
                String reviewerUBIT = currentReview.getName();
                Long currentTotalLikesInTextView = currentReview.getTotalLikes();
                Integer imgTag = (Integer) ivLike.getTag();
                imgTag = (imgTag == null) ? 0 : imgTag;
                // compose the ratings database path
                DatabaseReference ratingsDBPath = ratingsDB.child(reviewerUBIT).child(instructorName+"-"+courseID).child("likes");

                if(imgTag == 99){
                    Log.d(DEBUG_LIKES_TAG, userUBIT + " liked " + reviewerUBIT + "'s rating on " + instructorName + "'s " + courseID);
                    /**
                     * TODO implement: like clicked logic
                     */
                    // 1 increment that review's number of likes by 1
                    ratingsDBPath.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                // read the current number of likes then increment by 1
                                Long totalLikes = dataSnapshot.getValue(Long.class);
                                ratingsDBPath.setValue(totalLikes+1);
                                // 2 create a new node in likes database, organize in the format of userUBIT -> (reviewerUBIT-instructorName-courseID)
                                likesDB.child(userUBIT).child(reviewerUBIT+"-"+instructorName+"-"+courseID).setValue(1);
                                // 3 set the LIKE ImageView as clicked
                                ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_clicked));
                                ivLike.setTag(100);
                                // 4 increment the TextView counter by 1
                                // format the TextView according to the number of likes fixed as length 4, set as 999+ if exceeds 999
                                Long copyOfCurrentTotalLikesInTextView = currentTotalLikesInTextView;
                                copyOfCurrentTotalLikesInTextView += 1;
                                formatTextView(copyOfCurrentTotalLikesInTextView, tvTotalLikes);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });

                }else{
                    Log.d(DEBUG_LIKES_TAG, userUBIT + " unliked " + reviewerUBIT + "'s rating on " + instructorName + "'s " + courseID);
                    /**
                     * TODO implement: like unclicked logic
                     */

                    // 1 decrement that review's number of likes by 1
                    ratingsDBPath.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // read the current number of likes then decrement by 1
                            if(dataSnapshot.exists()){
                                Long totalLiks = dataSnapshot.getValue(Long.class);
                                ratingsDBPath.setValue(totalLiks-1);
                                // 2 delete the node in likes database, organize in the format of userUBIT -> (reviewerUBIT-instructorName-courseID)
                                likesDB.child(userUBIT).child(reviewerUBIT+"-"+instructorName+"-"+courseID).removeValue();
                                // 3 set the LIKE ImageView as unclicked
                                ivLike.setImageDrawable(getContext().getDrawable(R.drawable.like_unclicked));
                                ivLike.setTag(99);
                                // 4 decrement the TextView counter by 1
                                // format the TextView according to the number of likes fixed as length 4, set as 999+ if exceeds 999
                                formatTextView(currentTotalLikesInTextView, tvTotalLikes);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                }

            }
        });
        return listItemView;
    }

    private void formatTextView(Long copyOfCurrentTotalLikesInTextView, TextView tvTotalLikes){
        StringBuilder sb = new StringBuilder(String.valueOf(copyOfCurrentTotalLikesInTextView));
        if(sb.length() >= 4){
            // the length exceeds 4, just set it as 999
            Log.d(DEBUG_LIKES_TAG, "tv exceeds 999");
            tvTotalLikes.setText("999+");
        }else{
            // the length is less than 4, pad the TextView with spaces
            Log.d(DEBUG_LIKES_TAG, "tv less than 999");
            int paddingSpaces = 4 - sb.length();
            for (int i = 0; i < paddingSpaces; i++){
                sb.insert(0, " ");
            }
            tvTotalLikes.setText(sb.toString());
        }
    }
}
