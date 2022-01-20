package com.example.attend_test.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attend_test.R
import com.example.attend_test.activity.ConfigLangActivity
import java.util.*

class LangAdapter : RecyclerView.Adapter<LangAdapter.ViewHolder?>() {
    private var langList: ArrayList<String>? = null
    private var lang: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_lang_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.lang_icon.visibility = View.VISIBLE
            holder.lang.visibility = View.VISIBLE
        }
        holder.lang_txt.text = langList!![position]
        if (lang == langList!![position]) {
            holder.lang_btn.isChecked = true
        }
        holder.lang_btn.setOnClickListener{ v: View ->
            val sharedPref: SharedPreferences = v.context.getSharedPreferences(
                v.context.getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putString("app_lang", getLangType(langList!![position]))
            editor.apply()
            val locale = Locale(getLangType(langList!![position]))
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            v.context.resources.updateConfiguration(config, v.context.resources.displayMetrics)
            val intent = Intent(v.context, ConfigLangActivity::class.java)
            v.context.startActivity(intent)
            (v.context as Activity).overridePendingTransition(0, 0)
            (v.context as Activity).finish()
        }
    }

    override fun getItemCount(): Int {
        return langList!!.size
    }

    fun setLang(lang: String?) {
        this.lang = lang
    }

    fun setLangList(langList: ArrayList<String>?) {
        this.langList = langList
        notifyDataSetChanged()
    }

    private fun getLangType(lang: String): String {
        return if (lang == "중국어") {
            "zh"
        } else {
            "ko"
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var lang_icon: ImageView
        var lang: TextView
        var lang_txt: TextView
        var lang_btn: RadioButton

        init {
            lang_icon = itemView.findViewById(R.id.lang_icon)
            lang = itemView.findViewById<TextView>(R.id.lang)
            lang_txt = itemView.findViewById<TextView>(R.id.lang_txt)
            lang_btn = itemView.findViewById<RadioButton>(R.id.lang_btn)
        }
    }
}