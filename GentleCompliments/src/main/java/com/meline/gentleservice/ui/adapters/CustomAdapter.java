package com.meline.gentleservice.ui.adapters;

import android.content.Context;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.cutom_row_string, parent, false);

        String rowText = getItem(position);
        TextView twCustomRow = (TextView) customView.findViewById(R.id.twCustomRow);
        twCustomRow.setText(rowText);

        return customView;
    }
}
