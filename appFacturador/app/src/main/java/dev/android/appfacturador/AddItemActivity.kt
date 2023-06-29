package dev.android.appfacturador

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import dev.android.appfacturador.databinding.ActivityAddItemBinding

class AddItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnAddedProducts.background = getDrawable(R.drawable.degradado2)
        binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))

        binding.btnAllProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.degradado2)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))
            binding.btnAllProducts.background = getDrawable(R.drawable.degradado)
            binding.btnAllProducts.setTextColor(Color.parseColor("#ffffff"))
        }

        binding.btnAddedProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.degradado)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllProducts.background = getDrawable(R.drawable.degradado2)
            binding.btnAllProducts.setTextColor(Color.parseColor("#686868"))
        }
    }
}