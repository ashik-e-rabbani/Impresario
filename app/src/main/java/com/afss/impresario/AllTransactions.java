package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.afss.impresario.Adapter.RecyclerAdapter;
import com.afss.impresario.databinding.ActivityAllTransactionsBinding;

import java.util.ArrayList;
import java.util.Calendar;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AllTransactions extends AppCompatActivity {

    ActivityAllTransactionsBinding allTransactionsBinding;

    DatePickerDialog datePickerDialog;
    int year;
    int month;
    int dayOfMonth;
    Calendar calendar;
    String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allTransactionsBinding = ActivityAllTransactionsBinding.inflate(getLayoutInflater());
        View view = allTransactionsBinding.getRoot();
        setContentView(view);

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(50); // half second between each showcase view

        MaterialShowcaseSequence sequence2 = new MaterialShowcaseSequence(this, "HomePage_show");

        sequence2.setConfig(config);

        sequence2.addSequenceItem(allTransactionsBinding.monthPickerBtn,
                "Pick the Month", "GOT IT");

        sequence2.start();

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        final ArrayList<String> txnAmountList = new ArrayList<>();
        final ArrayList<String> txnAmountPathList = new ArrayList<>();
        final ArrayList<String> txnTypeList = new ArrayList<>();
        final ArrayList<String> txnTimeList = new ArrayList<>();


        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(txnAmountList, txnAmountPathList, txnTypeList, txnTimeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);


        allTransactionsBinding.goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        allTransactionsBinding.monthPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(AllTransactions.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                allTransactionsBinding.selectedDate.setText(day + "/" + (month +1) + "/" + year);
                                allTransactionsBinding.selectedDate.setText(months[month] + " " + year);
                            }
                        }, year, month, dayOfMonth);
//                datePickerDialog.getDatePicker().setMinDate();
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();


            }
        });

    }
}