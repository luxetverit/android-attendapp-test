package com.example.attend_test.zxing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.attend_test.R
import com.example.attend_test.activity.AdminActivity
import com.example.attend_test.databinding.ActivityCustomScannerBinding
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.util.*

class CustomScannerActivity : Activity() {
    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null

    private lateinit var binding: ActivityCustomScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomScannerBinding.inflate(layoutInflater)

        setLangData()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_custom_scanner)
        barcodeScannerView = findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView?
        capture = CaptureManager(this, barcodeScannerView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        binding.barcodeTitle.setText(R.string.barcode_title)

    }

    //언어 데이터 설정
    private fun setLangData() {
        val sharedPref: SharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        val lang: String = sharedPref.getString("app_lang", "ko").toString()
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    protected override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    //뒤로가기
    override fun onBackPressed() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}