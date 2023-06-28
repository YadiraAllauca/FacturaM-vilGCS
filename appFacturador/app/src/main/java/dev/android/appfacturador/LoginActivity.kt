package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        hiddeVisiblePassword()
        db = FirebaseDatabase.getInstance()
        login()
        session()
    }

    private fun hiddeVisiblePassword() {
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

    private fun login() {
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
        builder.setMessage("Ingrese correctamente su usuario y contraseña")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showNewActivity(email: String) {

        val usuariosRef = db.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                if (empleado.tipo_empleado == "A") {
                                    val preferences: SharedPreferences.Editor =
                                        getSharedPreferences(
                                            "PREFERENCE_FILE_KEY",
                                            Context.MODE_PRIVATE
                                        ).edit()
                                    preferences.putString("email", email)
                                    preferences.apply()
                                    val intent =
                                        Intent(this@LoginActivity, ProductActivity::class.java)
                                    startActivity(intent)
                                } else if (empleado.tipo_empleado == "V") {
                                    val preferences: SharedPreferences.Editor =
                                        getSharedPreferences(
                                            "PREFERENCE_FILE_KEY",
                                            Context.MODE_PRIVATE
                                        ).edit()
                                    preferences.putString("email", email)
                                    preferences.apply()
                                    val intent =
                                        Intent(this@LoginActivity, ProductActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Usuario no encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                break
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuario no encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
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