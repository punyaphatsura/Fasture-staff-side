package com.fasture.postman.presentation.home

import android.os.Bundle
import androidx.fragment.app.Fragment

class AppbarFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}