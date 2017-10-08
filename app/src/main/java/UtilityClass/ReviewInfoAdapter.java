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

        // we have to inflate a instructor_data_list_item when there is no view available
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.reviews_list_item, parent, false);
        }

        ReviewInfo currentReview = getItem(position);


        // show scores
        TextView overallQuality = (TextView) listItemView.findViewById(R.id.tv_overall_quality);
        overallQuality.setText("Overall: "+ currentReview.getOverallQuality());


        TextView lectureQuality = (TextView) listItemView.findViewById(R.id.tv_lecture_quality);
        lectureQuality.setText("Lecture Quality: "+ currentReview.getLectureQuality());


        TextView diff = (TextView) listItemView.findViewById(R.id.tv_assignment_difficulty);
        diff.setText("Assignment Difficulty: "+ currentReview.getAssignmentDifficulty());


        // show instructor's comment
        TextView commentView = (TextView) listItemView.findViewById(R.id.tv_comment);
        String comment = "Comment: " + currentReview.getComment();
        commentView.setText(comment);

        // later it will be great if we could show profile photo of each instructor.

        return listItemView;
    }
}
