package de.dosenhering.qrreader.dialogs

import android.content.DialogInterface
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.permissions.PermissionHandler
import de.dosenhering.qrreader.qrcodehandling.QRCodeResultHandler

public class CameraPermissionGrantOkListener : PermissionGrantOkListener {

    private val resultHandler: QRCodeResultHandler;

    public constructor(resultHandler: QRCodeResultHandler, permissionHandler: PermissionHandler)
            : super(android.Manifest.permission.CAMERA, permissionHandler) {
        this.resultHandler = resultHandler;
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        super.onClick(dialog, which);
        this.resultHandler.startCamera();
    }

}