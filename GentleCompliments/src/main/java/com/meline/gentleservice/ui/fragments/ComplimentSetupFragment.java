package com.meline.gentleservice.ui.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class ComplimentSetupFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, RadioGroup.OnCheckedChangeListener {
    private Activity mActivity;
    private FloatingActionButton mFabStartStop;
    private EditText mStartTime, mEndTime;
    private LinearLayout mAreaInput;
    private CheckBox mDontDisturb;
    private boolean isServiceRunning;
    private RadioButton mScheduleRadio;
    private RadioButton mSurpriseMeRadio;
    private Spinner mSpinner;
    private EditText mTimeWait;
    private static final String TIME_WAIT_TAG = "TimeWaitTag";

    public ComplimentSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compliment_setup, container, false);
        initComponents(view);


        return view;
    }

    private void initComponents(View view) {
        mActivity = getActivity();
        mFabStartStop = (FloatingActionButton) view.findViewById(R.id.fab_start_stop);
        mFabStartStop.setOnClickListener(this);

        mTimeWait = new EditText(mActivity);
        mSpinner = new Spinner(mActivity);
        mDontDisturb = (CheckBox) view.findViewById(R.id.chb_dont_disturb);
        mDontDisturb.setOnClickListener(this);

        mStartTime = (EditText) view.findViewById(R.id.et_start_time);
        mStartTime.setOnFocusChangeListener(this);
        mEndTime = (EditText) view.findViewById(R.id.et_end_time);
        mEndTime.setOnFocusChangeListener(this);

        mAreaInput = (LinearLayout) view.findViewById(R.id.ll_input_area);
        RadioGroup waitingTimeArea = (RadioGroup) view.findViewById(R.id.rgWaitingTimeArea);
        waitingTimeArea.setOnCheckedChangeListener(this);
        mScheduleRadio = (RadioButton) view.findViewById(R.id.rbSchedule);
        mSurpriseMeRadio = (RadioButton) view.findViewById(R.id.rbSurpriseMe);

        //manage preview set options
        isServiceRunning = SharedPreferencesUtils.loadBoolean(mActivity, getString(R.string.sp_isServiceRunning), false);
        managePreviouslyChosenValues();
        manageStartingValues(isServiceRunning);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fab_start_stop:
                if (isServiceRunning) {
                    //button stop is pressed
                    stopService();
                } else {
                    //button start is pressed
                    startService();
                }
                break;

            case R.id.chb_dont_disturb:
                SharedPreferencesUtils.saveBoolean(mActivity, getString(R.string.sp_do_not_disturb), mDontDisturb.isChecked());
                break;

            default:
                Toast.makeText(mActivity, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        int id = view.getId();
        if (id == R.id.et_start_time || id == R.id.et_end_time) {
            if (hasFocus) {
                TimePickerFragment.show((AppCompatActivity) mActivity, (EditText) view);
            }
        } else {
            Toast.makeText(mActivity, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        saveRadioButtonsState();
        addInputView(mAreaInput);
    }

    private void addInputView(LinearLayout areaInput) {
        //remove all views if there are any
        areaInput.removeAllViews();

        if (mSurpriseMeRadio.isChecked()) {
            // Application of the Array to the Spinner
            final ArrayAdapter<CharSequence> spinnerArrayAdapter =
                    ArrayAdapter.createFromResource(mActivity, R.array.surprises, R.layout.custom_spinner_item);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.custom_dropdown_spinner);

            mSpinner.setAdapter(spinnerArrayAdapter);
            mSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            );

            String spinnerValue = SharedPreferencesUtils.loadString(mActivity, getString(R.string.sp_surprise_spinner_value), null);
            int position = 1; //default position just in case
            if (spinnerValue != null) {
                position = spinnerArrayAdapter.getPosition(spinnerValue);
            }
            mSpinner.setSelection(position);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SharedPreferencesUtils.saveString(mActivity, getString(R.string.sp_surprise_spinner_value), ((TextView) view).getText().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //do nothing
                }
            });

            areaInput.addView(mSpinner);

        } else if (mScheduleRadio.isChecked()) {
            mTimeWait.setHint(getString(R.string.time_hint));
            mTimeWait.setGravity(Gravity.CENTER);
            mTimeWait.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            mTimeWait.setInputType(InputType.TYPE_CLASS_NUMBER);
            mTimeWait.setTextColor(ContextCompat.getColor(mActivity, R.color.colorDarkGrey));
            mTimeWait.setHintTextColor(ContextCompat.getColor(mActivity, R.color.colorDarkGrey));
            mTimeWait.setText(SharedPreferencesUtils.loadString(mActivity, getString(R.string.sp_time_wait_value), ""));

            mTimeWait.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(!hasFocus){
                        SharedPreferencesUtils.saveString(mActivity, getString(R.string.sp_time_wait_value), mTimeWait.getText().toString());
                    }
                }
            });

            areaInput.addView(mTimeWait);
            mTimeWait.requestFocus();
        }
    }

    private void manageStartingValues(boolean serverOn) {
        if (serverOn) {
            setStartingComponentsValues();
        } else {
            setDefaultComponentsValues();
        }
    }

    private void startService() {
        setStartingComponentsValues();

        if (mScheduleRadio.isChecked()) {
            //todo startScheduledComplimenting();
        } else if (mSurpriseMeRadio.isChecked()) {
            //todo startSurpriseComplimenting();
        } else {
            Toast.makeText(mActivity, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDontDisturbStartEndTime() {
        SharedPreferencesUtils.saveString(mActivity, getString(R.string.sp_start_time), mStartTime.getText().toString());
        SharedPreferencesUtils.saveString(mActivity, getString(R.string.sp_end_time), mEndTime.getText().toString());
    }


    private void saveRadioButtonsState() {
        SharedPreferencesUtils.saveBoolean(mActivity, getString(R.string.sp_isSurpriseMe), mSurpriseMeRadio.isChecked());
        SharedPreferencesUtils.saveBoolean(mActivity, getString(R.string.sp_isScheduled), mScheduleRadio.isChecked());
    }

    private void stopService() {
        //todo stop complimenting

        setDefaultComponentsValues();
    }

    private void managePreviouslyChosenValues() {
        mDontDisturb.setChecked(SharedPreferencesUtils.loadBoolean(mActivity, getString(R.string.sp_do_not_disturb), true));

        mSurpriseMeRadio.setChecked(SharedPreferencesUtils.loadBoolean(mActivity, getString(R.string.sp_isSurpriseMe), true));
        mScheduleRadio.setChecked(SharedPreferencesUtils.loadBoolean(mActivity, getString(R.string.sp_isScheduled), false));

        mStartTime.setText(SharedPreferencesUtils.loadString(mActivity, getString(R.string.sp_start_time), getString(R.string.default_start_time)));
        mEndTime.setText(SharedPreferencesUtils.loadString(mActivity, getString(R.string.sp_end_time), getString(R.string.default_end_time)));

        mTimeWait.clearFocus();
    }

    private void setDefaultComponentsValues() {
        mFabStartStop.setImageResource(android.R.drawable.ic_media_play);
        isServiceRunning = false;
        SharedPreferencesUtils.saveBoolean(mActivity, getString(R.string.sp_isServiceRunning), isServiceRunning);
        mDontDisturb.setEnabled(true);
        mTimeWait.setFocusableInTouchMode(true);
        mStartTime.setFocusableInTouchMode(true);
        mEndTime.setFocusableInTouchMode(true);
        mScheduleRadio.setEnabled(true);
        mSurpriseMeRadio.setEnabled(true);
        mSpinner.setEnabled(true);
    }

    private void setStartingComponentsValues() {
        saveDontDisturbStartEndTime();
        isServiceRunning = true;
        SharedPreferencesUtils.saveBoolean(mActivity, getString(R.string.sp_isServiceRunning), isServiceRunning);
        mFabStartStop.setImageResource(android.R.drawable.alert_light_frame);
        mDontDisturb.setEnabled(false);
        mTimeWait.setFocusable(false);
        mStartTime.setFocusable(false);
        mEndTime.setFocusable(false);
        mScheduleRadio.setEnabled(false);
        mSurpriseMeRadio.setEnabled(false);
        mSpinner.setEnabled(false);
    }
}
