package com.example.attend_test.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.attend_test.R
import com.example.attend_test.databinding.ActivityAdminBinding
import com.example.attend_test.AttendApplication
import com.example.attend_test.network.AttendService
import com.example.attend_test.network.model.ResultSaveData
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

/**
 * 메인화면 (첫 화면)
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminBinding
    //test
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        //langSetting
        interfaceUrl
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
        //출입체크 시작 버튼 클릭 리스너
        binding.adminBtnStart.setOnClickListener{
            //로그인 체크
            if ((applicationContext as AttendApplication).isLoginResult) {
                //로그인 O
                val intent = Intent(this, ResultActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            } else {
                //로그인 X
                Toast.makeText(this, R.string.login_null, Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "start button click", Toast.LENGTH_SHORT).show()
        }
        //출입체크 조회 버튼 클릭 리스너
        binding.adminBtnSearch.setOnClickListener{
            //로그인 체크
            if ((applicationContext as AttendApplication).isLoginResult) {
                //로그인 O
                val intent = Intent(this, AttendActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            } else {
                //로그인 X
                Toast.makeText(this, R.string.login_null, Toast.LENGTH_SHORT).show()
            }
            //Toast.makeText(this, "search button click", Toast.LENGTH_SHORT).show()
        }
        //설정 버튼 클릭 리스너
        findViewById<View>(R.id.admin_config).setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        })
    }

    //언어 선택
    /*private val langSetting: Unit
        private get() {
            val sharedPref: SharedPreferences =
                getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
            val lang: String = sharedPref.getString("app_lang", "ko").toString()
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
        }//실패//자동 로그인//성공*/

    //델리넷 인터페이스 API 호출 + 자동로그인
    //first.asp
    private val interfaceUrl: Unit
        get() {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://api.delynet.com/DTCS/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api: AttendService = retrofit.create(AttendService::class.java)
            api.getFirst().enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>) {
                    //성공
                    if (response.isSuccessful) {
                        val body: ResponseBody = response.body()!!
                        try {
                            (applicationContext as AttendApplication).url = body.string()
                            //자동 로그인
                            val autoId = getConfigData(this@AdminActivity, "id")
                            val autoPw = getConfigData(this@AdminActivity, "pw")
                            if (!(applicationContext as AttendApplication).isLoginResult && autoId != "" && autoPw != "") {
                                autoLogin(autoId, autoPw)
                            }
                        } catch (e: IOException) {
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                    //실패
                    Toast.makeText(this@AdminActivity, R.string.server_fail, Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }

    //델리넷 인터페이스 API 호출
    //login.asp
    fun autoLogin(id: String?, pw: String?) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl((applicationContext as AttendApplication).url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api: AttendService = retrofit.create(AttendService::class.java)

        //로그인 파라미터
        val obj = JSONObject()
        obj.put("nation", (applicationContext as AttendApplication).nation)
        obj.put("docname", (applicationContext as AttendApplication).docname)
        obj.put("docver", (applicationContext as AttendApplication).docver)
        obj.put("macaddr", (applicationContext as AttendApplication).macaddr)
        obj.put("id", id)
        obj.put("pwd", pw)
        api.getLogin(obj.toString()).enqueue(object : Callback<ResultSaveData?> {
            override fun onResponse(call: Call<ResultSaveData?>?, response: Response<ResultSaveData?>) {
                if (response.isSuccessful) {
                    val result: ResultSaveData = response.body()!!

                    //성공
                    if (result.getReturnRESULTCODE().equals("1a") && result.getReturnRESULTDESC()
                            .equals("ok")
                    ) {
                        //=========================================================
                        val arr = result.getReturnRESULTDATA()
                        var obj: JSONObject? = null
                        //=========================================================
                        try {
                            //로그인 리턴값
                            obj = JSONObject(arr?.get(0).toString())
                            (applicationContext as AttendApplication).username = obj.get("username") as String
                            (applicationContext as AttendApplication).userid = id
                            (applicationContext as AttendApplication).sendsms = obj.get("sendsms") as String
                            (applicationContext as AttendApplication).refertemperature = obj.get(
                                "refertemperature"
                            ) as Double
                            (applicationContext as AttendApplication).userdefinepath = obj.get(
                                "userdefinepath"
                            ) as String
                            (applicationContext as AttendApplication).isLoginResult = true
                        } catch (e: JSONException) {
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResultSaveData?>?, t: Throwable?) {
                //실패
                Toast.makeText(this@AdminActivity, R.string.server_fail, Toast.LENGTH_SHORT).show()
            }
        })
    }

    //SharedPreferences 데이터 가져오기
    fun getConfigData(context: Context, name: String?): String {
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        return sharedPref.getString(name, "").toString()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.app_name).toString() + " " + getString(R.string.exit_message))
            .setPositiveButton("OK"){_,_ -> finish()}
            .setNegativeButton("CANCEL", null)
            .create().show()
    }
}