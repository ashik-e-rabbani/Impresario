package com.afss.impresario;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class homepage extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;

//    private long lastPressedTime;
//    private static final int PERIOD = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }




//    @Override
//    public void onBackPressed() {
////        moveTaskToBack(true);
//    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            switch (event.getAction()) {
//                case KeyEvent.ACTION_DOWN:
//                    if (event.getDownTime() - lastPressedTime < PERIOD) {
//                        finish();
//                        System.exit(0);
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Press again to exit.",
//                                Toast.LENGTH_SHORT).show();
//                        lastPressedTime = event.getEventTime();
//                    }
//                    return true;
//            }
//        }
//        return false;
//    }
//
//}
}