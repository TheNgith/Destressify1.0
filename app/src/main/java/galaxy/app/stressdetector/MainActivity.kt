package galaxy.app.stressdetector

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        cameraButton.setOnClickListener{showInstruction()}
        historyButton.setOnClickListener{showHistory()}
        dbHandler = MyDBHandler(applicationContext)
    }

    private fun showInstruction(){
        val intent = Intent(applicationContext, Instruction::class.java)
        startActivityForResult(intent, 2)
    }

    private fun showHistory(){
        val intent = Intent(applicationContext, History::class.java)
        startActivityForResult(intent, 1)
    }

    private fun startDetection(){
        val intent = Intent(applicationContext, Camera::class.java)
        startActivityForResult(intent, 1)
    }

    private fun calculateHeartRate(){
        val intent = Intent(applicationContext, Results::class.java)
        startActivityForResult(intent, 3)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                calculateHeartRate()
            }
        }
        if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                startDetection()
            }
        }
    }

    companion object {
        val RECORDING_TIME = 15.0
        val CUTOFF_TIME = 5.0
        var dbHandler : MyDBHandler? = null
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