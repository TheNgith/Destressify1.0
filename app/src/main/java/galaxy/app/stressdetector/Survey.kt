package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_survey.*
import java.text.SimpleDateFormat
import java.util.*

class Survey : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)
        next_quest_button.setOnClickListener{ sendEverydayData() }
    }

    @SuppressLint("HardwareIds", "SimpleDateFormat")
    private fun sendEverydayData() {
        val day = SimpleDateFormat("dd/M/yyyy").format(Date()).replace("/","-")
        val sleep = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val walk = findViewById<EditText>(R.id.editTextTextPersonName5).text.toString()
        val rootNode = FirebaseDatabase.getInstance()
        val reference = rootNode.getReference("Data")
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val helperClass = UserHelperClassEveryday(sleep, walk)
        reference.child(androidID).child(day).child("prevDayStats").setValue(helperClass)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToPAM() {
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s?.get("aVNN") as String
        val sDNN = s?.get("sDNN") as String
        val rMSSD = s?.get("rMSSD") as String
        val predStress = s?.get("predStress") as String
        val messageSleep = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val intent = Intent(this, pam::class.java)
        intent.putExtra("sleep", messageSleep)
        intent.putExtra("bPM", bPM)
        intent.putExtra("aVNN", aVNN)
        intent.putExtra("sDNN", sDNN)
        intent.putExtra("rMSSD", rMSSD)
        intent.putExtra("predStress", predStress)
        startActivity(intent)
        }
    }