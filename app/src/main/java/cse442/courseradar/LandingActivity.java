package cse442.courseradar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
*
*   1, Sign in behavior:
*       1.1, If the user sign in directly from here, just redirect to MainActivity, and finish() this activity
*
*       1.2, Since we allow user proceed as a guest, they can also sign in from any other activity if they want,
*           in this case, read the extra from the intent bundle to identify where user come from then proceed login logic,
*           after user logged in, just finish() this activity to bring user back to the activity he/she comes from
*
* */

public class LandingActivity extends DrawerActivity implements View.OnClickListener{

    private static final String TAG = LandingActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final String USER_DATABASE = "UserDatabase";

    private Button btnSignInWithMyUB;
    private TextView tvAsGuest;

    private String sourceActivity; // from which activity does LandingActivity start
    private Bundle bundle;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_landing, null, false);
        drawer.addView(contentView, 0);

        btnSignInWithMyUB = (Button) findViewById(R.id.btn_sign_in_with_myub);
        btnSignInWithMyUB.setOnClickListener(this);
        tvAsGuest = (TextView) findViewById(R.id.tv_as_guest);
        tvAsGuest.setOnClickListener(this);

        /* Get Extra from source intent to check where the user starts this activity */
        Intent sourceIntent = getIntent();
        bundle = sourceIntent.getExtras();
        if(bundle != null){
            // if bundle is not null, it means the user comes from other activity
            sourceActivity = (String)bundle.get("source");
            Log.d("landing", "come from other activity " + sourceActivity);
        }else{
            // if the bundle is null, means user just started the app
            sourceActivity = TAG;
            Log.d("landing", "just landed: " + sourceActivity);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDB = FirebaseDatabase.getInstance().getReference(USER_DATABASE);
        lockDrawer();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        // mark this activity itself as current activity
        currentActivity = this;
        if(sourceActivity == null){
            sourceActivity = TAG;
        }

        /* Check if user is signed in (non-null) and redirect activity accordingly. */
        firebaseUser = firebaseAuth.getCurrentUser();
        super.onStart();
        if(firebaseUser != null){
            Log.d(TAG, "user is signed in, redirect to MainActivity");
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }else{
            Log.d(TAG, "user is not signed in");
        }
    }

    /* invoke sign in procedure */
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /* callback from sign in */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == RC_SIGN_IN){
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
        }else{
            Log.d(TAG, "result code is " + resultCode);
            Toast.makeText(this, "Sign in cancelled, or you don't have Internet access", Toast.LENGTH_SHORT).show();
        }
    }

    /* handles the sign in result from Google sign in API */
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            Log.wtf(TAG, "After sign in success, is Google API client connected ? " + googleApiClient.isConnected());
            if(isUBEmail(account)){
                firebaseAuthWithGoogle(account);
            }else{
                Toast.makeText(this, "Please use your UB email", Toast.LENGTH_SHORT).show();
                /* sign out this non-UB email google account */
                signOut();
            }
        }else {
            Log.d(TAG, "Sign in result: failed");
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
                                /* check if this user is in user database */
                                checkUserDatabase();
                                /*
                                * after database check, the behavior is as follows:
                                *
                                * 1, update the side nav drawer to signed status
                                * */

                                if(sourceActivity != null){
                                    if(sourceActivity.equals(TAG)){
                                        // 2.1 if user come from LandingActivity and signed in, redirect to MainActivity
                                        startActivity(new Intent(LandingActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                                    }else if(sourceActivity.equals("DetailedViewActivity")){
                                        /*
                                            2.2 else if user come from DetailedViewActivity, put the instructor's name and course ID
                                            to the intent, then start a new RatingActivity
                                         */
                                        Intent ratingIntent = new Intent(LandingActivity.this, RatingActivity.class);
                                        ratingIntent.putExtra("instructorName", bundle.getString("instructorName"));
                                        ratingIntent.putExtra("courseID", bundle.getString("courseID"));
                                        startActivity(ratingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                                    }
                                }
                                Log.d(TAG, "Successfully logged in");
                                // 2.3 always finish this activity
                                finish();
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
        firebaseDB.child(userUBIT).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.getValue(String.class);
                if(userEmail == null){
                    Log.d(TAG, "checkUserDataBase: update user");
                    firebaseDB.child(userUBIT).child("email").setValue(firebaseUser.getEmail());
                }else{
                    Log.d(TAG, "checkUserDataBase: user is already in database");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == btnSignInWithMyUB){
            signIn();
        }else if(v == tvAsGuest){
            Log.d(TAG, "sourceActivity: " + sourceActivity);
            if (sourceActivity.equals(TAG)){
                /* if this user just launched app, click "Proceed as guest" will redirect to MainActivity */
                startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            /* if this user come from other activity and clicked proceed as guest, just finish this activity without redirecting */
            finish();
            Log.d(TAG, "LandingActivity is finished");
        }
    }
}
