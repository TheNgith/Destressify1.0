package galaxy.app.stressdetector

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_family_list.*

class familyList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_list)
        button5.setOnClickListener{ updateContact() }
        loadData()
    }

    var SHARED_PREFS = "sharedPrefs"

    private fun loadData() {
        val viewName1 = findViewById<EditText>(R.id.contact_1_name)
        val viewName2 = findViewById<EditText>(R.id.contact_2_name)
        val viewName3 = findViewById<EditText>(R.id.contact_3_name)
        val viewName4 = findViewById<EditText>(R.id.contact_4_name)
        val viewName5 = findViewById<EditText>(R.id.contact_5_name)

        val viewRela1 = findViewById<EditText>(R.id.contact_1_rela)
        val viewRela2 = findViewById<EditText>(R.id.contact_2_rela)
        val viewRela3 = findViewById<EditText>(R.id.contact_3_rela)
        val viewRela4 = findViewById<EditText>(R.id.contact_4_rela)
        val viewRela5 = findViewById<EditText>(R.id.contact_5_rela)

        val viewInfo1 = findViewById<EditText>(R.id.contact_1_info)
        val viewInfo2 = findViewById<EditText>(R.id.contact_2_info)
        val viewInfo3 = findViewById<EditText>(R.id.contact_3_info)
        val viewInfo4 = findViewById<EditText>(R.id.contact_4_info)
        val viewInfo5 = findViewById<EditText>(R.id.contact_5_info)

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
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

        viewName1.setText(load_name1)
        viewName2.setText(load_name2)
        viewName3.setText(load_name3)
        viewName4.setText(load_name4)
        viewName5.setText(load_name5)
        viewRela1.setText(load_rela1)
        viewRela2.setText(load_rela2)
        viewRela3.setText(load_rela3)
        viewRela4.setText(load_rela4)
        viewRela5.setText(load_rela5)
        viewInfo1.setText(load_info1)
        viewInfo2.setText(load_info2)
        viewInfo3.setText(load_info3)
        viewInfo4.setText(load_info4)
        viewInfo5.setText(load_info5)
    }

    @SuppressLint("HardwareIds")
    private fun updateContact() {
        val viewName1 = findViewById<EditText>(R.id.contact_1_name)
        val viewName2 = findViewById<EditText>(R.id.contact_2_name)
        val viewName3 = findViewById<EditText>(R.id.contact_3_name)
        val viewName4 = findViewById<EditText>(R.id.contact_4_name)
        val viewName5 = findViewById<EditText>(R.id.contact_5_name)

        val viewRela1 = findViewById<EditText>(R.id.contact_1_rela)
        val viewRela2 = findViewById<EditText>(R.id.contact_2_rela)
        val viewRela3 = findViewById<EditText>(R.id.contact_3_rela)
        val viewRela4 = findViewById<EditText>(R.id.contact_4_rela)
        val viewRela5 = findViewById<EditText>(R.id.contact_5_rela)

        val viewInfo1 = findViewById<EditText>(R.id.contact_1_info)
        val viewInfo2 = findViewById<EditText>(R.id.contact_2_info)
        val viewInfo3 = findViewById<EditText>(R.id.contact_3_info)
        val viewInfo4 = findViewById<EditText>(R.id.contact_4_info)
        val viewInfo5 = findViewById<EditText>(R.id.contact_5_info)

        val name1 = viewName1.text.toString()
        val name2 = viewName2.text.toString()
        val name3 = viewName3.text.toString()
        val name4 = viewName4.text.toString()
        val name5 = viewName5.text.toString()
        val rela1 = viewRela1.text.toString()
        val rela2 = viewRela2.text.toString()
        val rela3 = viewRela3.text.toString()
        val rela4 = viewRela4.text.toString()
        val rela5 = viewRela5.text.toString()
        val info1 = viewInfo1.text.toString()
        val info2 = viewInfo2.text.toString()
        val info3 = viewInfo3.text.toString()
        val info4 = viewInfo4.text.toString()
        val info5 = viewInfo5.text.toString()

        val androidID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val rootNode = FirebaseDatabase.getInstance()
        val reference = rootNode.getReference("Data")

        val helperClass1 = UserHelperClassContact(name1, rela1, info1)
        val helperClass2 = UserHelperClassContact(name2, rela2, info2)
        val helperClass3 = UserHelperClassContact(name3, rela3, info3)
        val helperClass4 = UserHelperClassContact(name4, rela4, info4)
        val helperClass5 = UserHelperClassContact(name5, rela5, info5)

        reference.child(androidID).child("Contact").child("No-1").setValue(helperClass1)
        reference.child(androidID).child("Contact").child("No-2").setValue(helperClass2)
        reference.child(androidID).child("Contact").child("No-3").setValue(helperClass3)
        reference.child(androidID).child("Contact").child("No-4").setValue(helperClass4)
        reference.child(androidID).child("Contact").child("No-5").setValue(helperClass5)

        saveData()
        goBack()

        }

    private fun goBack() {
        val intent = Intent(applicationContext, UsageInstruction::class.java)
        startActivity(intent)
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val viewName1 = findViewById<EditText>(R.id.contact_1_name)
        val viewName2 = findViewById<EditText>(R.id.contact_2_name)
        val viewName3 = findViewById<EditText>(R.id.contact_3_name)
        val viewName4 = findViewById<EditText>(R.id.contact_4_name)
        val viewName5 = findViewById<EditText>(R.id.contact_5_name)

        val viewRela1 = findViewById<EditText>(R.id.contact_1_rela)
        val viewRela2 = findViewById<EditText>(R.id.contact_2_rela)
        val viewRela3 = findViewById<EditText>(R.id.contact_3_rela)
        val viewRela4 = findViewById<EditText>(R.id.contact_4_rela)
        val viewRela5 = findViewById<EditText>(R.id.contact_5_rela)

        val viewInfo1 = findViewById<EditText>(R.id.contact_1_info)
        val viewInfo2 = findViewById<EditText>(R.id.contact_2_info)
        val viewInfo3 = findViewById<EditText>(R.id.contact_3_info)
        val viewInfo4 = findViewById<EditText>(R.id.contact_4_info)
        val viewInfo5 = findViewById<EditText>(R.id.contact_5_info)

        val name1 = viewName1.text.toString()
        val name2 = viewName2.text.toString()
        val name3 = viewName3.text.toString()
        val name4 = viewName4.text.toString()
        val name5 = viewName5.text.toString()
        val rela1 = viewRela1.text.toString()
        val rela2 = viewRela2.text.toString()
        val rela3 = viewRela3.text.toString()
        val rela4 = viewRela4.text.toString()
        val rela5 = viewRela5.text.toString()
        val info1 = viewInfo1.text.toString()
        val info2 = viewInfo2.text.toString()
        val info3 = viewInfo3.text.toString()
        val info4 = viewInfo4.text.toString()
        val info5 = viewInfo5.text.toString()


        editor.putString("NAME1", name1)
        editor.putString("NAME2", name2)
        editor.putString("NAME3", name3)
        editor.putString("NAME4", name4)
        editor.putString("NAME5", name5)

        editor.putString("RELA1", rela1)
        editor.putString("RELA2", rela2)
        editor.putString("RELA3", rela3)
        editor.putString("RELA4", rela4)
        editor.putString("RELA5", rela5)

        editor.putString("INFO1", info1)
        editor.putString("INFO2", info2)
        editor.putString("INFO3", info3)
        editor.putString("INFO4", info4)
        editor.putString("INFO5", info5)

        editor.apply()
    }
}