package com.example.mlkitplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.mlkit.vision.pose.PoseLandmark

class LandmarksView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // screenSize / imageSize
    var landmarksSpec = Point(1, 1)

    private val landmarks = mutableListOf<PoseLandmark>()

    private val pointPaint = Paint().apply {
        color = context.getColor(android.R.color.white)
        strokeWidth = 3f
    }

    fun setLandmarks(list: List<PoseLandmark>) {
        landmarks.clear()
        landmarks.addAll(list)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            landmarks.forEach { pose ->
                val rx = width * 1f / landmarksSpec.x
                val ry = height * 1f / landmarksSpec.y
                Log.d("onDraw", "Canvas x = $width, y = $height")
                Log.d("onDraw", "landmarkSpec x = ${landmarksSpec.x}, y = ${landmarksSpec.y}")
                drawPoint(pose.position.x * rx, pose.position.y * ry / 1.2f - 100, pointPaint)
            }
        }
    }

}