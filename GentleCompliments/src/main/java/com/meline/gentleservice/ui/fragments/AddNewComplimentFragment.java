package com.meline.gentleservice.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.meline.gentleservice.R;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.utils.SoftInputManager;

import java.sql.SQLException;

import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewComplimentFragment extends Fragment implements View.OnClickListener {
    private EditText etAddCompliment;


    public AddNewComplimentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new_compliment, container, false);

        //todo saved instance
        etAddCompliment = (EditText)view.findViewById(R.id.etAddCompliment);
        etAddCompliment.setOnKeyListener(new SoftInputManager());

        FloatingActionButton btnAddCompliment = (FloatingActionButton) view.findViewById(R.id.fab_add_compliment);
        btnAddCompliment.setOnClickListener(this);
        return view;
    }



    @Override
    public void onClick(View view) {
        Context context = getContext();
        switch (view.getId()){
            case R.id.fab_add_compliment:
                String input = String.valueOf(etAddCompliment.getText());
                if (input.equals("")){
                    Toast.makeText(context, R.string.enter_compliment, Toast.LENGTH_SHORT).show();
                    return;
                }

                Compliment enteredCompliment = new Compliment(String.valueOf(input.trim()));
                enteredCompliment.setIsCustom(true); // it is a personal compliment not default
                DBHelper db = DBHelper.getInstance(context);

                try {
                    db.addComplement(enteredCompliment);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                etAddCompliment.setText("");
                SoftInputManager.hideSoftInput(view);
                Toast.makeText(context, R.string.compliment_added, Toast.LENGTH_LONG).show();

                break;

            default:
                Toast.makeText(context, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
