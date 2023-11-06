package com.fasture.postman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fasture.postman.databinding.ActivityMainBinding
import com.fasture.postman.presentation.home.BulkReadActivity
import com.fasture.postman.presentation.home.FullReadActivity
import com.fasture.postman.presentation.home.PostCodeReadActivity
import com.fasture.postman.presentation.home.ReadAndRegisterActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth;
    private lateinit var dutyIntent: Intent;
//    private lateinit var idView: TextView;
//    private lateinit var roleView: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        val root: View = binding.root

        val logOutSubmitButton = root.findViewById<Button>(R.id.signoutButton)
        logOutSubmitButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            updateUI(auth.currentUser)
        }
//        intent = Intent(this, BulkReadActivity::class.java)
//        startActivity(intent)

        val startButton = root.findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            startActivity(dutyIntent)
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

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "You Signed In successfully", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Toast.makeText(this, "You Didn't signed in", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        currentUser?.let {
            currentUser.getIdToken(true)
                .addOnSuccessListener(OnSuccessListener<GetTokenResult> { result ->
                    val claims = result.claims
                    Log.d("auth", claims.toString())
                    val role: String = result.claims["role"] as String
                    val uid: String = result.claims["user_id"] as String
//                    var intent: Intent
                    val root: View = binding.root
                    val idView = root.findViewById<TextView>(R.id.userIdView)
                    val roleView = root.findViewById<TextView>(R.id.userRoleView)
                    idView.text = "Welcome Back $uid"
                    roleView.text = "Your Role is $role"
                    Toast.makeText(this, "You are $role", Toast.LENGTH_LONG).show()
                    dutyIntent = when (role) {
                        "packer" -> Intent(this,BulkReadActivity::class.java)
                        "sorter" -> Intent(this, PostCodeReadActivity::class.java)
                        "postman" -> Intent(this, ReadAndRegisterActivity::class.java)
                        else -> Intent(this, PostCodeReadActivity::class.java )
                    }
                })


        } ?: run {
            intent = Intent(this, LoginActivity::class.java )
            startActivity(intent)
        }

    }

}