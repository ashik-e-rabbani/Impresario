package com.afss.impresario.Services;

import static com.google.firebase.crashlytics.internal.Logger.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUploader {
    String fileName;
    StorageReference storageRef ;
    String result = "";

    public String uploadFile(String selectedImageUri) {

        if (storageRef==null){
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmssSSS");
                Date now = new Date();
                String fileName = formatter.format(now);
                storageRef = FirebaseStorage.getInstance().getReference("TxnImages/"+fileName);

                //Store it offline first




                result = fileName;
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }

            Log.d(TAG, "pDebug Txn image uri " + selectedImageUri.toString());
            storageRef.putFile(Uri.parse(selectedImageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   result=fileName;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            });
        }


        return result;
    }
}
