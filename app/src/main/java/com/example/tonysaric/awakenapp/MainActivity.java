package com.example.tonysaric.awakenapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        try {
//
//            TextView outputView = (TextView) findViewById(R.id.status);
//            URL URL = new URL("https://www.awakenforandroid.herokuapp.com/users");
//
//            HttpsURLConnection connection = (HttpsURLConnection) URL.openConnection();
//
//            String urlParameters = "state=2a89e54d3148364e508e1b7010590801157fcfa3a0b4719a&code=4/IiGs8QkurM27SBwwb_GwtVy_IBVLmZRXd65ClEWDUvY";
//
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
//            connection.setRequestProperty("ACEPT-LANGUAGE", "en-US,en;0.5");
//
//            connection.setDoOutput(true);
//            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
//
//            dStream.writeBytes(urlParameters);
//            dStream.flush();
//            dStream.close();
//
//            int responseCode = connection.getResponseCode();
//            String output = "Request URL " + url;
//            output += System.getProperty("line.separator") + "Request Parameters " + urlParameters;
//            output += System.getProperty("line.separator") + "Request Code " + responseCode;
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line = "";
//            StringBuilder responseOutput = new StringBuilder();
//
//            responseOutput.append(line);
//
//            br.close();
//
//            output += System.getProperty("line.separtor") + responseOutput.toString();
//
//            outputView.setText(output);
//
//
//
//        } catch (MalformedURLException e){
//            e.printStackTrace();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                signOut();
                revokeAccess();

            }
        });

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

    }


    @Override
    public void onStart() {
        //revokeAccess();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //updateUI(true);
            Intent homeActivity = new Intent(getApplicationContext(), Home.class);
            startActivity(homeActivity);

        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivity(signInIntent);

        if (mGoogleApiClient.isConnected()) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }

    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        if (mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.disconnect();
                            mGoogleApiClient.connect();
                            // updateUI(false);
                            System.err.println("LOG OUT ^^^^^^^^^^^^^^^^^^^^ SUCESS");
                        }
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        if (mGoogleApiClient.isConnected() && mGoogleApiClient != null) {
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            //updateUI(false);
                            // [END_EXCLUDE]
                        }
                    });
        }
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

//    private void updateUI(boolean signedIn) {
//        if (signedIn) {
//            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
//        } else {
//            mStatusTextView.setText(R.string.signed_out);
//
//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
//        }
//    }
}


//import android.content.Intent;
//import android.content.IntentSender;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Toast;
//
//import com.google.android.gms.auth.GoogleAuthException;
//import com.google.android.gms.auth.GoogleAuthUtil;
//import com.google.android.gms.auth.UserRecoverableAuthException;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.plus.Plus;
//import com.google.gson.JsonObject;
//import com.koushikdutta.async.future.FutureCallback;
//import com.koushikdutta.ion.Ion;
//
//import java.io.IOException;
//
//public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
//    /**
//     * In order to be able to access the user login and email
//     */
//    private static final String LOGIN_SCOPES = "https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/userinfo.email";
//
//    private static final String SCOPES = "oauth2:server:client_id:" + BuildConfig.GOOGLE_SERVER_CLIENT_ID + ":api_scope:" + LOGIN_SCOPES;
//
//    /**
//     * Request code used to invoke sign in user interactions
//     */
//    private static final int SIGN_IN_REQUEST_CODE = 0;
//
//    private static final int AUTH_CODE_REQUEST_CODE = 2000;
//
//    private GoogleApiClient googleApiClient;
//
//    /**
//     * True if the sign-in button was clicked.  When true, we know to resolve all
//     * issues preventing sign-in without waiting.
//     */
//    private boolean signInClicked;
//
//    /**
//     * True if we are in the process of resolving a ConnectionResult
//     */
//    private boolean intentInProgress;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        findViewById(R.id.sign_in_button).setOnClickListener(this);
//
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_LOGIN)
//                .build();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if (googleApiClient.isConnected()) {
//            googleApiClient.disconnect();
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        if (!intentInProgress) {
//            if (signInClicked && result.hasResolution()) {
//                // The user has already clicked 'sign-in' so we attempt to resolve all
//                // errors until the user is signed in, or they cancel.
//                try {
//                    result.startResolutionForResult(this, SIGN_IN_REQUEST_CODE);
//                    intentInProgress = true;
//                } catch (IntentSender.SendIntentException e) {
//                    // The intent was canceled before it was sent.  Return to the default
//                    // state and attempt to connect to get an updated ConnectionResult.
//                    intentInProgress = false;
//                    googleApiClient.connect();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onClick(View view) {
//        if (view.getId() == R.id.sign_in_button && !googleApiClient.isConnecting()) {
//            if (googleApiClient.isConnected()) {
//                Plus.AccountApi.clearDefaultAccount(googleApiClient);
//                googleApiClient.disconnect();
//                Toast.makeText(this, "User is disconnected!", Toast.LENGTH_LONG).show();
//            } else {
//                signInClicked = true;
//                googleApiClient.connect();
//                Intent homeActivity = new Intent(getApplicationContext(), Home.class);
//                startActivity(homeActivity);
//            }
//        }
//    }
//
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        signInClicked = false;
//
//        new AsyncTask<Object, Void, Void>() {
//            @Override
//            protected Void doInBackground(Object... params) {
//                final String code = requestOneTimeCodeFromGoogle();
//
//                if (code != null)
//                    sendAuthorizationToServer(code);
//
//                return null;
//            }
//        }.execute();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        googleApiClient.connect();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
//        switch (requestCode) {
//            case SIGN_IN_REQUEST_CODE:
//                if (responseCode != RESULT_OK) {
//                    signInClicked = false;
//                }
//
//                intentInProgress = false;
//
//                if (!googleApiClient.isConnected()) {
//                    googleApiClient.reconnect();
//                }
//                break;
//            case AUTH_CODE_REQUEST_CODE:
//                if (responseCode == RESULT_OK)
//                    // the authorization is granted, now we retry to connect
//                    googleApiClient.connect();
//                break;
//        }
//    }
//
//    /**
//     * This method must be called on a background thread
//     */
//    private String requestOneTimeCodeFromGoogle() {
//        try {
//            return GoogleAuthUtil.getToken(
//                    this,
//                    Plus.AccountApi.getAccountName(googleApiClient),
//                    SCOPES
//            );
//        } catch (IOException transientEx) {
//            // network or server error, the call is expected to succeed if you try again later.
//            // Don't attempt to call again immediately - the request is likely to
//            // fail, you'll hit quotas or back-off.
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(MainActivity.this,
//                            "Network or server error, the call is expected to succeed if you try again later",
//                            Toast.LENGTH_LONG
//                    ).show();
//                }
//            });
//            return null;
//        } catch (UserRecoverableAuthException e) {
//            // Requesting an authorization code will always throw
//            // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
//            // because the user must consent to offline access to their data.  After
//            // consent is granted control is returned to your activity in onActivityResult
//            // and the second call to GoogleAuthUtil.getToken will succeed.
//            startActivityForResult(e.getIntent(), AUTH_CODE_REQUEST_CODE);
//            return null;
//        } catch (GoogleAuthException authEx) {
//            // Failure. The call is not expected to ever succeed so it should not be
//            // retried.
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(
//                            MainActivity.this,
//                            "Permanent error, something is wrong with your configuration.",
//                            Toast.LENGTH_LONG
//                    ).show();
//                }
//            });
//            return null;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void sendAuthorizationToServer(final String code) {
//        Ion.with(this)
//                .load(BuildConfig.BASE_URL + "/auth/google_oauth2/callback")
//                .setBodyParameter("code", code)
//                .setBodyParameter("redirect_uri", BuildConfig.GOOGLE_REDIRECT_URI)
//                .asJsonObject()
//                .setCallback(new FutureCallback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject result) {
//                        // Invalidate the token as soon as the server consumed it.
//                        GoogleAuthUtil.invalidateToken(getApplicationContext(), code);
//
//                    }
//                });
//    }
//}
