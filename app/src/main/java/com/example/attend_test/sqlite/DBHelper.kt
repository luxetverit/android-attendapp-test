package com.example.attend_test.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.fragment.app.FragmentActivity

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        //출입 테이블 스키마
        val sql = ("CREATE TABLE IF NOT EXISTS ATTEND ("
                + "IDX INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ID TEXT,"
                + "NAT TEXT,"
                + "NAME TEXT,"
                + "CODETYPE TEXT,"
                + "CODEDATA TEXT,"
                + "TEMPERATURE TEXT,"
                + "IMAGE TEXT,"
                + "DATETIME TEXT,"
                + "BIRTHDAY TEXT);")
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "attend.db"
        private var mInstance: DBHelper? = null
        fun getInstance(context: FragmentActivity?): DBHelper? {
            if (mInstance == null) {
                mInstance = DBHelper(context!!.applicationContext)
            }
            return mInstance
        }
    }
}