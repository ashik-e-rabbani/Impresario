package com.afss.impresario.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afss.impresario.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        holder.rowTextView.setText(String.valueOf(position));
        holder.textView.setText(moviesList.get(position));
        holder.rowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"Clicked "+pathList.get(position), BaseTransientBottomBar.LENGTH_SHORT).show();
//                update the data here
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference insertRtdbRef = database.getReference(pathList.get(position).toString());
//                Update child value
                insertRtdbRef.child("txn_amount").setValue("555");
                insertRtdbRef.child("time_stamp").setValue("17:50:00");

                //                Delete Data
//                insertRtdbRef.removeValue();
            }
        });

    }



    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        Button rowTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView);
            textView=itemView.findViewById(R.id.textView);
            rowTextView=itemView.findViewById(R.id.rowTextView);
        }
    }
}
