package dev.android.appfacturador

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.database.ClientDao
import dev.android.appfacturador.databinding.ActivityAddClientBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.Constants.Companion.KEY_CLIENT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddClientActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddClientBinding
    lateinit var email: String
    lateinit var shop: String
    var id = ""
    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClientBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initialize()
        //usuario y tienda actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        //Eventos
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAdd.setOnClickListener {
            val typeDNI = spinner.selectedItem.toString()
            val numberDNI = binding.edtNumDNI.text.toString()
            val fullName = (binding.edtNameClient.text.toString()).split(" ")
            val firstName = fullName[0].toLowerCase().capitalize()
            val secondName = fullName[1].toLowerCase().capitalize()
            val fullLastName = (binding.edtLastNameClient.text.toString()).split(" ")
            val firstLastName = fullLastName[0].toLowerCase().capitalize()
            val secondLastName = fullLastName[1].toLowerCase().capitalize()
            val email = binding.edtEmailClient.text.toString()
            val phone = binding.edtPhoneClient.text.toString()
            val address = binding.edtAddressClient.text.toString()
            if (fullName.isEmpty() || fullLastName.isEmpty() || numberDNI.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || address.isEmpty()
            ) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                var clientData =
                    CLIENTE(
                        id,
                        typeDNI,
                        numberDNI,
                        firstName,
                        secondName,
                        firstLastName,
                        secondLastName,
                        email,
                        phone,
                        address,
                        shop
                    )
                if (clientData.id.isEmpty()) {
                    addClient(clientData)
                    Toast.makeText(this, "¡Cliente agregado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    updateClient(clientData)
                    Toast.makeText(this, "¡Datos actualizados exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                }

                val intent = Intent(baseContext, ClientActivity::class.java)
                startActivity(intent)
            }
        }

    }

    fun initialize() {
        spinner = binding.spnDNI
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.TYPE_DNI)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val bundle = intent.extras
        bundle?.let {
            val client = it.getSerializable(KEY_CLIENT) as CLIENTE
            id = client.id
            binding.textView8.text =
                client.primer_nombre.first().toString() + client.apellido_paterno.first().toString()
            binding.btnAdd.text = "ACTUALIZAR"
            binding.edtNameClient.setText(client.primer_nombre + " " + client.segundo_nombre)
            binding.edtLastNameClient.setText(client.apellido_paterno + " " + client.apellido_materno)
            binding.edtNumDNI.setText(client.numero_dni)
            binding.edtEmailClient.setText(client.email)
            binding.edtPhoneClient.setText(client.telefono)
            binding.edtAddressClient.setText(client.direccion)
            val clientTypeDNI = client.tipo_dni
            val position = Constants.TYPE_DNI.indexOf(clientTypeDNI)
            spinner.setSelection(position)

        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtNameClient.setText("")
            binding.edtLastNameClient.setText("")
            binding.edtNumDNI.setText("")
            binding.edtEmailClient.setText("")
            binding.edtPhoneClient.setText("")
            binding.edtAddressClient.setText("")
        }
        binding.edtNameClient.requestFocus()
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                shop = empleado.negocio
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddClientActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addClient(cliente: CLIENTE) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientDao::class.java)
        val retrofit = retrofitBuilder.addClient(cliente)
        retrofit.enqueue(
            object : Callback<CLIENTE> {
                override fun onFailure(call: Call<CLIENTE>, t: Throwable) {
                    Log.d("Agregar", "Error al agregar cliente")

                }

                override fun onResponse(call: Call<CLIENTE>, response: Response<CLIENTE>) {
                    Log.d("Agregar", "Cliente agregado con éxito")

                }
            }
        )
    }

    private fun updateClient(cliente: CLIENTE) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientDao::class.java)

        val retrofit = retrofitBuilder.updateClient(cliente.id, cliente)
        retrofit.enqueue(
            object : Callback<CLIENTE> {
                override fun onFailure(call: Call<CLIENTE>, t: Throwable) {
                    Log.d("Actualizar", "Error al actualizar datos")
                }

                override fun onResponse(call: Call<CLIENTE>, response: Response<CLIENTE>) {
                    Log.d("Actualizar", "Datos actualizados")
                }
            }
        )
    }


}