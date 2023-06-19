package dev.android.appfacturador

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import dev.android.appfacturador.databinding.ActivityShopBinding

class ShopActivity : AppCompatActivity() {
    lateinit var binding: ActivityShopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        //obtener email de usuario
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        Toast.makeText(this, "Valor del email: $email", Toast.LENGTH_SHORT).show()
    }
}