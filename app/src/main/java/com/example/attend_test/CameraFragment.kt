package com.example.attend_test

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.attend_test.network.AttendService
import com.example.attend_test.sqlite.DBHandler
import com.google.common.util.concurrent.ListenableFuture
import com.hoho.android.usbserial.driver.SerialTimeoutException
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import okhttp3.ResponseBody
import org.json.simple.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt


class CameraFragment : Fragment(), ServiceConnection, SerialListener {

    //기준 온도
    var refertemperature = 0.0

    //최대 범위 온도, 최소 범위 온도, 보정 온도
    var maxData = 0
    var minData = 0
    var addData = 0

    //유저 아이디
    var userid: String? = null

    //인터페이스 url
    var url: String? = null

    //NAT
    var nation: String? = null

    //바코드, QR 데이터
    var resultData: String? = null

    //바코드 타입
    var resultType: String? = null

    //결과 데이터 Json
    var resultJsonData: JSONObject? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    var frame_preview: FrameLayout? = null
    var frame_camera_line: FrameLayout? = null
    var frame_capture: FrameLayout? = null
    var header_box: ConstraintLayout? = null
    var capture_layout: ConstraintLayout? = null
    var camera_line: ConstraintLayout? = null
    var capture_img: ImageView? = null
    var header_box_icon: ImageView? = null
    var header_box_title: TextView? = null
    var header_box_result: TextView? = null
    var name_title: TextView? = null
    var name_result: TextView? = null
    var previewView: PreviewView? = null
    var tt_list = IntArray(3)
    var tt_chk = 0
    var dbHandler: DBHandler? = null

    private enum class Connected {
        False, Pending, True
    }

    private var broadcastReceiver: BroadcastReceiver
    private var deviceId = 0
    private var portNum = 0
    private var baudRate = 0
    private var usbSerialPort: UsbSerialPort? = null
    private var service: SerialService? = null
    private var connected = Connected.False
    private var initialStart = true
    private val newline: String = TextUtil.newline_crlf

    //온도 request
    var byteArray = byteArrayOf(0x01, 0x03, 0x00, 0x01, 0x00, 0x04, 0x15, 0xc9.toByte())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        //DEVICE
        deviceId = requireArguments().getInt("device")
        //PORT
        portNum = requireArguments().getInt("port")
        //BAUDRATE
        baudRate = requireArguments().getInt("baud")
        //바코드, QR 데이터
        resultData = requireArguments().getString("resultData")
        //바코드 타입
        resultType = requireArguments().getString("resultType")
        //기준 온도
        refertemperature = requireArguments().getDouble("refertemperature")
        //최대 범위 온도
        maxData = requireArguments().getInt("maxData")
        //최소 범위 온도
        minData = requireArguments().getInt("minData")
        //보정 온도
        addData = requireArguments().getInt("addData")
        //유저 아이디
        userid = requireArguments().getString("userid")
        //인터페이스 URL
        url = requireArguments().getString("url")
        //국가
        nation = requireArguments().getString("nation")
    }

    override fun onDestroy() {
        if (connected != Connected.False) disconnect()
        requireActivity().stopService(Intent(activity, SerialService::class.java))
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        if (service != null) service!!.attach(this) else requireActivity().startService(
            Intent(
                activity,
                SerialService::class.java
            )
        ) // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    override fun onStop() {
        disconnect()
        if (service != null && !requireActivity().isChangingConfigurations) service!!.detach()
        super.onStop()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        requireActivity().bindService(
            Intent(getActivity(), SerialService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onDetach() {
        try {
            requireActivity().unbindService(this)
        } catch (ignored: Exception) {
        }
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(
            broadcastReceiver,
            IntentFilter(Constants.INTENT_ACTION_GRANT_USB)
        )
        if (initialStart && service != null) {
            initialStart = false
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onPause() {
        requireActivity().unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as SerialService.SerialBinder).service
        service!!.attach(this)
        if (initialStart && isResumed) {
            initialStart = false
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.activity_camera, container, false)
        frame_preview = view.findViewById<FrameLayout>(R.id.frame_preview)
        frame_camera_line = view.findViewById<FrameLayout>(R.id.frame_camera_line)
        frame_capture = view.findViewById<FrameLayout>(R.id.frame_capture)
        header_box = view.findViewById(R.id.header_box)
        capture_layout = view.findViewById(R.id.capture_layout)
        camera_line = view.findViewById(R.id.camera_line)
        capture_img = view.findViewById(R.id.capture_img)
        header_box_icon = view.findViewById(R.id.header_box_icon)
        header_box_title = view.findViewById<TextView>(R.id.header_box_title)
        header_box_result = view.findViewById<TextView>(R.id.header_box_result)
        name_title = view.findViewById<TextView>(R.id.name_title)
        name_result = view.findViewById<TextView>(R.id.name_result)
        previewView = view.findViewById(R.id.previewView)
        cameraUI()
        timer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                activity!!.runOnUiThread { sendByte(byteArray) }
            }
        }
        timer!!.schedule(timerTask, 0, 1000)
        return view
    }

    //CAMERA UI
    private fun cameraUI() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture!!.addListener(Runnable {
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture!!.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    //CAMERA PREVIEW
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(previewView!!.surfaceProvider)
        val camera: Camera =
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
    }

    //비트맵 캡쳐
    private val bitmapRootView: Bitmap
        get() {
            val v = requireActivity().window.decorView
            val bitmap: Bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            v.draw(canvas)
            return bitmap
        }

    //비트맵 문자열 변환
    private fun bitmapToString(bitmap: Bitmap?): String {
        val metrics: DisplayMetrics = requireContext().resources.displayMetrics
        val w:Int = requireContext().resources.getDimension(R.dimen.camera_line_width).roundToInt() / 2

        val h:Int =
            requireContext().resources.getDimension(R.dimen.camera_line_height).roundToInt() / 2
        val mw: Int = metrics.widthPixels / 2
        val mh: Int = metrics.heightPixels / 2
        var x:Int = 0
        var y = 0
        if (w > mw) {
            x = w - mw
        } else {
            x = mw - w
        }

        if (h > mh) {
            y = h - mh
        } else {
            y = mh - h
        }
        Log.d("TAG", "w = "+w + ", h =" + h)
        Log.d("TAG", "mw = "+mw + ", mh =" + mh)
        Log.d("TAG", "x = "+x + ", y =" + y)
        Log.d("TAG", "x+width = "+ x+ camera_line!!.width)
        Log.d("TAG", "camera_line w = "+ camera_line!!.width)
        Log.d("TAG", "camera_line h = "+ camera_line!!.height)
        Log.d("TAG", "bitmap.width = "+ bitmap!!.width)
        Log.d("TAG", "bitmap.height = "+ bitmap.height)

        val result: Bitmap =
            //Bitmap.createBitmap(bitmap!!, x, y, camera_line!!.width, camera_line!!.height)
            Bitmap.createBitmap(bitmap, x, y, camera_line!!.width, camera_line!!.height)
        val baos = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun connect(permissionGranted: Boolean? = null) {
        var device: UsbDevice? = null
        val usbManager: UsbManager = requireActivity().getSystemService(Context.USB_SERVICE) as UsbManager
        for (v in usbManager.deviceList.values) if (v.deviceId == deviceId) device = v
        if (device == null) {
            return
        }
        var driver: UsbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            driver = CustomProber.customProber.probeDevice(device)
        }
        if (driver == null) {
            return
        }
        if (driver.ports.size < portNum) {
            return
        }
        usbSerialPort = driver.ports[portNum]
        val usbConnection: UsbDeviceConnection = usbManager.openDevice(driver.device)
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.device)) {
            val usbPermissionIntent: PendingIntent = PendingIntent.getBroadcast(
                activity,
                0,
                Intent(Constants.INTENT_ACTION_GRANT_USB),
                0
            )
            usbManager.requestPermission(driver.device, usbPermissionIntent)
            return
        }
        if (usbConnection == null) {
            return
        }
        connected = Connected.Pending
        try {
            usbSerialPort?.open(usbConnection)
            usbSerialPort?.setParameters(
                baudRate,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            val socket = SerialSocket(requireActivity().applicationContext, usbConnection, usbSerialPort)
            service!!.connect(socket)
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect()
        } catch (e: Exception) {
            onSerialConnectError(e)
        }
    }

    private fun disconnect() {

        connected = Connected.False
        service!!.disconnect()
        usbSerialPort = null
        timer!!.cancel()
    }

    private fun sendByte(data: ByteArray) {
        if (connected != Connected.True) {
            return
        }
        try {
            service!!.write(data)
        } catch (e: SerialTimeoutException) {
        } catch (e: Exception) {
            onSerialIoError(e)
        }
    }

    private fun receive(data: ByteArray, tt_chk: Int, addData: Int) {
        var msg: String? = ""
        var temp = 0
        var tempFloat = 0.0
        if (data.size == 13) {
            temp = data[5] * 256 + data[6]
            tempFloat = temp * 0.02 - 273.15
            msg = String.format("%.2f", tempFloat)
        }
        if (newline == TextUtil.newline_crlf && msg!!.isNotEmpty()) {
            // don't show CR as ^M if directly before LF
            msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf)
            if (msg != null && msg != "") {
                try {
                    tt_list[tt_chk] = tempFloat.toInt() + addData
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun onSerialConnect() {
        connected = Connected.True
    }

    override fun onSerialConnectError(e: Exception?) {
        disconnect()
    }

    override fun onSerialRead(data: ByteArray?) {
            var result = 1
            for (i in tt_list) {
                if (i < minData || i > maxData) {
                    result = 0
                }
            }
            Toast.makeText(
                context,
                "[ " + tt_list[0] + " , " + tt_list[1] + " , " + tt_list[2] + " ]",
                Toast.LENGTH_SHORT
            ).show()
            if (result == 1) {
                if (tt_list[tt_chk] > refertemperature) {
                    //출입 불가능
                    capture_layout!!.background = resources.getDrawable(R.drawable.camera_border_red, null)
                    header_box!!.background = resources.getDrawable(R.drawable.gradient_red, null)
                    header_box_icon!!.setImageDrawable(resources.getDrawable(R.drawable.false_icon, null))
                    header_box_title!!.setText(R.string.camera_fail)
                } else {
                    //출입 가능
                    capture_layout!!.background = resources.getDrawable(R.drawable.camera_border_green, null)
                    header_box!!.background = resources.getDrawable(R.drawable.gradient_green, null)
                    header_box_icon!!.setImageDrawable(resources.getDrawable(R.drawable.true_icon, null))
                    header_box_title!!.setText(R.string.camera_success)
                    //알림 소리
                    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone: Ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone.play()
                }
                header_box_result!!.text = getString(R.string.camera_data) + " " + tt_list[tt_chk]
                capture_img!!.setImageBitmap(previewView!!.bitmap)
                frame_capture!!.visibility = View.VISIBLE
                disconnect()
                var handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ //캡쳐 저장
                    val bitmap: Bitmap = bitmapRootView
                    val str = bitmapToString(bitmap)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                    val date = Date()
                    val dd = dateFormat.format(date)
                    //서버 전송 파라미터
                    resultJsonData = JSONObject()
                    resultJsonData!!["id"] = userid
                    resultJsonData!!["codetype"] = resultType
                    resultJsonData!!["codedata"] = resultData
                    resultJsonData!!["temperature"] = tt_list[tt_chk]
                    resultJsonData!!["image"] = str
                    resultJsonData!!["datetime"] = dd
                    //서버 전송
                    //saveData(resultJsonData!!);
                    //내부 DB 저장
                    saveDataDB(resultJsonData!!)
                    (activity as MainActivity?)!!.backResultActivity()
                }, 2000)
            } else {
                receive(data!!, tt_chk, addData)
                if (tt_chk == 2) {
                    tt_chk = 0
                } else {
                    tt_chk++
                }
            }
        }

        //서버 데이터 전송
        //savedata.asp
        private fun saveData(obj: JSONObject) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api: AttendService = retrofit.create(AttendService::class.java)
            api.getSaveData(obj.toString()).enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    //성공
                }
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    //실패
                }
            })
        }

        //내부 DB 저장
        private fun saveDataDB(obj: JSONObject) {
            dbHandler = DBHandler.open(activity)
            val values = ContentValues()
            values.put("ID", obj["id"].toString())
            values.put("NAT", nation)
            values.put("NAME", "ANNA ELSA")
            values.put("CODETYPE", obj["codetype"].toString())
            values.put("CODEDATA", obj["codedata"].toString())
            values.put("TEMPERATURE", obj["temperature"].toString())
            values.put("IMAGE", obj["image"].toString())
            values.put("DATETIME", obj["datetime"].toString())
            values.put("BIRTHDAY", "1981.01.01")
            dbHandler!!.insertAttend(values)
            dbHandler!!.close()
        }

        override fun onSerialIoError(e: Exception?) {
            disconnect()
        }

        companion object {
            private var timer: Timer? = null
        }

        init {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (Constants.INTENT_ACTION_GRANT_USB == intent.action) {
                        val granted: Boolean =
                            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        connect(granted)
                    }
                }
            }
        }
    }