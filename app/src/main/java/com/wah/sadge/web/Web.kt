package com.wah.sadge.web

import android.content.Context
import com.wah.sadge.model.exchangerate.ExchangeRate
import kotlinx.coroutines.runBlocking
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val BASE_URL = "https://api.nbrb.by/exrates/rates/"
private const val USD_CODE = "431"

private const val CURRENCY_ID = "Cur_ID"
private const val CURRENCY_ABBREVIATION = "Cur_Abbreviation"
private const val CURRENCY_SCALE = "Cur_Scale"
private const val CURRENCY_NAME = "Cur_Name"
private const val CURRENCY_OFFICIAL_RATE = "Cur_OfficialRate"

fun getUSDExchangeRateBlocking(context: Context): Double = runBlocking {
    return@runBlocking getResponse(context)?.let {
        val stringBuilder =
            StringBuilder(it.substringAfter("{").substringBeforeLast("}"))
        stringBuilder.insert(0, "{").append("}")

        val jsonObject = JSONObject(stringBuilder.toString())
        val exchangeRate = ExchangeRate(jsonObject.getInt(CURRENCY_ID),
            jsonObject.getString(CURRENCY_ABBREVIATION),
            jsonObject.getInt(CURRENCY_SCALE),
            jsonObject.getString(CURRENCY_NAME),
            jsonObject.getDouble(CURRENCY_OFFICIAL_RATE))

        exchangeRate.currencyOfficialRate
    } ?: 0.0
}

private suspend fun getResponse(context: Context): String? {
    val cronetEngine: CronetEngine = CronetEngine.Builder(context).build()
    val executor = Executors.newSingleThreadExecutor()

    val result = suspendCoroutine { continuation ->
        val request = cronetEngine.newUrlRequestBuilder(
            BASE_URL + USD_CODE,
            object: UrlRequest.Callback() {
                val responseBuffer = ByteBuffer.allocateDirect(102400)

                override fun onRedirectReceived(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    newLocationUrl: String?
                ) {
                    request?.followRedirect()
                }

                override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
                    if (info?.httpStatusCode == 200) {
                        request?.read(responseBuffer)
                    } else {
                        request?.cancel()
                        continuation.resume(null)
                    }
                }

                override fun onReadCompleted(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    byteBuffer: ByteBuffer?
                ) {
                    byteBuffer?.clear()
                    request?.read(responseBuffer)
                }

                override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
                    continuation.resume(String(responseBuffer.array()))
                }

                override fun onFailed(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    error: CronetException?
                ) { }

            },
            executor
        ).build()
        request.start()
    }
    return result
}