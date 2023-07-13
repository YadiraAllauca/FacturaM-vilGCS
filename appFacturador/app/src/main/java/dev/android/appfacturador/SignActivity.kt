package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import dev.android.appfacturador.databinding.ActivitySignBinding

class SignActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)
        val width = window.widthPixels

        getWindow().setLayout(width, 700)
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)
        getWindow().setGravity(Gravity.BOTTOM)
    }
}