package com.meline.gentleservice.api.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.sql.SQLException;

import com.meline.gentleservice.R;

public class DatabaseAsyncManager extends AsyncTask<String, Void, Void> {
    private Context mContext;
    private ProgressDialog mDialog;
    private boolean isSuccessful = true;

    public DatabaseAsyncManager(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.please_wait));
        mDialog.setCancelable(true);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setIndeterminate(true);
        mDialog.setIndeterminateDrawable(ContextCompat.getDrawable(mContext, R.drawable.jumping_heart_cartoon));
        mDialog.show();
    }

    @Override
    protected Void doInBackground(String... input) {
        String requestStr = input[0];
        DBHelper db = DBHelper.getInstance(mContext);

        switch (requestStr) {
            case "changeComplimentsLocale":
                try {
                    db.deleteAllDefaultCompliments();
                    String[] complimentsStr = mContext.getResources().getStringArray(R.array.compliments_array);
                    db.addAllCompliments(complimentsStr);
                } catch (SQLException e) {
                    isSuccessful = false;
                }
                break;
            default:
                isSuccessful = false;
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        super.onPostExecute(params);
        if (!isSuccessful) {
            Toast.makeText(mContext, mContext.getString(R.string.action_failed), Toast.LENGTH_SHORT).show();
        }
        mDialog.dismiss();
    }
}
