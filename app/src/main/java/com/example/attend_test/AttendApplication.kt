package com.example.attend_test

import android.app.Application

class AttendApplication : Application() {
    //기준 온도
    var refertemperature = 0.0

    //최대 범위 온도
    val maxData = 40

    //최소 범위 온도
    val minData = 30

    //보정 온도
    val addData = 0

    //인터페이스 URL
    var url: String? = null

    //유저 아이디
    var userid: String? = null

    //로그인 파라미터
    val nation = "KOR"
    val docname = "dtcs"
    val docver = "1.0"
    val macaddr = "AB-CD-EF"

    //로그인 리턴값
    var username: String? = null
    var sendsms: String? = null
    var userdefinepath: String? = null

    //로그인 성공 여부
    var isLoginResult = false
}