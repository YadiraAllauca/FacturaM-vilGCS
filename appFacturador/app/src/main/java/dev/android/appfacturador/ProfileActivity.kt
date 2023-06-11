package dev.android.appfacturador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dev.android.appfacturador.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    var logueado = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.imgProfile.setImageURI(it)
            })

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }


        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "profile")
            }
            startActivity(intent)

        }
        val bundle: Bundle? = intent.extras
        val email: String? = intent.getStringExtra("email")
        Toast.makeText(this, "Valor del email: $email", Toast.LENGTH_SHORT).show()
        cerrarSesion()
        val preferencias: SharedPreferences.Editor =
            getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
        preferencias.putString("email", email)
        preferencias.apply()
        logueado = true


    }

    fun cerrarSesion() {

        binding.btnLogout.setOnClickListener {
            val preferencias: SharedPreferences.Editor =
                getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
            preferencias.clear()
            preferencias.apply()
            FirebaseAuth.getInstance().signOut()
            logueado = false
            onBackPressed()

        }
    }

    override fun onBackPressed() {
        if (logueado) {

        } else {
            super.onBackPressed()
        }
    }

}