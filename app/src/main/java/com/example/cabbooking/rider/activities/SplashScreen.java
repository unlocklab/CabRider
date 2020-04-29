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
        mAuth = FirebaseAuth.getInstance();
        createUser();

    }
    private void createUser() {
        try {
            pb1.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(Const.admin_email, Const.admin_password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {

                                    checkLogin();
                                } else {

                                    Toast.makeText(getApplicationContext(), getString(R.string.auth_failed), 6).show();
                                }
                            }
                            catch (Exception e){
                                e.printStackTrace();

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
            if(new MySharedPref().getData(getApplicationContext(), Const.ldata,"").length()>0){
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            } else {
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
