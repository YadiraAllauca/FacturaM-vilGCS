package dev.android.appfacturador

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import dev.android.appfacturador.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
//        setContentView(R.layout.activity_login)

//        var btnLogin = findViewById<Button>(R.id.button)
//        btnLogin.setOnClickListener {
//            var intent = Intent(this, PruebaLogin::class.java)
//            startActivity(intent)
//        }
        iniciarSesion()

    }

    fun iniciarSesion() {
        binding.button.setOnClickListener {
            if (binding.editTextTextPersonName.text.isNotEmpty() && binding.editTextTextPersonName2.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.editTextTextPersonName.text.toString(),
                    binding.editTextTextPersonName2.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mostrarVentanaNueva(binding.editTextTextPersonName?.toString() ?: "")
                    } else {
                        mostrarAlertaLogin()
                    }
                }
            }
        }
    }

    private fun mostrarAlertaLogin() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al iniciar sesion")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun mostrarVentanaNueva(email: String) {
        var intent = Intent(this, PruebaLogin::class.java).apply {
            putExtra("Email", binding.editTextTextPersonName.text)
        }
        startActivity(intent)
    }
}