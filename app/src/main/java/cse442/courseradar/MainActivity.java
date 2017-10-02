package cse442.courseradar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import UtilityClass.InstructorDataAdapter;
import UtilityClass.courseData;
import UtilityClass.instructorData;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private FirebaseDatabase searchCourse;
    private ProgressBar pbWait;
    private ListView lvSearchResultList;
    private TextView tvNoResult;
    private boolean noResult;

    /*Rate activity prototype*/
    private Button btnRateMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);
        unlockDrawer();
        svSearchBar = (SearchView) findViewById(R.id.sv_search_bar);
        svSearchBar.setIconifiedByDefault(false);
        svSearchBar.setOnQueryTextListener(this);
        pbWait = (ProgressBar) findViewById(R.id.pb_wait);

        /*TODO implement: initialize a new fragment when click listview item*/
        lvSearchResultList = (ListView) findViewById(R.id.lv_instructorData);
        lvSearchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, ((TextView)view.findViewById(R.id.tv_name)).getText().toString() + ((TextView)view.findViewById(R.id.tv_email)).getText(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        tvNoResult = (TextView) findViewById(R.id.tv_no_result);
        noResult = true;
        searchCourse= FirebaseDatabase.getInstance();

        btnRateMe = (Button) findViewById(R.id.btn_rate_me);
        btnRateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RatingActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf("onStart","MainActivity onStart");
        /*mark current activity as this activity*/
        currentActivity = this;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.wtf(TAG + " onStart", "user is signed out");
        }
        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        input = input.trim();
        final String keyword = input;
        showProgressBar();
        Log.d(TAG, "search this: " + input);
        //TODO to improve the illegal input detection
        if(input!= null && !input.isEmpty()&& input.matches("\\w+")){
            input= input.toUpperCase();
            String courseAbbr= "";
            for (int i= 0; i<input.length(); i++){
                if (input.charAt(i)>=65 &&input.charAt(i)<= 90){
                    courseAbbr= input.substring(0, i+1);
                }
                else{
                    break;
                }
            }
            searchCourse.getReference(courseAbbr).child(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    courseData resultCourse= dataSnapshot.getValue(courseData.class);
                    /*here it gets the courseData, it has a hashmap to store instructors and their email
                    * take those to form list*/
                    //TODO construct the instructor list for the course
                    Log.d("search test", resultCourse.getCredit());
                    Log.d("search test", resultCourse.getInstructor().toString());

                    /*noResult = instructorDataList.isEmpty();
                    hideProgressBar(keyword);

                    // show all founded instructorData to list view
                    InstructorDataAdapter instructorDataAdapter = new InstructorDataAdapter(MainActivity.this, instructorDataList);
                    lvSearchResultList.setAdapter(instructorDataAdapter);*/
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("onCancelled", "activited");
                }
            });
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        /* track keywords change, maybe useful for search suggestion */
        return false;
    }

    /*hide previously shown result list or no result text view, then show progress bar*/
    private void showProgressBar(){
        if(noResult){
            tvNoResult.setVisibility(View.GONE);
        }else {
            lvSearchResultList.setVisibility(View.GONE);
        }
        pbWait.setVisibility(View.VISIBLE);
    }

    /*show no search result or result list, then hide progress bar*/
    private void hideProgressBar(String keyword){
        if(noResult){
            String formatKeyword = "\"" + keyword + "\"";
            tvNoResult.setText(getString(R.string.no_result_found_for) + " " +formatKeyword);
            tvNoResult.setVisibility(View.VISIBLE);
        }else{
            lvSearchResultList.setVisibility(View.VISIBLE);
        }
        pbWait.setVisibility(View.GONE);
    }
}
