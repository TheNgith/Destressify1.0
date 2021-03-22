package galaxy.app.destressify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_pam.*
import java.text.SimpleDateFormat
import java.util.*


class pam : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pam)
        imageButton1.setOnClickListener{ sendData(1) }
        imageButton2.setOnClickListener{ sendData(2) }
        imageButton3.setOnClickListener{ sendData(3) }
        imageButton4.setOnClickListener{ sendData(4) }
        imageButton5.setOnClickListener{ sendData(5) }
        imageButton6.setOnClickListener{ sendData(6) }
        imageButton7.setOnClickListener{ sendData(7) }
        imageButton8.setOnClickListener{ sendData(8) }
        imageButton9.setOnClickListener{ sendData(9) }
        imageButton10.setOnClickListener{ sendData(10) }
        imageButton11.setOnClickListener{ sendData(11) }
        imageButton12.setOnClickListener{ sendData(12) }
        imageButton13.setOnClickListener{ sendData(13) }
        imageButton14.setOnClickListener{ sendData(14) }
        imageButton15.setOnClickListener{ sendData(15) }
        imageButton16.setOnClickListener{ sendData(16) }
    }

    @SuppressLint("HardwareIds", "SimpleDateFormat")
    private fun sendData(pam: Any?) {
        val s = intent.extras
        val bPM = s?.get("bPM") as String
        val aVNN = s.get("aVNN") as String
        val sDNN = s.get("sDNN") as String
        val rMSSD = s.get("rMSSD") as String
        val predStress = s.get("predStress") as String
        val stress = s.get("stress") as String
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val day = SimpleDateFormat("dd/M/yyyy").format(Date()).replace("/","-")
        val daytime = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()).replace("/","-")
        //send data to Firebase
        val rootNode = FirebaseDatabase.getInstance()
        val reference = rootNode.getReference("Data")

        val helperClass = UserHelperClass(bPM, aVNN, sDNN, rMSSD, predStress, stress, pam.toString())
        reference.child(androidID).child(day).child("Measurement").child(daytime).setValue(helperClass)
        val intent = Intent(this, ThankYouActivity::class.java)
        startActivity(intent)
    }

}