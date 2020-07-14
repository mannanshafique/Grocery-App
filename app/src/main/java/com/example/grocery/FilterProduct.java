package com.example.grocery;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProductSeller adapter;
    private ArrayList<ModelProducts> filterList;

    public FilterProduct(AdapterProductSeller adapter, ArrayList<ModelProducts> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length()>0){
            //change to Upper Case,to make insensitive
            constraint = constraint.toString().toUpperCase();

            //search field is not empty, perform search
            ArrayList<ModelProducts> filteredModels = new ArrayList<>();
            for(int i = 0 ; i<filterList.size();i++ ){
                //check search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
                    //add filter data to the list
                    filteredModels.add(filterList.get(i));

                }
            }
            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;
        }
        else{
            //search field is empty ,not searching anything ,return all original list
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.productList = (ArrayList<ModelProducts>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
