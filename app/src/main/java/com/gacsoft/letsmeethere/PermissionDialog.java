package com.gacsoft.letsmeethere;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by Gacsoft on 9/13/2016.
 */
public class PermissionDialog {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 191;

    @TargetApi(23)
    public static void askPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT < 23) return;
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{permission}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
}
