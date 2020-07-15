package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditProductActivity extends AppCompatActivity {

    String productId;

    ImageButton backBtn;
    ImageView productIconTv;
    TextView categoryET;    // category is edit text but take as textView because dropdown selection.
    EditText titleET,descriptionET,quantityET,priceET,discountPriceET,discountNoteET;
    Button updateProductBtn;
    SwitchCompat discountSwitch;

    //Firebase
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    // permission constants
    public static final int CAMERA_REQUEST_CODE=200;
    public static final int STORAGE_REQUEST_CODE=300;
    //image pick constants
    public static final int IMAGE_PICK_GALLERY_CODE=400;
    public static final int IMAGE_PICK_CAMERA_CODE=500;
    //permission array
    private String[] cameraPermission;
    private String[] storagePermission;
    //Image picked Uri
    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        titleET = findViewById(R.id.TitleTv);
        descriptionET = findViewById(R.id.DescriptionTv);
        categoryET = findViewById(R.id.CategoryTv);
        quantityET = findViewById(R.id.QuantityTv);
        priceET = findViewById(R.id.PriceTv);
        discountPriceET = findViewById(R.id.DiscountPriceTv);
        discountNoteET = findViewById(R.id.DiscountNoteTv);
        productIconTv = findViewById(R.id.ProductIconTv);
        backBtn = findViewById(R.id.BtnBacks);
        updateProductBtn = findViewById(R.id.BtnUpdateProduct);
        discountSwitch = findViewById(R.id.DiscountSwitchTv);

        productId = getIntent().getStringExtra("productId");

        //Init set to Gone
        discountPriceET.setVisibility(View.GONE);
        discountNoteET.setVisibility(View.GONE);

        //Firebase work
        firebaseAuth =FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init Permissions array
        cameraPermission = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        loadProductDetails();

        //if discountSwitch is Checked show discount price and discount note
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked
                    discountPriceET.setVisibility(View.VISIBLE);
                    discountNoteET.setVisibility(View.VISIBLE);
                }
                else {
                    //Unchecked
                    discountPriceET.setVisibility(View.GONE);
                    discountNoteET.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image method
                //Option to pick
                String[] option = {"Camera","Gallery"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProductActivity.this);
                builder.setTitle("Pick Image").setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //Camera clicked
                            if(checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();
                            }
                            else {
                                //not allowed
                                requestCameraPermission();
                            }


                        }
                        else{
                            //gallery clicked
                            if(checkStoragePermission()){
                                //storage permission allowed
                                pickFromGallery();
                            }
                            else {
                                //not allowed
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
            }
        });

        categoryET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProductActivity.this);
                builder.setTitle("Product Category").setItems(Constants.options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get Picked Category
                        String Category = Constants.options[which];
                        //set picked category
                        categoryET.setText(Category);
                    }
                }).show();
            }
        });

        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private void loadProductDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        databaseReference.child(firebaseAuth.getUid()).child("Products").child(productId).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String productId = ""+snapshot.child("productId").getValue();
                    String productTitle = ""+snapshot.child("productTitle").getValue();
                    String productDescription = ""+snapshot.child("productDescription").getValue();
                    String productCategory = ""+snapshot.child("productCategory").getValue();
                    String productQuantity = ""+snapshot.child("productQuantity").getValue();
                    String productIcon = ""+snapshot.child("productIcon").getValue();
                    String originalPrice = ""+snapshot.child("originalPrice").getValue();
                    String discountPrice = ""+snapshot.child("discountPrice").getValue();
                    String discountNote = ""+snapshot.child("discountNote").getValue();
                    String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                    String timestamp = ""+snapshot.child("timestamp").getValue();
                    String uid = ""+snapshot.child("uid").getValue();

                    titleET.setText(productTitle);
                    descriptionET.setText(productDescription);
                    categoryET.setText(productCategory);
                    discountNoteET.setText(discountNote);
                    quantityET.setText(productQuantity);
                    discountPriceET.setText(discountPrice);
                    priceET.setText(originalPrice);

                    if(discountAvailable.equals("true")){
                        discountSwitch.setChecked(true);
                        discountPriceET.setVisibility(View.VISIBLE);
                        discountNoteET.setVisibility(View.VISIBLE);
                    }
                    else{
                        discountSwitch.setChecked(false);
                        discountPriceET.setVisibility(View.GONE);
                        discountNoteET.setVisibility(View.GONE);
                    }
                    try {
                        Picasso.get().load(productIcon).placeholder(R.drawable.ic_store).into(productIconTv);
                    }
                    catch (Exception e ){
                        productIconTv.setImageResource(R.drawable.ic_store);
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    String productTitle,productDescription,productCategory, productQuantity,originalPrice,discountPrice,discountNote;
    boolean discountAvailable = false;
    private void inputData() {
        productTitle = titleET.getText().toString().trim();
        productDescription = descriptionET.getText().toString().trim();
        productCategory = categoryET.getText().toString().trim();
        productQuantity = quantityET.getText().toString().trim();
        originalPrice = priceET.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();      //true/false

        //validate data is on good
        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is requires ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is requires ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is requires ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(discountAvailable) {
            //product is with discount
            discountPrice = discountPriceET.getText().toString().trim();
            discountNote = discountNoteET.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is requires ...", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            discountPrice = "0";
            discountNote = "";
        }
        updateProduct();
    }

    private void updateProduct() {
        progressDialog.setMessage("Updating Product...");
        progressDialog.show();

        if(image_uri == null){
            //If user not gave any image
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");   //no image
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);


            // Now update hashMap to db(firebase)
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
            databaseReference.child(firebaseAuth.getUid()).child("Products").child(productId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        else {
            //If user Gave Image so save info with image
            String filePathName = "product_images/" + ""+productId;
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadImageUri = uriTask.getResult();
                    if(uriTask.isSuccessful()){
                        //If user not gave any image
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("productTitle",""+productTitle);
                        hashMap.put("productDescription",""+productDescription);
                        hashMap.put("productCategory",""+productCategory);
                        hashMap.put("productQuantity",""+productQuantity);
                        hashMap.put("productIcon",""+downloadImageUri);
                        hashMap.put("originalPrice",""+originalPrice);
                        hashMap.put("discountPrice",""+discountPrice);
                        hashMap.put("discountNote",""+discountNote);
                        hashMap.put("discountAvailable",""+discountAvailable);


                        // Now save hashMap to db(firebase)
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
                        databaseReference.child(firebaseAuth.getUid()).child("Products").child(productId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProductActivity.this, "Product Added...", Toast.LENGTH_SHORT).show();
                                        clearData();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void clearData() {
        // Clear data after uploading
        titleET.setText("");
        descriptionET.setText("");
        categoryET.setText("");
        quantityET.setText("");
        priceET.setText("");
        discountPriceET.setText("");
        discountNoteET.setText("");
        productIconTv.setImageResource(R.drawable.ic_store);
        image_uri = null;

    }

    private void pickFromCamera(){
        ContentValues contentValues =new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        Intent it = new Intent(Intent.ACTION_PICK);
        it.setType("image/*");
        startActivityForResult(it,IMAGE_PICK_GALLERY_CODE);
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

    //That method check the permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
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

    //That method set the image in activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to image view
                productIconTv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to imageView
                productIconTv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}