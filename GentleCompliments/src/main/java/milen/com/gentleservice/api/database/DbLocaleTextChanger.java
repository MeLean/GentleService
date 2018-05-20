package milen.com.gentleservice.api.database;

import android.content.Context;
import android.os.AsyncTask;


import milen.com.gentleservice.R;

import java.sql.SQLException;

public class DbLocaleTextChanger extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... contexts) {
        Context context = contexts[0];
        DBHelper db = DBHelper.getInstance(context);
        try {
            db.deleteAllDefaultCompliments();
            String[] localizedCompliments = context.getResources().getStringArray(R.array.compliments_array);
            db.addAllComplementsAsDefault(localizedCompliments);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}