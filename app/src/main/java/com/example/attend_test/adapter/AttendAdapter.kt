package com.example.attend_test.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.attend_test.R
import com.example.attend_test.activity.AttendInfoActivity
import com.example.attend_test.sqlite.model.Attend
import java.util.ArrayList

class AttendAdapter : RecyclerView.Adapter<AttendAdapter.ViewHolder?>() {
    private var attendList: ArrayList<Attend>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_attend_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendAdapter.ViewHolder, position: Int) {
        if (position % 2 == 0) {
            holder.attend_item.setBackgroundColor(Color.parseColor("#EBEBEB"))
        } else {
            holder.attend_item.setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
        holder.attend_id.text = attendList!![position].IDX
        holder.attend_name.text = attendList!![position].NAME
        holder.attend_nat.text = attendList!![position].NAT
        holder.attend_date.text = attendList!![position].DATETIME
        holder.attend_data.text = attendList!![position].TEMPERATURE
        if (position >= 1) {
            if (attendList!![position].CODEDATA != null) {
                holder.itemView.setOnClickListener(View.OnClickListener { v: View ->
                    val intent = Intent(v.context, AttendInfoActivity::class.java)
                    intent.putExtra("idx", attendList!![position].IDX)
                    v.context.startActivity(intent)
                    (v.context as Activity).overridePendingTransition(0, 0)
                })
            }
        } else {
            holder.attend_id.setTextColor(Color.parseColor("#B1B1B1"))
            holder.attend_name.setTextColor(Color.parseColor("#B1B1B1"))
            holder.attend_nat.setTextColor(Color.parseColor("#B1B1B1"))
            holder.attend_date.setTextColor(Color.parseColor("#B1B1B1"))
            holder.attend_data.setTextColor(Color.parseColor("#B1B1B1"))
        }
    }


    override fun getItemCount(): Int {
        return attendList!!.size
    }

    fun setAttendList(attendList: ArrayList<Attend>) {
        attendList.add(
            0,
            Attend(null, null, "Nat", "Eng Name", null, null, "체온", null, "체크시간", null)
        )
        if (attendList.size < 10) {
            for (i in attendList.size..10) {
                attendList.add(
                    i,
                    Attend(null, null, null, null, null, null, null, null, null, null)
                )
            }
        }
        this.attendList = attendList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var attend_item: ConstraintLayout
        var attend_id: TextView
        var attend_name: TextView
        var attend_nat: TextView
        var attend_date: TextView
        var attend_data: TextView

        init {
            attend_item = itemView.findViewById(R.id.attend_item)
            attend_id = itemView.findViewById<TextView>(R.id.attend_id)
            attend_name = itemView.findViewById<TextView>(R.id.attend_name)
            attend_nat = itemView.findViewById<TextView>(R.id.attend_nat)
            attend_date = itemView.findViewById<TextView>(R.id.attend_date)
            attend_data = itemView.findViewById<TextView>(R.id.attend_data)
        }
    }

}