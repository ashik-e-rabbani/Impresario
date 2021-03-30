package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class GoogleSingInPageActivity<mGoogleSignInClient> extends AppCompatActivity {


    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "SigningCode";
    GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sing_in);


        findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

// Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        updateUI(account);


        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signIn();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
//                Uri personPhoto = acct.getPhotoUrl();
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

                Intent HomePageIntent = new Intent(GoogleSingInPageActivity.this, HomepageActivity.class);
                HomePageIntent.putExtra("GG_Email", personEmail);
                HomePageIntent.putExtra("GG_ID", personId);
                HomePageIntent.putExtra("GG_NAME", personName);
                startActivity(HomePageIntent);



                saveCredentials(personName, personEmail, personId);
            }

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

            if (e.getStatusCode() == 7) {
                findViewById(R.id.Offline_thumnail).setVisibility(View.VISIBLE);
                findViewById(R.id.no_internet_alert).setVisibility(View.VISIBLE);
            } else if (e.getStatusCode() == 12501) {
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            }
//            updateUI(null);
        }
    }


    private void saveCredentials(String personName, String personEmail, String personId) {
        try {
//        save to shared preferences
            SharedPreferences sharedpreferences = getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putString("GG_ID", personId);
            editor.putString("GG_NAME", personName);
            editor.putString("GG_Email", personEmail);

            editor.commit();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

    }


}