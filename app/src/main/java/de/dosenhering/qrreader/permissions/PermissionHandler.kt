package de.dosenhering.qrreader.permissions

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

public class PermissionHandler {

    private val activity: Activity;
    private val requestCode: Int;

    constructor(context: Activity, requestCode: Int) {
        this.requestCode = requestCode;
        this.activity = context;
    }

    /**
     * Checks whether the specified permission was granted by the user
     * @param permission permission to be checked
     */
    public fun hasPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this.activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request multiple permissions from the user
     * @param permissions collection of permissions to be requested
     */
    public fun requestPermissions(permissions: Array<String>) {
        this.activity.requestPermissions(permissions, this.requestCode);
    }

    /**
     * Returns whether the permission request result with the specified requestCode was initiated by this PermissionHandler
     * @param requestCode returned request code of the permission-request-result
     */
    public fun isAppPermissionRequest(requestCode: Int) : Boolean {
        return requestCode == this.requestCode;
    }
}