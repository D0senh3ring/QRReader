package de.dosenhering.qrreader

import android.content.*
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import de.dosenhering.qrreader.dialogs.DialogBuilder
import de.dosenhering.qrreader.dialogs.PermissionGrantOkListener
import de.dosenhering.qrreader.permissions.PermissionHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeResultHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Checks if the currently installed API-Level is greater or equal to 23 (Marshmallow - 6.0)
         */
        public fun isMarshmallowOrGreater() : Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
        }

        /**
         * Checks if the currently installed API-Level is greater or equal to 26 (Oreo - 8.0)
         */
        public fun isOreoOrGreater() : Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O;
        }
    }

    private var overlayRoot: RelativeLayout? = null;
    private var qrCodeText: TextView? = null;

    private var qrCodeResultHandler: QRCodeResultHandler? = null;
    private var permissionHandler: PermissionHandler? = null;
    private var dialogBuilder: DialogBuilder? = null;
    private var qrCodeHandler: QRCodeHandler? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.overlayRoot = this.findViewById(R.id.overlayRoot);
        this.qrCodeText = this.findViewById(R.id.qrCodeText);

        this.permissionHandler = PermissionHandler(this, 1);
        this.dialogBuilder = DialogBuilder(this.applicationContext);
        this.qrCodeHandler = QRCodeHandler(this, this.dialogBuilder!!);
        this.qrCodeResultHandler = QRCodeResultHandler(this, this.dialogBuilder!!);

        this.qrCodeResultHandler!!.startCamera();

        if(MainActivity.isMarshmallowOrGreater()) {
            if(!this.hasCameraPermission()) {
                this.requestCameraPermission();
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(this.permissionHandler!!.isAppPermissionRequest(requestCode)) {
            if(grantResults.isEmpty()) {
                this.dialogBuilder!!.showAlert("Camera access could not be granted.");
            } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.dialogBuilder!!.showAlert("Camera access was successfully granted.");
                if(MainActivity.isMarshmallowOrGreater() && this.shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    this.dialogBuilder!!.showOkCancelMessage("Please grant access to your phones camera.", PermissionGrantOkListener(this.qrCodeResultHandler!!, this.permissionHandler!!));
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume();
        if(MainActivity.isMarshmallowOrGreater()) {
            if (!this.hasCameraPermission()) {
                this.requestCameraPermission();
            }
            this.qrCodeResultHandler!!.startCamera();
            this.hideOverlay();
        }
    }

    override fun onPause() {
        super.onPause()
        this.qrCodeResultHandler!!.stopCamera();
    }

    override fun onDestroy() {
        super.onDestroy()
        this.qrCodeResultHandler!!.stopCamera();
    }

    override fun onBackPressed() {
        if(this.isOverlayVisible()) {
            this.hideOverlay();
        } else {
            super.onBackPressed()
        }
    }

    public fun onOpenLinkClicked(view: View) {
        this.qrCodeHandler!!.openInDefaultBrowser(this.qrCodeText!!.text.toString());
        this.onDismissClicked(view);
    }

    public fun onCopyClicked(view: View) {
        this.qrCodeHandler!!.copyToClipboard(this.qrCodeText!!.text);
        this.onDismissClicked(view);
    }

    public fun onShareClicked(view: View) {
        this.qrCodeHandler!!.shareWithOtherApps(this.qrCodeText!!.text.toString());
        this.onDismissClicked(view);
    }

    public fun onSearchClicked(view: View) {
        this.qrCodeHandler!!.searchInDefaultBrowser(this.qrCodeText!!.text.toString());
        this.onDismissClicked(view);
    }

    public fun onDismissClicked(view: View) {
        this.hideOverlay();
        this.qrCodeResultHandler!!.resumeCamera();
    }


    /**
     * Shows the overlay-panel and sets the specified qrCodeText to the text display
     * @param qrCodeText text to be displayed in the text display TextView
     */
    public fun showOverlay(qrCodeText: String) {
        this.overlayRoot!!.visibility = View.VISIBLE;
        this.overlayRoot!!.bringToFront();

        this.qrCodeText?.text = qrCodeText;

        this.openLinkRow.visibility = if(this.isHyperlink(qrCodeText)) { View.VISIBLE; } else { View.GONE; }
    }

    /**
     * Hides the overlay-panel
     */
    public fun hideOverlay() {
        this.overlayRoot!!.visibility = View.GONE;
    }

    /**
     * Triggers a device-vibration for the specified amount of time in milliseconds
     */
    public fun triggerVibration(duration: Long) {
        if(MainActivity.isOreoOrGreater()) {
            (this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            (this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(duration);
        }
    }


    /**
     * Returns whether the camera-access-permission was granted by the user
     */
    private fun hasCameraPermission() : Boolean {
        return this.permissionHandler!!.hasPermission(android.Manifest.permission.CAMERA);
    }

    /**
     * Requests the camera-access-permission from the user
     */
    private fun requestCameraPermission() {
        this.permissionHandler!!.requestPermissions(arrayOf(android.Manifest.permission.CAMERA));
    }

    /**
     * Returns whether the given text is a valid URL
     * @param text text to be checked
     */
    private fun isHyperlink(text: String) : Boolean {
        return text.matches(Regex("^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#@!\\$&'\\(\\)\\*\\+,;=.]+$"));
    }

    /**
     * Returns whether the overly-panel is visible
     */
    private fun isOverlayVisible() : Boolean {
        return this.overlayRoot!!.visibility == View.VISIBLE;
    }
}
