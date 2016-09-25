package com.meline.gentleservice.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;

import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.R;

public class AddNewComplimentActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText etAddCompliment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_compliment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        btnAddCompliment.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                etAddCompliment.setText("");
                    Toast.makeText(this, R.string.compliment_added, Toast.LENGTH_LONG).show();

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

