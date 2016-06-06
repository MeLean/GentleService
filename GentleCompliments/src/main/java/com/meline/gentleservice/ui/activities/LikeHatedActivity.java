package com.meline.gentleservice.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;

import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.ui.adapters.CustomAdapter;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SdCardWriter;

public class LikeHatedActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ArrayList<String> mHatedComplimentsStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_hated);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DBHelper db = DBHelper.getInstance(this);
        ArrayList<Compliment> hatedCompliments = new ArrayList<>();
        try {
            hatedCompliments = db.getHeatedComplements();
        } catch (Exception e) {
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass().getSimpleName() + " hatedCompliments = db.getHeatedComplements();");
        }

        mHatedComplimentsStr = new ArrayList<>();
        for (Compliment compliment : hatedCompliments) {
            mHatedComplimentsStr.add(compliment.getContent());
        }

        loadList(mHatedComplimentsStr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_just_finish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_return:
                finish();
                break;
            default:
                Toast.makeText(this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        final String complimentText = parent.getItemAtPosition(position).toString();

        //show dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
        // set title
        alertDialogBuilder.setTitle(getString(R.string.like_hated_title));
        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.like_hated_message) + "\"" + complimentText + "\"")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeComplimentFromList(view, complimentText, false);
                    }
                })
                .setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeComplimentFromList(view, complimentText, true);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
        ;
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void loadList(ArrayList<String> hatedComplimentsStr) {
        ListView listView = (ListView) findViewById(R.id.lvLikeHated);

        if (hatedComplimentsStr.size() > 0) {
            listView.setOnItemClickListener(this);
        } else {
            hatedComplimentsStr.add(getString(R.string.no_hated_compliments_text));
            listView.setOnItemClickListener(null);
        }

        ArrayAdapter<String> adapter = new CustomAdapter(this, hatedComplimentsStr);
        listView.setAdapter(adapter);
        listView.setAdapter(adapter);
    }

    private void removeComplimentFromList(View view, String complimentText, boolean isItForDelete) {
        Animation fadeOut = AnimationUtils.loadAnimation(LikeHatedActivity.this, android.R.anim.fade_out);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);

        DBHelper db = DBHelper.getInstance(LikeHatedActivity.this);
        try {
            if (isItForDelete) {
                db.deleteCompliment(complimentText);
            } else {
                db.changeIsHatedStatus(complimentText, false);
            }

        } catch (SQLException e) {
            Toast.makeText(LikeHatedActivity.this, getString(R.string.action_failed) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass().getSimpleName() + " db.changeIsHatedStatus(complimentText, false);");
        }

        String msg = isItForDelete ? getString(R.string.hated_was_deleted) : getString(R.string.hated_was_removed);
        Toast toast = Toast.makeText(LikeHatedActivity.this, String.format("\"%s\" %s", complimentText, msg), Toast.LENGTH_SHORT);
        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
        toast.show();
        mHatedComplimentsStr.remove(complimentText);
        loadList(mHatedComplimentsStr);
    }
}
