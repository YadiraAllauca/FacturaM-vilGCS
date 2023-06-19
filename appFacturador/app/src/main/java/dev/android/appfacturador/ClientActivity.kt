package dev.android.appfacturador

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.databinding.ActivityClientBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants

class ClientActivity : AppCompatActivity() {
    lateinit var binding: ActivityClientBinding
    private var list: MutableList<CLIENTE> = ArrayList()
    private val adapter: ClientAdapter by lazy {
        ClientAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Client")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

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
        recyclerView = binding.rvClients
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        loadData()
    }

    fun loadData() {
        var listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { child ->
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
                            )
                        }
                    client?.let { list.add(it) }
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