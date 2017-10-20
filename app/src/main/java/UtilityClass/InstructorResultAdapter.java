package UtilityClass;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import cse442.courseradar.R;

/**
 * Created by yang on 10/2/17.
 * the adapter to populate the ListView in MainActivity(i.e. search course ID result overview)
 */

public class InstructorResultAdapter extends ArrayAdapter<InstructorInfo> {

    public InstructorResultAdapter(Activity context, ArrayList<InstructorInfo> pps) {
        super(context, 0, pps);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;

        // we have to inflate a instructor_data_list_item when there is no view available
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.instructor_data_list_item, parent, false);
        }

        InstructorInfo currentInstuctor = getItem(position);

        // show instructor's full name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.tv_name);
        nameTextView.setText(currentInstuctor.getName());


        // show instructor's email
        TextView emailTextView = (TextView) listItemView.findViewById(R.id.tv_email);
        emailTextView.setText(currentInstuctor.getEmail());


        // show rating bar according to the overall quality
        RatingBar overallRating = (RatingBar) listItemView.findViewById(R.id.rb_overall_quality);
        if (currentInstuctor.getTotalRatings() != 0) {
            overallRating.setRating((float) currentInstuctor.getOverallQuality() / currentInstuctor.getTotalRatings());
        } else {
            overallRating.setRating(0);
        }

        // a text information telling user the number of reviews
        TextView totalRatingsTextView = (TextView) listItemView.findViewById(R.id.tv_total_ratings);
        totalRatingsTextView.setText(currentInstuctor.getTotalRatings() + " reviews");

        return listItemView;
    }
}
