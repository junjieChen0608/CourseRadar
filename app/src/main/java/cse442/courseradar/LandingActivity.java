package cse442.courseradar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
* The activity that greets our user, it allows user to sign in with UB email, or simply proceed as guest.
*
*
* READ ME if you are a DEVELOPER:
*   1, It is the only activity that not extends BaseActivity(i.e., it has no side nav-bar)
*   2, Sign in behavior:
*       2.1, If the user sign in directly from here, just redirect to MainActivity, and finish() this activity
*
*       2.2, Since we allow user proceed as a guest, they can also sign in from any other activity if they want,
*           in this case, read the extra from the intent bundle to identify where user come from then proceed login logic,
*           after user logged in, just finish() this activity to bring user back to the activity he/she comes from
*
* */

public class LandingActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = LandingActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final String USER_DATABASE = "UserDatabase";


    private Button btnSignInWithMyUB;
    private TextView tvAsGuest;
    private String sourceActivity;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseDB;
    private GoogleApiClient googleApiClient;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        btnSignInWithMyUB = (Button) findViewById(R.id.btn_sign_in_with_myub);
        btnSignInWithMyUB.setOnClickListener(this);
        tvAsGuest = (TextView) findViewById(R.id.tv_as_guest);
        tvAsGuest.setOnClickListener(this);

        /* Get Extra from Bundle to check where the user starts this activity */
        Intent intentComeFrom = getIntent();
        Bundle bundle = intentComeFrom.getExtras();
        if(bundle != null){
            sourceActivity = (String)bundle.get("source");
            Log.d(TAG, "come from other activity " + sourceActivity);
        }else{
            sourceActivity = TAG;
            Log.d(TAG, "just landed: " + sourceActivity);
        }

        /* Config Google sign in */
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                    .requestIdToken(getString(R.string.default_web_client_id))
                                                    .requestEmail()
                                                    .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                            .enableAutoManage(this, this)
                            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                            .build();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDB = FirebaseDatabase.getInstance().getReference(USER_DATABASE);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        if(sourceActivity == null){
            sourceActivity = TAG;
        }

        /* Check if user is signed in (non-null) and redirect activity accordingly. */
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            Log.d(TAG, "user is signed in, redirect to MainActivity");
            super.onStart();
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }else{
            Log.d(TAG, "user is not signed in");
            super.onStart();
        }
    }

    /* invoke sign in procedure */
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /* invoke sign out procedure, this signs out non-UB email user from firebaseAuth and GoogleSignInAPI */
    private void signOut(){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient);
    }

    /* call back from sign in */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /* check if this account is UB email */
    private boolean isUBEmail(GoogleSignInAccount account){
        return account != null && account.getEmail().contains("@buffalo.edu");
    }

    /* handles the sign in result from Google sign in API */
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            if(isUBEmail(account)){
                firebaseAuthWithGoogle(account);
            }else{
                Toast.makeText(this, "Please use your UB email", Toast.LENGTH_SHORT).show();
                /* sign out this non-UB email google account */
                signOut();
            }
        }
    }

    /* authenticate this account with firebase, store user's information to our firebase UserDatabase directory */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            firebaseUser = firebaseAuth.getCurrentUser();
                            if(firebaseUser != null){
                                /* check if this user is not in user database */
                                checkUserDatabase();
                                /* after check, redirect to MainActivity */
                                startActivity(new Intent(LandingActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        }else{
                            Toast.makeText(LandingActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /* check if this user is in user database, if not just update it */
    private void checkUserDatabase(){
        final String userUBIT = parseUBIT(firebaseUser.getEmail());
        firebaseDB.child(userUBIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.getValue(String.class);
                if(userEmail == null){
                    Log.d(TAG, "checkUserDataBase: update user");
                    firebaseDB.child(userUBIT).setValue(firebaseUser.getEmail());
                }else{
                    Log.d(TAG, "checkUserDataBase: user is already in database");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

    @Override
    public void onClick(View v) {
        if(v == btnSignInWithMyUB){
            /*
            * check the sourceActivity flag to determine whether redirect to MainActivity or just call finish()
            * */
            signIn();
        }else if(v == tvAsGuest){
            /* if this user come from other activity and clicked proceed as guest, just finish this activity without redirecting */
            Log.d(TAG, "sourceActivity: " + sourceActivity);
            if (sourceActivity.equals(TAG)){
                startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
            finish();
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Processing...");
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
