package com.odnovolov.forgetmenot.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, HomeFragment.newInstance(), null)
                .commit()
        }
    }
}
