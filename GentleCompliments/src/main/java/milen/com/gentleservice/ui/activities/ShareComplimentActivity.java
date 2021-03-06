package milen.com.gentleservice.ui.activities;

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

import milen.com.gentleservice.R;
import milen.com.gentleservice.utils.RuntimePermissionAssistant;

import java.util.ArrayList;
import java.util.List;

public class ShareComplimentActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private EditText mShareComplimentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String smsText;
        if (savedInstanceState == null) {
            smsText = getIntent().getStringExtra(getString(R.string.sp_sms_text));
        } else{
            smsText = savedInstanceState.getString(getString(R.string.sp_sms_text));
        }

        mShareComplimentText = findViewById(R.id.et_share_text);
        mShareComplimentText.setText(smsText);
        mShareComplimentText.clearFocus();

        mShareComplimentText.setOnKeyListener(this);


        ImageButton btnSms =  findViewById(R.id.btn_sms);

        btnSms.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RuntimePermissionAssistant.PERMISSIONS_SEND_SMS_CONSTANT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendMessage(mShareComplimentText.getText().toString().trim());
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
            case R.id.btn_sms:
                boolean permission_received = RuntimePermissionAssistant.checkForPermission(
                        this,
                        Manifest.permission.SEND_SMS,
                        RuntimePermissionAssistant.PERMISSIONS_SEND_SMS_CONSTANT
                );

                if (permission_received) {
                    sendMessage(mShareComplimentText.getText().toString().trim());
                }
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
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_return:
                finish();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(ShareComplimentActivity.this, StartingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(
                getString(R.string.sp_sms_text),
                mShareComplimentText.getText().toString()
        );
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
                if (TextUtils.equals(packageName, "com.facebook.katana")) {
                    continue;
                }

                targetedShareIntent.putExtra(Intent.EXTRA_TEXT, messageText);
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
