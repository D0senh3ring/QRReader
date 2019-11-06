package de.dosenhering.qrreader.qrcodehandling

import android.app.Activity
import android.content.*
import android.net.Uri
import android.net.wifi.WifiManager
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.dialogs.DialogBuilder
import de.dosenhering.qrreader.net.WifiManagerHelper
import de.dosenhering.qrreader.net.WifiSecurity
import de.dosenhering.qrreader.permissions.PermissionHandler
import java.net.URLEncoder

public class QRCodeHandler {

    private val dialogBuilder: DialogBuilder;
    private val mainActivity: Activity;

    public var defaultSearchEngineQueryString = "https://duckduckgo.com/?q=%s" //TODO(Let the user choose the preferred search-engine)

    public constructor(mainActivity: Activity, dialogBuilder: DialogBuilder) {
        this.dialogBuilder = dialogBuilder;
        this.mainActivity = mainActivity;
    }

    public fun connectWifi(accessKey: String, permissionHandler: PermissionHandler) {
        if(!permissionHandler.hasWifiPermissions()) {
            permissionHandler.requestWifiPermissions();
        }

        if(permissionHandler.hasWifiPermissions()) {
            val match = Regex(MainActivity.WIFI_PATTERN).find(accessKey);

            val wifiManager : WifiManager = this.mainActivity.getSystemService(Context.WIFI_SERVICE) as WifiManager;

            val ssid : String = match!!.groups[1]!!.value;
            val pass : String = match!!.groups[3]!!.value;
            val security : WifiSecurity = WifiSecurity.valueOf(match!!.groups[2]!!.value);

            if(WifiManagerHelper(wifiManager).connectWIFI(ssid, pass, security)) {
                this.dialogBuilder.showAlert("Connected to wifi \"$ssid\"");
            } else {
                this.dialogBuilder.showAlert("Could not connect to wifi \"$ssid\"");
            }
        }
    }

    /**
     * Opens the devices default web browser with the given url
     * @param url url to be opened in the web browser
     */
    public fun openInDefaultBrowser(url: String) {
        try {
            this.mainActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ex: ActivityNotFoundException) {
            this.dialogBuilder!!.showAlert("Your default browser could not be located.");
        }
    }

    /**
     * Opens the devices default web browser with a duckduckgo-search for the given text
     * @param text text to be searched for on the web
     */
    public fun searchInDefaultBrowser(text: String) {
        this.openInDefaultBrowser(String.format(this.defaultSearchEngineQueryString, URLEncoder.encode(text, "utf-8")));
    }

    /**
     * Shows a picker-dialog to select an app to share the given content
     * @param content content to be shared with another app
     */
    public fun shareWithOtherApps(text: CharSequence) {
        val share : Intent = Intent(Intent.ACTION_SEND);
        share.type = "text/plain";
        share.putExtra(Intent.EXTRA_STREAM, text);

        try {
            this.mainActivity.startActivity(Intent.createChooser(share, "Share content"));
        } catch (ex: ActivityNotFoundException) {
            this.dialogBuilder!!.showAlert("Share-picker could not be opened.");
        }
    }

    /**
     * Copies the provided text into the clipboard
     * @param text text to be copied
     */
    public fun copyToClipboard(text: CharSequence) {
        val clipboard = this.mainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
        clipboard.primaryClip = ClipData.newPlainText(text, text);
        this.dialogBuilder!!.showAlert("Copied to clipboard");
    }
}