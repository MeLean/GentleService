package com.meline.gentleservice.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;

import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SdCardWriter;

public class AddNewComplimentActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText etAddCompliment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_compliment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etAddCompliment = (EditText)findViewById(R.id.etAddCompliment);
        etAddCompliment.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard(view);
                    return true;
                }
                return false;
            }
        });

        Button btnAddCompliment = (Button) findViewById(R.id.btnAddCompliment);
        Button btnBack = (Button) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(this);
        btnAddCompliment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAddCompliment:
                String input = String.valueOf(etAddCompliment.getText());
                if (input.equals("")){
                    Toast.makeText(this, R.string.enter_compliment, Toast.LENGTH_SHORT).show();
                    return;
                }

                Compliment enteredCompliment = new Compliment(String.valueOf(input.trim()));
                enteredCompliment.setIsCustom(true); // it is a personal compliment not default
                DBHelper db = DBHelper.getInstance(this);
                try {
                    db.addComplement(enteredCompliment);
                    etAddCompliment.setText("");
                    Toast.makeText(this, R.string.compliment_added, Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
                    sdCardWriter.appendNewLine(e.getLocalizedMessage());
                    sdCardWriter.appendNewLine(this.getClass().getSimpleName() + " db.addComplement(enteredCompliment);");
                }
                break;

            case R.id.btnBack:
                finish();
                break;

            default:
                Toast.makeText(this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

