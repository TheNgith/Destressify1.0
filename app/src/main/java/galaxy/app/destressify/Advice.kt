package galaxy.app.destressify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_advice.*

class Advice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice)
        option_1.setOnClickListener { option1() }
        option_2.setOnClickListener { option2() }
        option_3.setOnClickListener { option3() }
        option_4.setOnClickListener { option4() }
    }
    private fun option1(){
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        option = "time"
        val intent = Intent(applicationContext, AdviceResultActivity::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
    }

    private fun option2(){
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        option = "unconfident"
        val intent = Intent(applicationContext, AdviceResultActivity::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
    }

    private fun option3(){
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        option = "event"
        val intent = Intent(applicationContext, AdviceResultActivity::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
    }

    private fun option4(){
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        option = "people"
        val intent = Intent(applicationContext, AdviceResultActivity::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
    }

    companion object{
        var option: String = ""
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