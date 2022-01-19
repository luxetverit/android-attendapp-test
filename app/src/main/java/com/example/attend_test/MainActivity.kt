package com.example.attend_test

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.attend_test.zxing.ResultActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    private val baudRate = 19200
    private var args: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState == null) {
            refresh()
        } else {
            onBackStackChanged()
        }
    }

    private fun refresh() {
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber: UsbSerialProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber: UsbSerialProber = CustomProber.customProber
        for (device in usbManager.deviceList.values) {
            var driver: UsbSerialDriver = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                //열화상 장비 있음
                for (port in driver.ports.indices) {
                    args = Bundle()
                    //DEVICE
                    args!!.putInt("device", device.deviceId)
                    //PORT
                    args!!.putInt("port", port)
                    //BAUDRATE
                    args!!.putInt("baud", baudRate)
                    //바코드, QR 데이터
                    args!!.putString("resultData", intent.getStringExtra("resultData"))
                    //바코드 타입
                    args!!.putString("resultType", intent.getStringExtra("resultType"))
                    //기준 온도
                    args!!.putDouble(
                        "refertemperature",
                        (applicationContext as AttendApplication).refertemperature
                    )
                    //최대 범위 온도
                    args!!.putInt(
                        "maxData",
                        (applicationContext as AttendApplication).maxData
                    )
                    //최소 범위 온도
                    args!!.putInt(
                        "minData",
                        (applicationContext as AttendApplication).minData
                    )
                    //보정 온도
                    args!!.putInt(
                        "addData",
                        (applicationContext as AttendApplication).addData
                    )
                    //유저 아이디
                    args!!.putString(
                        "userid",
                        (applicationContext as AttendApplication).userid
                    )
                    //인터페이스 URL
                    args!!.putString("url", (applicationContext as AttendApplication).url)
                    //국가
                    args!!.putString(
                        "nation",
                        (applicationContext as AttendApplication).nation
                    )
                    val cameraFragment: Fragment = CameraFragment()
                    cameraFragment.arguments = args
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, cameraFragment, "camera").commit()
                }
            } else {
                //열화상 장비 없음
                Toast.makeText(this, R.string.not_device, Toast.LENGTH_LONG).show()
                backResultActivity()
            }
        }
        if (usbManager.deviceList.size == 0) {
            //열화상 장비 없음
            Toast.makeText(this, R.string.not_device, Toast.LENGTH_LONG).show()
            backResultActivity()
        }
    }

    //뒤로가기
    fun backResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNewIntent(intent: Intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == intent.action) {
            val terminal: CameraFragment =
                supportFragmentManager.findFragmentByTag("camera") as CameraFragment
            supportFragmentManager.popBackStack()
        }
        super.onNewIntent(intent)
    }

    override fun onBackPressed() {
        backResultActivity()
    }
}