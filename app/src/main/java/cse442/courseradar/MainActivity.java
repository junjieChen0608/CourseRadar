package cse442.courseradar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private SearchView svSearchBar;

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
    public boolean onQueryTextSubmit(String s) {
        Log.d(TAG, "search this: " + s);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        /* track keywords change, maybe useful for search suggestion */
        return false;
    }
}
