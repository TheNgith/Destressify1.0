package galaxy.app.destressify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_title.*


class Title : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)
        val getStarted = findViewById<Button>(R.id.getStarted)
        val userName = findViewById<EditText>(R.id.userName)
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val user = sharedPreferences.getString("userName", "")
        userName.setText(user)
        getStarted.setOnClickListener{mainAc()}
        button7.setOnClickListener{ goToInstruction() }
    }

    private fun goToInstruction() {
        val allow = saveUserName()
        val intent = Intent(applicationContext, UsageInstruction::class.java)
        if (allow) {
            startActivity(intent)
        }
    }

    private fun saveUserName(): Boolean {
        val userName = findViewById<EditText>(R.id.userName)
        val text = userName.text.toString()
        var state = true
        if (text != "") {
            val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("userName", text)
            editor.apply()
        }
        else {
            userName.error = "Nhập tên bạn"
            state = false
        }
        return state
    }

    private fun mainAc(){
        val allow = saveUserName()
        val intent = Intent(applicationContext, MainActivity::class.java)
        if (allow) {
            startActivity(intent)
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