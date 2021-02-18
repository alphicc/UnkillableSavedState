package com.savestatesample.sampleScreen

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.savestatesample.R

class SampleFragment : Fragment(R.layout.fragment_sample) {

    private var viewModel: SampleViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.application?.let { application ->
            viewModel = ViewModelProvider(this, SavedStateViewModelFactory(application, this))
                .get(SampleViewModel::class.java)
        }

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener { viewModel?.onSetDataClicked() }
    }
}