package com.meline.gentleservice.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.meline.gentleservice.R;

public class CustomAdapter extends ArrayAdapter<String> {
    public CustomAdapter(Context context, ArrayList<String> complimentsStr) {
        super(context, R.layout.cutom_row_string,complimentsStr);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("ViewHolder")
        View customView = inflater.inflate(R.layout.cutom_row_string, parent, false);

        String rowText = getItem(position);
        TextView twCustomRow =  customView.findViewById(R.id.tw_custom_row);
        twCustomRow.setText(rowText);

        return customView;
    }
}
