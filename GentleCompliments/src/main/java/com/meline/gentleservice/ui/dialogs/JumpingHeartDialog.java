package com.meline.gentleservice.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.meline.gentleservice.R;

public class JumpingHeartDialog extends ProgressDialog {
    private ProgressDialog mDialog;

    public JumpingHeartDialog(Context context) {
        super(context);
        this.mDialog = new ProgressDialog(context);
        this.mDialog.setMessage(context.getString(R.string.please_wait));
        this.mDialog.setCancelable(false);
        this.mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mDialog.setIndeterminate(true);
        this.mDialog.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.jumping_heart_spinner));
    }

    @Override
    public void show() {
        mDialog.show();
    }

    @Override
    public void dismiss() {
        mDialog.dismiss();
    }
}
