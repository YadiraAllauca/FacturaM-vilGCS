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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.databinding.ActivityLoginBinding
import dev.android.appfacturador.model.EMPLEADO


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        hiddeVisiblePassword()
        database = FirebaseDatabase.getInstance()
        login()
        session()

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

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val empleado = dataSnapshot.getValue(EMPLEADO::class.java)

                    if (empleado != null && empleado.tipoEmpleado == "A") {
                        Toast.makeText(this@LoginActivity, "ADMIN", Toast.LENGTH_SHORT).show()
                    } else if (empleado != null && empleado.tipoEmpleado == "V") {
                        Toast.makeText(this@LoginActivity, "VENDEDOR", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Tipo de empleado desconocido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Usuario no encontrado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error en la solicitud: " + databaseError.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
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
    override fun onBackPressed() {
        Toast.makeText(this, "Botón bloqueado por su seguridad", Toast.LENGTH_SHORT).show()
    }

}