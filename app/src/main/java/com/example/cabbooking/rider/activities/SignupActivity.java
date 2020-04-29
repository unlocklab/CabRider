package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.adapters.AutoAdp;
import com.example.cabbooking.rider.dto.UserConst;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.other.Validations;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private EditText et1,et2,et3,et4,et5;
    private TextView cc_digit;
    private ProgressDialog pd;
    private ValueEventListener listener = null;
    private AutoAdp sa;
    private Dialog dialog = null;
    private JSONArray countries = null, countries1 = null;

    private JSONArray filterData(String s) {
        JSONArray lk = new JSONArray();
        try {
            for (int i = 0; i < countries.length(); i++) {
                if (countries.getJSONObject(i).getString(Const.name).toLowerCase().trim()
                        .toLowerCase().contains(s.toLowerCase().trim())) {
                    lk.put(countries.getJSONObject(i));
                }
                else if (countries.getJSONObject(i).getString(Const.dial_code).toLowerCase().trim()
                        .toLowerCase().contains(s.toLowerCase().trim())) {
                    lk.put(countries.getJSONObject(i));
                }
                else if (countries.getJSONObject(i).getString(Const.code).toLowerCase().trim()
                        .toLowerCase().contains(s.toLowerCase().trim())) {
                    lk.put(countries.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lk;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        try {
            countries = new JSONArray(Const.country_codes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        initUI();
        initPD();
    }

    private void initUI() {
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        cc_digit = findViewById(R.id.cc_digit);

        findViewById(R.id.cc_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc_digit.performClick();
            }
        });

        findViewById(R.id.down_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc_digit.performClick();
            }
        });

        cc_digit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountryPop();
            }
        });
    }

    private void CountryPop() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }

            dialog = new Dialog(this);
            dialog.setCancelable(true);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.country_code_popup);
//            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Rect displayRectangle = new Rect();
            Window window = dialog.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            window.setBackgroundDrawableResource(android.R.color.transparent);


            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.country_code_popup, null);
            layout.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
            layout.setMinimumHeight((int)(displayRectangle.height() * 0.9f));

            dialog.setContentView(layout);

            final ListView gv1 = dialog.findViewById(R.id.gv1);
            final EditText search_et = dialog.findViewById(R.id.search_et);


            sa = new AutoAdp(this, countries);
            gv1.setAdapter(sa);

            Const.showKeyboardFrom(SignupActivity.this,findViewById(R.id.rl_main));

            gv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        JSONObject country = countries.getJSONObject(position);
                        cc_digit.setText(country.getString(Const.dial_code));
                        Const.hideKeyboardFrom(SignupActivity.this,findViewById(R.id.rl_main));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            search_et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (search_et.getText().toString().length() > 0) {
                        countries1 = filterData(search_et.getText().toString());
                        sa = new AutoAdp(SignupActivity.this, countries1);
                        gv1.setAdapter(sa);
                        gv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    JSONObject country = countries1.getJSONObject(position);
                                    cc_digit.setText(country.getString(Const.dial_code));

                                    Const.hideKeyboardFrom(SignupActivity.this,findViewById(R.id.rl_main));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        });
                    } else {
                        sa = new AutoAdp(SignupActivity.this, countries);
                        gv1.setAdapter(sa);
                        gv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    JSONObject country = countries.getJSONObject(position);
                                    cc_digit.setText(country.getString(Const.dial_code));

                                    Const.hideKeyboardFrom(SignupActivity.this,findViewById(R.id.rl_main));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                dialog.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }

    public void LoginNow(View v){
        if (Validations.valEdit(this, et1, getString(R.string.fname))
                && Validations.valEdit(this, et2, getString(R.string.lname))
                && Validations.valEditEmail(this, et3, getString(R.string.email))
                && Validations.valEditMob(this, et4, getString(R.string.mobile_number))
                && Validations.valEditPassword(this, et5, getString(R.string.password))
                && Const.isOnline(this)) {
            try {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.rl_main).getWindowToken(), 0);

                final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);


                UserDto userDto = new UserDto();


                userDto.setUserId(mDatabaseUser.push().getKey());
                userDto.setFname(et1.getText().toString());
                userDto.setLname(et2.getText().toString());
                userDto.setEmail(et3.getText().toString());
                userDto.setMobile(cc_digit.getText().toString()+et4.getText().toString());
                userDto.setPassword(et5.getText().toString());
                userDto.setDatetime(Const.getUtcTime());
                userDto.setUser_type(Const.rider);


                checkKeyValue(UserConst.email,userDto.getEmail(),userDto);


            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void checkKeyValue(final String key, final String value, final UserDto userDto) {
        try{
            final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);

            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mDatabaseUser.removeEventListener(listener);
                    if(dataSnapshot.getValue()!=null){
                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            UserDto userDto1 = dataSnapshot1.getValue(UserDto.class);
                            if(key.equals(UserConst.email)){
                                if(userDto1.getEmail().equals(value)){
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),getString(R.string.email_exist),6).show();
                                }
                                else{
                                    checkKeyValue(UserConst.mobile,userDto.getMobile(),userDto);
                                }
                            }
                            else{
                                if(userDto1.getMobile().equals(value)){
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),getString(R.string.mob_exist),6).show();
                                }
                                else{
                                    CreateUser(userDto);
                                }
                            }
                        }
                    }
                    else{
                        if(key.equals(UserConst.email)){
                            checkKeyValue(UserConst.mobile,userDto.getMobile(),userDto);
                        }
                        else{
                            CreateUser(userDto);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabaseUser.removeEventListener(listener);
                    if(key.equals(UserConst.email)){
                        checkKeyValue(UserConst.mobile,userDto.getMobile(),userDto);
                    }
                    else{
                        CreateUser(userDto);
                    }
                }
            };

            mDatabaseUser.orderByChild(key).equalTo(value).addValueEventListener(listener);
        }
        catch (Exception e){
            e.printStackTrace();
            pd.dismiss();
        }
    }

    private void CreateUser(UserDto userDto) {
        pd.dismiss();

        final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);

        Toast.makeText(getApplicationContext(),getString(R.string.signup_succ),6).show();
        new MySharedPref().saveData(getApplicationContext(),Const.ldata, new Gson().toJson(userDto)+"");

        System.out.println("tiger--------------"+new Gson().toJson(userDto));
        mDatabaseUser.child(userDto.getUserId()).setValue(userDto);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
