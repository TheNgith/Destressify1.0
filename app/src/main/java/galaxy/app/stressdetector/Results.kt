package galaxy.app.stressdetector

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import galaxy.app.stressdetector.ml.SwellModel
import kotlinx.android.synthetic.main.activity_results.*
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.text.SimpleDateFormat
import java.util.*

class Results : AppCompatActivity() {
    private lateinit var mSectionsPageAdapter: SectionsPageAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var dbHandler: MyDBHandler
    private val mean: FloatArray = floatArrayOf(14.977498F, 73.94182397F, 109.3525313F)
    private val std: FloatArray = floatArrayOf(4.12076058F, 10.3374393F, 77.1169201F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_results)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy 'at' hh:mm:ss aaa",
                Locale.getDefault())
        val date = Date()
        dateTime = dateFormat.format(date)

        val values = values
        bpmView.text = String.format("%.1f", heartData?.bPM)
        avnnView.text = String.format("%.3f", heartData?.aVNN)
        sdnnView.text = String.format("%.3f", heartData?.sDNN)
        rmssdView.text = String.format("%.3f", heartData?.rMSSD)

        val model = this.let { SwellModel.newInstance(it) }
        var rawInput =
                heartData?.rMSSD?.toFloat()?.let { heartData?.bPM?.toFloat()?.let { it1 ->
                    heartData?.sDNN?.toFloat()?.let { it2 ->
                        floatArrayOf(it,
                                it1, it2
                        )
                    }
                } }
        for (i in 0..2){
            rawInput?.set(i, (rawInput[i] - mean[i])/std[i])
        }
        var inputFeature = TensorBuffer.createFixedSize(intArrayOf(1,1,3), DataType.FLOAT32)
        var maxIdx = 0
        if (rawInput != null) {
            inputFeature.loadArray(rawInput)
            val output = model?.process(inputFeature)
            val outputFeature0 = output?.outputFeature0AsTensorBuffer
            model?.close()
            if (outputFeature0 != null) {
                var outputArray = outputFeature0.floatArray
                maxIdx = outputArray.indices.maxBy { outputArray[it] } ?: -1
                heartData?.stress = maxIdx
            }
        }

        if (maxIdx == 0) {
            stressView.text = "Không"
            emotion.setImageResource(R.drawable.ic_in_love)
            prompting.text = getString(R.string.no_stress)
        }
        else {
            stressView.text = "Có"
            emotion.setImageResource(R.drawable.ic_worried)
            prompting.text = getString(R.string.yes_stress)
        }

        dbHandler = MyDBHandler(this)
        val hData = heartData
        val record = dateTime?.let { dbHandler.existHandler(it) }
        if (record == false){
            if (hData != null) {
                dbHandler.addHandler(hData)
            }
        }
        advice_show.setOnClickListener { startAdvice() }
    }

    private fun startAdvice(){
        val intent = Intent(applicationContext, Advice::class.java)
        startActivity(intent)
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
            heartData = HeartData(hueValues)
            return hueValues
        }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    companion object{
        var dateTime: String? = null
        var heartData: HeartData? = null
    }
}