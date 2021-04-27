package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.afss.impresario.databinding.ActivityTransactionDetailsBinding;


public class TransactionDetailsActivity extends AppCompatActivity {

    ActivityTransactionDetailsBinding transactionDetailsBinding;
    String amount,time,description,type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionDetailsBinding = ActivityTransactionDetailsBinding.inflate(getLayoutInflater());
        View view = transactionDetailsBinding.getRoot();
        setContentView(view);

        Intent myIntent = getIntent();
        amount = myIntent.getStringExtra("amount");
        time = myIntent.getStringExtra("time");
        description = myIntent.getStringExtra("description");
        type = myIntent.getStringExtra("type");

        transactionDetailsBinding.amountText.setText(amount);
        transactionDetailsBinding.timeText.setText(time);
        transactionDetailsBinding.txnDescription.setText(description);

        if (type.contains("exp")) {
//               holder.avatarView.setBackgroundResource(rounded_expense_bg);
            transactionDetailsBinding.avatarHolder.setTextColor(Color.parseColor("#B71C1C"));
        }

        transactionDetailsBinding.goBackBtn.setOnClickListener(v -> onBackPressed());


    }
}