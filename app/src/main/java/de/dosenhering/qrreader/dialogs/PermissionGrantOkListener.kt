package de.dosenhering.qrreader.dialogs

import android.content.DialogInterface
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.permissions.PermissionHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeResultHandler

public class PermissionGrantOkListener : DialogInterface.OnClickListener {

    private val permissionHandler: PermissionHandler?;
    private val resultHandler: QRCodeResultHandler;

    public constructor(resultHandler: QRCodeResultHandler, permissionHandler: PermissionHandler) {
        this.permissionHandler = permissionHandler;
        this.resultHandler = resultHandler;
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if(MainActivity.isMarshmallowOrGreater()) {
            this.permissionHandler!!.requestPermissions(arrayOf(android.Manifest.permission.CAMERA));
            this.resultHandler.startCamera();
        }
    }

}