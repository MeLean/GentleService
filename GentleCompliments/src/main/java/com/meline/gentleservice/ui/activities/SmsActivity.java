package com.meline.gentleservice.ui.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SdCardWriter;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

import java.util.ArrayList;

public class SmsActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{
    final private static int CONTACTS_REQUEST = 1;
    final private static String TAG = "Debug";
    private TextView twSmsName;
    private TextView twNumber;
    private EditText etSmsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String smsText = getIntent().getStringExtra(getString(R.string.sp_sms_text));
        twSmsName = (TextView) findViewById(R.id.twSmsName);
        twNumber = (TextView) findViewById(R.id.twNumber);
        etSmsText = (EditText) findViewById(R.id.etSmsText);
        etSmsText.setText(smsText);
        etSmsText.requestFocus();
        Button btnChose = (Button) findViewById(R.id.btnYes);
        Button btnYes = (Button) findViewById(R.id.btnChose);
        Button btnNo = (Button) findViewById(R.id.btnNo);

        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);
        btnChose.setOnClickListener(this);

        etSmsText.setOnKeyListener(this);

        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        String spPhoneNum = spUtils.getStringFromSharedPreferences(getString(R.string.sp_contact_phone_num));
        String spName = spUtils.getStringFromSharedPreferences(getString(R.string.sp_contact_name));

        if(spName != null){
            twSmsName.setText(spName);
        }

        if(spPhoneNum != null){
            twNumber.setText(spPhoneNum);
        }
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
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String nameStr = String.valueOf(cursor.getString(nameIndex)).trim();
                    twNumber.setText(phoneNumberStr);
                    twSmsName.setText(nameStr);
                    SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
                    spUtils.putStringInSharedPreferences(getString(R.string.sp_contact_name), nameStr);
                    spUtils.putStringInSharedPreferences(getString(R.string.sp_contact_phone_num), phoneNumberStr);
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnChose:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, SmsActivity.CONTACTS_REQUEST);
                break;
            case R.id.btnYes:
                String numberValue = twNumber.getText().toString().trim();
                String textValue = etSmsText.getText().toString().trim();
                if(!numberValue.equals("") &&  !textValue.equals("")){
                    sendSMS(numberValue, textValue);
                }else {
                    Toast.makeText(this, R.string.empty_input_values, Toast.LENGTH_SHORT).show();
                    return;
                }

                this.finish();
                break;
            case R.id.btnNo:
                this.finish();
                break;
            default:
                break;
        }
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
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
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
            Uri rowUri = getApplicationContext().getContentResolver().insert(uri, values);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(SmsActivity.this, R.string.sms_did_not_save, Toast.LENGTH_SHORT).show();
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass().getSimpleName() + " db.changeIsHatedStatus(complimentText, false);");
        }
    }


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager manager = (InputMethodManager) view.getContext()
                    .getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null)
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        }
        return false;
    }
}
