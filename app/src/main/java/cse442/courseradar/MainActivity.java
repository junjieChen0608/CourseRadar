package cse442.courseradar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import UtilityClass.InstructorDataAdapter;
import UtilityClass.instructorData;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private DatabaseReference searchProfessor;
    private ProgressBar pbWait;
    private ListView lvSearchResultList;
    private TextView tvNoResult;
    private boolean noResult;


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
        lvSearchResultList = (ListView) findViewById(R.id.lv_instructorData);
        tvNoResult = (TextView) findViewById(R.id.tv_no_result);
        noResult = true;
        searchProfessor= FirebaseDatabase.getInstance().getReference("instructors");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf("onStart","MainActivity onStart");
        currentActivity = this;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.wtf(TAG + " onStart", "user is signed out");
        }
        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        final String keyword = input;
        showProgressBar();
        Log.d(TAG, "search this: " + input);
        if(input!= null && !input.isEmpty()){
            input= input.toUpperCase();
            searchProfessor.orderByChild("firstName").equalTo(input).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> list = dataSnapshot.getChildren();

                    // initialize ArrayList to store all instructorData
                    ArrayList<instructorData> instructorDataList = new ArrayList<instructorData>();

                    for (DataSnapshot s : list) {
                        Log.d("ins", s.getKey());
                        instructorData data = s.getValue(instructorData.class);


                        if (data != null) {
                            instructorDataList.add(data);

                        } else {
                            Log.d("onDataChange", "not found");
                        }
                    }

                    noResult = instructorDataList.isEmpty();
                    hideProgressBar(keyword);

                    // show all founded instructorData to list view
                    InstructorDataAdapter instructorDataAdapter = new InstructorDataAdapter(MainActivity.this, instructorDataList);
                    lvSearchResultList.setAdapter(instructorDataAdapter);

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
