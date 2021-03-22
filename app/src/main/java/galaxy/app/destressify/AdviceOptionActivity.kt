package galaxy.app.destressify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_advice_option.*

class AdviceOptionActivity : AppCompatActivity() {

    private val optionMap = mapOf(
        "time" to arrayOf("Nghía thử một vài cách để sắp xếp lại công việc", "Viết ra suy nghĩ của bản thân", "Xem ảnh mèo"),
        "unconfident" to arrayOf("Ngồi thiền một chút", "Học cách chấp nhận thất bại", "Viết ra suy nghĩ của bản thân", "Xem ảnh mèo"),
        "event" to arrayOf("Sắp xếp lại một thứ gì đó của mình, như bàn học, tủ quần áo hay cặp sách", "Tin tưởng vào khả năng của bản thân",
            "Viết ra suy nghĩ của bản thân", "Xem ảnh mèo"),
        "people" to arrayOf("Dành chút thời gian để hít thở và giữ bình tĩnh", "Viết ra suy nghĩ của bản thân", "Xem ảnh mèo")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice_option)
        var name: String = "note_stress_" + Advice.option
        val pack = packageName
        note.text = getString(resources.getIdentifier(name, "string", pack))
        val suggest = optionMap[Advice.option]?.random()
        choice.text = suggest
        button.setOnClickListener{ goToStress() }
    }

    private fun goToStress() {
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        val intent = Intent(applicationContext, StressQuest::class.java)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
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