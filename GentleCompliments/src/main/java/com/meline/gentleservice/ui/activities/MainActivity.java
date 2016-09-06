package com.meline.gentleservice.ui.activities;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.meline.gentleservice.R;
import com.meline.gentleservice.api.api_module.GentleIntentLauncher;
import com.meline.gentleservice.utils.CalendarUtils;
import com.meline.gentleservice.utils.LocaleManagementUtils;
import com.meline.gentleservice.utils.SdCardWriter;
import com.meline.gentleservice.utils.SharedPreferencesUtils;
import com.meline.gentleservice.ui.fragments.TimePickerFragment;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnFocusChangeListener, RadioGroup.OnCheckedChangeListener {
    private EditText etFirstTime, etSecondTime;
    private LinearLayout llAreaInput;
    private Button btnStartStop;
    private CheckBox chbDontDisturb;

    private boolean isServiceRunning;
    private RadioButton rbSchedule;
    private RadioButton rbSurpriseMe;
    private Spinner mSpinner;
    private EditText etTimeWait;
    private SharedPreferencesUtils spUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etTimeWait = new EditText(this);
        mSpinner = new Spinner(this);
        chbDontDisturb = (CheckBox) findViewById(R.id.chbDontDisturb);
        etFirstTime = (EditText) findViewById(R.id.etFirstTime);
        etSecondTime = (EditText) findViewById(R.id.etSecondTime);
        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        llAreaInput = (LinearLayout) findViewById(R.id.llAreaInput);
        RadioGroup rgWaitingTimeArea = (RadioGroup) findViewById(R.id.rgWaitingTimeArea);
        rbSchedule = (RadioButton) findViewById(R.id.rbSchedule);
        rbSurpriseMe = (RadioButton) findViewById(R.id.rbSurpriseMe);

        btnStartStop.setOnClickListener(this);
        chbDontDisturb.setOnClickListener(this);

        rgWaitingTimeArea.setOnCheckedChangeListener(this);

        etFirstTime.setOnFocusChangeListener(this);
        etSecondTime.setOnFocusChangeListener(this);
        spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        boolean isSchedule = false;
        try {
            isSchedule = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isScheduled));
        } catch (RuntimeException e) {
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass() + " isSchedule = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isScheduled));");
        }

        boolean isSurpriseMe = false;
        try {
            isSurpriseMe = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isSurpriseMe));
        } catch (RuntimeException e) {
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass() + " isSurpriseMe = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isSurpriseMe)");
        }

        if (!isSchedule && !isSurpriseMe) {
            isSchedule = true;//first load of program and isSchedule = true is default
        }

        boolean willNotDisturb;
        try {
            willNotDisturb = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_do_not_disturb));
        } catch (RuntimeException e) {
            //default value if there isn't any in sharedPreferences
            willNotDisturb = true;
        }

        //set disturbed value if any
        chbDontDisturb.setChecked(willNotDisturb);
        String fistDate = spUtils.getStringFromSharedPreferences(getString(R.string.sp_firstTime));
        String secondDate = spUtils.getStringFromSharedPreferences(getString(R.string.sp_secondTime));
        if (fistDate != null && secondDate != null) {
            etFirstTime.setText(fistDate);
            etSecondTime.setText(secondDate);
        } else {
            etFirstTime.setText(getString(R.string.default_first_time));
            etSecondTime.setText(getString(R.string.default_second_time));
        }

        rbSchedule.setChecked(isSchedule);
        rbSurpriseMe.setChecked(isSurpriseMe);

        isServiceRunning = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isServiceRunning));
        if (isServiceRunning) {
            this.addInputView(llAreaInput);
            manageComponentsValues();
        }

        Bundle intentHasExtras = getIntent().getExtras();
        if (intentHasExtras != null) {
            if (intentHasExtras.getBoolean("reloadComplimentingOnly", false)) {
                SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
                this.startService(spUtils);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //keeps  an eye on locale changes
        if (spUtils != null) {
            LocaleManagementUtils localeManagementUtils = new LocaleManagementUtils(this);
            localeManagementUtils.manageLocale(spUtils, spUtils);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                finish();
                break;
            case R.id.action_add_new_compliment:
                Intent addComplimentIntent = new Intent(this, AddNewComplimentActivity.class);
                this.startActivity(addComplimentIntent);
                break;
            case R.id.action_like_hated:
                Intent likeHatedIntent = new Intent(this, LikeHatedActivity.class);
                this.startActivity(likeHatedIntent);
                break;
            case R.id.get_next_load_date:
                String nextDate = CalendarUtils.stringifyDateInFormat(
                        new Date(spUtils.getLongFromSharedPreferences(getString(R.string.sp_fireNextInMilliseconds)))
                );
                Toast.makeText(MainActivity.this, nextDate, Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(MainActivity.this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        switch (view.getId()) {
            case R.id.btnStartStop:
                isServiceRunning = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isServiceRunning));
                if (isServiceRunning) {
                    //button stop is pressed
                    stopService(spUtils);
                } else {
                    //button start is pressed
                    startService(spUtils);
                }
                break;

            case R.id.chbDontDisturb:
                spUtils.putBooleanInSharedPreferences(getString(R.string.sp_do_not_disturb), chbDontDisturb.isChecked());
                break;

            default:
                Toast.makeText(MainActivity.this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(MainActivity.this, getString(R.string.sp_name));
        boolean serviceDown = !(spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isServiceRunning)));
        switch (view.getId()) {
            case R.id.etFirstTime:
                if (hasFocus && serviceDown) {
                    showTimePicker(etFirstTime);
                }
                break;

            case R.id.etSecondTime:
                if (hasFocus && serviceDown) {
                    showTimePicker(etSecondTime);
                }
                break;

            default:
                Toast.makeText(MainActivity.this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        //auto puts scheduled option in sharedPreferences
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        spUtils.putBooleanInSharedPreferences(getString(R.string.sp_isScheduled), rbSchedule.isChecked());
        spUtils.putBooleanInSharedPreferences(getString(R.string.sp_isSurpriseMe), rbSurpriseMe.isChecked());
        addInputView(llAreaInput);
    }

    @Override
    protected void onStop() {
        super.onStop();
        spUtils = null;
    }

    private void addInputView(LinearLayout llAreaInput) {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        if (rbSurpriseMe.isChecked()) {
            //remove all views if there are any
            llAreaInput.removeAllViews();
            // Application of the Array to the Spinner
            ArrayAdapter<CharSequence> spinnerArrayAdapter =
                    ArrayAdapter.createFromResource(this, R.array.surprises, R.layout.custom_spinner_item);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.custom_dropdown_spinner);
            mSpinner.setAdapter(spinnerArrayAdapter);
            mSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            if (isServiceRunning) {
                //if service running the value of chosen period must be set
                String spinnerValue = spUtils.getStringFromSharedPreferences(getString(R.string.sp_surprise_spinner_value));
                int position = 1; //default position just in case
                if (spinnerValue != null) {
                    position = spinnerArrayAdapter.getPosition(spinnerValue);
                }
                mSpinner.setSelection(position);
            }

            llAreaInput.addView(mSpinner);

        } else if (rbSchedule.isChecked()) {
            //remove all views if there are any
            llAreaInput.removeAllViews();
            etTimeWait.setHint(getString(R.string.time_hint));
            etTimeWait.setGravity(Gravity.CENTER);
            etTimeWait.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            etTimeWait.setInputType(InputType.TYPE_CLASS_NUMBER);
            llAreaInput.addView(etTimeWait);
            etTimeWait.requestFocus();
        }
    }

    private void showTimePicker(EditText editText) {
        DialogFragment fragmentOne = new TimePickerFragment(editText);
        fragmentOne.show(getSupportFragmentManager(), "datePicker");
        editText.clearFocus();
    }

    private void startService(SharedPreferencesUtils spUtils) {
        spUtils.putStringInSharedPreferences(getString(R.string.sp_firstTime), etFirstTime.getText().toString());
        spUtils.putStringInSharedPreferences(getString(R.string.sp_secondTime), etSecondTime.getText().toString());

        if (rbSchedule.isChecked()) {
            this.startScheduledComplimenting();
        } else if (rbSurpriseMe.isChecked()) {
            this.startSurpriseComplimenting();
        } else {
            Toast.makeText(this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopService(SharedPreferencesUtils spUtils) {
        GentleIntentLauncher gentleIntentLauncher = GentleIntentLauncher.getInstance();
        gentleIntentLauncher.stopComplimenting(this);
        setDefaultComponentsValues(spUtils);
    }

    private void startScheduledComplimenting() {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        boolean isServiceRunning = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isServiceRunning));
        long waitingTime;
        if (!isServiceRunning) {
            long MINIMUM_WAITING_TIME = 60 * 1000; //todo make 2 * 60 * 60 * 1000; // two hours is a minimum time
            try {
                int inputNum = Integer.parseInt(String.valueOf(etTimeWait.getText()));
                waitingTime = CalendarUtils.minutesToMilliseconds(inputNum);
                spUtils.putLongInSharedPreferences(getString(R.string.sp_timeWaiting), waitingTime);

                if (waitingTime < MINIMUM_WAITING_TIME) {
                    if (waitingTime < 0) {
                        throw new NumberFormatException("entered number is bigger then int max value");
                    }
                    throw new FormatException("Input is smaller then minimum waiting time!");
                }
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, R.string.invalid_number_text, Toast.LENGTH_LONG).show();
                return;
            } catch (FormatException e) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.minimum_waiting_time_text) + CalendarUtils.millisecondsToMinutes(MINIMUM_WAITING_TIME), Toast.LENGTH_LONG).show();
                return;
            }

            spUtils.putLongInSharedPreferences(getString(R.string.sp_timeWaiting), waitingTime);
            spUtils.putBooleanInSharedPreferences(getString(R.string.sp_isServiceRunning), true);
            manageComponentsValues();
        }

        waitingTime = spUtils.getLongFromSharedPreferences(getString(R.string.sp_timeWaiting));
        spUtils.putLongInSharedPreferences(getString(R.string.sp_fireNextInMilliseconds), System.currentTimeMillis() + waitingTime);
        waitingTime = spUtils.getLongFromSharedPreferences(getString(R.string.sp_timeWaiting));
        GentleIntentLauncher gentleIntentLauncher = GentleIntentLauncher.getInstance();
        gentleIntentLauncher.startComplimenting(getApplicationContext(), waitingTime);
        finish();
    }

    private void startSurpriseComplimenting() {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        boolean isServiceRunning = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_isServiceRunning));
        if (!isServiceRunning) {
            long timeSurpriseWait;
            String spinValue = mSpinner.getSelectedItem().toString();
            spUtils.putStringInSharedPreferences(getString(R.string.sp_surprise_spinner_value), spinValue);//needed for activity next starts
            if (spinValue.equals(getString(R.string.surprise_option_every_week))) {
                timeSurpriseWait = 7 * 24 * 60 * 60 * 1000; //days * hours * minutes * seconds * milliseconds
            } else if (spinValue.equals(getString(R.string.surprise_option_every_12_hours))) {
                timeSurpriseWait = 12 * 60 * 60 * 1000; //hours * minutes * seconds * milliseconds
            } else if (spinValue.equals(getString(R.string.surprise_option_every_8_hours))) {
                timeSurpriseWait = 8 * 60 * 60 * 1000; //hours * minutes * seconds * milliseconds
            } else if (spinValue.equals(getString(R.string.surprise_option_every_6_hours))) {
                timeSurpriseWait = 6 * 60 * 60 * 1000; //hours * minutes * seconds * milliseconds
            } else if (spinValue.equals(getString(R.string.surprise_option_every_day))) {
                timeSurpriseWait = 2 * 60 * 1000; //todo 24 * 60 * 60 * 1000; //hours * minutes * seconds * milliseconds
            } else {
                throw new NullPointerException("Unimplemented option!");
            }

            spUtils.putLongInSharedPreferences(getString(R.string.sp_timeWaitingPeriod), timeSurpriseWait);
            spUtils.putLongInSharedPreferences(getString(R.string.sp_timeEndPeriod), System.currentTimeMillis());
            spUtils.putBooleanInSharedPreferences(getString(R.string.sp_isServiceRunning), true);
            manageComponentsValues();
        }

        GentleIntentLauncher gentleIntentLauncher = GentleIntentLauncher.getInstance();
        gentleIntentLauncher.startComplimenting(getApplicationContext(), null);
        finish();
    }

    private void manageComponentsValues() {
        chbDontDisturb.setEnabled(false);
        etTimeWait.setVisibility(View.INVISIBLE);
        etFirstTime.setFocusable(false);
        etSecondTime.setFocusable(false);
        rbSchedule.setEnabled(false);
        rbSurpriseMe.setEnabled(false);
        mSpinner.setEnabled(false);
        btnStartStop.setText(getString(R.string.btnStop_text));
    }

    private void setDefaultComponentsValues(SharedPreferencesUtils spUtils) {
        btnStartStop.setText(getString(R.string.btnStart_text));
        spUtils.putBooleanInSharedPreferences(getString(R.string.sp_isServiceRunning), false);
        spUtils.putLongInSharedPreferences(getString(R.string.sp_fireNextInMilliseconds), 0);
        chbDontDisturb.setEnabled(true);
        etTimeWait.setVisibility(View.VISIBLE);
        etFirstTime.setFocusableInTouchMode(true);
        etSecondTime.setFocusableInTouchMode(true);
        rbSchedule.setEnabled(true);
        rbSurpriseMe.setEnabled(true);
        mSpinner.setEnabled(true);
    }
}