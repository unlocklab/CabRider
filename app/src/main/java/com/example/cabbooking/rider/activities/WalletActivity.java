package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.dto.UserConst;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.ImageCaching;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.other.RoundImage;
import com.example.cabbooking.rider.other.Validations;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.MediaPicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class WalletActivity extends AppCompatActivity{

    private TextView txt3;
    private ProgressDialog pd;
    private ValueEventListener listener = null;
    private UserDto userDto = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        initPD();
        initUI();
        setData();
    }

    private void setData() {
        try{
            userDto = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(),Const.ldata,"")
                    , UserDto.class);
//            txt3.setText("Balance: $"+userDto.getBalance());
            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
            mDatabaseUser.child(userDto.getUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null){
                        userDto = dataSnapshot.getValue(UserDto.class);
                        txt3.setText("Balance: $"+userDto.getBalance());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private void initUI() {
        txt3 = findViewById(R.id.txt3);
    }


    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }

    public void LoginNow(View v){
//        if (Validations.valEdit(this, et1, getString(R.string.fname))
//                && Validations.valEdit(this, et2, getString(R.string.lname))
//                && Validations.valEditEmail(this, et3, getString(R.string.email))
//                && Validations.valEdit(this, et4, getString(R.string.mobile_number))
//                && Const.isOnline(this)) {
//            try {
//                pd.show();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(findViewById(R.id.rl_main).getWindowToken(), 0);
//
//                if(myFile!=null){
//                    uploadFile();
//                }
//                else {
//                    up_pro();
//                }
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//
//        }
    }




    public void NavNow(View v){
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
