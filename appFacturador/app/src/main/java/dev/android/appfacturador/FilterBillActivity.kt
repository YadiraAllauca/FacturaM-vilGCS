package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Window
import dev.android.appfacturador.databinding.ActivityFilterBillBinding

class FilterBillActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBillBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBillBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)
        val width = window.widthPixels
        val height = window.heightPixels

        getWindow().setLayout(((width * 0.85).toInt()), ((height * 0.2).toInt()))
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)
    }
}