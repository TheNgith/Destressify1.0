package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Switch
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
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.collections.ArrayList


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
        if (listStressDay.size > -1) {
            val returnString = consecutive(listStressDay)
            if (returnString != "NAN") {
                val consecutive = returnString.substring(1..returnString.length).toBoolean()
                if (consecutive) {
                    contactParent()
                }
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
        if (listStressDay.size > 0) {
            for (i in 0..k!!) {
                listStressDay.removeAt(0)
            }
            val earliestDay = listStressDay[0]
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
        }
        else {
            returnString = "NAN"
        }
        return returnString

        //for loop to find the earliest day in array
//        for (i in 1 until listStressDay.size) {
//            val item = listStressDay[i]
//            val date = LocalDate.parse(item, DateTimeFormatter.ofPattern("dd-M-yyyy"))
//            if (date.isBefore(LocalDate.parse(earliestDay, DateTimeFormatter.ofPattern("dd-M-yyyy")))) {
//                earliestDay = date.toString()
//            }
//        }
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
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val load_name1 = sharedPreferences.getString("NAME1", "")
        val load_name2 = sharedPreferences.getString("NAME2", "")
        val load_name3 = sharedPreferences.getString("NAME3", "")
        val load_name4 = sharedPreferences.getString("NAME4", "")
        val load_name5 = sharedPreferences.getString("NAME5", "")

        val load_rela1 = sharedPreferences.getString("RELA1", "")
        val load_rela2 = sharedPreferences.getString("RELA2", "")
        val load_rela3 = sharedPreferences.getString("RELA3", "")
        val load_rela4 = sharedPreferences.getString("RELA4", "")
        val load_rela5 = sharedPreferences.getString("RELA5", "")

        val load_info1 = sharedPreferences.getString("INFO1", "")
        val load_info2 = sharedPreferences.getString("INFO2", "")
        val load_info3 = sharedPreferences.getString("INFO3", "")
        val load_info4 = sharedPreferences.getString("INFO4", "")
        val load_info5 = sharedPreferences.getString("INFO5", "")

        val fields = Bundle()
        fields.putString("name1", load_name1.toString())
        fields.putString("rela1", load_rela1.toString())
        fields.putString("info1", load_info1.toString())
        fields.putString("name2", load_name2.toString())
        fields.putString("rela2", load_rela2.toString())
        fields.putString("info2", load_info2.toString())
        fields.putString("name3", load_name3.toString())
        fields.putString("rela3", load_rela3.toString())
        fields.putString("info3", load_info3.toString())
        fields.putString("name4", load_name4.toString())
        fields.putString("rela4", load_rela4.toString())
        fields.putString("info4", load_info4.toString())
        fields.putString("name5", load_name5.toString())
        fields.putString("rela5", load_rela5.toString())
        fields.putString("info5", load_info5.toString())

        buildInfoList(fields)
    }

    private fun buildInfoList(fields: Bundle) {
        val infoList = Bundle()
        var person = ""
        var keyName = ""
        var keyRela = ""
        var keyInfo = ""
        val info = ArrayList<String>(3)
        for (i in 1..5) {
            person = "person$i"
            keyName = "name$i"
            keyInfo = "info$i"
            keyRela = "rela$i"
            info.add(fields[keyName].toString())
            info.add(fields[keyRela].toString())
            info.add(fields[keyInfo].toString())
            infoList.putStringArrayList(person, info)
        }
        buildMessage(infoList)
    }

    private fun buildMessage(infoList: Bundle) {
        var person = ""
        var message = ""
        var userName = ""
        var email = ""
        var name = ""
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        for (i in 1..5) {
            person = "person$i"
            val person_info: ArrayList<String> = infoList[person] as ArrayList<String>
            name = person_info[0]
            email = person_info[2]
            userName = sharedPreferences.getString("userName", "").toString()
            message = "Chào " + name + ",\nChúng mình đến từ Destressify, một phần mềm theo dõi và cảnh báo stress. " +
                    "Gần đây bạn đã được thêm vào danh sách người thân của " + userName + " - một trong những người dùng của chúng tôi.\nGần" +
                    "đây, phần mềm Destressify đã nhận thấy " + userName + " có dấu hiệu căng thẳng kéo dài (trong hơn 30 ngày). Có vẻ như " + userName + " cần" +
                    "sự giúp đỡ từ những người xung quanh để vượt qua những căng thẳng nghiêm trọng này\nVà vì bạn là một trong những người được " + userName + " tin" +
                    "tưởng và đưa vào danh sách người thân nên chúng tôi gửi email này đến bạn để thông báo về tình trạng nghiêm trọng của " + userName + ", " +
                    "hi vọng rằng bạn có thể giúp" + userName + " vượt qua.\nCảm ơn bạn đã dành thời gian đọc email. Chúc một ngày tốt lành!\n\nTrân " +
                    "trọng,\nDestressify\nMọi thắc mắc xin vui lòng phản hồi email này"
        }
        send(messageText = message, userName = userName, email = email)
    }

    private fun send(messageText: String, email: String, userName: String) {
        //Initialize Properties
        val senderID = "service.destressify@gmail.com"
        val senderPassword = "destressify@2021"
        val properties = Properties()
        properties.put("mail.smtp.auth", "true")
        properties.put("mail.smtp.starttls.enable", "true")
        properties.put("mail.smtp.host", "smtp.gmail.com")
        properties.put("mail.smtp.port", "587")

        //Initialize session
        val session = Session.getInstance(properties, object: Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderID, senderPassword)
            }
        })
        try {
            //Initialize email content
            val message = MimeMessage(session)

            //Sender email
            message.setFrom(InternetAddress(senderID))

            //Recipient email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.trim()))

            //Email subject
            message.setSubject("Thông báo từ Destressify về $userName")

            //Email message
            message.setText(messageText)

            //send email
            SendEmail().execute(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
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
//                        Toast.makeText(this, listStressDay.toString(), Toast.LENGTH_SHORT)
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
//                Toast.makeText(applicationContext, "gotTimestatus", Toast.LENGTH_SHORT)
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
//                Toast.makeText(applicationContext, "gotDayList", Toast.LENGTH_SHORT)
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
//                Toast.makeText(applicationContext, "gotTimeList", Toast.LENGTH_SHORT)

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

    private class SendEmail: AsyncTask<Message, String, String>() {
        override fun doInBackground(vararg message: Message?): String? {
            try {
                Transport.send(message[0])
                return "Success"
            }
            catch (e: MessagingException) {
                e.printStackTrace()
                return "Error"
            }
        }
    }
