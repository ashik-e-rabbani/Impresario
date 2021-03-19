package com.afss.impresario.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{


    public RecyclerAdapter(ArrayList<String> moviesList, ArrayList<String> pathList) {
        this.moviesList = moviesList;
        this.pathList = pathList;
    }

    ArrayList<String> pathList;
    ArrayList<String> moviesList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater= LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.txnrecycler_layout,parent,false);
        ViewHolder viewHolder= new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



//        holder.editButton.setText(String.valueOf(position));
        holder.textView.setText(moviesList.get(position));
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogPlus dialog = DialogPlus.newDialog(holder.editButton.getContext())

                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_item_layout))
                        .setExpanded(true,1000)  // This will enable the expand feature, (similar to android L share dialog)
                        .create();


                View updateView = dialog.getHolderView();

                Button updateBtn = updateView.findViewById(R.id.update_amountBtn);
                EditText updateAmount = updateView.findViewById(R.id.update_amount);
                int selectedId = updateView.findViewById(R.id.amount_type_group).getId();

                // find the radiobutton by returned id
                RadioButton radioButton = (RadioButton) updateView.findViewById(R.id.amount_type_income);
                radioButton.setChecked(true);


                updateAmount.setText(moviesList.get(position));

                dialog.show();

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ////                update the data here
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference insertRtdbRef = database.getReference(pathList.get(position).toString());
//                Update child value
                insertRtdbRef.child("txn_amount").setValue(updateAmount.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                            }
                        });
                    }
                });





//                Snackbar.make(v,"Clicked "+pathList.get(position), BaseTransientBottomBar.LENGTH_SHORT).show();

//                insertRtdbRef.child("time_stamp").setValue("17:50:00");
//
//                //                Delete Data
////                insertRtdbRef.removeValue();
            }
        });

    }



    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView editButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView=itemView.findViewById(R.id.textView);
            editButton=itemView.findViewById(R.id.btn_edit);
        }
    }
}
