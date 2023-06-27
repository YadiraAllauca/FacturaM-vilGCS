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
import dev.android.appfacturador.databinding.ActivityEmployeeBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants

class EmployeeActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmployeeBinding
    lateinit var email: String
    lateinit var shop: String
    private var list: MutableList<EMPLEADO> = ArrayList()
    private val adapter: EmployeeAdapter by lazy {
        EmployeeAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Empleado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        binding.btnAddEmployee.setOnClickListener {
            val intent = Intent(this, AddEmployeeActivity::class.java)
            startActivity(intent)
        }
        adapter.setOnClickEmployee = {
            val bundle = Bundle().apply {
                putSerializable(Constants.KEY_EMPLOYEE, it)
            }
            val intent = Intent(this, AddEmployeeActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }
        recyclerView = binding.rvEmployees
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        binding.btnBack.setOnClickListener { finish() }
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
                                loadData()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@EmployeeActivity,
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
                    val userEmail = child.child("correo_electronico").value?.toString()
                    if (negocio == shop && userEmail != email) {
                        val employee: EMPLEADO? =
                            child.key?.let {
                                EMPLEADO(
                                    child.key.toString(),
                                    child.child("apellido_materno").value.toString(),
                                    child.child("apellido_paterno").value.toString(),
                                    child.child("clave").value.toString(),
                                    child.child("correo_electronico").value.toString(),
                                    child.child("numero_dni").value.toString(),
                                    child.child("primer_nombre").value.toString(),
                                    child.child("segundo_nombre").value.toString(),
                                    child.child("tipo_dni").value.toString(),
                                    child.child("tipo_empleado").value.toString(),
                                    child.child("negocio").value.toString()
                                )
                            }
                        employee?.let { list.add(it) }
                    }
                }
                adapter.updateListEmployees(list)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

}