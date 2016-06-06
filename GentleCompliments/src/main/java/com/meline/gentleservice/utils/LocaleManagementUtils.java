package com.meline.gentleservice.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.meline.gentleservice.R;
import com.meline.gentleservice.api.database.DatabaseAsyncManager;
import com.meline.gentleservice.ui.interfaces.LocaleLoader;
import com.meline.gentleservice.ui.interfaces.LocaleSaver;

import java.util.Locale;

public class LocaleManagementUtils {
    private Context mContext;
    private String mNewLocaleStr;
    public LocaleManagementUtils(Context context) {
        this.mContext = context;
        this.mNewLocaleStr = context.getResources().getConfiguration().locale.toString();
    }

    public void manageLocale(LocaleLoader localeLoader, LocaleSaver localeSaver) {
        String oldLocale = localeLoader.loadLocale(mContext.getString(R.string.sp_locale));
        if (oldLocale != null) {
            if (isLocaleChanged(oldLocale)) {
                changeLocale(mNewLocaleStr);
                localeSaver.saveLocale(mContext.getString(R.string.sp_locale), mNewLocaleStr);
                DatabaseAsyncManager managerDB = new DatabaseAsyncManager(mContext);
                managerDB.execute("changeComplimentsLocale");
            }
        } else {
            localeSaver.saveLocale(mContext.getString(R.string.sp_locale), mNewLocaleStr);
        }
    }

    public boolean isLocaleChanged(String oldLocale) {
        return !oldLocale.equalsIgnoreCase(mNewLocaleStr);
    }


    private void changeLocale(String newLocale) {
        Configuration config = new Configuration();
        config.locale = new Locale(newLocale);
        mContext.getResources().updateConfiguration(config, mContext.getResources().getDisplayMetrics());
    }
}
