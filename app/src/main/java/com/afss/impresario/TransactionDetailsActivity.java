package com.afss.impresario;

import static com.afss.impresario.utils.Converters.getTextFromJson;
import static com.afss.impresario.utils.Converters.isInternetConnected;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.afss.impresario.databinding.ActivityTransactionDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TransactionDetailsActivity extends AppCompatActivity {

    ActivityTransactionDetailsBinding transactionDetailsBinding;
    String amount, time, description, type, txn_image_uri,txnImageOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionDetailsBinding = ActivityTransactionDetailsBinding.inflate(getLayoutInflater());
        View view = transactionDetailsBinding.getRoot();
        setContentView(view);


        File cacheDir = getApplicationContext().getCacheDir();



        transactionDetailsBinding.imageView2.setVisibility(View.GONE);
        Intent myIntent = getIntent();
        amount = myIntent.getStringExtra("amount");
        time = myIntent.getStringExtra("time");
        type = myIntent.getStringExtra("type");
        description = myIntent.getStringExtra("description");
        txn_image_uri = description.contains("descImg") ? getTextFromJson(description, "descImg") : "no_image";
        txnImageOffline = description.contains("descImgOffline") ? getTextFromJson(description, "descImgOffline") : "no_image";

        StorageReference imageRef = null;
        File imageFile = new File(cacheDir, txn_image_uri);
        if (txn_image_uri != null && txn_image_uri!="no_image" && isInternetConnected(getApplicationContext())) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            imageRef = storageRef.child("TxnImages/" + txn_image_uri);
            Picasso.get()
                    .load(R.drawable.image_place_cholder)
                    .into(transactionDetailsBinding.imageView2);

            transactionDetailsBinding.imageView2.setVisibility(View.VISIBLE);

        }else {

            Toast.makeText(getBaseContext(), "Showing image offline", Toast.LENGTH_SHORT).show();
            byte[] decodedString = Base64.decode(txnImageOffline, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            transactionDetailsBinding.imageView2.setImageBitmap(bitmap);
            transactionDetailsBinding.imageView2.setVisibility(View.VISIBLE);
        }

        transactionDetailsBinding.amountText.setText(amount);
        transactionDetailsBinding.timeText.setText(time);

        transactionDetailsBinding.txnDescription.setText(getTextFromJson(description, "descText"));


        if (imageRef != null) {
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Use the download URL to load the image into the ImageView in online mode

                    Picasso.get()
                            .load(String.valueOf(uri))
                            .into(transactionDetailsBinding.imageView2);
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle any errors that occurred while generating the download URL
                    Toast.makeText(getBaseContext(), "Failed to load server image", Toast.LENGTH_SHORT).show();
                    byte[] decodedString = Base64.decode(txnImageOffline, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    transactionDetailsBinding.imageView2.setImageBitmap(bitmap);
                    transactionDetailsBinding.imageView2.setVisibility(View.VISIBLE);
                }
            });
        }


        if (type.contains("exp")) {
//               holder.avatarView.setBackgroundResource(rounded_expense_bg);
            transactionDetailsBinding.avatarHolder.setTextColor(Color.parseColor("#B71C1C"));
        }

        transactionDetailsBinding.goBackBtn.setOnClickListener(v -> onBackPressed());


    }


}