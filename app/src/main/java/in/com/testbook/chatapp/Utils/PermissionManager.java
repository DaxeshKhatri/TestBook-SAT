package in.com.testbook.chatapp.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;


/**
 * Created by Kiran Zinzuvadia on 26/10/16.
 * Utility class that wraps access to the runtime permissions API in M and provides basic
 * helping methods.
 */
public final class PermissionManager {

    public static final int REQUEST_CAMERA = 0x1;
    public static final int REQUEST_READ_STORAGE = 0x2;
    public static final int REQUEST_WRITE_STORAGE = 0x3;
    public static final int REQUEST_READ_CONTACTS = 0x4;
    public static final int REQUEST_WRITE_CONTACTS = 0x5;

    public static final int REQUEST_GROUP_PERMISSION = 0x6;

    public static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};

    public static boolean checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(final Activity activity, final int requestCode, final String permission, String someRationalMsg) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
//            // Provide an additional rationale to the user if permission is previously denied by the use.
//            Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), someRationalMsg, Snackbar.LENGTH_INDEFINITE)
//                    .setAction("OK", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//                        }
//                    })
//                    .show();
//        } else {
        // Permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//        }
    }

    public static void requestPermission(final Activity activity, final int requestCode, final String[] permission, String someRationalMsg) {
//        boolean needRationalMsg = false;
//        for (String perm : permission) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
//                needRationalMsg = true;
//            }
//        }
//
//        if (needRationalMsg) {
//            // Provide an additional rationale to the user if permission is previously denied by the use.
//            Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), someRationalMsg, Snackbar.LENGTH_INDEFINITE)
//                    .setAction("OK", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ActivityCompat.requestPermissions(activity, permission, requestCode);
//                        }
//                    })
//                    .show();
//        } else {
        // Permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(activity, permission, requestCode);
//        }
    }

    public static boolean verifyPermission(int[] grantResults) {
        // At least one result must be checked
        if (grantResults.length < 1) return false;
        // Verify that each required permission has been granted, otherwise return false
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

}
