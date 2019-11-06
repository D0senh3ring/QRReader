package de.dosenhering.qrreader.permissions

import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.dialogs.DialogBuilder
import de.dosenhering.qrreader.dialogs.CameraPermissionGrantOkListener
import de.dosenhering.qrreader.dialogs.PermissionGrantOkListener
import de.dosenhering.qrreader.qrcodehandling.QRCodeResultHandler

public class PermissionHandler {

    private val requestCode: Int;
    private val mainActivity: MainActivity;

    constructor(mainActivity: MainActivity, requestCode: Int) {
        this.mainActivity = mainActivity;
        this.requestCode = requestCode;
    }

    /**
     * Checks whether the specified permission was granted by the user
     * @param permission permission to be checked
     */
    public fun hasPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this.mainActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request multiple permissions from the user
     * @param permissions collection of permissions to be requested
     */
    public fun requestPermissions(permissions: Array<String>) {
        this.mainActivity.requestPermissions(permissions, this.requestCode);
    }

    /**
     * Returns whether the camera-access-permission was granted by the user
     */
    public fun hasCameraPermission() : Boolean {
        return this.hasPermission(android.Manifest.permission.CAMERA);
    }

    /**
     * Requests the camera-access-permission from the user
     */
    public fun requestCameraPermission() {
        this.requestPermissions(arrayOf(android.Manifest.permission.CAMERA));
    }

    /**
     * Returns whether the change- and access-permission to the phones wifi was granted by the user
     */
    public fun hasWifiPermissions() : Boolean {
        return    this.hasPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
               && this.hasPermission(android.Manifest.permission.CHANGE_WIFI_STATE);
    }

    /**
     * Request change- and access-permissions to the phones wifi from the user
     */
    public fun requestWifiPermissions() {
        this.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_WIFI_STATE,
                                        android.Manifest.permission.CHANGE_WIFI_STATE));
    }

    /**
     * Returns whether the permission request result with the specified requestCode was initiated by this PermissionHandler
     * @param requestCode returned request code of the permission-request-result
     */
    public fun isAppPermissionRequest(requestCode: Int) : Boolean {
        return requestCode == this.requestCode;
    }

    /**
     * Handels permission request results
     */
    public fun handlePermissionRequestResult(permissions: Array<out String>, grantResults: IntArray, dialogBuilder: DialogBuilder, qrCodeResultHandler: QRCodeResultHandler) {
        if(grantResults.isNotEmpty()) {
            for(index in permissions.indices) {
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    when(permissions[index]) {
                        android.Manifest.permission.CAMERA -> dialogBuilder!!.showAlert("Camera access was successfully granted.");
                        android.Manifest.permission.CHANGE_WIFI_STATE -> dialogBuilder.showAlert("Change permission to Wifi-state successfully granted");
                        android.Manifest.permission.ACCESS_WIFI_STATE -> dialogBuilder.showAlert("Read permission to Wifi-state successfully granted");
                    }
                } else if(grantResults[index] == PackageManager.PERMISSION_DENIED && this.mainActivity.shouldShowRequestPermissionRationale(permissions[index])) {
                    when(permissions[index]) {
                        android.Manifest.permission.CAMERA -> {
                            dialogBuilder.showOkCancelMessage("Please grant camera access in order to scan qr codes.",
                                                              CameraPermissionGrantOkListener(qrCodeResultHandler, this))
                        };
                        android.Manifest.permission.CHANGE_WIFI_STATE -> {
                            dialogBuilder.showOkCancelMessage("Please enable Wifi-change access in order to add a wifi-network after scanning its qr code",
                                                              PermissionGrantOkListener(android.Manifest.permission.CHANGE_WIFI_STATE, this));
                        };
                        android.Manifest.permission.ACCESS_WIFI_STATE -> {
                            dialogBuilder.showOkCancelMessage("Please enable Wifi-read access in order to connect to a wifi-network after scanning its qr code",
                                                              PermissionGrantOkListener(android.Manifest.permission.ACCESS_WIFI_STATE, this));
                        };
                    }
                }
            }
        }
    }
}