package UtilityClass;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cse442.courseradar.R;

/**
 * Created by yang on 10/5/17.
 */

public class ReviewInfoAdapter extends ArrayAdapter<ReviewInfo> {

    public ReviewInfoAdapter(Activity context, ArrayList<ReviewInfo> pps) {
        super(context, 0, pps);
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


        // show a face expression according to the overall quality
        // note that 0 and 1 point in overall quality both will be setted to face awful
        ImageView profileImage = (ImageView) listItemView.findViewById(R.id.iv_pp_image);
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
                // this shoud never happen, it is only used for debug in developing
                profileImage.setImageResource(R.drawable.pic_holder);
        }

        // TODO implement: add an onClickListener to the LIKE image and a textview to display total number of likes
        return listItemView;
    }
}
