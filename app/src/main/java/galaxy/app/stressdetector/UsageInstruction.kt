package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_usage_instruction.*

class UsageInstruction : AppCompatActivity() {
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage_instruction)
        button3.setOnClickListener{ goToHome() }
        family_list_button.setOnClickListener{ goToFamilyList() }
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val userID = findViewById<EditText>(R.id.editTextTextPersonName3)
        button4.setOnClickListener{ goToSurvey() }
        userID.setText(androidID)
    }

    private fun goToFamilyList() {
        val intent = Intent(this, familyList::class.java)
        startActivity(intent)
    }

    private fun goToSurvey() {
        val uri: Uri = Uri.parse("https://forms.gle/GsXoYU6BHWdkWGbG6") // missing 'http://' will cause crashed
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}