package dev.android.appfacturador

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.EditText
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
import dev.android.appfacturador.databinding.ActivityBillBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.utils.Constants

class BillActivity : AppCompatActivity() {
    lateinit var binding: ActivityBillBinding
    lateinit var email: String
    lateinit var shop: String
    lateinit var searchEditText: EditText
    private var list: MutableList<FACTURA> = mutableListOf()
    private val adapter: BillAdapter by lazy {
        BillAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Factura")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        searchEditText = binding.edtBuscador

        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        if (email.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        getShop()
        setupViews()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                updateBillList(searchTerm)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupActions()

    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val usuariosRef = FirebaseDatabase.getInstance().getReference("Empleado")

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
                        loadData()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@BillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun setupViews(){
        recyclerView = binding.rvBills
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.setOnClickListenerBillEdit = {
            val bundle = Bundle().apply {
                putSerializable(Constants.KEY_BILL, it)
            }
            val intent =
                Intent(applicationContext, BillDetailActivity::class.java).putExtras(bundle)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    fun setupActions(){
        binding.btnFilters.setOnClickListener {
            val intent = Intent(this, FilterBillActivity::class.java)
            startActivity(intent)
        }

        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "bill")
            }
            startActivity(intent)
        }

        binding.btnAddBill.setOnClickListener {
            val intent = Intent(this, AddBillActivity::class.java)
            startActivity(intent)
        }

        binding.btnCanceledBills.background = getDrawable(R.drawable.degradado2)
        binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))

        binding.btnAllBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.degradado2)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))
            binding.btnAllBills.background = getDrawable(R.drawable.degradado)
            binding.btnAllBills.setTextColor(Color.parseColor("#ffffff"))
            adapter.updateListbills(list)
        }

        binding.btnCanceledBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.degradado)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllBills.background = getDrawable(R.drawable.degradado2)
            binding.btnAllBills.setTextColor(Color.parseColor("#686868"))
            val anuladasList = list.filter { factura -> factura.estado == "-1" }
            adapter.updateListbills(anuladasList)
        }
    }

    fun loadData() {
        val listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { child ->
                    val negocio = child.child("negocio").value?.toString()
                    if (negocio == shop) {
                        val bill: FACTURA? = child.getValue(FACTURA::class.java)
                        bill?.let { list.add(it) }
                    }
                }
                adapter.updateListbills(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

    fun updateBillList(searchTerm: String) {
        val filteredList = list.filter { factura ->
            factura.numero_factura.contains(searchTerm, ignoreCase = true) ||
                    factura.cliente?.numero_dni?.contains(searchTerm, ignoreCase = true) == true ||
                    factura.fecha.contains(searchTerm, ignoreCase = true)
        }
        adapter.updateListbills(filteredList)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}