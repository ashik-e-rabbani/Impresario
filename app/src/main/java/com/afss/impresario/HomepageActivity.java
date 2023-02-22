package com.afss.impresario;


import static com.afss.impresario.utils.Converters.stringToJson;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
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
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afss.impresario.Adapter.RecyclerAdapter;
import com.afss.impresario.Model.TransactionsModel;
import com.afss.impresario.Services.AlarmService;
import com.afss.impresario.Services.DataService;
import com.afss.impresario.Services.FileUploader;
import com.afss.impresario.databinding.ActivityHomepageBinding;
import com.afss.impresario.utils.Converters;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class HomepageActivity extends AppCompatActivity {

    private static final String TAG = "HomepageActivity";
    ActivityHomepageBinding homepageBinding;
    String amount, description;
    String path;
    static FirebaseDatabase database;
    static long back_pressed;
    String GG_Email, GG_ID, GG_NAME, BALANCEPREF, TXN_TYPE;
    String year, month;
    String balance, totalIncome, totalExpense;
    SharedPreferences myPrefs;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    DataService dataService;
    String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

    ArrayList<String> txnAmountList;
    ArrayList<String> txnAmountPathList;
    ArrayList<String> txnTypeList;
    ArrayList<String> txnTimeList;
    ArrayList<String> txnDescriptionList;
    ArrayList<String> txnImagePathList;

    Animation fadeInAnimation, fadeOutAnimation;

    String selectedImageUri = null;
    String offlineImage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);

        description = "";
        TXN_TYPE = "Expense";

        homepageBinding.incomeBalanceHolder.setVisibility(View.INVISIBLE);
        homepageBinding.expenseBalanceHolder.setVisibility(View.INVISIBLE);

        myPrefs = this.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

        try {
            Log.d(TAG, "SharedPref read");
            BALANCEPREF = myPrefs.getString("BALANCE", "123");
            if (BALANCEPREF.contains("-")) {
                homepageBinding.allBalanceHolder.setBackground(getDrawable(R.drawable.gradient_bg_red));

            } else {
                homepageBinding.allBalanceHolder.setBackground(getDrawable(R.drawable.gradient_bg));
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
        char avatarChar = GG_NAME.charAt(0);
        homepageBinding.topRightProfileMenu.setText(String.valueOf(avatarChar));
//        homepageBinding.emailPlaceholder.setText(GG_Email);
//        homepageBinding.titleName.setText(GG_NAME);


//        Generating Date Year Month for hierarchy

        Date date = new Date();
        LocalDate localDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        txnImagePathList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), txnAmountList, txnAmountPathList, txnTypeList, txnTimeList, txnDescriptionList);
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




        homepageBinding.transactionTxt.setText(months[Integer.parseInt(month) - 1] + " " + year);


        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(50); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "HomePage_show");

        sequence.setConfig(config);

        sequence.addSequenceItem(homepageBinding.addExpenseAndIncome,
                getString(R.string.addExpenseAndIncome), getString(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.transactionTxt,
                getString(R.string.transaction_recyclerView), getString(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.balance,
                getString(R.string.balance_showcase_text), getString(R.string.dismiss_text));
        sequence.addSequenceItem(homepageBinding.leftTopMenu,
                getString(R.string.leftTopMenu), getString(R.string.dismiss_text));


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

        homepageBinding.balance.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    Log.d(TAG, "Loaded  above lollipop");
                    // Do something for above lollipop and above versions
                    fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                    fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);

                    homepageBinding.incomeBalanceHolder.setVisibility(View.VISIBLE);
                    homepageBinding.expenseBalanceHolder.setVisibility(View.VISIBLE);
                    homepageBinding.expenseBalanceHolder.setAnimation(fadeInAnimation);
                    homepageBinding.incomeBalanceHolder.setAnimation(fadeInAnimation);

                    Timer t = new Timer();
                    t.schedule(new TimerTask() {


                        @Override
                        public void run() {
                            homepageBinding.expenseBalanceHolder.setAnimation(fadeOutAnimation);
                            homepageBinding.incomeBalanceHolder.setAnimation(fadeOutAnimation);
                            homepageBinding.incomeBalanceHolder.setVisibility(View.INVISIBLE);
                            homepageBinding.expenseBalanceHolder.setVisibility(View.INVISIBLE);

                        }
                    }, 10000);
                } else {
                    Log.d(TAG, "Loaded  lollipop or below");
                    // do something for phones running an SDK below or  lollipop
                    homepageBinding.incomeBalanceHolder.setVisibility(View.VISIBLE);
                    homepageBinding.expenseBalanceHolder.setVisibility(View.VISIBLE);


                }


                return false;
            }
        });

        homepageBinding.topRightProfileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ProfileIntent = new Intent(HomepageActivity.this, ProfileActivity.class);

                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(homepageBinding.topRightProfileMenu, "profile_avatar");
                ProfileIntent.putExtra("GG_Email", GG_Email);
                ProfileIntent.putExtra("GG_ID", GG_ID);
                ProfileIntent.putExtra("GG_NAME", GG_NAME);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(HomepageActivity.this, pairs);

                startActivity(ProfileIntent, options.toBundle());
            }
        });
    }

    public void TransactionsLoader(DatabaseReference myRef_reader, View view) {
//        DatabaseReference myRef_reader = database.getReference(path);

        myRef_reader.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
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
                        if (snapshotTxn.child("txn_description").getValue().toString().isEmpty()) {
                            txnDescriptionList.add("Edit to add description");
                        } else {
                            txnDescriptionList.add(snapshotTxn.child("txn_description").getValue().toString());
                        }
                    }

                    Collections.reverse(txnAmountList);
                    Collections.reverse(txnAmountPathList);
                    Collections.reverse(txnTypeList);
                    Collections.reverse(txnTimeList);
                    Collections.reverse(txnDescriptionList);


                    if (txnAmountList.size() == 0) {
                        homepageBinding.recyclerView.setVisibility(View.GONE);
                        showTooltip();
                    } else {
                        homepageBinding.recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(recyclerAdapter);
                    }

//                    recyclerView.setAdapter(recyclerAdapter);
                    widgetUpdater();
                    Log.d(TAG, "Server got new data");

                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "No Data Found", BaseTransientBottomBar.LENGTH_LONG).show();
                }


                dataService = new DataService();
                balance = dataService.getBalance(txnAmountList, txnTypeList);
                totalExpense = dataService.getTotalExpense(txnAmountList, txnTypeList);
                totalIncome = dataService.getTotalIncome(txnAmountList, txnTypeList);

                SharedPreferences.Editor editor = myPrefs.edit();
                Log.d(TAG, "SharedPref Edit hit");
                editor.putString("BALANCE", balance.toString());
                editor.commit();

                if (balance.contains("-")) {
                    homepageBinding.allBalanceHolder.setBackground(getDrawable(R.drawable.gradient_bg_red));
//                    homepageBinding.balance.setTextColor(Color.parseColor("#CCE53935"));

                } else {
                    homepageBinding.allBalanceHolder.setBackground(getDrawable(R.drawable.gradient_bg));
//                    homepageBinding.balance.setTextColor(Color.parseColor("#FBFAFB"));
                }
                homepageBinding.balance.setText("৳ " + balance);
                homepageBinding.expenseBalance.setText("৳ " + totalExpense);
                homepageBinding.incomeBalance.setText("৳ " + totalIncome);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showTooltip() {
        new SimpleTooltip.Builder(this)
                .anchorView(homepageBinding.addExpenseAndIncome)
                .text("Tap to begin")
                .gravity(Gravity.TOP)
                .animated(true)
                .dismissOnOutsideTouch(true)
                .dismissOnInsideTouch(true)
                .transparentOverlay(true)
                .build()
                .show();
    }


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    selectedImageUri = String.valueOf(uri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        byte[] byteArray = outputStream.toByteArray();
                        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                        Log.d("base64",base64String);
                        offlineImage = base64String;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private String showAddExpenseAndIncomeDialog(Context c, String _amountType, DatabaseReference databaseReference) {



        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.alert_dialog_inputbox, null);
        final EditText expenseIncomeAmount = (EditText) dialogView.findViewById(R.id.inputedAmount);
        final EditText expenseIncomeDescription = (EditText) dialogView.findViewById(R.id.inputedDescription);
        final MaterialButtonToggleGroup mtGrp = (MaterialButtonToggleGroup) dialogView.findViewById(R.id.txnTypeToggleGroup);
        final Button btnTypeExp = (Button) dialogView.findViewById(R.id.btnTypeExp);
        final Button btnTypeInc = (Button) dialogView.findViewById(R.id.btnTypeInc);
        final Button btnAddImg = (Button) dialogView.findViewById(R.id.add_desc_image);

//      set default expense type
        Log.d(TAG, "Default select" + mtGrp.getCheckedButtonId());
        TXN_TYPE = "Expense";

        mtGrp.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {

                if (checkedId == R.id.btnTypeExp) {
                    TXN_TYPE = "Expense";
                    mtGrp.check(R.id.btnTypeExp);
                    Log.d(TAG, "btn Type choosed EXP " + mtGrp.getCheckedButtonId());
                } else {
                    TXN_TYPE = "Income";
                    mtGrp.check(R.id.btnTypeInc);
                    Log.d(TAG, "btn Type choosed INc " + mtGrp.getCheckedButtonId());

                }
            }
        });

        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");

            }
        });

        // catching returned result

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(c);
//        dialog.setTitle("New Transaction")
//                .setMessage("Enter your amount")
        dialog.setView(dialogView)
                .setCancelable(false)

                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        amount = String.valueOf(expenseIncomeAmount.getText());
                        description = String.valueOf(expenseIncomeDescription.getText());

                        Log.d(TAG, "Amounts are :" + amount + description);


                        if (amount.isEmpty()) {
                            Toast.makeText(HomepageActivity.this, "Add Amount", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d(TAG, "pDebug Got Else");
                            SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM hh.mm aa");
                            String currentTime = sdf.format(new Date());
                            TransactionsModel transactions = new TransactionsModel();

                            transactions.setTxn_amount(amount);

                            if (TXN_TYPE == "Income") {
                                transactions.setTxn_type("inc");
                                Log.d(TAG, "pDebug INC type");
                            } else {
                                transactions.setTxn_type("exp");
                                Log.d(TAG, "pDebug Exp");
                            }


                            transactions.setTime_stamp(currentTime);
                            Log.d(TAG, "pDebug Txn info " + transactions);
                            Log.d(TAG, "pDebug Data stored to " + databaseReference);

                            if (selectedImageUri!=null)
                            {
                                FileUploader fileUploader = new FileUploader();
                                String fileName = fileUploader.uploadFile(selectedImageUri);

                                transactions.setTxn_description(stringToJson(description,fileName,offlineImage));
                            }else {
                                transactions.setTxn_description(stringToJson(description,"no_image","no_image"));
                            }

                            databaseReference.push()
                                    .setValue(transactions);
                            Log.d(TAG, "pDebug Data Pushed");



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