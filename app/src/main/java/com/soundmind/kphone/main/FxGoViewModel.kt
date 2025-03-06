package com.soundmind.kphone.main

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.soundmind.kphone.util.Language
import com.soundmind.kphone.util.LanguageFlag
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http .GET
import retrofit2.http.Path
import retrofit2.converter.moshi.MoshiConverterFactory
//import retrofit2.converter.scalars.ScalarsConverterFactory


data class ExchangeRateResponse (
    val currency: String,
    val currency_name: String,
    val ttb: Float,
    val tts: Float,
    val deal_bas_r: Float,
    val bkpr: Float,
    val kftc_bkpr: Float,
    val kftc_deal_bas_r: Float,
    val updated_at: String,
)

interface RetrofitExchangeRateApi {
    @GET("rates/{currency}")
    fun getExchangeRates(@Path("currency") currency: String): Call<ExchangeRateResponse>
}

object ExchangeRateApi {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl("http://grandphone.cafe24.com:3170/")
        .build()

    val retrofitService: RetrofitExchangeRateApi by lazy {
        retrofit.create(RetrofitExchangeRateApi::class.java)
    }

    suspend fun getExchangeRates(currency: String): ExchangeRateResponse? {
        val response = retrofitService.getExchangeRates(currency)
        return response.execute().body()
    }
}

class FxGoViewModel(application: Application) : AndroidViewModel(application) {
    val exchangeRate = MutableLiveData<Float>()
    //var exchangeRate: Float = -1f
    val currencyCode = MutableLiveData<String>()
    val updated_at = MutableLiveData<String>()

    var calculatedSource = MutableLiveData<Float>()
    var calculatedDestination = MutableLiveData<Float>()

    var toKRW = MutableLiveData<Boolean>()
    var systemLanguage = MutableLiveData<String>()

    init {
        toKRW.value = true
        updated_at.value = ""
        exchangeRate.value = -1f
        currencyCode.value = "USD"
        calculatedSource.value = 0f
        calculatedDestination.value = 0f
    }

    fun changeDirection() {
        toKRW.value = !toKRW.value!!
    }

    fun getSourceCurrency(): String {
        return if (toKRW.value!!) LanguageFlag.getCurrencyCodeForLanguage(systemLanguage.value!!) else "KRW"
    }
    fun getDestinationCurrency(): String {
        return if (toKRW.value!!) "KRW" else LanguageFlag.getCurrencyCodeForLanguage(systemLanguage.value!!)
    }

    fun getSourceLanguage(): String {
        return if (toKRW.value!!) systemLanguage.value!! else "KRW"
    }
    fun getDestinationLanguage(): String {
        return if (toKRW.value!!) "KRW" else systemLanguage.value!!
    }

    fun doCalculate() {
        if (exchangeRate.value != -1f) {
            if (toKRW.value!!) {
                calculatedDestination.value = calculatedSource.value!! * exchangeRate.value!!
            } else {
                calculatedDestination.value = calculatedSource.value!! / exchangeRate.value!!
            }
        }
    }

    fun getExchangeRate(language: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val obj = ExchangeRateApi.getExchangeRates(LanguageFlag.getCurrencyCodeForLanguage(language))
            if (obj != null) {
                exchangeRate.postValue(obj.bkpr)
                //exchangeRate.postValue(obj.deal_bas_r)
                currencyCode.postValue(obj.currency)
                val date = obj.updated_at.split("T")[0]
                val time = obj.updated_at.split("T")[1].split(".")[0]
                updated_at.postValue("${date} ${time}")
                //Toast.makeText(context, obj.currency_name, Toast.LENGTH_SHORT).show()
            } else {
                //Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var dotIsPushed = false
    var touchedOp = ""
    var savedOp = ""
    var savedValue = 0f

    fun doCalculateSource() {
        when (savedOp) {
            "+" -> {
                calculatedSource.value = savedValue + calculatedSource.value!!
            }
            "-" -> {
                calculatedSource.value = savedValue - calculatedSource.value!!
            }
            "*" -> {
                calculatedSource.value = savedValue * calculatedSource.value!!
            }
            "/" -> {
                calculatedSource.value = savedValue / calculatedSource.value!!
            }
            "%" -> {
                calculatedSource.value = savedValue * calculatedSource.value!! / 100
            }
        }
        touchedOp = ""
        savedOp = ""
        savedValue = 0f
        dotIsPushed = false
    }

    fun processKey(key: String) {
        when (key) {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" -> {
                if (touchedOp != "") {
                    savedOp = touchedOp
                    touchedOp = ""
                    savedValue = calculatedSource.value!!
                    calculatedSource.value = 0f
                }

                if (dotIsPushed) {
                    var temp = calculatedSource.value.toString()

                    var arr = temp.split(".")
                    if (arr.size == 2) {
                        if (arr[1].toInt() == 0) {  // 소수점 아래가 0인 경우
                            calculatedSource.value = "${arr[0]}.$key".toFloat()
                        } else { // 소수점 아래가 0이 아닌 경우
                            calculatedSource.value = "${arr[0]}.${arr[1]}$key".toFloat()
                        }
                    } else { // 소수점이 없는 경우
                        calculatedSource.value = calculatedSource.value!! * 10 + key.toInt()
                    }
                } else { // 소수점이 없는 경우
                    calculatedSource.value = calculatedSource.value!! * 10 + key.toInt()
                }
            }
            "+", "-", "*", "/", "%" -> {
                if (savedOp != "") {
                    doCalculateSource()
                }
                touchedOp = key
                savedOp = ""
                dotIsPushed = false
            }
            "C" -> {
                calculatedSource.value = 0f
                calculatedDestination.value = 0f
                dotIsPushed = false
            }
            "+/-" -> {
                calculatedSource.value = calculatedSource.value!! * -1.0f
            }
            "=" -> {
                doCalculateSource()
                dotIsPushed = false
            }
            "." -> {
                dotIsPushed = true
            }
            "<" -> {
                val temp = calculatedSource.value.toString()
                if (temp.contains('E')) {
                    Toast.makeText(getApplication(), "Can NOT erase", Toast.LENGTH_SHORT).show()
                } else {
                    if (temp.length == 1) {
                        calculatedSource.value = 0f
                    } else {
                        var arr = temp.split(".")
                        if (arr.size == 2) {
                            if (arr[1].toInt() == 0) {  // 소수점 아래가 0인 경우
                                if (arr[0].length == 1) {
                                    calculatedSource.value = 0f
                                } else {
                                    calculatedSource.value = arr[0].dropLast(1).toFloat()
                                }
                            } else { // 소수점 아래가 0이 아닌 경우
                                calculatedSource.value = temp.dropLast(1).toFloat()
                            }
                        } else { // 소수점이 없는 경우
                            calculatedSource.value = temp.dropLast(1).toFloat()
                        }

                    }
                }
            }
        }
        /*
        if (key == "C") {
            calculatedSource.value = 0f
            calculatedDestination.value = 0f
        } else if (key == "+/-") {
            calculatedSource.value = calculatedSource.value!! * -1.0f
        }
        */
    }
}