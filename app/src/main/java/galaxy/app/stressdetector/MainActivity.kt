package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        cameraButton.setOnClickListener{showInstruction()}
        everydayButton.setOnClickListener{goToEveryday()}
        historyButton.setOnClickListener{showHistory()}
        dbHandler = MyDBHandler(applicationContext)
        button8.setOnClickListener{ goToInstruction() }
        loadSwitch()
        val numberOfStressDay = checkSwitchandGetNumberOfStressDay()
        if (numberOfStressDay > 30) {
            contactParent()
        }
    }

    private fun contactParent() {
        TODO()
    }

    @SuppressLint("HardwareIds")
    val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)


    private fun checkSwitchandGetNumberOfStressDay(): Int {
        var numberOfStressDay = 0
        if (switch1.isChecked) {
            val dayList: ArrayList<String> = getDayList()
            for (day in dayList) {
                var dayStatus = false
                val timeList = getTimeList(day)
                for (time in timeList) {
                    if (dayStatus == false) {
                        val timeStatus = checkTime(day, time)
                        if (timeStatus == "Có") {
                            dayStatus = true
                            numberOfStressDay += 1
                        }
                    }
                    else {
                        break
                    }
                }
            }
        if (!switch1.isChecked) {
                numberOfStressDay = -1
            }
        }
        return numberOfStressDay
    }

    private fun checkTime(time: String, day: String): String {
        var status = ""
        val reference = FirebaseDatabase.getInstance().getReference("Data").child(androidID).child(day).child("Measurement").child(time)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                status = snapshot.child("predStress").value as String
            }
        }
        )
        return status
    }

    private fun getDayList(): ArrayList<String> {
        val dayList = ArrayList<String>()
        val reference = FirebaseDatabase.getInstance().getReference("Data").child(androidID)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                dayList.clear()
                for (snap in snapshot.children) {
                    dayList.add(snap.value.toString())
                }
            }

        })
        return dayList
    }

    private fun getTimeList(day: String): ArrayList<String> {
        val timeList = ArrayList<String>()
        val reference = FirebaseDatabase.getInstance().getReference("Data").child(androidID).child(day).child("Measurement")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                timeList.clear()
                for (snap in snapshot.children) {
                    timeList.add(snap.value.toString())
                }
            }
        })
        return timeList
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun loadSwitch() {
        val switch1 = findViewById<Switch>(R.id.switch1)
        val sharedPreferences = getSharedPreferences("switchState", MODE_PRIVATE)
        switch1.isChecked = sharedPreferences.getBoolean("switch", false)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun saveSwitch() {
        val switch1 = findViewById<Switch>(R.id.switch1)
        val sharedPreferences = getSharedPreferences("switchState", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("switch", switch1.isChecked)
        editor.apply()
    }

    private fun goToEveryday() {
        saveSwitch()
        val intent = Intent(applicationContext, Survey::class.java)
        startActivity(intent)
    }

    private fun goToInstruction() {
        saveSwitch()
        val intent = Intent(applicationContext, UsageInstruction::class.java)
        startActivity(intent)
    }

    private fun showInstruction(){
        saveSwitch()
        val intent = Intent(applicationContext, Instruction::class.java)
        startActivityForResult(intent, 2)
    }

    private fun showHistory(){
        saveSwitch()
        val intent = Intent(applicationContext, History::class.java)
        startActivityForResult(intent, 1)
    }

    private fun startDetection(){
        saveSwitch()
        val intent = Intent(applicationContext, Camera::class.java)
        startActivityForResult(intent, 1)
    }

    private fun calculateHeartRate(){
        saveSwitch()
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
        val RECORDING_TIME = 32.0
        val CUTOFF_TIME = 14.0
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