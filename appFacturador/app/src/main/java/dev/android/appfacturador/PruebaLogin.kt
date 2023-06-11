package dev.android.appfacturador

import android.content.Context
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
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        binding.txtViewSaludo.text = "Bienvenido $email"

        cerrarSesion()

        //guardando los datos
        val preferencias : SharedPreferences.Editor = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
        preferencias.putString("email", email)
        preferencias.apply()
    }

    fun cerrarSesion() {

        binding.btnCerrarSesion.setOnClickListener {
            val preferencias : SharedPreferences.Editor = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
            preferencias.clear()
            preferencias.apply()
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }

}