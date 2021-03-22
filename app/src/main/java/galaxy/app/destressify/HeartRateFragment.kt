package galaxy.app.destressify

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import galaxy.app.destressify.ml.SwellModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*

class HeartRateFragment : Fragment() {

    private lateinit var bpm: TextView
    private lateinit var graphView: GraphView
    private lateinit var dbHandler: MyDBHandler
    private val mean: FloatArray = floatArrayOf(14.977498F, 73.94182397F, 109.3525313F)
    private val std: FloatArray = floatArrayOf(4.12076058F, 10.3374393F, 77.1169201F)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.fragment_heart_rate, container, false)
        bpm = view.findViewById(R.id.bpm)
        graphView = view.findViewById(R.id.graph)
        graphView.getGridLabelRenderer().isHorizontalLabelsVisible = false
        graphView.getGridLabelRenderer().isVerticalLabelsVisible = false

        return view
    }

    val values: ArrayList<Double>
        get() {
            val colorValues: ArrayList<Int>? = CameraFragment.getColorValues()
            var hueValues = ArrayList<Double>()
            if (colorValues != null) {
                for (i in colorValues) {
                    val hsv = FloatArray(3)
                    Color.RGBToHSV(
                        Color.red(i),
                        Color.green(i),
                        Color.blue(i),
                        hsv
                    )
                    hueValues.add(hsv[0].toDouble())
                }
            }
            hueValues = SignalProcessing.signalProcess(hueValues)
            for (d in hueValues) {
                println(d)
            }
            return hueValues
        }

    private fun drawGraph(values: ArrayList<Double>) {
        val size = values.size
        val dataPoints =
            arrayOfNulls<DataPoint>(size)
        for (i in 0 until size) {
            dataPoints[i] = DataPoint(
                MainActivity.RECORDING_TIME * i / size,
                values[i]
            )
        }
        val series = LineGraphSeries(dataPoints)
        graphView!!.addSeries(series)
    }

    companion object {
        private var heartData: HeartDataNew? = null
        fun getHeartData(): HeartDataNew? {
            return heartData
        }
    }
}