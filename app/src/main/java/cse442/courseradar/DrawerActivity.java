package cse442.courseradar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = DrawerActivity.class.getSimpleName();
    protected NavigationView navigationView;
    protected static GoogleApiClient googleApiClient;
    /* the universal drawer that shared among sub-classes */
    protected DrawerLayout drawer;
    /* the activity that user is currently in, it is used to implement back button logic */
    protected Activity currentActivity;
    private ProgressDialog progressDialog;

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
            Log.wtf(TAG, "google api client is created and connected ? " + googleApiClient.isConnected());
        }else{
            Log.wtf(TAG, "google api client is null");
            /* Config Google sign in */
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .build();
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
                Intent signInIntent = new Intent(currentActivity, LandingActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                /* the extra information is used to notify the LandingActivity from which activity does this intent is made */
                signInIntent.putExtra("source", currentActivity.getClass().getSimpleName());
                startActivity(signInIntent);
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
            Log.d(TAG, "close window");
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(currentActivity == null){
                currentActivity = this;
            }else if(currentActivity.getClass() == MainActivity.class){
                Log.d(TAG, "go home");
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }else{
                Log.d(TAG, "go back to previous activity on the stack");
                super.onBackPressed();
            }
        }
    }

    /* invoke sign out procedure, this signs out current user from firebaseAuth and GoogleSignInAPI
      *  it is also called if a non-UB email tried to sign in
      * */
    protected void signOut(){
        showProgressDialog();
        FirebaseAuth.getInstance().signOut();
        if(googleApiClient != null){
            /*
            must check if google API client is connected to successfully sign user out
            */
            googleApiClient.connect();
            googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(TAG, "After sign out, is this google API client connected ? " + googleApiClient.isConnected());
                            updateDrawerUI(null);
                        }
                    });
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d(TAG, "Google API Client Connection Suspended");
                }
            });
        }
        hideProgressDialog();
    }

    /* update drawer UI element according to user's sign up status */
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
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer_signed_in);
        }else{
            Log.d(TAG, "upated as signed out");
            tvUserName.setText("Guset");
            tvUserEmail.setText("");
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer_signed_out);
        }
    }

    /* lock the drawer and hide toolbar in this activity */
    protected void lockDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().hide();
    }

    /* unlock the drawer and reveal toolbar in this activity */
    protected void unlockDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().show();
    }

    protected void showProgressDialog() {
        Log.d(TAG, "Show progress dialog in " + currentActivity.getClass().getSimpleName());
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(currentActivity);
            progressDialog.setMessage("Processing...");
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /* check if this account is UB email */
    protected boolean isUBEmail(GoogleSignInAccount account){
        return account != null && account.getEmail().contains("@buffalo.edu");
    }

    protected String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
