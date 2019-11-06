package de.dosenhering.qrreader

import android.content.*
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
        public const val HYPERLINK_PATTERN : String = "^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#@!\\$&'\\(\\)\\*\\+,;=.]+$";
        public const val WIFI_PATTERN : String = "^WIFI:S:(.*?);T:(\\w+)?;P:([\\w\\W]+)?;(\\w+)?;$";

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

    private var lastScanText: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.overlayRoot = this.findViewById(R.id.overlayRoot);
        this.qrCodeText = this.findViewById(R.id.qrCodeText);

        this.permissionHandler = PermissionHandler(this, 1001);
        this.dialogBuilder = DialogBuilder(this.applicationContext, this);
        this.qrCodeHandler = QRCodeHandler(this, this.dialogBuilder!!);
        this.qrCodeResultHandler = QRCodeResultHandler(this, this.dialogBuilder!!);

        this.qrCodeResultHandler!!.startCamera();

        if(MainActivity.isMarshmallowOrGreater() && !this.permissionHandler!!.hasCameraPermission()) {
            this.permissionHandler!!.requestCameraPermission();
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(this.permissionHandler!!.isAppPermissionRequest(requestCode)) {
            this.permissionHandler!!.handlePermissionRequestResult(permissions, grantResults, this.dialogBuilder!!, this.qrCodeResultHandler!!);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume();
        if (this.permissionHandler!!.hasCameraPermission()) {
            this.qrCodeResultHandler!!.startCamera();
        }
        this.hideOverlay();
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
        this.qrCodeHandler!!.openInDefaultBrowser(this.qrCodeResultHandler!!.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onCopyClicked(view: View) {
        this.qrCodeHandler!!.copyToClipboard(this.qrCodeResultHandler!!.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onShareClicked(view: View) {
        this.qrCodeHandler!!.shareWithOtherApps(this.qrCodeResultHandler!!.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onSearchClicked(view: View) {
        this.qrCodeHandler!!.searchInDefaultBrowser(this.qrCodeResultHandler!!.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onConnectWifiClick(view: View) {
        this.qrCodeHandler!!.connectWifi(this.qrCodeResultHandler!!.lastScanResult!!, this.permissionHandler!!);
        this.onDismissClicked(view);
    }

    public fun onDismissClicked(view: View) {
        this.hideOverlay();
        this.qrCodeResultHandler!!.resumeCamera();
        this.lastScanText = null;
    }


    /**
     * Shows the overlay-panel and sets the specified qrCodeText to the text display
     * @param qrCodeText text to be displayed in the text display TextView
     */
    public fun showOverlay(qrCodeText: String) {
        val isWifiAccessKey : Boolean = this.isWifiAccessKey(qrCodeText);
        val isHyperlink : Boolean = this.isHyperlink(qrCodeText);

        this.overlayRoot!!.visibility = View.VISIBLE;
        this.overlayRoot!!.bringToFront();

        this.qrCodeText?.text = if(this.isWifiAccessKey(qrCodeText)) { this.getWifiSSID(qrCodeText) } else { qrCodeText; }

        this.connectWifiRow.visibility = if(isWifiAccessKey) { View.VISIBLE } else { View.GONE };
        this.openLinkRow.visibility = if(isHyperlink) { View.VISIBLE; } else { View.GONE; }
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
     * Returns whether the given text is a valid URL
     * @param text text to be checked
     */
    private fun isHyperlink(text: String) : Boolean {
        return text.matches(Regex(MainActivity.HYPERLINK_PATTERN));
    }

    /**
     * Returns whether the given text is a valid wifi access key
     * @param text text to be checked
     */
    private fun isWifiAccessKey(text: String) : Boolean {
        return text.matches(Regex(MainActivity.WIFI_PATTERN));
    }

    /**
     * Returns the regex-matches for the given access-key with the pattern MainActivity.WIFI_PATTERN
     * @param accessKey access-key to extract the matches from
     */
    private fun getWifiMatches(accessKey: String) : MatchResult? {
        return Regex(MainActivity.WIFI_PATTERN).find(accessKey)
    }

    /**
     * Gets the wifi's ssid from the given wifi-access-key
     * @param accessKey access-key to extract the ssid from
     */
    private fun getWifiSSID(accessKey: String) : String {
        val match = this.getWifiMatches(accessKey);
        if(match != null && match!!.groups.size > 1 && match!!.groups[1] != null) {
            return match!!.groups[1]!!.value;
        }
        return "<could not read ssid>";
    }

    /**
     * Returns whether the overly-panel is visible
     */
    private fun isOverlayVisible() : Boolean {
        return this.overlayRoot!!.visibility == View.VISIBLE;
    }
}
