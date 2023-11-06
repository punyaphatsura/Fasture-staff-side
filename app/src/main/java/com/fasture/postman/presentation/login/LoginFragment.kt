package com.fasture.postman.presentation.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fasture.postman.R
import com.google.firebase.auth.FirebaseAuth
import com.fasture.postman.application.repository.server.Auth
import com.fasture.postman.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginFragment: Fragment() {
    private val mainScope = MainScope()
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        val loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val et_username = root.findViewById<TextView>(R.id.txtUserID)
        val et_password = root.findViewById<TextView>(R.id.txtUserPassword)
        auth = Firebase.auth
        val LoginSubmitButton = root.findViewById<Button>(R.id.btnLogin)
        LoginSubmitButton.setOnClickListener {
            val username = et_username.text as String
            val password = et_password.text as String
            Log.d("Login", "$username $password")
            val a = Auth()
            a.signIn(username, password)

        }

        return  root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}