package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_title.*
import java.util.*


class Title : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)
        val getStarted = findViewById<Button>(R.id.getStarted)
        getStarted.setOnClickListener{mainAc()}
        button7.setOnClickListener{ goToInstruction() }
    }

    private fun goToInstruction() {
        val intent = Intent(applicationContext, UsageInstruction::class.java)
        startActivity(intent)
    }

    private fun mainAc(){
        val intent = Intent(applicationContext, MainActivity::class.java)
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