package com.fasture.postman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.fasture.postman.application.repository.server.TokenResponse
import com.fasture.postman.databinding.ActivityLoginBinding
import com.fasture.postman.presentation.home.PostCodeReadActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.MainScope
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoginActivity : Activity() {
    private val mainScope = MainScope()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth;
    private final val baseUri  = "https://postman-fasture.thegoose.work/api/v1"
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val tokenJsonAdapter = moshi.adapter(TokenResponse::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val root: View = binding.root
        val et_username = root.findViewById<TextView>(R.id.txtUserID)
        val et_password = root.findViewById<TextView>(R.id.txtUserPassword)
        val LoginSubmitButton = root.findViewById<Button>(R.id.btnLogin)
        LoginSubmitButton.setOnClickListener {
            if (et_username.text.toString().isNotEmpty() && et_password.text.toString().isNotEmpty()) {
                val username = et_username.text.toString();
                val password = et_password.text.toString();
                Log.d("Login", "$username $password")
                getCustomToken(username, password)
            }

        }


//        val navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }

    public fun getCustomToken(user: String, pass : String) {
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
                    if (!response.isSuccessful) {
                        Toast.makeText(baseContext, "Login failed.",
                            Toast.LENGTH_SHORT).show()
                        throw IOException("Unexpected code $response")
                    }
                    auth = Firebase.auth
                    val token =  tokenJsonAdapter.fromJson(response.body!!.source())!!.token
                    signWithToken(token)

                }
            }
        })
    }

    public fun signWithToken(token: String) {
        Log.d("Auth",token)
        auth.signInWithCustomToken(token)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Auth", "signInWithCustomToken:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Auth", "signInWithCustomToken:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "You Signed In successfully", Toast.LENGTH_LONG).show()
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "You Didnt signed in", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        var intent: Intent
        currentUser?.let {
            // case Found
            intent = Intent(this, MainActivity::class.java )
            startActivity(intent)
        } ?: run {
        }

    }
}