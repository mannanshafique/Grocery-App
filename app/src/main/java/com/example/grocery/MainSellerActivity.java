package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    TextView nameTitle,shopNameTitle,emailTitle,tabProductsTv,tabOrdersTv,filteredProductTv;
    EditText searchProductET;
    ImageButton logoutBtn,editBtn,addProductBtn, filterProductBtn;
    ImageView profileTv;
    RelativeLayout productR1,ordersR1;
    RecyclerView productsRv;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private ArrayList<ModelProducts> productList;
    //class
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        filteredProductTv = findViewById(R.id.filteredProductTv);
        searchProductET = findViewById(R.id.searchProductET);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productsRv = findViewById(R.id.productsRv);

        nameTitle = findViewById(R.id.SellerUserNameTV);
        logoutBtn = findViewById(R.id.BtnSLogout);
        editBtn = findViewById(R.id.BtnSEdit);
        addProductBtn = findViewById(R.id.BtnSCart);
        shopNameTitle = findViewById(R.id.ShopTv);
        emailTitle = findViewById(R.id.EmailTv);
        profileTv = findViewById(R.id.ProfileTv);
        productR1 = findViewById(R.id.productsR1);
        ordersR1 = findViewById(R.id.ordersR1);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);


        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        showProductsUI();
        loadAllProducts();


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             makeMeOffline();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,ProfileEditSellerActivity
                        .class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,AddProductActivity.class));
            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category").
                        setItems(Constants.productCategoriesOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = Constants.productCategoriesOptions[which];
                                filteredProductTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else {
                                    loadfilteredProducts(selected);
                                }
                            }
                        }).show();
            }
        });

        searchProductET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        adapterProductSeller.getFilter().filter(s);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void loadfilteredProducts(final String selected) {
        productList = new ArrayList<>();
        //get all products
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        databaseReference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data rest / file
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){

                    String productCategory = ""+ds.child("productCategory").getValue();
                    //if selected category matches product category then add in list
                    if(selected.equals(productCategory)){
                        ModelProducts modelProducts = ds.getValue(ModelProducts.class);
                        productList.add(modelProducts);
                    }

                }
                //Setup adapter
                adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                productsRv.setAdapter(adapterProductSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        //get all products
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        databaseReference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting data rest / file
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProducts modelProducts = ds.getValue(ModelProducts.class);
                    productList.add(modelProducts);
                }
                //Setup adapter
                adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                productsRv.setAdapter(adapterProductSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showProductsUI() {
        productR1.setVisibility(View.VISIBLE);
        ordersR1.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }
    private void showOrdersUI() {
        productR1.setVisibility(View.GONE);
        ordersR1.setVisibility(View.VISIBLE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));



    }

    private void makeMeOffline() {
        progressDialog.setMessage("Checking User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");                           // make the user/seller offline

        //Update to db that user is offline

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("User");
        firebaseRef.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //sign out
                firebaseAuth.signOut();
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            Intent it = new Intent(MainSellerActivity.this,LoginActivity.class);
            startActivity(it);
            finish();
        }
        else {
            LoadMyInfo();
        }
    }

    private void LoadMyInfo() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    String accountType = ""+ds.child("accountType").getValue();
                    String email = ""+ds.child("email").getValue();
                    String shopName = ""+ds.child("shopName").getValue();
                    String profileImage = ""+ds.child("profileImage").getValue();

                    nameTitle.setText(name +" ("+accountType+")");
                    emailTitle.setText(email);
                    shopNameTitle.setText(shopName);
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store).into(profileTv);
                    }
                    catch (Exception e){
                        profileTv.setImageResource(R.drawable.ic_store);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}