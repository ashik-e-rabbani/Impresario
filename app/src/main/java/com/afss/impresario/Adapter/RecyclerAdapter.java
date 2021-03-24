package com.afss.impresario.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afss.impresario.R;
import com.afss.impresario.Services.JsonParsingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {




    public RecyclerAdapter(ArrayList<String> retrievesTransactions, ArrayList<String> transactionsPath) {
        this.retrievesTransactions = retrievesTransactions;
        this.transactionsPath = retrievesTransactions;

    }

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    boolean updateDialogDismiss;
    JSONObject json;
    String r_Amount;
    ArrayList<String> retrievesTransactions;
    private final ArrayList<String> transactionsPath;

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
        json = new JSONObject();
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



        String retrievesTransactionsJSON = retrievesTransactions.get(position);

        JsonParsingService jsonParsingService = new JsonParsingService();
        try {
            r_Amount = jsonParsingService.parse(retrievesTransactionsJSON,"txn_amount");
            Log.d("JSON", retrievesTransactionsJSON+" "+r_Amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        holder.editButton.setText(String.valueOf(position));
        holder.textView.setText(r_Amount);
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
                String retrievesTransactionsJSON = retrievesTransactions.get(position);
                JsonParsingService jsonParsingService = new JsonParsingService();
                try {
                    r_Amount = jsonParsingService.parse(retrievesTransactionsJSON,"txn_amount");
                    Log.d("JSON", retrievesTransactionsJSON+" "+r_Amount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateAmount.setText(r_Amount);

                dialog.show();

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ////                update the data here

                        databaseReference = database.getReference(transactionsPath.get(position).toString());
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

                        databaseReference = database.getReference(transactionsPath.get(position).toString());

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
        return retrievesTransactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
            editButton = itemView.findViewById(R.id.btn_edit);
        }
    }
}
