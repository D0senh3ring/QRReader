package de.dosenhering.qrreader.qrcodehandling

import android.util.Log
import android.widget.RelativeLayout
import com.google.zxing.Result
import de.dosenhering.qrreader.MainActivity
import de.dosenhering.qrreader.R
import de.dosenhering.qrreader.dialogs.DialogBuilder
import me.dm7.barcodescanner.zxing.ZXingScannerView

public class QRCodeResultHandler : ZXingScannerView.ResultHandler {

    private val scannerView: ZXingScannerView;
    private val overlayRoot:  RelativeLayout;
    private val dialogBuilder: DialogBuilder;
    private val mainActivity: MainActivity;

    public constructor(mainActivity: MainActivity, dialogBuilder: DialogBuilder) {
        this.scannerView = mainActivity.findViewById(R.id.scannerView);
        this.overlayRoot = mainActivity.findViewById(R.id.overlayRoot);
        this.dialogBuilder = dialogBuilder;
        this.mainActivity = mainActivity;
    }

    override fun handleResult(result: Result?) {
        if(result != null) {
            this.mainActivity.triggerVibration(100);

            if(this.overlayRoot == null) {
                this.dialogBuilder.showAlert("Could not instantiate overlay.");
                this.resumeCamera();
            } else {
                Log.d("QRReader", result.barcodeFormat.toString());
                this.mainActivity.showOverlay(result.text);
            }
        }
    }

    /**
     * Stops the execution of the camera preview
     */
    public fun stopCamera() {
        this.scannerView!!.stopCamera();
    }

    /**
     * Starts the execution of the camera preview
     */
    public fun startCamera() {
        this.scannerView!!.setResultHandler(this);
        this.scannerView!!.startCamera();
    }

    /**
     * Resumes the execution of the camera preview
     */
    public fun resumeCamera() {
        this.scannerView!!.resumeCameraPreview(this);
    }
}