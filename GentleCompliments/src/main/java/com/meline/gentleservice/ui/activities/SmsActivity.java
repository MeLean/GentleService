package com.meline.gentleservice.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;


import com.meline.gentleservice.R;

import java.util.ArrayList;
import java.util.List;

public class SmsActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private EditText etSmsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }*/

        String smsText = getIntent().getStringExtra(getString(R.string.sp_sms_text));
        etSmsText = (EditText) findViewById(R.id.etSmsText);
        etSmsText.setText(smsText);
        etSmsText.requestFocus();

        etSmsText.setOnKeyListener(this);


        ImageButton btnSms = (ImageButton) findViewById(R.id.btnSms);
        ImageButton btnCancel = (ImageButton) findViewById(R.id.btnCancel);

        btnSms.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSms:
                AsyncLoadMessagingApps asyncLoadMessagingApps = new AsyncLoadMessagingApps();
                asyncLoadMessagingApps.doInBackground(etSmsText.getText().toString().trim());
                break;
            case R.id.btnCancel:
                finish();
                break;
            default:
                throw new UnsupportedOperationException("No action found fot id: " + view.getId());
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            hideSoftInput(view);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_just_finish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_return:
                finish();
                return true;

            /*case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;*/

        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private class AsyncLoadMessagingApps extends AsyncTask<String, Void, Void> {
        private ProgressDialog mDialog;
        private boolean mOperationStatusOK = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context context = getApplicationContext();
            mDialog = new ProgressDialog(context);
            mDialog.setMessage(context.getString(R.string.please_wait));
            mDialog.setCancelable(true);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setIndeterminate(true);
            mDialog.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.jumping_heart_cartoon));
        }

        @Override
        protected Void doInBackground(String... params) {
            String messageText = params[0];
            List<Intent> targetedShareIntents = new ArrayList<>();
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
            if (!resInfo.isEmpty()) {
                for (ResolveInfo resolveInfo : resInfo) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    Intent targetedShareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    targetedShareIntent.setType("text/plain");

                    //facebook has a better app for messaging and should not show as an option
                    if (!TextUtils.equals(packageName, "com.facebook.katana")) {
                        targetedShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageText);
                    } else {
                        continue;
                    }

                    targetedShareIntent.setPackage(packageName);
                    targetedShareIntent.setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name);
                    targetedShareIntents.add(targetedShareIntent);
                }
                //todo change recipient text to "Chose app"
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), getString(R.string.chose_recipient));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
                startActivity(chooserIntent);
            }

            mOperationStatusOK = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mOperationStatusOK) {
                if (mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        }
    }
}
