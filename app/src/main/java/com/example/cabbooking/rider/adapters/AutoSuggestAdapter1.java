package com.example.cabbooking.rider.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cabbooking.rider.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AutoSuggestAdapter1 extends ArrayAdapter<String> implements Filterable {
    private JSONArray mlistData;
    private LayoutInflater inflater;
    private Activity activity;

    public AutoSuggestAdapter1(@NonNull Activity context, int resource) {
        super(context.getApplicationContext(), resource);
        mlistData = new JSONArray();
        activity = context;
        inflater = LayoutInflater.from(context);
    }

    public void setData(JSONArray list) {
        mlistData = list;
    }

    @Override
    public int getCount() {
        return mlistData.length();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        try {
            return mlistData.getJSONObject(position).getString("description");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.sugg_item, parent, false);

        TextView txt1 = convertView.findViewById(R.id.txt1);

        try {

            txt1.setText(mlistData.getJSONObject(position).getString("description"));


        }
        catch (Exception e){
            e.printStackTrace();
        }


        return convertView;
    }

    public JSONObject getObject(int position) {
        try {
            return mlistData.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = mlistData;
                    filterResults.count = mlistData.length();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
    }
}
