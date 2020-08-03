package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.services.GPSTracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout bottom_opt;
    private ProgressBar pb1;

    private void initPD() {
        bottom_opt = findViewById(R.id.bottom_opt);
        pb1 = findViewById(R.id.pb1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initPD();
        if(Const.isMyServiceRunning(SplashScreen.this, GPSTracker.class)==false){
            startService(new Intent(getApplicationContext(),GPSTracker.class));
        }
        mAuth = FirebaseAuth.getInstance();
        createUser();
//        throw new NumberFormatException();

    }
    private void createUser() {
        try {
            pb1.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(Const.admin_email, Const.admin_password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkLogin();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.auth_failed), 6).show();
                                }

                            }
                        });

        }
        catch (Exception e){
            e.printStackTrace();

        }
    }

    private void checkLogin() {
        try{
            System.out.println("run1--2--");
            if(new MySharedPref().getData(getApplicationContext(), Const.ldata,"").length()>0){
                System.out.println("run1--3--");
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            } else {
                System.out.println("run1--4--");
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        }
    }

    public void LoginNow(View v){
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    public void RegNow(View v){
        startActivity(new Intent(getApplicationContext(),SignupActivity.class));
        finish();
    }
}
