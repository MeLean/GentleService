package milen.com.gentleservice.utils;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SoftInputManager implements View.OnKeyListener {


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            hideSoftInput(view);
            view.clearFocus();
            return true;
        }

        return false;
    }

    public static void hideSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
