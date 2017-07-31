package com.meline.gentleservice.api.database;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.meline.gentleservice.R;
import com.meline.gentleservice.services.GentleSystemActionReceiver;

import java.sql.SQLException;

public class AsyncTaskManageDbAfterLocaleChanges extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... contexts) {
        Context context = contexts[0];
        boolean hasNoErrors = true;
        DBHelper db = DBHelper.getInstance(context);
        try {
            db.deleteAllDefaultCompliments();
            String[] localizedCompliments = context.getResources().getStringArray(R.array.compliments_array);
            db.addAllComplementsAsDefault(localizedCompliments);

        } catch (SQLException e) {
            hasNoErrors = false;
            //Log.d("AppDebug", "AsyncTaskManageDbAfterLocaleChanges error:\n" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        if(hasNoErrors){
            broadcastManageLoacaleChangesAction(context);
            //Log.d("AppDebug", "AsyncTaskManageDbAfterLocaleChanges everything is OK");
        }

        return null;
    }

    private static void broadcastManageLoacaleChangesAction(Context context) {
        Intent intent = new Intent();
        intent.setAction(GentleSystemActionReceiver.ACTION_MANAGE_AFTER_LOCALE_CHANGED);
        context.sendBroadcast(intent);
    }
}