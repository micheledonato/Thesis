package com.devmicheledonato.thesis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = this.getClass().getSimpleName();

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_CONNECTION = 1986;

    public static final String PERSON_NAME = "PERSON_NAME";
    public static final String PERSON_EMAIL = "PERSON_EMAIL";
    public static final String PERSON_ID = "PERSON_ID";
    public static final String PHOTO_PNG = "photo.png";

    public static final String SIGNING = "SIGNING";
    public static final String LOGOUT = "LOGOUT";
    public static final String DISCONNECT = "DISCONNECT";

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private String personName;
    private String personEmail;
    private String personId;
    private Uri personPhoto;
    private SharedPreferences sharedPref;
    private String signing;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                });

        buildGoogleSignInOptions();
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleSignInOptions() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder()
                .requestProfile()
                .requestEmail()
                .requestId()
                .build();
    }

    protected synchronized void buildGoogleApiClient() {
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .addApi(Plus.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        if (requestCode == RC_CONNECTION) {
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                personName = acct.getDisplayName();
                personEmail = acct.getEmail();
                personId = acct.getId();
                personPhoto = acct.getPhotoUrl();
//              Plus.PeopleApi.load(mGoogleApiClient, "me").setResultCallback(this);
                Log.i(TAG, "Name " + personName + " ID " + personId + " Email " + personEmail +
                        " Uri " + personPhoto);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(PERSON_NAME, personName);
                editor.putString(PERSON_EMAIL, personEmail);
                editor.putString(PERSON_ID, personId);
                editor.apply();
                if (personPhoto != null) {
                    downloadImage(personPhoto.toString());
                } else {
                    finishSignInActivity();
                }
            } else {
                Toast.makeText(this, R.string.no_acct, Toast.LENGTH_LONG).show();
            }
        } else {
            // Signed out, show unauthenticated UI.
            Log.i(TAG, "Signed failed");
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "signOut.onResult");
                        if (status.isSuccess()) {
                            Log.i(TAG, "SIGNOUT");
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "REVOKE");
                            clearApplicationData();
                        }
                    }
                });
    }

    private void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                }
            }
        }
        cache = getExternalCacheDir();
        if (cache != null) {
            appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib")) {
                        deleteDir(new File(appDir, s));
                        Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                    }
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        if (connectionResult.hasResolution()) {
            Log.i(TAG, "hasResolution");
            try {
                connectionResult.startResolutionForResult(this, RC_CONNECTION);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "not Resolution");
            int errorCode = connectionResult.getErrorCode();
            String errorMessage = connectionResult.getErrorMessage();
            Log.i(TAG, "ErrorCode: " + errorCode + " ErrorMessage: " + errorMessage);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
        // Intent for logout or disconnect
        Intent intent = getIntent();
        if (intent.hasExtra(SIGNING)) {
            signing = intent.getStringExtra(SIGNING);
            if (LOGOUT.equals(signing)) {
                Log.i(TAG, "intent_logout");
                signOut();
            }
            if (DISCONNECT.equals(signing)) {
                revokeAccess();
            }
        }
        intent.removeExtra(SIGNING);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    private void downloadImage(final String imageUrl) {

        progressDialog = new ProgressDialog(this);

        Log.i(TAG, "downloadImage");
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected void onPreExecute() {
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Waiting...");
                progressDialog.setTitle("Signing");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                Log.i(TAG, "doInBackground");
                Bitmap bitmap = null;
                HttpURLConnection connection = null;
                InputStream is = null;
                try {
                    connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    is = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Log.i(TAG, "Sleep");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                Log.i(TAG, "onPostExecute");
                if (bitmap != null) {
                    saveBitmap(bitmap);
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finishSignInActivity();
            }
        }.execute();
    }

    private void saveBitmap(Bitmap bitmap) {
        Log.i(TAG, "saveBitmap");
        File cache = getExternalCacheDir();
        File image = new File(cache, PHOTO_PNG);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private void finishSignInActivity() {
        // Login ok, then finish this activity
        finish();
    }

//    @Override
//    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
//        if (loadPeopleResult.getStatus().isSuccess()) {
//            PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
//            if (personBuffer != null && personBuffer.getCount() > 0) {
//                Log.i(TAG, "onResult");
//                Person currentUser = personBuffer.get(0);
//                personBuffer.release();
//
//                if (currentUser.hasBirthday()) {
//                    String birth = currentUser.getBirthday();
//                    Log.i(TAG, birth);
//                }
//                if (currentUser.hasDisplayName()) {
//                    String name = currentUser.getDisplayName();
//                    Log.i(TAG, name);
//                }
//                if (currentUser.hasAgeRange()) {
//                    Person.AgeRange age = currentUser.getAgeRange();
//                    Log.i(TAG, age.toString());
//                }
//                if (currentUser.hasGender()) {
//                    int gender = currentUser.getGender();
//                    Log.i(TAG, Integer.toString(gender));
//                }
//            }
//        }
//    }
}
