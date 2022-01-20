package com.example.attend_test.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attend_test.R
import com.example.attend_test.adapter.LangAdapter
import com.example.attend_test.databinding.ActivityConfigLangBinding
import java.util.ArrayList

class ConfigLangActivity : AppCompatActivity() {
    var lang_result: RecyclerView? = null
    private lateinit var binding: ActivityConfigLangBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigLangBinding.inflate(layoutInflater)

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
        lang_result = findViewById<RecyclerView>(R.id.lang_result)

        //언어 추가
        val langList = ArrayList<String>()
        langList.add("한국어")
        langList.add("중국어")
        getLangDataList(langList, getLangType(getConfigLangData(this, "app_lang")))

        //뒤로가기 클릭 리스너
        binding.langBack.setOnClickListener { backPressed() }
    }

    //언어 가져오기
    fun getLangDataList(langList: ArrayList<String>?, lang: String?) {
        val langAdapter = LangAdapter()
        lang_result!!.adapter = langAdapter
        lang_result!!.layoutManager = LinearLayoutManager(this)
        langAdapter.setLang(lang)
        langAdapter.setLangList(langList)
    }

    //SharedPreferences 언어 가져오기
    private fun getConfigLangData(context: Context, name: String?): String {
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        return sharedPref.getString(name, "ko").toString()
    }

    //언어 문자열 변환
    private fun getLangType(lang: String): String {
        return if (lang == "zh") {
            "중국어"
        } else {
            "한국어"
        }
    }

    //뒤로가기
    fun backPressed() {
        val intent = Intent(this, ConfigActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onBackPressed() {
        backPressed()
    }
}