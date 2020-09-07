package com.example.mlkitplay

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MyImageAnalyzer(
    val onObjectDetected: (objects: List<String>) -> Unit
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            /** To use default options */
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            /** Or, to set the minimum confidence required */
            // val options = ImageLabelerOptions.Builder()
            //     .setConfidenceThreshold(0.7f)
            //     .build()
            // val labeler = ImageLabeling.getClient(options)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    onObjectDetected(labels.map { it.text }.toList())
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