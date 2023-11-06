package com.fasture.postman.application.repository.server

import android.util.Log
import com.fasture.postman.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class Auth() {
    private final val baseUri  = "https://postman-fasture.thegoose.work"
    private lateinit var auth: FirebaseAuth;
    private val moshi = Moshi.Builder().build()
    private val tokenJsonAdapter = moshi.adapter(TokenResponse::class.java)


    public fun signIn(user: String, pass: String) {

        Log.d("Auth","Sending Req" + this.baseUri)
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
        val body = FormBody.Builder().add("Username",user).add("Password",pass).build()
        val request = Request.Builder().url(this.baseUri+"/auth/signin").post(body).build();
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    auth = Firebase.auth
                    val token =  tokenJsonAdapter.fromJson(response.body!!.source())!!.token
                    auth.signInWithCustomToken(token)

                }
            }
        })
    }

}