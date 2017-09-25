package UtilityClass;

import android.app.Activity;
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
 * Created by yang on 9/25/17.
 */

public class InstructorDataAdapter extends ArrayAdapter<instructorData> {

    public InstructorDataAdapter(Activity context, ArrayList<instructorData> pps) {
        super(context, 0, pps);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.instructor_data_list_item, parent, false);
        }

        instructorData currentInstData = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.tw_name);

        nameTextView.setText(currentInstData.getName());


        TextView emailTextView = (TextView) listItemView.findViewById(R.id.tw_email);

        emailTextView.setText(currentInstData.getEmail());

        return listItemView;
    }
}
