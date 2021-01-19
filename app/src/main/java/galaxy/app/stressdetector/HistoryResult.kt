package galaxy.app.stressdetector

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_history_result.*
import kotlinx.android.synthetic.main.activity_results.*
import kotlinx.android.synthetic.main.activity_results.avnnView
import kotlinx.android.synthetic.main.activity_results.bpmView
import kotlinx.android.synthetic.main.activity_results.emotion
import kotlinx.android.synthetic.main.activity_results.rmssdView
import kotlinx.android.synthetic.main.activity_results.sdnnView
import kotlinx.android.synthetic.main.activity_results.stressView


class HistoryResult : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_history_result)

        val receivedIntent = intent
        val dateTime = receivedIntent.getStringExtra("DateTime")
        val bpm = receivedIntent.getDoubleExtra("BPM", 0.0)
        val avnn = receivedIntent.getDoubleExtra("AVNN", 0.0)
        val sdnn = receivedIntent.getDoubleExtra("SDNN", 0.0)
        val rmssd = receivedIntent.getDoubleExtra("RMSSD", 0.0)
        val ppn50 = receivedIntent.getDoubleExtra("PPN50", 0.0)
        val stress = receivedIntent.getIntExtra("Stress", 0)

        dateTimeView.text = dateTime
        bpmView.text = String.format("%.1f", bpm)
        avnnView.text = String.format("%.3f", avnn)
        sdnnView.text = String.format("%.3f", sdnn)
        rmssdView.text = String.format("%.3f", rmssd)
        if (stress == 0) {
            stressView.text = "Không Stress"
            emotion.setImageResource(R.drawable.ic_in_love)
        }
        else {
            stressView.text = "Stress"
            emotion.setImageResource(R.drawable.ic_worried)
        }
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

}