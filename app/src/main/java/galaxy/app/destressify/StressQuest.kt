package galaxy.app.destressify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_stress_quest.*

class StressQuest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stress_quest)
        yesButton.setOnClickListener{ goToPAM_yes() }
        noButton.setOnClickListener{ goToPAM_no() }
    }

    private fun goToPAM_no() {
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s.get("aVNN") as String
        val sDNN = s.get("sDNN") as String
        val rMSSD = s.get("rMSSD") as String
        val predStress = s.get("predStress") as String
        val stress = "0"
        val intent = Intent(this, pam::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        intent.putExtra("stress", stress)
        startActivity(intent)
    }

    private fun goToPAM_yes() {
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s.get("aVNN") as String
        val sDNN = s.get("sDNN") as String
        val rMSSD = s.get("rMSSD") as String
        val predStress = s.get("predStress") as String
        val stress = "1"
        val intent = Intent(this, pam::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        intent.putExtra("stress", stress)
        startActivity(intent)
    }
}