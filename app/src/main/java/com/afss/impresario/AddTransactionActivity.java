package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.afss.impresario.databinding.ActivityAddTransactionBinding;
import com.afss.impresario.databinding.ActivityHomepageBinding;

public class AddTransactionActivity extends AppCompatActivity {

    ActivityAddTransactionBinding addTransactionBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addTransactionBinding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        View view = addTransactionBinding.getRoot();
        setContentView(view);
    }
}