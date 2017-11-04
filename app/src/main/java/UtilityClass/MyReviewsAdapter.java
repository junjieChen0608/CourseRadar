package UtilityClass;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.Source;

import cse442.courseradar.R;

/**
 * Created by wiiSo on 10/29/2017.
 */

public class MyReviewsAdapter extends BaseExpandableListAdapter {

    private ArrayList<String> header;
    private HashMap<String, ArrayList<String>> child;
    private Context ctx;

    public MyReviewsAdapter(Context ctx, ArrayList<String> header, HashMap<String, ArrayList<String>> child){
        this.ctx = ctx;
        this.header = header;
        this.child = child;
    }

    @Override
    public int getGroupCount() {
        // how many reviews that the user has made
        return header.size();
    }

    @Override
    public int getChildrenCount(int i) {
        // initialize each header's sub item once
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        // the i indicates which review item to be pinpoint
        return header.get(i);
    }

    @Override
    public Object getChild(int groupPos, int childPos) {
        // given header index, return the ArrayList<String>
        return child.get(header.get(groupPos));
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int groupPos, int childPos) {
        return childPos;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPos, boolean b, View view, ViewGroup viewGroup) {
        // initialize the header view
        String headerTitle = (String) this.getGroup(groupPos);
        Log.d("header", "header title " + headerTitle);
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.my_reviews_list_item_header, null);
        }
        TextView tvHeaderInstructorName = view.findViewById(R.id.tv_my_reviews_instructor_name);
        TextView tvHeaderCourseID = view.findViewById(R.id.tv_my_reivews_course_id);
        Log.d("header", "header title splited " + tvHeaderInstructorName.getText().toString() + "\n" + tvHeaderCourseID.getText().toString());
        int indexOfDash = headerTitle.indexOf("-");
        tvHeaderInstructorName.setText(headerTitle.substring(0, indexOfDash));
        tvHeaderCourseID.setText((headerTitle.substring(indexOfDash+1)));
        // TODO implement: can get child and set the ImageView of the rating face
        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
        // the childPos is effectively ignored
        ArrayList<String> specificRating = (ArrayList<String>)this.getChild(groupPos, childPos);
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.my_reviews_list_item_content, null);
        }

        // 0: overall, 1: lecture, 2: assignment, 3: comment
        TextView tvMyRevOverall = view.findViewById(R.id.tv_my_reviews_overall);
        tvMyRevOverall.setText(this.ctx.getResources().getString(R.string.overall_quality) + " " + specificRating.get(0));

        TextView tvMyRevLecture = view.findViewById(R.id.tv_my_reviews_lecture);
        tvMyRevLecture.setText(this.ctx.getResources().getString(R.string.lecture_quality) + " " + specificRating.get(1));

        TextView tvMyRevAssignment = view.findViewById(R.id.tv_my_reviews_assignment_diff);
        tvMyRevAssignment.setText(this.ctx.getResources().getString(R.string.assignment_difficulty) + " " + specificRating.get(2));

        TextView tvMyRevComment = view.findViewById(R.id.tv_my_reviews_comment);
        tvMyRevComment.setText(this.ctx.getResources().getString(R.string.comment) + " " + specificRating.get(3));

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
