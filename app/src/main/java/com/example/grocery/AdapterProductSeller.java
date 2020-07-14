package com.example.grocery;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        ModelProducts modelProducts = productList.get(position);
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
        holder.discountPriceTv.setText(modelProducts.getDiscountPrice());
        holder.originalPriceTv.setText(modelProducts.getOriginalPrice());

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
                //handle item click      means show detail

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
