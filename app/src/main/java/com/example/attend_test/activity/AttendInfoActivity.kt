package com.example.attend_test.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.attend_test.R
import com.example.attend_test.databinding.ActivityAttendInfoBinding
import com.example.attend_test.sqlite.DBHandler
import com.example.attend_test.sqlite.model.Attend

class AttendInfoActivity : AppCompatActivity() {
    var name_result: TextView? = null
    var nat_result: TextView? = null
    var birth_result: TextView? = null
    var result_date: TextView? = null
    var result_img: ImageView? = null
    var dbHandler: DBHandler? = null

    private lateinit var binding: ActivityAttendInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAttendInfoBinding.inflate(layoutInflater)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)
        designUI()
    }

    //DESIGN UI
    fun designUI() {
        name_result = findViewById<TextView>(R.id.name_result)
        nat_result = findViewById<TextView>(R.id.nat_result)
        birth_result = findViewById<TextView>(R.id.birth_result)
        result_date = findViewById<TextView>(R.id.result_date)
        result_img = findViewById<ImageView>(R.id.result_img)
        //뒤로가기 클릭 리스너
        binding.infoBack.setOnClickListener{ backPressed() }
        attendDataOne
    }

    //출입 데이터 하나 가져오기
    private val attendDataOne: Unit
        get() {
            dbHandler = DBHandler.open(this)
            val info: Attend = dbHandler!!.selectAttendOne(intent.getStringExtra("idx")!!)!!
            binding.nameResult.text = info.NAME
            binding.natResult.text = info.NAT
            binding.birthResult.text = info.BIRTHDAY
            //binding.resultDate.text = "Time : " + info.DATETIME
            binding.resultDate.text = info.DATETIME
            binding.resultImg.setImageBitmap(stringToBitmap(info.IMAGE))
            dbHandler!!.close()
        }

    //문자열 비트맵 변환
    private fun stringToBitmap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte =
                Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            null
        }
    }

    //뒤로가기
    fun backPressed() {
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        backPressed()
    }
}