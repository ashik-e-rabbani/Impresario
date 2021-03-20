package com.afss.impresario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;

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

    ActivityHomepageBinding homepageBinding;
    String amount;
    String path;
    private static FirebaseDatabase database;

    String GG_Email, GG_ID, GG_NAME;
    String year, month;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);


//        Get passed Intent Data
        Intent myIntent = getIntent();
        GG_Email = myIntent.getStringExtra("GG_Email");
        GG_ID = myIntent.getStringExtra("GG_ID");
        GG_NAME = myIntent.getStringExtra("GG_NAME");
        homepageBinding.titleName.setText(GG_NAME);


//        Generating Date Year Month for hierarchy

        Date date = new Date();
        LocalDate localDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            year = Integer.toString(localDate.getYear());
            month = Integer.toString(localDate.getMonthValue());


        } else {

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            month = Integer.toString(cal.get(Calendar.MONTH));
            year = Integer.toString(cal.get(Calendar.YEAR));

        }

        final ArrayList<String> txnAmountList = new ArrayList<>();
        final ArrayList<String> txnAmountPathList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(txnAmountList, txnAmountPathList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }


        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(50); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "HomePage_show");

        sequence.setConfig(config);

        sequence.addSequenceItem(homepageBinding.addExpenseAndIncome,
                "Single Tap to Add expense and Hold to Add Income", "GOT IT");

        sequence.addSequenceItem(homepageBinding.showTransactions,
                "Clicking All Transaction will be visible to You", "GOT IT");


        sequence.start();

//        Connecting to FireBase DB
        path = "Users/" + GG_ID + "/" + year + "/" + month;
        DatabaseReference insertRtdbRef = database.getReference(path);

        DatabaseReference myRef_reader = database.getReference("Users/" + GG_ID + "/" + year + "/" + month);


        myRef_reader.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String retrieve_amount = snapshot.getValue().toString();
//                            ShowNotification();
                    txnAmountList.clear();
                    txnAmountPathList.clear();
                    for (DataSnapshot snapshotTxn : snapshot.getChildren()) {
                        txnAmountList.add(snapshotTxn.child("txn_amount").getValue().toString());
                        txnAmountPathList.add(path + "/" + snapshotTxn.getKey().toString());


                    }
                    Collections.reverse(txnAmountList);
                    Collections.reverse(txnAmountPathList);
                    recyclerView.setAdapter(recyclerAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "No Data Found", BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        homepageBinding.addExpenseAndIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Expense", insertRtdbRef);
            }

        });

        homepageBinding.addExpenseAndIncome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Income", insertRtdbRef);

                return false;
            }
        });

//        Show txns by button Click
        homepageBinding.showTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef_reader.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String retrieve_amount = snapshot.getValue().toString();
//                            ShowNotification();
                            txnAmountList.clear();
                            txnAmountPathList.clear();
                            for (DataSnapshot snapshotTxn : snapshot.getChildren()) {
                                txnAmountList.add(snapshotTxn.child("txn_amount").getValue() + " " + snapshotTxn.getKey().toString());
                                txnAmountPathList.add(path + "/" + snapshotTxn.getKey().toString());


                            }
                            Collections.reverse(txnAmountList);
                            Collections.reverse(txnAmountPathList);
                            recyclerView.setAdapter(recyclerAdapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar.make(view, "No Data Found", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }


    private String showAddExpenseAndIncomeDialog(Context c, String _amountType, DatabaseReference insertRtdbRef) {

        LayoutInflater inflater = this.getLayoutInflater();


        View dialogView = inflater.inflate(R.layout.alert_dialog_inputbox, null);
        final EditText expenseIncomeAmount = (EditText) dialogView.findViewById(R.id.inputedAmount);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(c);
        dialog.setTitle("Enter " + _amountType)
//                .setMessage("Enter your amount")
                .setView(dialogView)

                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        amount = String.valueOf(expenseIncomeAmount.getText());

                        if (amount.isEmpty()) {
                            Toast.makeText(HomepageActivity.this, "Add Amount", Toast.LENGTH_SHORT).show();

                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            TransactionsModel transactions = new TransactionsModel();

                            transactions.setTxn_amount(amount);

                            if (_amountType == "Income") {
                                transactions.setTxn_type("inc");
                            } else {
                                transactions.setTxn_type("exp");
                            }

                            transactions.setTime_stamp(currentTime);

                            insertRtdbRef.push()
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

    private void ShowNotification() {

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

}