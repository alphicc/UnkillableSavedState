package com.savestatesample.sampleScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.savestatesample.R

class SampleFragment : Fragment(R.layout.fragment_sample) {

    private var viewModel: SampleViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel == null) {
            Log.d("Alpha", "chee")
            viewModel = ViewModelProvider(
                this,
                SavedStateViewModelFactory(activity?.application, this)
            ).get(SampleViewModel::class.java)
        }

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener { viewModel?.onSetTextClicked() }

        viewModel?.text?.observe(viewLifecycleOwner, Observer {
            val textView = view.findViewById<TextView>(R.id.fragmentSampleTitleTextView)
            textView.text = it
        })
    }
}