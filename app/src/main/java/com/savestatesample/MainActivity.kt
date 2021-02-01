package com.savestatesample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.savestatesample.sampleScreen.SampleFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.activityMainFragmentContainerView, SampleFragment())
            .commit()
    }
}