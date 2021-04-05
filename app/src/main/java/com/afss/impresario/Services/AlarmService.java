package com.afss.impresario.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.afss.impresario.HomepageActivity;

public class AlarmService extends BroadcastReceiver {

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(Context context, Intent intent) {


        //you can check the log that it is fired
        //Here we are actually not doing anything
        //but you can do any task here that you want to be done at a specific time everyday
        Log.d("Alarm", "Alarm just fired");

        Toast.makeText(context, "Alarm", Toast.LENGTH_SHORT).show();
    }

}
