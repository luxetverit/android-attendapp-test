package com.example.attend_test.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attend_test.R
import com.example.attend_test.databinding.ActivityAttendBinding
import com.lakue.pagingbutton.LakuePagingButton
import com.lakue.pagingbutton.OnPageSelectListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * 출입체크조회
 */
class AttendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendBinding

    var max_page = 1
    var count = 0
    var page = 1
    //var dbHandler: DBHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityAttendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        designUI()
    }

    //DESIGN UI
    private fun designUI() {
        /*attend_start_txt = findViewById<TextView>(R.id.attend_start_txt)
        var txtAttendStart = binding.attendStartTxt
        attend_end_txt = findViewById<TextView>(R.id.attend_end_txt)
        var txtAttendEnd = binding.attendEndTxt
        attend_search_input = findViewById<EditText>(R.id.attend_search_input)
        var txtAttendSearch = binding.attendSearchTxt
        attend_count = findViewById<TextView>(R.id.attned_count)
        var AttendCount = binding.attendCount
        attend_result = findViewById<RecyclerView>(R.id.attend_result)
        var AttendResult = binding.attendResult
        lpb_buttonlist = findViewById<LakuePagingButton>(R.id.lpb_buttonlist)
        var btnPageList = binding.lpbButtonlist*/
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        val date = Date()
        binding.attendStartTxt.text = dateFormat.format(date)
        binding.attendEndTxt.text = dateFormat.format(date)
        val cal = Calendar.getInstance()
        //출입 시작일 버튼 클릭 리스너
        binding.attendStartBox.setOnClickListener{
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    binding.attendStartTxt.text =
                        String.format("%d-%d-%d", year, month + 1, dayOfMonth)

                },
                cal[Calendar.YEAR],
                cal[Calendar.MONTH],
                cal[Calendar.DATE]
            ).show()
        }
        //출입 종료일 버튼 클릭 리스너
        binding.attendEndBox.setOnClickListener{
            DatePickerDialog(
                this,
                { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    binding.attendEndTxt.text =
                        String.format("%d-%d-%d", year, month + 1, dayOfMonth)
                },
                cal[Calendar.YEAR],
                cal[Calendar.MONTH],
                cal[Calendar.DATE]
            ).show()
        }
        //출입 조회 버튼 클릭 리스너
        binding.attendSearchBtn.setOnClickListener {
            page = 1
            //attendDataList
            pagingUI()
        }
        //뒤로가기 클릭 리스너
        binding.attendBack.setOnClickListener { v: View? -> backPressed() }
        //attendDataList
        pagingUI()
    }

    //출입 데이터 전체 가져오기
    /*val attendDataList: Unit
        get() {
            val attendAdapter = AttendAdapter()
            binding.attendResult.adapter = attendAdapter
            binding.attendResult.layoutManager = LinearLayoutManager(this)
            dbHandler = DBHandler.open(this)
            count = dbHandler.selectAttendCount(
                binding.attendSearchInput.text.toString(), //text 가 get 이었음, 다른건 set (gettext, settext)
                binding.attendStartTxt.text.toString(),
                binding.attendEndTxt.text.toString()
            )
            binding.attendCount.text = getString(R.string.attend_search1) + count + getString(R.string.attend_search2)
            val attendList: ArrayList<Attend> = dbHandler.selectAttendAll(
                binding.attendSearchInput.text.toString(),
                binding.attendStartTxt.text.toString(),
                binding.attendEndTxt.text.toString(),
                (page - 1) * 10
            )
            dbHandler.close()
            attendAdapter.setAttendList(attendList)
        }*/

    //PAGING UI
    private fun pagingUI() {
        //한 번에 표시되는 버튼 수 (기본값 : 5)
        binding.lpbButtonlist.setPageItemCount(5)
        max_page = Math.ceil(count.toDouble() / 10).toInt()
        //총 페이지 버튼 수와 현재 페이지 설정
        binding.lpbButtonlist.addBottomPageButton(max_page, page)
        //페이지 리스너를 클릭 했을 때의 이벤트
        binding.lpbButtonlist.setOnPageSelectListener(object : OnPageSelectListener {
            //PrevButton Click
            override fun onPageBefore(now_page: Int) {
                page = now_page
                binding.lpbButtonlist.addBottomPageButton(max_page, now_page)
                //attendDataList
            }

            override fun onPageCenter(now_page: Int) {
                page = now_page
                //attendDataList
            }

            //NextButton Click
            override fun onPageNext(now_page: Int) {
                page = now_page
                binding.lpbButtonlist.addBottomPageButton(max_page, now_page)
                //attendDataList
            }
        })
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