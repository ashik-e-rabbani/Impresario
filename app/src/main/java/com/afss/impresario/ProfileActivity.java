package com.afss.impresario;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.afss.impresario.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    String GG_Email, GG_ID, GG_NAME;
    SharedPreferences myPrefs;
   ActivityProfileBinding ProfileBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = ProfileBinding.getRoot();
        setContentView(view);

        try {
            myPrefs = this.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

            GG_Email = myPrefs.getString("GG_Email", "No mail found");
            GG_NAME = myPrefs.getString("GG_NAME", "No name found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        Get passed Intent Data
//        Intent myIntent = getIntent();
//        GG_Email = myIntent.getStringExtra("GG_Email");
//        GG_ID = myIntent.getStringExtra("GG_ID");
//        GG_NAME = myIntent.getStringExtra("GG_NAME");

char avatarChar  = GG_NAME.charAt(0);
        ProfileBinding.profileAvatar.setText(String.valueOf(avatarChar));
        ProfileBinding.profileEmail.setText(GG_Email);
        ProfileBinding.profileName.setText(GG_NAME);


        ProfileBinding.goBackBtn.setOnClickListener(v -> onBackPressed());

    }

}