package de.dosenhering.qrreader.dialogs

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.Toast

public class DialogBuilder {

    private val mainActivityContext : Context;
    private val appContext : Context;

    constructor(appContext: Context, mainActivityContext: Context) {
        this.mainActivityContext = mainActivityContext;
        this.appContext = appContext;
    }

    /**
     * Shows a dialog box with the specified text and an ok- and cancel-button
     * @param message Text to be displayed
     * @param okHandler Object that will handle the ok-button-click-event
     */
    public fun showOkCancelMessage(message: String, okHandler: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this.mainActivityContext)
            .setMessage(message)
            .setPositiveButton("OK", okHandler)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    }

    /**
     * Shows an alert with the specified message for the specified duration
     * @param message Text to be displayed
     * @param duration
     */
    public fun showAlert(message: String, duration: Int) {
        Toast.makeText(this.appContext, message, duration).show();
    }

    /**
     * Shows an alert with the specified message for a long period of time
     * @param message Text to be displayed
     */
    public fun showAlert(message: String) {
        this.showAlert(message, Toast.LENGTH_LONG);
    }
}