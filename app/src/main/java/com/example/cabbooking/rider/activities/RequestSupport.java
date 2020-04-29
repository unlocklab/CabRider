package com.example.cabbooking.rider.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.dto.SupportDto;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.other.Validations;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class RequestSupport extends AppCompatActivity {

    private EditText et1,et2;
    private ProgressDialog pd;
    private UserDto userDto = null;
    private ValueEventListener listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        initPD();
        initUI();
        userDto = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata,"")
                , UserDto.class);
    }

    private void initUI() {
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);

    }


    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }

    public void LoginNow(View v){
        if (Validations.valEdit(this, et1, getString(R.string.title))
                && Validations.valEdit(this, et2, getString(R.string.details))
                && Const.isOnline(this)) {
            try {
                pd.show();
                final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.support_tbl);

                SupportDto supportDto = new SupportDto();

                supportDto.setSupportId(mDatabaseUser.push().getKey());
                supportDto.setAdminId(userDto.getFname()+" "+userDto.getLname());
                supportDto.setUserId(userDto.getUserId());
                supportDto.setDatetime(Const.getLocalTime(Const.getUtcTime()));
                supportDto.setTitle(et1.getText().toString());
                supportDto.setDescription(et2.getText().toString());

                mDatabaseUser.child(supportDto.getSupportId()).setValue(supportDto);

                Toast.makeText(getApplicationContext(),"Request has submitted we will contact you soon",6).show();

                finish();

            } catch (Exception e) {
                e.printStackTrace();
                pd.dismiss();
            }
        }
    }


    public void NavNow(View v){
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
