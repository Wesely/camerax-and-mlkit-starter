package com.example.mlkitplay

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.View
import android.view.WindowManager
import androidx.camera.camera2.internal.ImageAnalysisConfigProvider
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainCameraActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUESTS = 0x000001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isPermissionGranted(this)) {
            startCamera()
        } else {
            askCameraPermissions()
        }
    }

    private fun askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), PERMISSION_REQUESTS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isPermissionGranted(this)) {
            camera_preview.post { startCamera() }
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider)
            }, ContextCompat.getMainExecutor(this)
        )
    }

    @SuppressLint("RestrictedApi")
    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        try {
            cameraProvider.unbindAll()
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val camera = cameraProvider.bindToLifecycle(this, cameraSelector)

            val preview = buildPreview(camera)

            val imageAnalysis = ImageAnalysis.Builder
                .fromConfig(ImageAnalysisConfigProvider(this).getConfig(camera.cameraInfo))
                .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            camera_preview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val previewSize = SizeF(camera_preview.width.toFloat(), camera_preview.height.toFloat())

            val analyzer = MyImageAnalyzer(previewSize) { poseList, spec ->
                // render landmarks, but image's resolution != screen's size
                // TODO: don't need to calc every time, can be moved to somewhere else.
                landmark_detected.landmarksSpec = spec
                landmark_detected.setLandmarks(poseList)
                landmark_detected.invalidate()
                Log.d("pose Detected", "preview Width:${landmark_detected.width}")
            }

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), analyzer)

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun buildPreview(camera: Camera): Preview {
        return Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(camera_preview.createSurfaceProvider())
            }
    }


}