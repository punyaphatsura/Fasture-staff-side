package com.fasture.postman.application.repository.server

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.TimeUnit


class Parcel {
    private final val baseUri  = "https://postman-fasture.thegoose.work/api/v1/parcel"
//private final val baseUri  = "http://192.168.40.201:4444/api/v1/parcel"
    private var auth = Firebase.auth
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val ParcelJSONAdapter = moshi.adapter(ParcelData::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS).authenticator(object: Authenticator {
            @Throws(IOException::class)
            override fun authenticate(route: Route?, response: Response): Request? {
                if (response.request.headers("Authorization") != null) {
                    return null
                }
                var token = auth.getAccessToken(true)
                return response.request.newBuilder().header("Authorization", "Bearer $token").build()
            }
        }).build()

    public  fun getParcelInfo(parcelID: String, token: String): ParcelData? {
        Log.d("pauth", "sddfbff"+token)
        val request = Request.Builder().header("Authorization", "Bearer $token").url(this.baseUri+"/$parcelID").get().build()
        this.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {

                Log.d("Parcel","Get Parcel Error")
                Log.d("Parcel",response.code.toString())
                return null
            }

            val data =  ParcelJSONAdapter.fromJson(response.body!!.source())


            for ((name, value) in response.headers) {
                println("$name: $value")
            }

//            println(response.body!!.string())
//            Log.d("pauth", response.body!!.string())
            return data
        }
    }

    public fun setParcelReceived(parcelID: String): String {
        val request = Request.Builder().url(this.baseUri+"/status/received").post(parcelID.toRequestBody()).build()
        this.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }

            println(response.body!!.string())
            Log.d("auth", response.body!!.string())
            return response.body!!.string()
        }
    }
}