package com.example.attend_test.network.model

import com.google.gson.annotations.SerializedName
import org.json.simple.JSONArray

data class ResultSaveData (
    @SerializedName("resultcode")
    var RESULTCODE: String?,
    @SerializedName("resultdesc")
    var RESULTDESC: String?,
    @SerializedName("resultdata")
    var RESULTDATA: JSONArray?) {
    fun getReturnRESULTCODE(): String? {
        return RESULTCODE
    }
    fun getReturnRESULTDESC(): String? {
        return RESULTDESC
    }
    fun getReturnRESULTDATA(): JSONArray? {
        return RESULTDATA
    }
}
