package com.example.attend_test.network

import com.example.attend_test.network.model.ResultSaveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API 인터페이스
 */
interface AttendService {
    //인터페이스 호출
    @POST("first.asp")
    fun getFirst(): Call<ResponseBody>

    //인터페이스 + 로그인 API
    @POST("login.asp")
    fun getLogin(@Query("a") a: String?): Call<ResultSaveData?>

    //인터페이스 + 서버 데이터 저장 API
    @POST("savedata.asp")
    fun getSaveData(@Query("data") data: String?): Call<ResponseBody?>
}