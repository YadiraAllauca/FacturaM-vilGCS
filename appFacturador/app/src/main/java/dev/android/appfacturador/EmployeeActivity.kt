package dev.android.appfacturador

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.database.EmployeeDao
import dev.android.appfacturador.database.ProductDao
import dev.android.appfacturador.databinding.ActivityEmployeeBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

class EmployeeActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmployeeBinding
    private lateinit var email: String
    private lateinit var shop: String
    private var list: MutableList<EMPLEADO> = ArrayList()
    private val adapter: EmployeeAdapter by lazy {
        EmployeeAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val instanceFirebase = Firebase.database
    private val db = instanceFirebase.getReference("Empleado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        events()
        recyclerView = binding.rvEmployees
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

    }

    private fun events() {
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
        search()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        db.orderByChild("correo_electronico").equalTo(email)
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


    private fun loadData() {
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
                adapter.setOnClickListenerEmployeeDelete = {
                    deleteEmployee(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        db.addValueEventListener(listen)
    }

    private fun deleteEmployee(empleado: EMPLEADO) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmployeeDao::class.java)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar registro")
        builder.setMessage("¿Desea continuar?")
        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            Executors.newSingleThreadExecutor().execute {
                val retrofit = retrofitBuilder.deleteEmployee(empleado.id)
                retrofit.enqueue(object : Callback<EMPLEADO> {
                    override fun onResponse(call: Call<EMPLEADO>, response: Response<EMPLEADO>) {
                        Log.e("Eliminación", "Eliminado con éxito")
                    }

                    override fun onFailure(call: Call<EMPLEADO>, t: Throwable) {
                        Log.e("Eliminación", "No se ha podido eliminar")
                    }
                })
                runOnUiThread {
                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show()
                }
            }
        })
        builder.show()
    }

    private fun search() = with(binding) {
        edtSearchEmployee.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(
                filterText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (filterText?.length!! > 0) {
                    val filterList = list.filter { employee ->
                        val fullName =
                            "${employee.primer_nombre} ${employee.segundo_nombre} ${employee.apellido_paterno} ${employee.apellido_materno}"
                        fullName.uppercase().startsWith(filterText.toString().uppercase()) ||
                                employee.numero_dni.uppercase()
                                    .startsWith(filterText.toString().uppercase())
                    }
                    adapter.updateListEmployees(filterList)
                } else {
                    loadData()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

}