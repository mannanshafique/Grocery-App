package com.example.grocery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProducts> productList,filterList;
    //FilterProduct is filter class
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProducts> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
    //getData
        final ModelProducts modelProducts = productList.get(position);
        String id = modelProducts.getProductID();
        String uid = modelProducts.getUid();
        String productCategory = modelProducts.getProductCategory();
        String productDescription = modelProducts.getProductDescription();
        String icon = modelProducts.getProductIcon();
        String timestamp = modelProducts.getTimestamp();
        String discountAvailable = modelProducts.getDiscountAvailable();


        holder.titleTv.setText(modelProducts.getProductTitle());
        holder.quantityTv.setText(modelProducts.getProductQuantity());
        holder.discountNoteTv.setText(modelProducts.getDiscountNote());
        holder.discountPriceTv.setText("$"+modelProducts.getDiscountPrice());
        holder.originalPriceTv.setText("$"+modelProducts.getOriginalPrice());

        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            // add strike through on original price
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            //product is not on discount
            holder.discountPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_blue).into(holder.productIconTv);
        }
        catch (Exception e){
            holder.productIconTv.setImageResource(R.drawable.ic_cart_blue);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item click      means show detail i used bottom sheet
                detailsBottomCheat(modelProducts);

            }
        });

    }

    private void detailsBottomCheat(final ModelProducts modelProducts) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view to bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set View to Bottom Sheet
        bottomSheetDialog.setContentView(view);
        //init View to BottomSheet
        ImageButton backBtn = view.findViewById(R.id.BtnBack);
        ImageButton deleteBtn = view.findViewById(R.id.BtnDelete);
        ImageButton editBtn = view.findViewById(R.id.BtnEdit);
        ImageView productIconTv = view.findViewById(R.id.productIconTV);
        TextView discountNoteTv = view.findViewById(R.id.DiscountNoteTv);
        TextView discountPriceTv = view.findViewById(R.id.DiscountPriceTvs);
        final TextView titleTv = view.findViewById(R.id.TitleTv);
        TextView descriptionTv = view.findViewById(R.id.DescriptionTv);
        TextView categoryTv = view.findViewById(R.id.CategoryTv);
        TextView quantityTv = view.findViewById(R.id.QuantityTv);
        TextView originalPriceTv = view.findViewById(R.id.PriceTv);

        //getData
        final String id = modelProducts.getTimestamp();
        String uid = modelProducts.getUid();
        String productCategory = modelProducts.getProductCategory();
        String productDescription = modelProducts.getProductDescription();
        String icon = modelProducts.getProductIcon();
        String timestamp = modelProducts.getTimestamp();
        String discountAvailable = modelProducts.getDiscountAvailable();
        String discountedPriceTV = modelProducts.getDiscountPrice();
        String originalPrice = modelProducts.getOriginalPrice();
        final String title = modelProducts.getProductTitle();


        titleTv.setText(modelProducts.getProductTitle());
        quantityTv.setText(modelProducts.getProductQuantity());
        discountNoteTv.setText(modelProducts.getDiscountNote());
        discountPriceTv.setText("$"+discountedPriceTV);
        originalPriceTv.setText("$"+originalPrice);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);

        if(discountAvailable.equals("true")){
            //product is on discount
            discountPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            // add strike through on original price
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            //product is not on discount
            discountPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_blue).into(productIconTv);
        }
        catch (Exception e){
            productIconTv.setImageResource(R.drawable.ic_cart_blue);
        }
        //show dialog
        bottomSheetDialog.show();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent it = new Intent(context,EditProductActivity.class);
                it.putExtra("productId",id);
                context.startActivity(it);


            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("Are you sure you want to delete product"+title+"?").
                        setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
                                databaseReference.child(firebaseAuth.getUid()).child("Products").child(id). //id is intalialy get
                                        removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel Dismiss
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{
        //Holds View of recycleViews

        ImageView productIconTv;
        TextView discountNoteTv,titleTv,quantityTv,discountPriceTv,originalPriceTv;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconTv = itemView.findViewById(R.id.ProductIconTv);
            discountNoteTv = itemView.findViewById(R.id.DiscountNoteTv);
            titleTv = itemView.findViewById(R.id.TitleTv);
            quantityTv = itemView.findViewById(R.id.QuantityTv);
            discountPriceTv = itemView.findViewById(R.id.DiscountPriceTv);
            originalPriceTv = itemView.findViewById(R.id.PriceTv);

        }
    }
}
