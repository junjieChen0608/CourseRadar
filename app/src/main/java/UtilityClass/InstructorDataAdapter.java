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

    /**
     *
     * @param context
     * @param pps:
     */
    public InstructorDataAdapter(Activity context, ArrayList<instructorData> pps) {
        super(context, 0, pps);
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        View listItemView = convertView;

        // we have to inflate a instructor_data_list_item when there is no view available
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.instructor_data_list_item, parent, false);
        }

        instructorData currentInstData = getItem(position);


        // show instructor's full name, currently first name + last name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.tv_name);

        nameTextView.setText(currentInstData.getName());


        // show instructor's email
        TextView emailTextView = (TextView) listItemView.findViewById(R.id.tv_email);

        emailTextView.setText(currentInstData.getEmail());

        // later it will be great if we could show profile photo of each instructor.

        return listItemView;
    }
}
