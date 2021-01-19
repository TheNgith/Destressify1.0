package galaxy.app.stressdetector

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.TooltipCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import galaxy.app.stressdetector.ml.SwellModel
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*


class HRVFragment : Fragment() {
    private lateinit var heartData: HeartData
    private lateinit var avnnLayout: LinearLayout
    private lateinit var sdnnLayout: LinearLayout
    private lateinit var rmssdLayout: LinearLayout
    private lateinit var pnn50Layout: LinearLayout
    private lateinit var avnnView: TextView
    private lateinit var sdnnView: TextView
    private lateinit var rmssdView: TextView
    private lateinit var pnn50View: TextView
    private lateinit var graphView: GraphView
    private val TAG = "HRVModel"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_hrv,container, false)
        graphView = view.findViewById(R.id.graph2)
        avnnView = view.findViewById(R.id.avnnView)
        sdnnView = view.findViewById(R.id.sdnnView)
        rmssdView = view.findViewById(R.id.rmssdView)
        pnn50View = view.findViewById(R.id.pnn50View)
        avnnLayout = view.findViewById(R.id.avnnLayout)
        sdnnLayout = view.findViewById(R.id.sdnnLayout)
        rmssdLayout = view.findViewById(R.id.rmssdLayout)
        pnn50Layout = view.findViewById(R.id.pnn50Layout)
        setTooltip()
        graphView.getGridLabelRenderer().isHorizontalLabelsVisible = false
        graphView.getGridLabelRenderer().isVerticalLabelsVisible = false
        heartData = HeartRateFragment.getHeartData()!!
        drawGraph(heartData.getRrInterval())
        avnnView.text = (String.format("%.3f", heartData.aVNN))
        sdnnView.text = (String.format("%.3f", heartData.sDNN))
        rmssdView.text = (String.format("%.3f", heartData.rMSSD))
        pnn50View.text = (String.format("%.1f", heartData.pPN50) + "%")

        return view
    }

    private fun drawGraph(values: ArrayList<Double>) {
        val size = values.size
        val dataPoints = arrayOfNulls<DataPoint>(size)
        for (i in 0 until size) {
            dataPoints[i] = DataPoint(i.toDouble(), values[i])
        }
        val series =
            LineGraphSeries(dataPoints)
        graphView!!.addSeries(series)
    }

    private fun setTooltip() {
//        TooltipCompat.setTooltipText(avnnLayout, getContext().getString(R.string.avnn));
        avnnLayout!!.setOnLongClickListener { v ->
            Toast.makeText(v.context, R.string.avnn, Toast.LENGTH_SHORT).show()
            true
        }
        sdnnLayout!!.setOnLongClickListener { v ->
            Toast.makeText(v.context, R.string.sdnn, Toast.LENGTH_SHORT).show()
            true
        }
        rmssdLayout!!.setOnLongClickListener { v ->
            Toast.makeText(v.context, R.string.rmssd, Toast.LENGTH_SHORT).show()
            true
        }
        pnn50Layout!!.setOnLongClickListener { v ->
            Toast.makeText(v.context, R.string.pnn50, Toast.LENGTH_SHORT).show()
            true
        }
    }
}