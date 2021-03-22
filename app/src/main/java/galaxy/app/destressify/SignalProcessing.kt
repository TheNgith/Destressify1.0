package galaxy.app.destressify

import android.util.Log
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import uk.me.berndporr.iirj.Butterworth
import java.util.*

object SignalProcessing {
    var frameRate = 0.0
    fun movingAverage(values: ArrayList<Double>): ArrayList<Double> {
        val newValues = ArrayList<Double>()
        val size = values.size
        var newValue = 0.0
        for (i in 0 until size) {
            newValue = if (i == 0) {
                (values[i] + values[i + 1] + values[i + 2] + values[i + 3] + values[i + 4]) / 5
            } else if (i == 1) {
                (values[i - 1] + values[i] + values[i + 1] + values[i + 2] + values[i + 3]) / 5
            } else if (i == size - 1) {
                (values[i - 4] + values[i - 3] + values[i - 2] + values[i - 1] + values[i]) / 5
            } else if (i == size - 2) {
                (values[i - 3] + values[i - 2] + values[i - 1] + values[i] + values[i + 1]) / 5
            } else {
                (values[i - 2] + values[i - 1] + values[i] + values[i + 2]) / 5
            }
            newValues.add(newValue)
        }
        return newValues
    }

    fun signalProcess(values: ArrayList<Double>): ArrayList<Double> {
        var values = values
        frameRate = values.size / MainActivity.RECORDING_TIME
        values = butterworthFilter(values)
        values = splineInterpolate(values)

//        values = slidingWindowTransform(values, frameRate);
//        values = movingAverage(values);
        return values
    }

    fun getBPM(values: ArrayList<Double>): Double {
        val fftValues = fftTransform(values)

//        int initialIndex = (int) ((40.0/60.0) * fftValues.size() / (frameRate * 2));
//        int finalIndex = (int) ((230.0/60.0) * fftValues.size() / (frameRate * 2));
//        Log.d("AAA", Double.toString(initialIndex) + " " + Double.toString(finalIndex));
//        Map<Double, Double> fftPeaks = getPeaks(new ArrayList<Double>(fftValues.subList(initialIndex, finalIndex)), false);
        val fftPeaks =
            getPeaks(
                ArrayList(
                    fftValues.subList(
                        0,
                        fftValues.size / 2
                    )
                ), false
            )
        var maxValue = -1.0
        var maxIndex = 0.0
        for ((key, value) in fftPeaks) {
            if (value >= maxValue) {
                maxValue = value
                maxIndex = key
            }
        }

//        double bpm = ((maxIndex * frameRate) / 10) / fftValues.size() * (230 - 40) + 40;
        //        Log.d("AAA", Double.toString(frameRate) + " " + Double.toString((double)maxIndex / fftValues.size()) + " " + Double.toString(maxIndex) + " " + Double.toString(bpm));
        return maxIndex * frameRate * 60 / fftValues.size
    }

    fun butterworthFilter(values: ArrayList<Double>): ArrayList<Double> {
        var newValues = ArrayList<Double>()
        val butterworth = Butterworth()
        val initialValue = frameRate * MainActivity.CUTOFF_TIME
        Log.d(
            "DDD",
            java.lang.Double.toString(frameRate) + " " + java.lang.Double.toString(
                initialValue
            ) + " " + Integer.toString(values.size)
        )
        val bpmLow = 40.0 / 60.0
        val bpmHigh = 230.0 / 60.0
        butterworth.bandPass(
            2,
            frameRate * 2,
            (bpmLow + bpmHigh) / 2,
            (bpmHigh - bpmLow) / 2
        )
        for (value in values) {
            val newValue = butterworth.filter(value)
            newValues.add(newValue)
        }
        newValues = ArrayList(
            newValues.subList(
                Math.ceil(initialValue).toInt(),
                newValues.size
            )
        )
        return newValues
    }

    fun slidingWindowTransform(values: ArrayList<Double>): ArrayList<Double> {
        val windowSize = values.size / 2
        val windowInterval = (0.5 * frameRate).toInt()
        val steps = values.size / windowInterval
        val newValues = ArrayList<Double>()
        for (i in 0 until steps) {
            val initialIndex = i * windowInterval
            val finalIndex = Math.min(initialIndex + windowSize, values.size)
            var subValues =
                ArrayList(values.subList(initialIndex, finalIndex))
            subValues = fftTransform(subValues)
            newValues.addAll(subValues)
        }
        return newValues
    }

    fun fftTransform(values: ArrayList<Double>): ArrayList<Double> {
        val initialSize = values.size
        val hw = HanningWindow()
        val size = closestPowerOfTwo(initialSize)
        val newValues = DoubleArray(size)
        for (i in 0 until size) {
            if (i < initialSize) {
                newValues[i] = values[i] * hw.value(i, size)
            } else {
                newValues[i] = 0.0
            }
        }
        val magValues = ArrayList<Double>()
        val transformer = FastFourierTransformer(DftNormalization.STANDARD)
        val complexResults =
            transformer.transform(newValues, TransformType.FORWARD)
        for (c in complexResults) {
            magValues.add(c.real * c.real + c.imaginary * c.imaginary)
        }
        return ArrayList(magValues)
    }

    private fun closestPowerOfTwo(size: Int): Int {
        var power = 1
        while (power < size) {
            power = power shl 1
        }
        return power
    }

    fun getPeaks(
        values: ArrayList<Double>,
        x: ArrayList<Double?>
    ): Map<Double, Double> {
        val maxima: MutableMap<Double, Double> =
            LinkedHashMap()
        val minima: MutableMap<Double?, Double> =
            LinkedHashMap()
        var maximum: Double? = null
        var minimum: Double? = null
        var maximumPos: Double? = null
        var minimumPos: Double? = null
        var lookForMax = true
        val size = values.size
        for (i in 0 until size) {
            val value = values[i]
            if (maximum == null || value > maximum) {
                maximum = value
                maximumPos = x[i]
            }
            if (minimum == null || value < minimum) {
                minimum = value
                minimumPos = x[i]
            }
            if (lookForMax) {
                if (value < maximum) {
                    maxima[maximumPos!!] = value
                    minimum = value
                    minimumPos = x[i]
                    lookForMax = false
                }
            } else {
                if (value > minimum) {
                    minima[minimumPos] = value
                    maximum = value
                    maximumPos = x[i]
                    lookForMax = true
                }
            }
        }
        return maxima
    }

    fun getPeaks(
        values: ArrayList<Double>,
        isTime: Boolean
    ): Map<Double, Double> {
        val x = ArrayList<Double?>()
        val size = values.size
        for (i in 0 until size) {
            if (!isTime) x.add(i.toDouble()) else x.add((MainActivity.RECORDING_TIME - MainActivity.CUTOFF_TIME) * i / size)
        }
        return getPeaks(values, x)
    }

    fun splineInterpolate(values: ArrayList<Double>): ArrayList<Double> {
        val size = values.size
        val x = DoubleArray(size)
        val y = DoubleArray(size)
        for (i in 0 until size) {
            x[i] = (MainActivity.RECORDING_TIME - MainActivity.CUTOFF_TIME) * i / size
            y[i] = values[i]
        }
        val si = SplineInterpolator()
        val f = si.interpolate(x, y)
        val res = ArrayList<Double>()
        for (d in x) {
            res.add(f.value(d))
        }
        return res
    }
}