package com.example.attend_test.sqlite

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.fragment.app.FragmentActivity
import com.example.attend_test.sqlite.model.Attend
import java.util.ArrayList

class DBHandler private constructor(context: FragmentActivity?) {
    private val db: SQLiteDatabase
    private val helper: DBHelper? = DBHelper.getInstance(context)
    fun close() {
        openCount -= 1
        if (openCount == 0) {
            helper!!.close()
        }
    }

    //출입 데이터 저장
    fun insertAttend(values: ContentValues?) {
        db.insert("ATTEND", null, values)
    }

    //출입 데이터 전체 가져오기
    fun selectAttendAll(
        keyword: String?,
        start_date: String?,
        end_date: String?,
        page: Int
    ): ArrayList<Attend> {
        val list: ArrayList<Attend> = ArrayList<Attend>()
        var sql =
            "SELECT IDX, ID, NAT, NAME, CODETYPE, CODEDATA, TEMPERATURE, IMAGE, TIME(DATETIME), BIRTHDAY FROM ATTEND "
        if (keyword != null && keyword != "") {
            sql += " WHERE NAME LIKE '%$keyword%'"
            if (start_date != null && start_date != "" && end_date != null && end_date != "") {
                sql += " AND DATETIME BETWEEN '$start_date 00:00:00' AND '$end_date 23:59:59'"
            }
        } else if (start_date != null && start_date != "" && end_date != null && end_date != "") {
            sql += " WHERE DATETIME BETWEEN '$start_date 00:00:00' AND '$end_date 23:59:59'"
        }
        sql += " ORDER BY IDX DESC "
        sql += " LIMIT 10 OFFSET $page"
        val cursor: Cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Attend(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    //출입 데이터 카운트
    fun selectAttendCount(keyword: String?, start_date: String?, end_date: String?): Int {
        var count = 0
        var sql = "SELECT COUNT(*) FROM ATTEND "
        if (keyword != null && keyword != "") {
            sql += " WHERE NAME LIKE '%$keyword%'"
            if (start_date != null && start_date != "" && end_date != null && end_date != "") {
                sql += " AND DATETIME BETWEEN '$start_date 00:00:00' AND '$end_date 23:59:59'"
            }
        } else if (start_date != null && start_date != "" && end_date != null && end_date != "") {
            sql += " WHERE DATETIME BETWEEN '$start_date 00:00:00' AND '$end_date 23:59:59'"
        }
        val cursor: Cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    //출입 데이터 하나 가져오기
    fun selectAttendOne(idx: String): Attend? {
        var info: Attend? = null
        val sql = "SELECT * FROM ATTEND WHERE IDX = $idx"
        val cursor: Cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            info = Attend(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9)
            )
        }
        cursor.close()
        return info
    }

    companion object {
        var openCount = 0
        @Throws(SQLException::class)
        fun open(context: FragmentActivity?): DBHandler {
            openCount += 1
            return DBHandler(context)
        }
    }

    init {
        db = helper!!.writableDatabase
    }
}