package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.afss.impresario.databinding.ActivityTransactionDetailsBinding;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TransactionDetailsActivity extends AppCompatActivity {

    ActivityTransactionDetailsBinding transactionDetailsBinding;
    String amount,time,description,type,txn_image_uri;
    StorageReference storageRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionDetailsBinding = ActivityTransactionDetailsBinding.inflate(getLayoutInflater());
        View view = transactionDetailsBinding.getRoot();
        setContentView(view);

        if (storageRef==null){
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmssSSS");
                Date now = new Date();
                String fileName = formatter.format(now);
                storageRef = FirebaseStorage.getInstance().getReference("TxnImages/"+fileName);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }
        Intent myIntent = getIntent();
        amount = myIntent.getStringExtra("amount");
        time = myIntent.getStringExtra("time");
        type = myIntent.getStringExtra("type");
        description = myIntent.getStringExtra("description");
        txn_image_uri = myIntent.getStringExtra("txn_image_uri");


        transactionDetailsBinding.amountText.setText(amount);
        transactionDetailsBinding.timeText.setText(time);
        transactionDetailsBinding.txnDescription.setText(description+ txn_image_uri);

        String imageURL = "https://media.geeksforgeeks.org/wp-content/cdn-uploads/gfg_200x200-min.png";
        Picasso.get()
                .load(imageURL)
                .into(transactionDetailsBinding.imageView2);


        if (type.contains("exp")) {
//               holder.avatarView.setBackgroundResource(rounded_expense_bg);
            transactionDetailsBinding.avatarHolder.setTextColor(Color.parseColor("#B71C1C"));
        }

        transactionDetailsBinding.goBackBtn.setOnClickListener(v -> onBackPressed());


    }


}