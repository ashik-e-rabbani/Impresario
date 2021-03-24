package com.afss.impresario.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


    public RecyclerAdapter(ArrayList<String> txnAmountList, ArrayList<String> pathList, ArrayList<String> txnTypeList, ArrayList<String> txnTimeList) {
        this.txnAmountList = txnAmountList;
        this.pathList = pathList;
        this.txnTypeList = txnTypeList;
        this.txnTimeList = txnTimeList;
    }

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    boolean updateDialogDismiss;

    ArrayList<String> pathList;
    ArrayList<String> txnAmountList;
    ArrayList<String> txnTypeList;
    ArrayList<String> txnTimeList;

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
        holder.timeview.setText(txnTimeList.get(position));
        holder.textView.setText(txnAmountList.get(position));
        if (txnTypeList.get(position).contains("exp"))
        {
            holder.avatarView.setBackgroundTintList(ColorStateList.valueOf(R.color.orange_200));
        }


        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogPlus dialog = DialogPlus.newDialog(holder.editButton.getContext())

                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_item_layout))
                        .setExpanded(true, 1000)  // This will enable the expand feature, (similar to android L share dialog)
                        .create();


                View updateView = dialog.getHolderView();
                ImageView deleteTransaction = updateView.findViewById(R.id.deleteTransactionBtn);
                ImageView dismissDialogByBack = updateView.findViewById(R.id.backFromUpdateDialog);
                Button updateBtn = updateView.findViewById(R.id.update_amountBtn);
                EditText updateAmount = updateView.findViewById(R.id.update_amount);
                int selectedId = updateView.findViewById(R.id.amount_type_group).getId();

                // find the radiobutton by returned id
                RadioButton radioButton = (RadioButton) updateView.findViewById(R.id.amount_type_income);
                radioButton.setChecked(true);


                updateAmount.setText(txnAmountList.get(position));

                dialog.show();

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ////                update the data here

                        databaseReference = database.getReference(pathList.get(position).toString());
//                Update child value
                        try {
                            databaseReference.child("txn_amount").setValue(updateAmount.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
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
                                dialog.dismiss();
                            }
                        }
                    }
                });

                deleteTransaction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        databaseReference = database.getReference(pathList.get(position).toString());

                        databaseReference.removeValue();
                        dialog.dismiss();
                        Snackbar.make(v, "Transaction Deleted", BaseTransientBottomBar.LENGTH_SHORT).show();

                    }
                });

                dismissDialogByBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

        TextView textView, timeview, avatarView;
        ImageView editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeview = itemView.findViewById(R.id.timeText);
            avatarView = itemView.findViewById(R.id.avatarHolder);
            textView = itemView.findViewById(R.id.textView);
            editButton = itemView.findViewById(R.id.btn_edit);
        }
    }
}
