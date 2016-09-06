package com.meline.gentleservice.ui.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SdCardWriter;

import java.util.ArrayList;

public class SmsActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    final private static int CONTACTS_REQUEST = 1;
    final private static String TAG = "Debug";
    private EditText etSmsText;

    private void hideSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean smsSendingIsAvailable() {
        PackageManager pm = getPackageManager();
        return (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) || pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA));
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.d(TAG, "Generic failure");
                        return;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.d(TAG, "No Service");
                        return;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.d(TAG, "Null PDU");
                        return;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.d(TAG, "Radio off");
                        return;
                    default:
                        return;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "SMS NOT delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> partsSms = sms.divideMessage(message);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        ArrayList<PendingIntent> listSendPI = new ArrayList<>();
        listSendPI.add(sentPI);
        ArrayList<PendingIntent> listDeliveredPI = new ArrayList<>();
        listDeliveredPI.add(deliveredPI);
        sms.sendMultipartTextMessage(phoneNumber, null, partsSms, listSendPI, listDeliveredPI);

        saveSmsInSentMessages(phoneNumber, message);
    }

    private void saveSmsInSentMessages(String phoneNumber, String message) {
        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber); // phone number to send
            values.put("date", String.valueOf(System.currentTimeMillis()));
            values.put("read", "0"); // if you want to mark is as unread set to 0
            values.put("type", "2"); // 2 means sent message
            values.put("body", message);
            Uri uri = Uri.parse("content://sms/");
            //Uri rowUri =
            getApplicationContext().getContentResolver().insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SmsActivity.this, R.string.sms_did_not_save, Toast.LENGTH_SHORT).show();
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass().getSimpleName() + " db.changeIsHatedStatus(complimentText, false);");
        }
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing, the activity will stay open and wait to another reaction by the user
                        dialog.dismiss();
                    }
                }).show();
    }

    private void startActivityForMessaging(String appUri) {
        if (isNetworkAvailable()) {
            if(isAppInstalled(appUri)) {
                showDialog(getString(R.string.send_again));
                Toast.makeText(SmsActivity.this, getString(R.string.chose_recipient), Toast.LENGTH_LONG).show();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setPackage(appUri);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, etSmsText.getText().toString());
                this.startActivity(share);
            }else{
                Toast.makeText(SmsActivity.this, getString(R.string.app_not_installed), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(SmsActivity.this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_activity);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            String smsText = getIntent().getStringExtra(getString(R.string.sp_sms_text));
            etSmsText = (EditText) findViewById(R.id.etSmsText);
            etSmsText.setText(smsText);
            etSmsText.requestFocus();

            etSmsText.setOnKeyListener(this);

            ImageButton btnViber = (ImageButton) findViewById(R.id.btnViber);
            ImageButton btnWhatsapp = (ImageButton) findViewById(R.id.btnWhatsapp);
            ImageButton btnSms = (ImageButton) findViewById(R.id.btnSms);
            ImageButton btnCancel = (ImageButton) findViewById(R.id.btnCancel);

            btnViber.setOnClickListener(this);
            btnWhatsapp.setOnClickListener(this);
            btnSms.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SmsActivity.CONTACTS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String phoneNumberStr = String.valueOf(cursor.getString(numberIndex));
                    //int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    //String nameStr = String.valueOf(cursor.getString(nameIndex)).trim();
                    cursor.close();

                    sendSMS(phoneNumberStr, etSmsText.getText().toString().trim());
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSms:
                if (smsSendingIsAvailable()) {
                    if (!etSmsText.getText().toString().trim().equals("")) {
                        this.hideSoftInput(etSmsText);
                        //todo translate string
                        showDialog(getString(R.string.send_again));
                        Toast.makeText(SmsActivity.this, getString(R.string.chose_recipient), Toast.LENGTH_LONG).show();
                        //choosing an contact number from android people application
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(intent, SmsActivity.CONTACTS_REQUEST);
                    } else {
                        Toast.makeText(this, R.string.empty_input_values, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(SmsActivity.this, getString(R.string.could_not_messaging), Toast.LENGTH_SHORT).show();
                }
                break;

            /*case R.id.btnChose:
            Intent intent = new Intent(Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, SmsActivity.CONTACTS_REQUEST);
                break;*/

            case R.id.btnViber:
                startActivityForMessaging("com.viber.voip");
                break;
            case R.id.btnWhatsapp:
                startActivityForMessaging("com.whatsapp");
                break;
            case R.id.btnCancel:
                //todo translate string
                showDialog(getString(R.string.quit));
                break;
            default:
                break;
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
}
