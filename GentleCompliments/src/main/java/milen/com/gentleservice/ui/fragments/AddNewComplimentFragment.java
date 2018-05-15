package milen.com.gentleservice.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import milen.com.gentleservice.R;
import milen.com.gentleservice.api.database.DBHelper;
import milen.com.gentleservice.api.objects_model.Compliment;
import milen.com.gentleservice.utils.SoftInputManager;

import java.sql.SQLException;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewComplimentFragment extends Fragment implements View.OnClickListener {
    private static final String SAVED_COMPLIMENT_VALUE = "AddNewComplimentFragment.SAVED_COMPLIMENT_VALUE";
    private EditText etAddCompliment;


    public AddNewComplimentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new_compliment, container, false);

        etAddCompliment = view.findViewById(R.id.etAddCompliment);
        etAddCompliment.setOnKeyListener(new SoftInputManager());

        if(savedInstanceState != null){
            etAddCompliment.setText(savedInstanceState.getString(SAVED_COMPLIMENT_VALUE));
        }

        FloatingActionButton btnAddCompliment = view.findViewById(R.id.fab_add_compliment);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_COMPLIMENT_VALUE, etAddCompliment.getText().toString());
    }
}
