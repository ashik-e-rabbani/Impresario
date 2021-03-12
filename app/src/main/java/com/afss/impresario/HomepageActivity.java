package com.afss.impresario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
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

public class HomepageActivity extends AppCompatActivity {

    ActivityHomepageBinding homepageBinding;
    String amount;
    DatabaseReference myRef;
    DatabaseReference myRef_reader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homepageBinding = ActivityHomepageBinding.inflate(getLayoutInflater());
        View view = homepageBinding.getRoot();
        setContentView(view);


//        Connecting to FireBase DB
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users/USER_002");
        myRef_reader = database.getReference("Users");

        homepageBinding.addExpenseAndIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Expense");
            }

        });

        homepageBinding.addExpenseAndIncome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showAddExpenseAndIncomeDialog(HomepageActivity.this, "Income");

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

                        homepageBinding.txnSummary.setText(retrieve_amount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }


    private String showAddExpenseAndIncomeDialog(Context c, String _amountType) {
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

}