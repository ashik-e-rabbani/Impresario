package com.afss.impresario.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afss.impresario.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.afss.impresario.R.drawable.rounded_expense_bg;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


    public RecyclerAdapter(ArrayList<String> txnAmountList, ArrayList<String> txnPathList, ArrayList<String> txnTypeList, ArrayList<String> txnTimeList, ArrayList<String> txnDescriptionList) {
        this.txnAmountList = txnAmountList;
        this.txnPathList = txnPathList;
        this.txnTypeList = txnTypeList;
        this.txnTimeList = txnTimeList;
        this.txnDescriptionList = txnDescriptionList;
    }

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    boolean updateDialogDismiss;

    ArrayList<String> txnPathList, txnAmountList, txnTypeList, txnTimeList, txnDescriptionList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.txnrecycler_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        database = FirebaseDatabase.getInstance();
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }

        updateDialogDismiss = true;

        return viewHolder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


//        holder.editButton.setText(String.valueOf(position));
        holder.timeView.setText(txnTimeList.get(position));
        holder.txn_description.setText(txnDescriptionList.get(position));
        holder.amountText.setText(txnAmountList.get(position));
        if (txnTypeList.get(position).contains("exp")) {

            holder.avatarView.setTextColor(Color.parseColor("#B71C1C"));
        }

        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogPlus dialog = DialogPlus.newDialog(holder.itemContainer.getContext())

                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_item_layout))
                        .setExpanded(false, 700)  // This will enable the expand feature, (similar to android L share dialog)
                        .setOnBackPressListener(dialogPlus -> dialogPlus.dismiss())
                        .create();


                View updateView = dialog.getHolderView();
                ImageView deleteTransaction = updateView.findViewById(R.id.deleteTransactionBtn);
                ImageView dismissDialogByBack = updateView.findViewById(R.id.backFromUpdateDialog);
                Button updateBtn = updateView.findViewById(R.id.update_amountBtn);
                EditText updateAmount = updateView.findViewById(R.id.update_amount);
                TextView timeTextView = updateView.findViewById(R.id.timeTextView);
                EditText update_description = updateView.findViewById(R.id.update_description);
                timeTextView.setText(txnTimeList.get(position));

                // find the radiobutton by returned id
                RadioButton incomeRadioBtn = (RadioButton) updateView.findViewById(R.id.amount_type_income);
                RadioButton expenseRadioBtn = (RadioButton) updateView.findViewById(R.id.amount_type_expense);

                if (txnTypeList.get(position).contains("exp")) {
                    expenseRadioBtn.setChecked(true);
                    incomeRadioBtn.setChecked(false);
                } else {
                    expenseRadioBtn.setChecked(false);
                    incomeRadioBtn.setChecked(true);
                }


                updateAmount.setText(txnAmountList.get(position));
                update_description.setText(txnDescriptionList.get(position));
                dialog.show();

                expenseRadioBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expenseRadioBtn.setChecked(true);
                        incomeRadioBtn.setChecked(false);
                    }
                });

                incomeRadioBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expenseRadioBtn.setChecked(false);
                        incomeRadioBtn.setChecked(true);
                    }
                });


                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //                update the data here
                        HashMap updateDataMap = new HashMap();
                        updateDataMap.put("txn_amount", updateAmount.getText().toString());
                        updateDataMap.put("txn_description", update_description.getText().toString());
                        if (expenseRadioBtn.isChecked()) {
                            updateDataMap.put("txn_type", "exp");
                        } else {
                            updateDataMap.put("txn_type", "inc");
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM hh.mm aa");
                        String currentTime = sdf.format(new Date());
                        updateDataMap.put("time_stamp", currentTime);

                        databaseReference = database.getReference(txnPathList.get(position).toString());
//                Update child value
                        try {
//                            databaseReference.child("txn_amount").setValue(updateAmount.getText().toString())
                            databaseReference.updateChildren(updateDataMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            updateView.clearFocus();
                                            dialog.dismiss();
                                            updateDialogDismiss = false;
                                        }

                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("ADA", "KAJ");
                        } finally {
                            if (updateDialogDismiss == true) {
                                updateView.clearFocus();
                                dialog.dismiss();

                            }
                        }
                    }
                });

                deleteTransaction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        databaseReference = database.getReference(txnPathList.get(position).toString());

                        databaseReference.removeValue();
                        dialog.dismiss();
                        Snackbar.make(v, "Transaction Deleted", BaseTransientBottomBar.LENGTH_SHORT).show();

                    }
                });

                dismissDialogByBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateView.clearFocus();
                        dialog.dismiss();
                    }
                });
            }
        });

    }


    @Override
    public int getItemCount() {
        return txnAmountList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView amountText, timeView, avatarView, txn_description;
        ImageView editButton;
        LinearLayout itemContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            timeView = itemView.findViewById(R.id.timeText);
            txn_description = itemView.findViewById(R.id.txn_description);
            avatarView = itemView.findViewById(R.id.avatarHolder);
            amountText = itemView.findViewById(R.id.amountText);
            editButton = itemView.findViewById(R.id.btn_edit);
        }
    }
}
