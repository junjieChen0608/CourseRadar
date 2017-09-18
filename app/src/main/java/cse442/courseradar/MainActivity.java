package cse442.courseradar;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

import static java.security.AccessController.getContext;

public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);
        unlockDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf("onStart","MainActivity onStart");
        currentContext = this;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.wtf(TAG + " onStart", "user is signed out");
        }
        updateDrawerUI(FirebaseAuth.getInstance().getCurrentUser());
    }
}
