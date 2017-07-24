package com.meline.gentleservice.ui.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.meline.gentleservice.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ComplimentSetupFragment extends Fragment {


    public ComplimentSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_compliment_setup, container, false);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_start_stop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCompliments();
            }
        });
        return rootView;
    }

    private void toggleCompliments() {
        //todo implement
        Toast.makeText(getContext(), "Implement me", Toast.LENGTH_SHORT).show();
    }
}
