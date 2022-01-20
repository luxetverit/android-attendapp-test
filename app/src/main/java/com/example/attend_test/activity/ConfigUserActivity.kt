package com.example.attend_test.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.attend_test.AttendApplication
import com.example.attend_test.R
import com.example.attend_test.databinding.ActivityConfigUserBinding
import com.example.attend_test.network.AttendService
import com.example.attend_test.network.model.ResultSaveData
import org.json.JSONException
import org.json.JSONObject
import org.json.simple.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.Base64

class ConfigUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigUserBinding.inflate(layoutInflater)

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
        binding.userIdResult.text = getConfigData(this, "id")
        binding.userPwResult.text = getConfigData(this, "pw")
        binding.userCoResult.text = (applicationContext as AttendApplication).username
        //뒤로가기 클릭 리스너
        binding.userBack.setOnClickListener{ backPressed() }
        //아이디 클릭 리스너
        binding.userIdResult.setOnClickListener{
            setSubDialog(
                binding.userIdResult,
                R.string.config_sub_id
            )
        }
        //비밀번호 클릭 리스너
        binding.userPwResult.setOnClickListener{
            setSubDialogPassword(
                binding.userPwResult,
                R.string.user_pw
            )
        }
        //로그인 클릭 리스너
        binding.userLoginModify.setOnClickListener{
            //Toast.makeText(this, " login click ", Toast.LENGTH_SHORT).show()
            getLogin(
                binding.userIdResult.text.toString(),
                binding.userPwResult.text.toString()
            )
        }
    }

    //일반 다이얼로그 호출
    private fun showDialog(t: String) {
        val dialog2 = Dialog(this)
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog2.setContentView(R.layout.custom_dialog)
        dialog2.show()
        (dialog2.findViewById<View>(R.id.dialog_title) as TextView).text = t
        dialog2.findViewById<View>(R.id.dialog_close)
            .setOnClickListener(View.OnClickListener { v: View? -> dialog2.dismiss() })
    }

    //입력 다이얼로그 호출
    private fun setSubDialog(t: TextView?, r: Int) {
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        builder.setTitle(r)
            .setView(editText)
            .setPositiveButton(
                "OK"
                ) { _, _ -> t!!.text = editText.text }
            .setNegativeButton("CANCEL", null)
            .create().show()
    }

    //비밀번호 입력 다이얼로그 호출
    private fun setSubDialogPassword(t: TextView?, r: Int) {
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setTitle(r)
            .setView(editText)
            .setPositiveButton(
                "OK"
                ) { _, _ -> t!!.text = editText.text }
            .setNegativeButton("CANCEL", null)
            .create().show()
    }

    //델리넷 인터페이스 API 호출
    //login.asp

    private fun getLogin(id: String?, pw: String?) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl((applicationContext as AttendApplication).url.toString())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api: AttendService = retrofit.create(AttendService::class.java)

        //로그인 파라미터

        val bytePassword = pw?.toByteArray(charset("UTF-8"))
        val encodedPassword = Base64.getEncoder().encodeToString(bytePassword)

        val obj = org.json.simple.JSONObject()
        obj["nation"] = (applicationContext as AttendApplication).nation
        obj["docname"] = (applicationContext as AttendApplication).docname
        obj["docver"] = (applicationContext as AttendApplication).docver
        obj["macaddr"] = (applicationContext as AttendApplication).macaddr
        obj["id"] = id
        obj["pwd"] = encodedPassword
        Toast.makeText(this, " login ", Toast.LENGTH_SHORT).show()
        api.getLogin(obj.toString()).enqueue(object : Callback<ResultSaveData?> {

            override fun onResponse(
                call: Call<ResultSaveData?>,
                response: Response<ResultSaveData?>
            ) {
                if (response.isSuccessful) {
                    val result: ResultSaveData? = response.body()

                    //성공
                    if (result!!.getReturnRESULTCODE().equals("1a") && result.getReturnRESULTDESC()
                            .equals("ok")
                    ) {
                        val arr: JSONArray = result.getReturnRESULTDATA()!!
                        var obj: JSONObject? = null
                        try {
                            //로그인 리턴값
                            obj = JSONObject(arr[0].toString())
                            binding.userCoResult.text = obj["username"] as String
                            (applicationContext as AttendApplication).username = (obj["username"] as String)
                            (applicationContext as AttendApplication).userid = (id)
                            (applicationContext as AttendApplication).sendsms = (obj["sendsms"] as String)
                            (applicationContext as AttendApplication).refertemperature = (obj["refertemperature"] as Double)
                            (applicationContext as AttendApplication).userdefinepath = (obj["userdefinepath"] as String)
                            (applicationContext as AttendApplication).isLoginResult = true
                            saveUserAutoData(applicationContext, id, pw)
                            showDialog(getString(R.string.login_success))
                        } catch (e: JSONException) {
                        }
                    } else {
                        Toast.makeText(
                            this@ConfigUserActivity,
                            R.string.login_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResultSaveData?>, t: Throwable) {
                //실패
                Toast.makeText(this@ConfigUserActivity, R.string.login_fail, Toast.LENGTH_SHORT)
                    .show()
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

    //SharedPreferences id, pw 저장 (자동 로그인)
    fun saveUserAutoData(context: Context, id: String?, pw: String?) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString("id", id)
        editor.putString("pw", pw)
        editor.apply()

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