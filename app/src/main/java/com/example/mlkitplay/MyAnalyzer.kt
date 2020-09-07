package com.example.mlkitplay

import android.annotation.SuppressLint
import android.graphics.Point
import android.util.Log
import android.util.SizeF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark

class MyImageAnalyzer(
    val previewSize: SizeF,
    val onDetected: (objects: List<PoseLandmark>, spec: Point) -> Unit
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Pose detection with streaming frames
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setPerformanceMode(PoseDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()

            /** Pose detection on static images **/
            // val options = PoseDetectorOptions.Builder()
            //     .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            //     .setPerformanceMode(PoseDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            //     .build()

            val poseDetector = PoseDetection.getClient(options)
            Log.d("imageWidth", "w = ${image.width}")
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    onDetected(pose.allPoseLandmarks, Point(image.width, image.height))
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    // aware of this, or you will be able to process only one frame
                    imageProxy.close()
                }
        }

    }
}