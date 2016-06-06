package com.meline.gentleservice.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.meline.gentleservice.utils.SdCardWriter;


@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private String TIME_SEPARATOR = ":";
    EditText mTextView;
    TimePickerDialog mTimePickerDialog;

    public TimePickerFragment(EditText textview)
    {
        this.mTextView = textview;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        String[] array = mTextView.getText().toString().split(TIME_SEPARATOR);
        int hour = 0;
        int minute = 0;
        try {
            hour = Integer.parseInt(array[0].trim());
            minute = Integer.parseInt(array[1].trim());
        }catch (Exception e){
            SdCardWriter sdCardWriter = new SdCardWriter("GentleComplimentsLog.txt");
            sdCardWriter.appendNewLine(e.getLocalizedMessage());
            sdCardWriter.appendNewLine(this.getClass() + " db.changeIsHatedStatus(complimentText, false);");
        }

        mTimePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        // Create a new instance of TimePickerDialog and return it
         return mTimePickerDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        mTextView.setText(String.format("%s%s%s",putLeadingZero(hourOfDay), TIME_SEPARATOR, putLeadingZero(minute)));
    }

    private String putLeadingZero(int value) {
        String result = String.valueOf(value);
        if (value < 9){
            result = "0" + value;
        }

        return result;
    }

}

