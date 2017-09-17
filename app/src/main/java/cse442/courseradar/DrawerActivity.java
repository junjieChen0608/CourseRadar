package cse442.courseradar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = DrawerActivity.class.getSimpleName();
    protected NavigationView navigationView;
    protected static GoogleApiClient googleApiClient;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(googleApiClient != null){
            Log.wtf(TAG, "google api client is created");
        }else{
            Log.wtf(TAG, "google api client is somehow become null again");
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // TODO implement click menu item logic
        switch (id){
            case R.id.nav_sign_in:
                startActivity(new Intent(this, LandingActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            case R.id.nav_my_profile:
                break;
            case R.id.nav_my_review:
                break;
            case R.id.nav_mentioned_me:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* override the back button logic, press back will go to home screen
     * this is a workaround to avoid signing user out if the app is cleaned from memory or this activity is destroyed */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    protected String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

    /* invoke sign out procedure, this signs out non-UB email user from firebaseAuth and GoogleSignInAPI */
    protected void signOut(){
        FirebaseAuth.getInstance().signOut();
        if(googleApiClient != null){
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    updateDrawerUI(null);
                }
            });

        }else{
            Log.wtf(TAG, "google api is null in sign out");
        }

    }

    protected void updateDrawerUI(FirebaseUser user){
        Log.d(TAG, "upating UI...");
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = (TextView) headerView.findViewById(R.id.tv_user_name);
        TextView tvUserEmail = (TextView) headerView.findViewById(R.id.tv_user_email);
        if(tvUserName == null || tvUserEmail == null){
            Log.wtf("update drawer UI", "This should never happen");
        }
        if(user != null){
            Log.d(TAG, "upated as signed in");
            tvUserName.setText(parseUBIT(user.getEmail()));
            tvUserEmail.setText(user.getEmail());
            navigationView.inflateMenu(R.menu.drawer_signed_in);
        }else{
            Log.d(TAG, "upated as signed out");
            tvUserName.setText("Guset");
            tvUserEmail.setText("");
            navigationView.inflateMenu(R.menu.drawer_signed_out);
        }
    }

    protected void lockDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().hide();
    }

    protected void unlockDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().show();
    }
}
