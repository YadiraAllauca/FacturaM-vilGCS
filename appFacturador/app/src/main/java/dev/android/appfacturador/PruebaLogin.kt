package dev.android.appfacturador

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import dev.android.appfacturador.databinding.ActivityPruebaLoginBinding

class PruebaLogin : AppCompatActivity() {
    private lateinit var binding: ActivityPruebaLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cerrarSesion()

        
    }

    fun cerrarSesion() {
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        binding.txtViewSaludo.text = "Bienvenido $email"

        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }

}