package de.dosenhering.qrreader.qrcodehandling

import com.google.zxing.Result
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.dialogs.DialogBuilder
import me.dm7.barcodescanner.zxing.ZXingScannerView

public class QRCodeResultHandler : ZXingScannerView.ResultHandler {

    private val scannerView: ZXingScannerView;
    private val dialogBuilder: DialogBuilder;
    private val mainActivity: MainActivity;

    public constructor(mainActivity: MainActivity, scannerView: ZXingScannerView, dialogBuilder: DialogBuilder) {
        this.dialogBuilder = dialogBuilder;
        this.mainActivity = mainActivity;
        this.scannerView = scannerView;
    }

    override fun handleResult(result: Result?) {
        if(result != null) {
            this.mainActivity.triggerVibration(100);
            this.mainActivity.showOverlay(result.text);
        }
    }

    /**
     * Stops the execution of the camera preview
     */
    public fun stopCamera() {
        this.scannerView.stopCamera();
    }

    /**
     * Starts the execution of the camera preview
     */
    public fun startCamera() {
        this.scannerView.setResultHandler(this);
        this.scannerView.startCamera();
    }

    /**
     * Resumes the execution of the camera preview
     */
    public fun resumeCamera() {
        this.scannerView.resumeCameraPreview(this);
    }
}