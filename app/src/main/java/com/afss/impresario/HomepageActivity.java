package com.afss.impresario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afss.impresario.Adapter.RecyclerAdapter;
import com.afss.impresario.Model.TransactionsModel;
import com.afss.impresario.Services.AlarmService;
import com.afss.impresario.Services.DataService;
import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.afss.impresario.R.layout.txnlist_layout;

public class HomepageActivity extends AppCompatActivity {

    private static final String TAG = "HomepageActivity";
    ActivityHomepageBinding homepageBinding;
    String amount, description;
    String path;
    private static FirebaseDatabase database;
    private static long back_pressed;
    String GG_Email, GG_ID, GG_NAME, BALANCEPREF;
    String year, month;
    String balance;
    SharedPreferences myPrefs;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    DataService dataService;

    ArrayList<String> txnAmountList;
    ArrayList<String> txnAmountPathList;
    ArrayList<String> txnTypeList;
    ArrayList<String> txnTimeList;
    ArrayList<String> txnDescriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);

        description = "";

        myPrefs = this.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

        try {

            BALANCEPREF = myPrefs.getString("BALANCE", "123");
            if (BALANCEPREF.contains("-")) {
                homepageBinding.balance.setTextColor(Color.parseColor("#B71C1C"));

            } else {
                homepageBinding.balance.setTextColor(Color.parseColor("#FF2196F3"));
            }
            homepageBinding.balance.setText("৳ " + balance);


            Log.d(TAG, "Balance found in SharedPref");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "No balance found in SharedPref");
        }

//        Get passed Intent Data
        Intent myIntent = getIntent();
        GG_Email = myIntent.getStringExtra("GG_Email");
        GG_ID = myIntent.getStringExtra("GG_ID");
        GG_NAME = myIntent.getStringExtra("GG_NAME");
        homepageBinding.emailPlaceholder.setText(GG_Email);
        homepageBinding.titleName.setText(GG_NAME);


//        Generating Date Year Month for hierarchy

        Date date = new Date();
        LocalDate localDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            year = Integer.toString(localDate.getYear());
            month = Integer.toString(localDate.getMonthValue());
            Log.d(TAG, "Parsing for OS OREO (or above)'s Month " + month);

        } else {

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            month = Integer.toString(cal.get(Calendar.MONTH) + 1);
            year = Integer.toString(cal.get(Calendar.YEAR));
            Log.d(TAG, "Parsing OS below OREO's Month " + month);

        }

        txnAmountList = new ArrayList<>();
        txnAmountPathList = new ArrayList<>();
        txnTypeList = new ArrayList<>();
        txnTimeList = new ArrayList<>();
        txnDescriptionList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(txnAmountList, txnAmountPathList, txnTypeList, txnTimeList, txnDescriptionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        if (database == null) {
            try {
                database = FirebaseDatabase.getInstance();
                database.setPersistenceEnabled(true);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }


        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(50); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "HomePage_show");

        sequence.setConfig(config);

        sequence.addSequenceItem(homepageBinding.addExpenseAndIncome,
                String.valueOf(R.string.addExpenseAndIncome), String.valueOf(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.recyclerView,
                String.valueOf(R.string.transaction_recyclerView), String.valueOf(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.balance,
                String.valueOf(R.string.balance_showcase_text), String.valueOf(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.leftTopMenu,
                String.valueOf(R.string.leftTopMenu), String.valueOf(R.string.dismiss_text));



        sequence.start();

//        Connecting to FireBase DB
        path = "Users/" + GG_ID + "/" + year + "/" + month;
        DatabaseReference databaseReference = database.getReference(path);
        TransactionsLoader(databaseReference, view);

        homepageBinding.addExpenseAndIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Expense", databaseReference);
            }

        });

        homepageBinding.addExpenseAndIncome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Income", databaseReference);

                return false;
            }
        });

        homepageBinding.viewAllTxn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, AllTransactions.class);
                startActivity(intent);
            }
        });

        homepageBinding.leftTopMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(homepageBinding.leftTopMenu.getContext());
                dialog.setView(R.layout.developer_info_layout)
                        .show();
//                .setMessage("Enter your amount")
            }
        });

    }

    public void TransactionsLoader(DatabaseReference myRef_reader, View view) {
//        DatabaseReference myRef_reader = database.getReference(path);

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
                    widgetUpdater();
                    Log.d(TAG,"Server got new data");

                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "No Data Found", BaseTransientBottomBar.LENGTH_LONG).show();
                }


                dataService = new DataService();
                balance = dataService.getBalance(txnAmountList, txnTypeList);

                SharedPreferences.Editor editor = myPrefs.edit();

                editor.putString("BALANCE", balance.toString());
                editor.commit();

                if (balance.contains("-")) {
                    homepageBinding.balance.setTextColor(Color.parseColor("#B71C1C"));

                } else {
                    homepageBinding.balance.setTextColor(Color.parseColor("#FF2196F3"));
                }
                homepageBinding.balance.setText("৳ " + balance);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String showAddExpenseAndIncomeDialog(Context c, String _amountType, DatabaseReference databaseReference) {

        LayoutInflater inflater = this.getLayoutInflater();


        View dialogView = inflater.inflate(R.layout.alert_dialog_inputbox, null);
        final EditText expenseIncomeAmount = (EditText) dialogView.findViewById(R.id.inputedAmount);
        final EditText expenseIncomeDescription = (EditText) dialogView.findViewById(R.id.inputedDescription);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(c);
        dialog.setTitle("Enter " + _amountType)
//                .setMessage("Enter your amount")
                .setView(dialogView)

                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        amount = String.valueOf(expenseIncomeAmount.getText());
                        description = String.valueOf(expenseIncomeDescription.getText());

                        if (amount.isEmpty()) {
                            Toast.makeText(HomepageActivity.this, "Add Amount", Toast.LENGTH_SHORT).show();

                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM hh.mm aa");
                            String currentTime = sdf.format(new Date());
                            TransactionsModel transactions = new TransactionsModel();

                            transactions.setTxn_amount(amount);

                            if (_amountType == "Income") {
                                transactions.setTxn_type("inc");
                            } else {
                                transactions.setTxn_type("exp");
                            }
                            transactions.setTxn_description(description);

                            transactions.setTime_stamp(currentTime);
                            Log.d(TAG, "Data stored to " + databaseReference);
                            databaseReference.push()
                                    .setValue(transactions);
                            expenseIncomeAmount.setText("");
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        return amount;
    }

    public void ShowNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Amount_info_channel";
            String description = "Onkkk";
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel("Amount_info_channel", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(HomepageActivity.this, "Amount_info_channel")
                .setSmallIcon(R.drawable.ic_plusminus)
                .setContentTitle("I am Title")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(alarmSound)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(HomepageActivity.this);
        notificationManagerCompat.notify(1, builder.build());


    }

    @Override
    protected void onPause() {
        super.onPause();
        widgetUpdater();
        Log.d(TAG, "App send to Background");

    }

    @Override
    protected void onStop() {
        super.onStop();
        widgetUpdater();
        Log.d(TAG, "App Stopped");
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            widgetUpdater();
            Log.d(TAG, "App Exited by Back button");

        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    public void widgetUpdater() {
        Intent intent = new Intent(this, BalanceWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), BalanceWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }


    private void setAlarm(long time) {
        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, AlarmService.class);

        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi);
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
    }
}