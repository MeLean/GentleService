package meline.com.gentleservice.ui.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;


@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    public static final String TAG = "TimePickerFragment";
    private String TIME_SEPARATOR = ":";
    EditText mTextView;
    TimePickerDialog mTimePickerDialog;

    public TimePickerFragment(EditText textView) {
        this.mTextView = textView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        String[] array = mTextView.getText().toString().split(TIME_SEPARATOR);
        int hour = 0;
        int minute = 0;

        hour = Integer.parseInt(array[0].trim());
        minute = Integer.parseInt(array[1].trim());


        mTimePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        // Create a new instance of TimePickerDialog and return it
        return mTimePickerDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        mTextView.setText(String.format("%s%s%s", putLeadingZero(hourOfDay), TIME_SEPARATOR, putLeadingZero(minute)));
    }

    private String putLeadingZero(int value) {
        String result = String.valueOf(value);
        if (value < 9) {
            result = "0" + value;
        }

        return result;
    }

    public static void show(AppCompatActivity activity,EditText editText){
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment)fragmentManager.findFragmentByTag(TAG);

        if(fragment != null){
            fragment.dismiss();
        }

        fragment = new TimePickerFragment(editText);
        fragment.setCancelable(false);
        fragment.show(activity.getSupportFragmentManager(), TimePickerFragment.TAG);
        editText.clearFocus();
    }

}

