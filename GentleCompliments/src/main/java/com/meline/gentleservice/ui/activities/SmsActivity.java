package com.meline.gentleservice.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import com.meline.gentleservice.utils.RuntimePermissionAssistant;

import java.util.ArrayList;
import java.util.List;

public class SmsActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private EditText mSmsText;

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
        mSmsText = (EditText) findViewById(R.id.etSmsText);
        mSmsText.setText(smsText);
        mSmsText.requestFocus();

        mSmsText.setOnKeyListener(this);


        ImageButton btnSms = (ImageButton) findViewById(R.id.btnSms);
        ImageButton btnCancel = (ImageButton) findViewById(R.id.btnCancel);

        btnSms.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RuntimePermissionAssistant.PERMISSIONS_SEND_SMS_CONSTANT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendMessage(mSmsText.getText().toString().trim());
                }
            }
            break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSms:
                boolean permission_received = RuntimePermissionAssistant.checkForPermission(
                        this,
                        Manifest.permission.SEND_SMS,
                        RuntimePermissionAssistant.PERMISSIONS_SEND_SMS_CONSTANT
                );

                if (permission_received) {
                    sendMessage(mSmsText.getText().toString().trim());
                }
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
        getMenuInflater().inflate(R.menu.menu_settings_finish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_return:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void sendMessage(String messageText) {
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                targetedShareIntent.setType("text/plain");

                //facebook has a better app for messaging and should not show as an option
                if (!TextUtils.equals(packageName, "com.facebook.katana")) {
                    targetedShareIntent.putExtra(Intent.EXTRA_TEXT, messageText);
                } else {
                    continue;
                }

                targetedShareIntent.setPackage(packageName);
                targetedShareIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                targetedShareIntents.add(targetedShareIntent);
            }

            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), getString(R.string.chose_application));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
            startActivity(chooserIntent);
        }
    }
}
