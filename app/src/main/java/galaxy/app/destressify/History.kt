package galaxy.app.destressify

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import java.util.*

class History : AppCompatActivity() {
    private lateinit var mListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbHandler: MyDBHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_history)
        mListView = findViewById(R.id.listView) as ListView
        registerForContextMenu(mListView)
        dbHandler = MyDBHandler(this)
        val dates: ArrayList<String> = dbHandler.allDate
        dates.let { populateListView(it) }
    }

    private fun populateListView(dates: ArrayList<String>) {
        Collections.reverse(dates)
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dates)
        mListView!!.adapter = adapter
        mListView!!.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l ->
                val date = adapterView.getItemAtPosition(i).toString()
                viewItem(date)
            }
    }

    private fun viewItem(date: String) {
        val data: ArrayList<Any?>? =
            dbHandler.findHandler(date) //get the id associated with that name
        val historyResult = Intent(this@History, HistoryResult::class.java)
        historyResult.putExtra("DateTime", data?.get(0) as String)
        historyResult.putExtra("BPM", data[1] as Double)
        historyResult.putExtra("AVNN", data[2] as Double)
        historyResult.putExtra("SDNN", data[3] as Double)
        historyResult.putExtra("RMSSD", data[4] as Double)
        historyResult.putExtra("PPN50", data[5] as Double)
        historyResult.putExtra("STRESS", data[6] as Int)
        startActivity(historyResult)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenuInfo?
    ) {
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.list_menu, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        val date = mListView!!.getItemAtPosition(info.position).toString()
        when (item.itemId) {
            R.id.view -> viewItem(date)
            R.id.delete -> {
                dbHandler.deleteHandler(date)
                adapter!!.remove(adapter!!.getItem(info.position))
                adapter!!.notifyDataSetChanged()
            }
        }
        return true
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