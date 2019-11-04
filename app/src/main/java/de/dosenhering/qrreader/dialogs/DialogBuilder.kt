package de.dosenhering.qrreader.dialogs

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

public class DialogBuilder {

    private val context: Context;

    constructor(context: Context) {
        this.context = context;
    }

    /**
     * Shows a dialog box with the specified text and an ok- and cancel-button
     * @param message Text to be displayed
     * @param okHandler Object that will handle the ok-button-click-event
     */
    public fun showOkCancelMessage(message: String, okHandler: DialogInterface.OnClickListener) {
        android.support.v7.app.AlertDialog.Builder(this.context)
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
        Toast.makeText(this.context, message, duration).show();
    }

    /**
     * Shows an alert with the specified message for a long period of time
     * @param message Text to be displayed
     */
    public fun showAlert(message: String) {
        this.showAlert(message, Toast.LENGTH_LONG);
    }
}