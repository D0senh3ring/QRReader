package de.dosenhering.qrreader

import android.content.*
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import de.dosenhering.qrreader.databinding.ActivityMainBinding
import de.dosenhering.qrreader.dialogs.DialogBuilder
import de.dosenhering.qrreader.permissions.PermissionHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeResultHandler

class MainActivity : AppCompatActivity() {

    companion object {
        public const val HYPERLINK_PATTERN : String = "^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#@!\\$&'\\(\\)\\*\\+,;=.]+$";
        public const val WIFI_PATTERN : String = "^WIFI:S:(.*?);T:(\\w+)?;P:([\\w\\W]+)?;(\\w+)?;$";
    }

    private lateinit var binding : ActivityMainBinding;

    private var qrCodeResultHandler: QRCodeResultHandler? = null;
    private var permissionHandler: PermissionHandler? = null;
    private var dialogBuilder: DialogBuilder? = null;
    private var qrCodeHandler: QRCodeHandler? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        this.permissionHandler = PermissionHandler(this, 1001);
        this.dialogBuilder = DialogBuilder(this.applicationContext, this);
        this.qrCodeHandler = QRCodeHandler(this, this.dialogBuilder!!);
        this.qrCodeResultHandler = QRCodeResultHandler(this, this.binding.scannerView, this.dialogBuilder!!);

        if(!this.permissionHandler!!.hasCameraPermission()) {
            this.permissionHandler!!.requestCameraPermission();
        }
        this.qrCodeResultHandler!!.startCamera();
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
        this.qrCodeHandler!!.openInDefaultBrowser(this.binding.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onCopyClicked(view: View) {
        this.qrCodeHandler!!.copyToClipboard(this.binding.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onShareClicked(view: View) {
        this.qrCodeHandler!!.shareWithOtherApps(this.binding.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onSearchClicked(view: View) {
        this.qrCodeHandler!!.searchInDefaultBrowser(this.binding.lastScanResult!!);
        this.onDismissClicked(view);
    }

    public fun onConnectWifiClick(view: View) {
        this.qrCodeHandler!!.connectWifi(this.binding.lastScanResult!!, this.permissionHandler!!);
        this.onDismissClicked(view);
    }

    public fun onDismissClicked(@Suppress("UNUSED_PARAMETER") view: View) {
        this.hideOverlay();
        this.qrCodeResultHandler!!.resumeCamera();
        this.binding.lastScanResult = "";
    }


    /**
     * Shows the overlay-panel and sets the specified qrCodeText to the text display
     * @param qrCodeText text to be displayed in the text display TextView
     */
    public fun showOverlay(qrCodeText: String) {
        val isWifiAccessKey : Boolean = this.isWifiAccessKey(qrCodeText);
        val isHyperlink : Boolean = this.isHyperlink(qrCodeText);
        val wifiSSID : String = this.getWifiSSID(qrCodeText);

        this.binding.apply {
            this.overlayVisibility = true;
            this.scanDisplayText = if(isWifiAccessKey) { wifiSSID } else { qrCodeText; }
            this.lastScanResult = qrCodeText;
            this.connectWifiVisibility = isWifiAccessKey;
            this.openLinkVisibility = isHyperlink;
        }
    }

    /**
     * Triggers a device-vibration for the specified amount of time in milliseconds
     */
    public fun triggerVibration(duration: Long) {
        (this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
    }


    /**
     * Hides the overlay-panel
     */
    private fun hideOverlay() {
        this.binding.overlayVisibility = false;
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
        if(match != null && match.groups.size > 1 && match.groups[1] != null) {
            return match.groups[1]!!.value;
        }
        return "<could not read ssid>";
    }

    /**
     * Returns whether the overly-panel is visible
     */
    private fun isOverlayVisible() : Boolean {
        return this.binding.overlayVisibility!!;
    }
}
