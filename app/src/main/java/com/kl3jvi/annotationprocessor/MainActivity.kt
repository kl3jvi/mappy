package com.kl3jvi.annotationprocessor

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kl3jvi.annotations.DeepLink

@DeepLink
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}
