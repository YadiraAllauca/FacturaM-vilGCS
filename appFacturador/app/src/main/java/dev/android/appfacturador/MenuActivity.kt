package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import dev.android.appfacturador.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMenuBinding
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnArrow.setOnClickListener {
            finish()
        }

        val bundle = intent.extras
        val option = bundle?.getString("option")

        if (option == "profile") {
            binding.btnProfile.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnProfiles.setColorFilter(ContextCompat.getColor(this, R.color.white))
        } else if (option == "bill") {
            binding.btnBill.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnBills.setColorFilter(ContextCompat.getColor(this, R.color.white))
        } else if (option == "client") {
            binding.btnClient.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnClients.setColorFilter(ContextCompat.getColor(this, R.color.white))
        } else if (option == "product") {
            binding.btnProduct.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnProducts.setColorFilter(ContextCompat.getColor(this, R.color.white))
        }

        binding.btnProducts.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnClients.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnBills.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnProfiles.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)

        getWindow().setLayout(370, 950)
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)
        getWindow().setGravity(Gravity.START)
    }
}