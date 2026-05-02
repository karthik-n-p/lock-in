package `in`.karthiknp.myapplication.pose

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object AngleCalculator {

    /**
     * Calculates the angle (0–180°) at [mid] formed by the vectors [mid→first] and [mid→last].
     * Uses double precision to avoid Float precision loss from ML Kit positions.
     */
    fun angle(
        firstX: Float, firstY: Float,
        midX:   Float, midY:   Float,
        lastX:  Float, lastY:  Float
    ): Double {
        val radians = atan2(
            (lastY - midY).toDouble(),
            (lastX - midX).toDouble()
        ) - atan2(
            (firstY - midY).toDouble(),
            (firstX - midX).toDouble()
        )
        var degrees = Math.toDegrees(radians)
        degrees = abs(degrees)
        if (degrees > 180.0) degrees = 360.0 - degrees
        return degrees
    }

    /**
     * Euclidean distance between two landmark positions.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = (x2 - x1).toDouble()
        val dy = (y2 - y1).toDouble()
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * Average two angles — useful for bilateral (left+right) joint averaging.
     */
    fun average(a: Double, b: Double) = (a + b) / 2.0
}
