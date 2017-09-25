package cse442.courseradar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import UtilityClass.InstructorDataAdapter;
import UtilityClass.instructorData;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;
    private TextView tvSearchResult;
    private DatabaseReference searchProfessor;


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
        tvSearchResult = (TextView) findViewById(R.id.tv_search_result);
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


                    // show all founded instructorData to list view
                    InstructorDataAdapter instructorDataAdapter = new InstructorDataAdapter(MainActivity.this, instructorDataList);
                    ListView listView = (ListView) findViewById(R.id.lw_instructorData);
                    listView.setAdapter(instructorDataAdapter);

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
}
