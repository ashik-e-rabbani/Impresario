package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import com.afss.impresario.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    ProgressBar splashProgress;
    ActivitySplashBinding activitySplashBinding;
    int SPLASH_TIME = 2000; //This is 3 seconds
    private String GG_Email = null;
    private String GG_NAME = null;
    private String GG_ID = null;
    private static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        Check logged in or Not

        try {
            SharedPreferences myPrefs = this.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);
            GG_Email = myPrefs.getString("GG_Email", null);
            GG_NAME = myPrefs.getString("GG_NAME", null);
            GG_ID = myPrefs.getString("GG_ID", null);
            Log.d(TAG,"Credentials found in SharedPref");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"No Credentials found in SharedPref");
        }
        //This is additional feature, used to run a progress bar
        splashProgress = findViewById(R.id.splashProgress);
        playProgress();
        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do any action here. Now we are moving to next page
                if (GG_Email != null && GG_ID != null && GG_NAME != null) {
                    Intent HomePageIntent = new Intent(SplashActivity.this, HomepageActivity.class);
                    HomePageIntent.putExtra("GG_Email", GG_Email);
                    HomePageIntent.putExtra("GG_ID", GG_ID);
                    HomePageIntent.putExtra("GG_NAME", GG_NAME);
                    startActivity(HomePageIntent);
                    Log.d(TAG,"Credentials available Landing to Homepage");

                } else {
                    startActivity(new Intent(SplashActivity.this, GoogleSingInPageActivity.class));
                    Log.d(TAG,"Credentials not available Landing to Signing page");

                }
                //This 'finish()' is for exiting the app when back button pressed from Home page which is ActivityHome
                finish();

            }
        }, SPLASH_TIME);


    }

    //Method to run progress bar for 1.1 seconds
    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(2000)
                .start();
    }

}