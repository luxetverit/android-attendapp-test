package com.example.attend_test.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import com.example.attend_test.R
import com.example.attend_test.databinding.ActivityConfigBinding

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding : ActivityConfigBinding

    var sendsms = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigBinding.inflate(layoutInflater)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)
        designUI()
    }

    //DESIGN UI
    fun designUI() {
        //사용자 설정 클릭 리스너
        binding.configUserBox.setOnClickListener {
            val intent = Intent(this, ConfigUserActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        //언어 클릭 리스너
        binding.configLangBox.setOnClickListener {
            val intent = Intent(this, ConfigLangActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        //뒤로가기
        binding.configBack.setOnClickListener { backPressed() }
        binding.configLangTxt.text = getLangType(getConfigLangData(this, "app_lang"))
        //알림 토글 스위치 클릭 리스너
        binding.sub2Result.setOnCheckedChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                //알림 ON
                binding.sub2Result.trackDrawable = getDrawable(R.drawable.switch_track_on)
            } else {
                //알림 OFF
                binding.sub2Result.trackDrawable = getDrawable(R.drawable.switch_track_off)
            }
        }
    }

    //SharedPreferences 언어 데이터 가져오기
    private fun getConfigLangData(context: Context, name: String?): String? {
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.shared_preferences),
            MODE_PRIVATE
        )
        return sharedPref.getString(name, "ko")
    }

    //언어 문자열 변환
    private fun getLangType(lang: String?): String {
        return if (lang == "zh") {
            "중국어"
        } else {
            "한국어"
        }
    }

    //뒤로가기
    fun backPressed() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onBackPressed() {
        backPressed()
    }
}