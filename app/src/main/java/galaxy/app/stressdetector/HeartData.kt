package galaxy.app.stressdetector

import java.util.*

class HeartData(private val values: ArrayList<Double>) {
    private val peaks: LinkedHashMap<Double, Double>
    private val rrInterval = ArrayList<Double>()
    var bPM = 0.0
        private set

    // average of NN-interval
    var aVNN = 0.0
        private set

    // sd of NN-interval
    var sDNN = 0.0
        private set

    // rMSSD = square root of the mean of the squares of differences between adjacent NN intervals
    var rMSSD = 0.0
        private set

    // pNN50 = % of differences between adjacent NN intervals that are greater than 50 ms
    var pPN50 = 0.0
        private set

    var stress = 0

    private fun initBPM() {
        bPM = SignalProcessing.getBPM(values)
        //        bpm = (peaks.size() * 60.0 / (MainActivity.RECORDING_TIME - MainActivity.CUTOFF_TIME));
    }

    private fun initRR() {
        var i = 0
        var prevPeak: Double? = null
        for ((key) in peaks) {
            if (i > 0) {
                rrInterval.add(key - prevPeak!!)
            }
            prevPeak = key
            ++i
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
        val sum = 0.0
        var standardDeviation = 0.0
        val mean = aVNN
        for (d in rrInterval) {
            standardDeviation += Math.pow(d - mean, 2.0)
        }
        sDNN = Math.sqrt(standardDeviation / rrInterval.size)
    }

    // rMSSD = square root of the mean of the squares of differences between adjacent NN intervals
    private fun initRMSSD() {
        var sum = 0.0
        val size = rrInterval.size
        for (i in 1 until size) {
            val diff = rrInterval[i] - rrInterval[i - 1]
            sum += diff * diff
        }
        rMSSD = Math.sqrt(sum / (rrInterval.size - 1))
    }

    // pNN50 = % of differences between adjacent NN intervals that are greater than 50 ms
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

    fun getRrInterval(): ArrayList<Double> {
        val rrClone = ArrayList<Double>()
        for (d in rrInterval) {
            rrClone.add(d)
        }
        return rrClone
    }

    init {
        peaks = SignalProcessing.getPeaks(
            values,
            true
        ) as LinkedHashMap<Double, Double>
        initBPM()
        initRR()
        initAVNN()
        initSDNN()
        initRMSSD()
        initPPN50()
    }
}