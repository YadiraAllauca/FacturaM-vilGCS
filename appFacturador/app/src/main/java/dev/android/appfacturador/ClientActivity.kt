package dev.android.appfacturador

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.databinding.ActivityClientBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants.Companion.KEY_CLIENT

class ClientActivity : AppCompatActivity() {
    lateinit var binding: ActivityClientBinding
    lateinit var email: String
    lateinit var shop: String
    private var list: MutableList<CLIENTE> = ArrayList()
    private val adapter: ClientAdapter by lazy {
        ClientAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Cliente")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "client")
            }
            startActivity(intent)
        }

        binding.btnAddClient.setOnClickListener {
            val intent = Intent(this, AddClientActivity::class.java)
            startActivity(intent)
        }
        adapter.setOnClickClient = {
            val bundle = Bundle().apply {
                putSerializable(KEY_CLIENT, it)
            }
            val intent = Intent(this, AddClientActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }
        recyclerView = binding.rvClients
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val empleado = dataSnapshot.getValue(EMPLEADO::class.java)

                    if (empleado != null) {
                        shop = empleado.negocio
                        Toast.makeText(this@ClientActivity, shop, Toast.LENGTH_SHORT).show()
                        loadData() // Llamar a loadData() una vez que se ha obtenido el valor de shop
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@ClientActivity,
                    "Error en la solicitud: " + databaseError.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun loadData() {
        var listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { child ->
                    val negocio = child.child("negocio").value?.toString()
                    if (negocio == shop) {
                        val client: CLIENTE? =
                            child.key?.let {
                                CLIENTE(
                                    child.key.toString(),
                                    child.child("tipo_dni").value.toString(),
                                    child.child("numero_dni").value.toString(),
                                    child.child("primer_nombre").value.toString(),
                                    child.child("segundo_nombre").value.toString(),
                                    child.child("apellido_paterno").value.toString(),
                                    child.child("apellido_materno").value.toString(),
                                    child.child("email").value.toString(),
                                    child.child("telefono").value.toString(),
                                    child.child("direccion").value.toString(),
                                    child.child("negocio").value.toString()
                                )
                            }
                        client?.let { list.add(it) }
                    }
                }
                adapter.updateListClients(list)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }


}