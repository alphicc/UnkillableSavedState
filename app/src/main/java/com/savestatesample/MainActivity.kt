package com.savestatesample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.savestatesample.sampleScreen.SampleFragment

class MainActivity : AppCompatActivity() {

    var isAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.activityMainFragmentContainerView, SampleFragment())
                .commit()
        }
    }
}