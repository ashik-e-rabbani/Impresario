package com.afss.impresario;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.afss.impresario.Adapter.RecyclerAdapter;
import com.afss.impresario.Services.DataService;
import com.afss.impresario.databinding.ActivityAllTransactionsBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AllTransactions extends AppCompatActivity {

    private static final String TAG = "AllTransactions";
    ActivityAllTransactionsBinding allTransactionsBinding;
    private static FirebaseDatabase database;
    String path;
    DatePickerDialog datePickerDialog;
    int year;
    int month;
    int dayOfMonth;
    String GG_Email, GG_ID, GG_NAME, FILTERED_MONTH,FILTERED_MONTH_INC, FILTERED_DAY, FILTERED_YEAR;
    SharedPreferences myPrefs;
    Calendar calendar;
    String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    DataService dataService;
    String balance;

    ArrayList<String> txnAmountList;
    ArrayList<String> txnAmountPathList;
    ArrayList<String> txnTypeList;
    ArrayList<String> txnTimeList;
    ArrayList<String> txnDescriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allTransactionsBinding = ActivityAllTransactionsBinding.inflate(getLayoutInflater());
        View view = allTransactionsBinding.getRoot();
        setContentView(view);

        try {
            SharedPreferences myPrefs = this.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);
            GG_Email = myPrefs.getString("GG_Email", null);
            GG_NAME = myPrefs.getString("GG_NAME", null);
            GG_ID = myPrefs.getString("GG_ID", null);
            FILTERED_MONTH = myPrefs.getString("FILTERED_MONTH", null);
            FILTERED_MONTH_INC = myPrefs.getString("FILTERED_MONTH_INC", null);
            FILTERED_DAY= myPrefs.getString("FILTERED_DAY", null);
            FILTERED_YEAR = myPrefs.getString("FILTERED_YEAR", null);
            Log.d(TAG,"Credentials found in SharedPref");
            allTransactionsBinding.selectedDate.setText(months[Integer.parseInt(FILTERED_MONTH)] + " " + FILTERED_YEAR);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
            Log.e(TAG,"No Credentials found in SharedPref");
        }


        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        txnAmountList = new ArrayList<>();
        txnAmountPathList = new ArrayList<>();
        txnTypeList = new ArrayList<>();
        txnTimeList = new ArrayList<>();
        txnDescriptionList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(txnAmountList, txnAmountPathList, txnTypeList, txnTimeList, txnDescriptionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        database = FirebaseDatabase.getInstance();
        if (database == null) {
            try {
                database = FirebaseDatabase.getInstance();
                database.setPersistenceEnabled(true);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }

        //        Connecting to FireBase DB
        try {
            path = "Users/" + GG_ID + "/" + FILTERED_YEAR + "/" + FILTERED_MONTH_INC;
            Log.d(TAG,"Saved Path "+path);
            TransactionsLoader(path,view);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        allTransactionsBinding.goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        allTransactionsBinding.monthPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    calendar = Calendar.getInstance();
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(AllTransactions.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int pickedYear, int pickedMonth, int day) {
    //                                allTransactionsBinding.selectedDate.setText(day + "/" + (month +1) + "/" + year);
                                    allTransactionsBinding.selectedDate.setText(months[pickedMonth] + " " + pickedYear);
                                    int incrementedPickedMonth = pickedMonth+1;
                                    path = "Users/" + GG_ID + "/" + pickedYear + "/" + incrementedPickedMonth;
                                    Log.d(TAG,"Changed Path "+path);
                                    TransactionsLoader(path, v);
                                    saveFilter(String.valueOf(pickedMonth),String.valueOf(incrementedPickedMonth),String.valueOf(day), String.valueOf(pickedYear));

                                }


                            }, year, month, dayOfMonth);


//                datePickerDialog.getDatePicker().setMinDate();
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePickerDialog.show();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    e.printStackTrace();
                }


            }
        });

//        allTransactionsBinding.downloadReport.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(AllTransactions.this,collapsing.class);
//                startActivity(i);
//            }
//        });

    }


    public void TransactionsLoader(String path, View view)
    {
        DatabaseReference myRef_reader = database.getReference(path);

        myRef_reader.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {

//                            ShowNotification();
                    txnAmountList.clear();
                    txnAmountPathList.clear();
                    txnTimeList.clear();
                    txnTypeList.clear();
                    txnDescriptionList.clear();
                    for (DataSnapshot snapshotTxn : snapshot.getChildren()) {
                        txnAmountList.add(snapshotTxn.child("txn_amount").getValue().toString());
                        txnAmountPathList.add(path + "/" + snapshotTxn.getKey().toString());
                        txnTimeList.add(snapshotTxn.child("time_stamp").getValue().toString());
                        txnTypeList.add(snapshotTxn.child("txn_type").getValue().toString());
                        txnDescriptionList.add(snapshotTxn.child("txn_description").getValue().toString());
                    }

                    Collections.reverse(txnAmountList);
                    Collections.reverse(txnAmountPathList);
                    Collections.reverse(txnTypeList);
                    Collections.reverse(txnTimeList);
                    Collections.reverse(txnDescriptionList);
                    recyclerView.setAdapter(recyclerAdapter);

                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    e.printStackTrace();
                    txnAmountList.clear();
                    txnAmountPathList.clear();
                    txnTimeList.clear();
                    txnTypeList.clear();
                    txnDescriptionList.clear();
                    Snackbar.make(view, "No Data Found "+path, BaseTransientBottomBar.LENGTH_LONG).show();
                }


                dataService = new DataService();
                balance = dataService.getBalance(txnAmountList, txnTypeList);

                if (balance.contains("-")) {
                    allTransactionsBinding.balance.setTextColor(Color.parseColor("#B71C1C"));
                    allTransactionsBinding.balance.setText("৳ " + balance);

                } else {
                    allTransactionsBinding.balance.setTextColor(Color.parseColor("#FF2196F3"));
                    allTransactionsBinding.balance.setText("৳ " + balance);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveFilter(String month,String incrementedMonth, String day, String year) {
        try {
//        save to shared preferences
            SharedPreferences sharedpreferences = getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putString("FILTERED_MONTH", month);
            editor.putString("FILTERED_MONTH_INC", incrementedMonth);
            editor.putString("FILTERED_DAY", day);
            editor.putString("FILTERED_YEAR", year);

            editor.commit();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

    }
}