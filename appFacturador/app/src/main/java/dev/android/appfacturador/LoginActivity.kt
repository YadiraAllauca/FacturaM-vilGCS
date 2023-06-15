package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import dev.android.appfacturador.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        login()
        session()
        hiddeVisiblePassword()
    }

    fun hiddeVisiblePassword() {
        var passwordHidden = false
        binding.btnHide.setOnClickListener {
            if (passwordHidden) {
                binding.txtPaassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                passwordHidden = false
                binding.btnHide.setColorFilter(ContextCompat.getColor(this, R.color.gray))
            } else {
                binding.txtPaassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                passwordHidden = true
                binding.btnHide.setColorFilter(ContextCompat.getColor(this, R.color.blues))
            }
        }
    }

    fun login() {
        binding.btnNext.setOnClickListener {
            if (binding.edtEmail.text.isNotEmpty() && binding.txtPaassword.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.edtEmail.text.toString(),

                    binding.txtPaassword.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val email = binding.edtEmail.text.toString()
                        showNewActivity(email ?: "")
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al iniciar sesión")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showNewActivity(email: String) {
        val preferences: SharedPreferences.Editor =
            getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
        preferences.putString("email", email)
        preferences.apply()

        var intent = Intent(this, ProductActivity::class.java)
        startActivity(intent)
    }

    private fun session() {
        val preferences: SharedPreferences =
            getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val email: String? = preferences.getString("email", null)
        if (email != null) {
            showNewActivity(email)

        }
    }
}