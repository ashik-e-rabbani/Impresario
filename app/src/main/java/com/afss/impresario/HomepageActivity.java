package com.afss.impresario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomepageActivity extends AppCompatActivity {

    ActivityHomepageBinding homepageBinding;
    String amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);



//        Connecting to FireBase DB
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users/USER_002");
        DatabaseReference myRef_reader = database.getReference("Users");

        homepageBinding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               amount = homepageBinding.expenseAmount.getText().toString();
               if (amount.isEmpty())
               {
                   Toast.makeText(HomepageActivity.this, "Add Amount", Toast.LENGTH_SHORT).show();
               }else {
                   myRef.setValue(amount);
                   homepageBinding.expenseAmount.setText("");
               }

            }
        });

        homepageBinding.showTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef_reader.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       String retrive_amount = snapshot.getValue().toString();

                       homepageBinding.txnSummary.setText(retrive_amount);
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
            }
        });

        // Write a message to the database
//
//        Toast.makeText(this, "DB connect"+" "+amount, Toast.LENGTH_SHORT).show();

    }

}