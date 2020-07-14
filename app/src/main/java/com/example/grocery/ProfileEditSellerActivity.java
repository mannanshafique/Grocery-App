package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener {

    ImageButton backBtn, gpsBtn;
    ImageView profileImageTV;
    EditText fullNameET,phoneET,countryET,stateET,cityET,shopNameET,deliverFeeET,addressET;
    SwitchCompat shopOpenSwitch;
    Button updateBtn;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    // permission constants
    public static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission array
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    //Image picked Uri
    private Uri image_uri;

    private LocationManager locationManager;

    private double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_seller);


        backBtn = findViewById(R.id.BtnBack);
        gpsBtn = findViewById(R.id.BtnGps);
        updateBtn = findViewById(R.id.BtnUpdate);
        profileImageTV = findViewById(R.id.ProfileTv);
        fullNameET = findViewById(R.id.FullNameTv);
        phoneET = findViewById(R.id.PhoneTv);
        countryET = findViewById(R.id.CountryTv);
        stateET = findViewById(R.id.StateTv);
        cityET = findViewById(R.id.CityTv);
        deliverFeeET = findViewById(R.id.DeliveryTv);
        shopNameET = findViewById(R.id.ShopTv);
        addressET = findViewById(R.id.FullAddressTv);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);

        //FireBaseWork
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        checkUser();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    inputData();
            }
        });

    }
    String fullName,shopName,phone,deliverFee,country,state,city,fullAddress;
    boolean shopOpen;
    private void inputData() {
            fullName = fullNameET.getText().toString().trim();
            shopName = shopNameET.getText().toString().trim();
            phone = phoneET.getText().toString().trim();
            deliverFee = deliverFeeET.getText().toString().trim();
            country = countryET.getText().toString().trim();
            state = stateET.getText().toString().trim();
            city = cityET.getText().toString().trim();
            fullAddress = addressET.getText().toString().trim();
            shopOpen = shopOpenSwitch.isChecked();  // true/false

            updateProfile();
    }

    private void updateProfile() {
        progressDialog.setMessage("Updating Info...");
        progressDialog.show();
        if(image_uri == null){
            //If user not gave any image
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("name",""+fullName);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phone);
            hashMap.put("deliveryFee",""+deliverFee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+fullAddress);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);



            // Now save hashMap to db(firebase)
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
            databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        else {
            //If user Gave Image so save info with image
            String filePathName = "profile_images/" + ""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadImageUri = uriTask.getResult();
                    if(uriTask.isSuccessful()){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("name",""+fullName);
                        hashMap.put("shopName",""+shopName);
                        hashMap.put("phone",""+phone);
                        hashMap.put("deliveryFee",""+deliverFee);
                        hashMap.put("country",""+country);
                        hashMap.put("state",""+state);
                        hashMap.put("city",""+city);
                        hashMap.put("address",""+fullAddress);
                        hashMap.put("latitude",""+latitude);
                        hashMap.put("longitude",""+longitude);
                        hashMap.put("shopOpen",""+shopOpen);
                        hashMap.put("profileImage",""+downloadImageUri); //url of uploaded image

                        // Now save hashMap to db(firebase)
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
                        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            Intent it = new Intent(getApplicationContext(),LoginActivity.class);
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
                    String phone = ""+ds.child("phone").getValue();
                    String country = ""+ds.child("country").getValue();
                    String state = ""+ds.child("state").getValue();
                    String city = ""+ds.child("city").getValue();
                    String address = ""+ds.child("address").getValue();
                    String shopName = ""+ds.child("shopName").getValue();
                    String deliveryFee = ""+ds.child("deliveryFee").getValue();
                    String shopOpen = ""+ds.child("shopOpen").getValue();
                    String profileImage = ""+ds.child("profileImage").getValue();

                    fullNameET.setText(name);
                    phoneET.setText(phone);
                    countryET.setText(country);
                    stateET.setText(state);
                    cityET.setText(city);
                    addressET.setText(address);
                    shopNameET.setText(shopName);
                    deliverFeeET.setText(deliveryFee);

                    if(shopOpen.equals("true")){
                        shopOpenSwitch.setChecked(true);
                    }
                    else{
                        shopOpenSwitch.setChecked(false);
                    }
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store).into(profileImageTV);
                    }
                    catch (Exception e ){
                        profileImageTV.setImageResource(R.drawable.ic_person);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent it = new Intent(Intent.ACTION_PICK);
        it.setType("image/*");
        startActivityForResult(it, IMAGE_PICK_GALLERY_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private  boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }

    private  boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private  boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }


    @Override
    public void onLocationChanged(Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses =geocoder.getFromLocation(latitude,longitude,1);

            String address = addresses.get(0).getAddressLine(0);   // Fetch Complete Address
            String cityLocation =addresses.get(0).getLocality();
            String stateLocation =addresses.get(0).getAdminArea();
            String countryLocation =addresses.get(0).getCountryName();

            //set address
            addressET.setText(address);
            cityET.setText(cityLocation);
            stateET.setText(stateLocation);
            countryET.setText(countryLocation);
        }
        catch (Exception e) {
            Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //gpsLocation disable
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_REQUEST_CODE : {
                if(grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Location Permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE : {
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Camera Permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE : {
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage Permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to image view
                profileImageTV.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to imageView
                profileImageTV.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}