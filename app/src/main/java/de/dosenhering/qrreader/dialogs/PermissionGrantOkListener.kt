package de.dosenhering.qrreader.dialogs

import android.content.DialogInterface
import de.dosenhering.qrreader.permissions.PermissionHandler

public open class PermissionGrantOkListener : DialogInterface.OnClickListener {

    protected val permissionHandler: PermissionHandler;
    protected val permission : String;

    public constructor(permission: String, permissionHandler: PermissionHandler) {
        this.permissionHandler = permissionHandler;
        this.permission = permission;
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        this.permissionHandler.requestPermissions(arrayOf(this.permission));
    }
}