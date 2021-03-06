package galaxy.app.stressdetector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.SQLException
import kotlin.collections.ArrayList


class MyDBHandler(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_CREATED_AT + " TEXT PRIMARY KEY ," +
                    COLUMN_BPM + " DOUBLE ," +
                    COLUMN_AVNN + " DOUBLE ," +
                    COLUMN_SDNN + " DOUBLE ," +
                    COLUMN_RMSSD + " DOUBLE ," +
                    COLUMN_PPN50 + " DOUBLE " +
                    COLUMN_STRESS + " DOUBLE " + ");"
        Log.v("TableCreated", createTable)
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add a column
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_STRESS")
            } catch (e: SQLException) {
                Log.i("ADD COLUMN STRESS", "Stress already exists")
            }
        }

    }

    fun addHandler(hd: HeartData) {
        val values = ContentValues()
        values.put(COLUMN_CREATED_AT, Results.dateTime)
        values.put(COLUMN_BPM, hd.bPM)
        values.put(COLUMN_AVNN, hd.aVNN)
        values.put(COLUMN_SDNN, hd.sDNN)
        values.put(COLUMN_RMSSD, hd.rMSSD)
        values.put(COLUMN_PPN50, hd.pPN50)
        values.put(COLUMN_STRESS, hd.stress)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun existHandler(dateTime: String): Boolean{
        val query =
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED_AT = '$dateTime'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0){
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun findHandler(dateTime: String): ArrayList<Any?>? {
        val query =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED_AT = '$dateTime'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var curResult: ArrayList<Any?>? = ArrayList()
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val bpm = cursor.getDouble(1)
            val avnn = cursor.getDouble(2)
            val sdnn = cursor.getDouble(3)
            val rmssd = cursor.getDouble(4)
            val ppn50 = cursor.getDouble(5)
            val stress = cursor.getInt(6)
            curResult!!.add(dateTime)
            curResult.add(bpm)
            curResult.add(avnn)
            curResult.add(sdnn)
            curResult.add(rmssd)
            curResult.add(ppn50)
            curResult.add(stress)
            cursor.close()
        } else {
            curResult = null
        }
        db.close()
        return curResult
    }

    fun deleteHandler(dateTime: String): Boolean {
        var result = false
        val query =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED_AT = '$dateTime'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
//            student.setStudentID(Integer.parseInt(cursor.getString(0)));
            db.delete(
                TABLE_NAME,
                "$COLUMN_CREATED_AT=?",
                arrayOf(dateTime)
            )
            cursor.close()
            result = true
        }
        db.close()
        return result
    }

    val allDate: ArrayList<String>
        get() {
            val result = ArrayList<String>()
            val query = "SELECT * FROM $TABLE_NAME WHERE 1"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                if (cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_AT)) != null) {
                    val dateTime = cursor.getString(0)
                    result.add(dateTime)
                }
                cursor.moveToNext()
            }
            cursor.close()
            db.close()
            return result
        }



    companion object {
        private const val DATABASE_VERSION = 9
        private const val DATABASE_NAME = "studentDB.db"
        const val TABLE_NAME = "HeartRate"
        const val COLUMN_CREATED_AT = "DateTime"
        const val COLUMN_BPM = "HeartRate"
        const val COLUMN_AVNN = "HRV_AVNN"
        const val COLUMN_SDNN = "HRV_SDNN"
        const val COLUMN_RMSSD = "HRV_RMSSD"
        const val COLUMN_PPN50 = "HRV_PPN50"
        const val COLUMN_STRESS = "HRV_STRESS"
    }
}