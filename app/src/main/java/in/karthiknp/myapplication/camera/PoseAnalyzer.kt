package `in`.karthiknp.myapplication.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Callback delivers:
 *  - pose: the detected landmarks
 *  - displayWidth / displayHeight: image dimensions AFTER rotation is applied.
 *    Use these to map landmark pixel coordinates → screen coordinates.
 */
class PoseAnalyzer(
    private val onPoseDetected: (pose: Pose, displayWidth: Int, displayHeight: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector  = PoseDetection.getClient(options)
    private val processing = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            processing.set(false)
            return
        }

        val rotation   = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        // After rotation is applied, width/height may swap
        val displayW = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val displayH = if (rotation == 90 || rotation == 270) imageProxy.width  else imageProxy.height

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                if (pose.allPoseLandmarks.isNotEmpty()) {
                    onPoseDetected(pose, displayW, displayH)
                }
            }
            .addOnFailureListener { /* drop bad frames silently */ }
            .addOnCompleteListener {
                imageProxy.close()
                processing.set(false)
            }
    }

    fun close() { detector.close() }
}
