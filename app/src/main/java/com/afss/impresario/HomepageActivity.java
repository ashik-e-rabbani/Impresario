package com.afss.impresario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class HomepageActivity extends AppCompatActivity {

    ActivityHomepageBinding homepageBinding;
    String amount;
    private static FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);

        if (database == null) {
            database=FirebaseDatabase.getInstance();
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


//        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users/USER_002");
        DatabaseReference myRef_reader = database.getReference("Users");

        homepageBinding.addExpenseAndIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Expense",myRef);
            }

        });

        homepageBinding.addExpenseAndIncome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Income",myRef);

                return false;
            }
        });

        homepageBinding.showTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef_reader.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String retrieve_amount = snapshot.getValue().toString();
                        ShowNotification();
                        homepageBinding.txnSummary.setText(retrieve_amount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }


    private String showAddExpenseAndIncomeDialog(Context c, String _amountType, DatabaseReference myRef) {
        final EditText expenseIncomeAmount = new EditText(c);
        expenseIncomeAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        expenseIncomeAmount.setHint("000.00");

        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Enter " + _amountType)
//                .setMessage("Enter your mobile number?")
                .setView(expenseIncomeAmount)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        amount = String.valueOf(expenseIncomeAmount.getText());

                        if (amount.isEmpty()) {
                            Toast.makeText(HomepageActivity.this, "Add Amount", Toast.LENGTH_SHORT).show();

                        } else {
                            myRef.setValue(amount);
                            expenseIncomeAmount.setText("");
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        return amount;
    }

    private void ShowNotification()
    {

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
        notificationManagerCompat.notify(1,builder.build());



    }

}