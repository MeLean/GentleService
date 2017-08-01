package com.meline.gentleservice.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.meline.gentleservice.R;

public class RuntimePermissionAssistant {
    public static final int PERMISSIONS_SEND_SMS_CONSTANT = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkForPermission(final Context context, final String manifestPermission, final int requestConstant)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, manifestPermission)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(context.getString(R.string.permission_needed));

                    if(manifestPermission.contains("SEND_SMS")){
                        alertBuilder.setMessage(context.getString(R.string.sms_permission_needed));
                    }

                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{manifestPermission}, requestConstant);
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{manifestPermission}, requestConstant);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}