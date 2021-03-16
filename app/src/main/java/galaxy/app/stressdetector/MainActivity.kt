package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
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
        val listStressDay = checkSwitchandGetNumberOfStressDay()
        if (listStressDay.size > 30) {
            val returnString = consecutive(listStressDay)
            val consecutive = returnString.substring(1..returnString.length).toBoolean()
            if (consecutive) {
                contactParent()
            }
        }
    }
    var SHARED_PREFS = "sharedPrefs"

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun consecutive(listStressDay: ArrayList<String>): String {
        //Did not try SharedPreferences
        var returnString = ""
        val listStressDay = sortDayList(listStressDay)
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val k = sharedPreferences.getString("returnString", "0false")?.get(0)?.toInt()
        for (i in 0..k!!) {
            listStressDay.removeAt(0)
        }
        val earliestDay = listStressDay[0]

        //for loop to find the earliest day in array
//        for (i in 1 until listStressDay.size) {
//            val item = listStressDay[i]
//            val date = LocalDate.parse(item, DateTimeFormatter.ofPattern("dd-M-yyyy"))
//            if (date.isBefore(LocalDate.parse(earliestDay, DateTimeFormatter.ofPattern("dd-M-yyyy")))) {
//                earliestDay = date.toString()
//            }
//        }

        val alreadyCheckedDay = ArrayList<String>()
        val period = Period.of(0,0,1)
        var still = true
        var currentDay = LocalDate.parse(earliestDay, DateTimeFormatter.ofPattern("dd-M-yyyy"))
        var nextDay = currentDay.plus(period)
        var numOfConsecutiveDay = 0
        while (still) {
            if (listStressDay.contains(nextDay.toString())) {
                alreadyCheckedDay.add(currentDay.toString())
                currentDay = nextDay
                nextDay = currentDay.plus(period)
                numOfConsecutiveDay += 1
            }
            else {
                still = false
                var k = listStressDay.indexOf(currentDay.toString())
                val q = k
                var m = -1
                while (k != -1) {
                    m += 1
                    for (i in 0..k) {
                        listStressDay.removeAt(0)
                    k = listStressDay.indexOf(currentDay.toString())
                    }
                }
                returnString += (m+q).toString()
            }
        }
        if (numOfConsecutiveDay < 30) {
            still = false
        }
        returnString += still.toString()
        editor.putString("returnString", returnString)
        editor.apply()
        return returnString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortDayList(listStressDay: ArrayList<String>): ArrayList<String> {
        var temp = ""
        val n = listStressDay.size
        for (i in 0 until n-1) {
            for (j in i until n-1) {
                if (LocalDate.parse(listStressDay[i], DateTimeFormatter.ofPattern("dd-M-yyyy")).isBefore(LocalDate.parse(listStressDay[j], DateTimeFormatter.ofPattern("dd-M-yyyy")))) {
                    temp = listStressDay[i]
                    listStressDay[i] = listStressDay[j]
                    listStressDay[j] = temp
                }
            }
        }
        return listStressDay
    }

    private fun contactParent() {
        TODO()
    }



    private fun checkSwitchandGetNumberOfStressDay(): ArrayList<String> {
        val listStressDay = ArrayList<String>()
        if (switch1.isChecked) {
            val dayList: ArrayList<String> = getDayList()
            for (day in dayList) {
                var dayStatus = false
                val timeList = getTimeList(day)
                for (time in timeList) {
                    if (!dayStatus) {
                        val timeStatus = checkTime(day, time)
                        if (timeStatus == "Có") {
                            dayStatus = true
                            listStressDay.add(day)
                        }
                    }
                    else {
                        Toast.makeText(this, listStressDay.toString(), Toast.LENGTH_SHORT)
                        break
                    }
                }
            }
        }
        return listStressDay
    }

    private fun checkTime(time: String, day: String): String {
        @SuppressLint("HardwareIds")
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID).toString()
        var status = ""
        val reference = FirebaseDatabase.getInstance().getReference("Data").child(androidID).child(day).child("Measurement").child(time)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                status = snapshot.child("predStress").value as String
                Toast.makeText(applicationContext, "gotTimestatus", Toast.LENGTH_SHORT)
            }
        }
        )
        return status
    }

    private fun getDayList(): ArrayList<String> {
        @SuppressLint("HardwareIds")
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID).toString()
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
                Toast.makeText(applicationContext, "gotDayList", Toast.LENGTH_SHORT)
            }

        })
        return dayList
    }

    private fun getTimeList(day: String): ArrayList<String> {
        @SuppressLint("HardwareIds")
        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID).toString()
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
                Toast.makeText(applicationContext, "gotTimeList", Toast.LENGTH_SHORT)
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
        val RECORDING_TIME = 12.0
        val CUTOFF_TIME = 4.0
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