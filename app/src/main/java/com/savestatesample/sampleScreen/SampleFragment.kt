package com.savestatesample.sampleScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.savestatesample.R

class SampleFragment : Fragment(R.layout.fragment_sample) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this).get(SampleViewModel::class.java)
        viewModel.sampleMethod()
    }
}