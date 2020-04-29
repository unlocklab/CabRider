package com.example.cabbooking.rider.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.cabbooking.rider.R;

public class Payout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout);
        initUI();
    }

    private void initUI() {

    }
    public void LoginNow(View v){
        finish();
    }


    public void NavNow(View v){
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
