package galaxy.app.destressify

import java.util.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

class HeartDataNew(beatsArray: ArrayList<Double>, private val avgBPM: Int) {
    private val peaks : ArrayList<Double> = beatsArray

    private val rrInterval = ArrayList<Double>()

    var bpm = 0

    // average of NN-interval
    var aVNN = 0.0
        private set

    // sd of NN-interval
    var sDNN = 0.0
        private set

    // rMSSD = square root of the mean of the squares of differences between adjacent NN intervals
    var rMSSD = 0.0
        private set

    var pPN50 = 0.0
        private set

    // stress value
    var stress = 0

    private fun initBPM() {
        bpm = avgBPM
        //        bpm = (peaks.size() * 60.0 / (MainActivity.kt.RECORDING_TIME - MainActivity.kt.CUTOFF_TIME));
    }

    private fun initRR() {
        var prevPeak: Double? = null
        for ((i, value) in peaks.withIndex()) {
            if (i > 0) {
                rrInterval.add((value - prevPeak!!) * 1000.0)
            }
            prevPeak = value
        }
    }

    // average of NN-interval
    private fun initAVNN() {
        var sum = 0.0
        for (d in rrInterval) {
            sum += d
        }
        aVNN = sum / rrInterval.size
    }

    // sd of NN-interval
    private fun initSDNN() {
        var standardDeviation = 0.0
        val mean = aVNN
        for (d in rrInterval) {
            standardDeviation += (d - mean).pow(2.0)
        }
        sDNN = sqrt(standardDeviation / rrInterval.size)
    }

    // rMSSD = square root of the mean of the squares of differences between adjacent NN intervals
    private fun initRMSSD() {
        var sum = 0.0
        val size = rrInterval.size
        for (i in 1 until size) {
            val diff = rrInterval[i] - rrInterval[i - 1]
            sum += diff * diff
        }
        rMSSD = sqrt(sum / (rrInterval.size - 1))
    }

    private fun initPPN50() {
        var count = 0
        val size = rrInterval.size
        for (i in 1 until size) {
            val diff = rrInterval[i] - rrInterval[i - 1]
            if (diff > 0.05) {
                ++count
            }
        }
        pPN50 = count.toDouble() / (rrInterval.size - 1) * 100
    }

    init {
        initBPM()
        initRR()
        initAVNN()
        initSDNN()
        initRMSSD()
        initPPN50()
    }
}