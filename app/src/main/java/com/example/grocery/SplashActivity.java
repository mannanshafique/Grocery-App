package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Full Screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        firebaseAuth = FirebaseAuth.getInstance();

        // craete delay .....splash delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //Firstly we check user is logged in or not if not login then login activity else check user type and show activity

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    Intent it = new Intent(SplashActivity.this,LoginActivity.class);
                    startActivity(it);
                    finish();
                }
                else {
                    checkUserType();
                }
            }
        },1000);

    }

    private void checkUserType() {
        //In this method we check the user is seller or buyer
        // if Seller open seller main Screen
        // If buyer then open main screen

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String accountType = ""+ds.child("accountType").getValue();
                    if(accountType.equals("Seller")){

                        //User is Seller
                        startActivity(new Intent(SplashActivity.this,MainSellerActivity.class));
                        finish();
                    }
                    else {
                        //User Is Buyer
                        startActivity(new Intent(SplashActivity.this,MainUserActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}