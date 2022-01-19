package com.example.attend_test.zxing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.attend_test.MainActivity
import com.google.zxing.integration.android.IntentIntegrator

class ResultActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        val integrator = IntentIntegrator(this)
        integrator.captureActivity = CustomScannerActivity::class.java
        integrator.setCameraId(1)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        var intent = intent
        super.onActivityResult(requestCode, resultCode, intent)
        //바코드 스캔 성공
        if (resultCode == RESULT_OK) {
            val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
            intent = Intent(this, MainActivity::class.java)
            //바코드 데이터
            intent.putExtra("resultData", scanResult.contents)
            //바코드 타입
            intent.putExtra("resultType", scanResult.formatName)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}