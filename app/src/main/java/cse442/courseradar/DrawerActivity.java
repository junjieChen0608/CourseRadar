package cse442.courseradar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_ALBUM = 2;
    public static final int REQUEST_CROP = 3;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String TAG = DrawerActivity.class.getSimpleName();

    protected RoundedImageView ivProfilePicture;
    private StorageReference firebaseStorage;

    /* the flag that is used to determine if upload image is done before retrieve image from firebase */
    private boolean setFromLocal;

    protected TextView tvUserName;
    protected TextView tvUserEmail;
    protected File imageFile;
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
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

        /*find the UI element in the drawer header and initialize them*/
        View headerView = navigationView.getHeaderView(0);
        tvUserName = (TextView) headerView.findViewById(R.id.tv_user_name);
        tvUserEmail = (TextView) headerView.findViewById(R.id.tv_user_email);
        ivProfilePicture = (RoundedImageView) headerView.findViewById(R.id.iv_user_profile_photo);
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* check if user is sign in/out to enable/disable alertdialog */
                if(FirebaseAuth.getInstance().getCurrentUser() != null){
                    verifyStoragePermissions(DrawerActivity.this);
                    new AlertDialog.Builder(DrawerActivity.this)
                            .setTitle("Select")
                            .setItems(new String[]{"Camera", "Album"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0) {
                                        selectCamera();
                                    } else {
                                        selectAlbum();
                                    }
                                }
                            })
                            .create()
                            .show();
                }
                else{
                    Log.wtf("onClickAvatar","user signed out");
                }
            }
        });
        firebaseStorage = FirebaseStorage.getInstance().getReference();

        /* method that is used to ignore uri exposure */
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

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

        setFromLocal = false;

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // TODO implement: click menu item logic
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

        if(tvUserName == null || tvUserEmail == null){
            Log.wtf("update drawer UI", "This should never happen");
        }
        if(user != null){
            Log.d(TAG, "upate UI as signed in status");
            tvUserName.setText(parseUBIT(user.getEmail()));
            tvUserEmail.setText(user.getEmail());
            /* check if image is exsited in local repo */
            imageFile = new File(Environment.getExternalStorageDirectory(), parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail()) + ".png");
            if(imageFile.exists()){
                ivProfilePicture.setImageURI(Uri.fromFile(imageFile));
            } else {
                if(!setFromLocal){
                /* update user avatar by capturing the image url from firebase storage determined by user UBIT */
                    firebaseStorage.child("avatar/"+parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail())).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(DrawerActivity.this).load(uri).into(ivProfilePicture);
                        }
                    });
                } else {
                    setFromLocal = false;
                }
            }

            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer_signed_in);
        }else{
            Log.d(TAG, "upate UI as signed out status");
            tvUserName.setText(R.string.guest);
            tvUserEmail.setText("");
            /* set the avatar to default by passing the pic holder into the imageview */
            ivProfilePicture.setImageDrawable(getDrawable(R.drawable.pic_holder));
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

    /* invoke camera by seclecting alertdialog poped up from pressing the avatar */
    protected void selectCamera() {
        createImageFile();
        if (!imageFile.exists()) {
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    /* invoke album by slecting alertdialog poped up from pressing the avatar */
    protected void selectAlbum() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, REQUEST_ALBUM);
    }

    /* crop the image either from camera or album by pass the image uri */
    protected void cropImage(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, REQUEST_CROP);
    }

    /* create a image file with png format and name the file by user UBIT
     * therefore, it will overwrite the image with the newest one and save user storage */
    protected void createImageFile() {
        imageFile = new File(Environment.getExternalStorageDirectory(), parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail()) + ".png");
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "createImageFile error", Toast.LENGTH_SHORT).show();
        }
    }

    /* verify if we have storage permission from user cellphone and prompt the user if we do not have it */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have storage permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /* handle the storage permission request by toast message to user when permission is granted or denied */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Permission is granted",
                            Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(this,
                            "Permission is denied",
                            Toast.LENGTH_SHORT).show();

                }
                return;
            }
        }
    }

    /* handle the avatar function result from selecting alertdialog */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CAMERA:
                cropImage(Uri.fromFile(imageFile));
                break;
            case REQUEST_ALBUM:
                createImageFile();
                if (!imageFile.exists()) {
                    return;
                }
                Uri uri = data.getData();
                if (uri != null) {
                    cropImage(uri);
                }
                break;
            case REQUEST_CROP:
                Log.d(TAG, "is profile pic button null ? " + String.valueOf(ivProfilePicture == null));
                ivProfilePicture.setImageURI(Uri.fromFile(imageFile));
                /* upload image uri to firebase storage and name the image file by user UBIT and save it in avatar folder */
                StorageReference filepath = firebaseStorage.child("avatar").child(parseUBIT(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                filepath.putFile(Uri.fromFile(imageFile));
                setFromLocal = true;
                break;
        }
    }

}
