package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    private var filter = ""
    private var bundle: Bundle? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        searchEditText = binding.edtSearch
        bundle = intent.extras
        binding.txtResult.visibility = View.GONE

        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        if (email.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        getShop()
        setupViews()
        darkMode()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                val filteredList = list.filter { factura ->
                    factura.numero_factura.contains(searchTerm, ignoreCase = true)
                }
                adapter.updateListbills(filteredList)
                if (adapter.bills.isEmpty()) {
                    binding.txtResult.visibility = View.VISIBLE
                    binding.txtResult.text = "¡No hay resultados con ese número de factura!"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupActions()
        binding.btnAddBill.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))

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
            startActivityForResult(intent, REQUEST_CODE)
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

        binding.btnCanceledBills.background = getDrawable(R.drawable.gradienttwo)
        binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))

        buttonsDarkMode(binding.btnAllBills, binding.btnCanceledBills)

        binding.btnAllBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.gradienttwo)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))
            binding.btnAllBills.background = getDrawable(R.drawable.gradient)
            binding.btnAllBills.setTextColor(Color.parseColor("#ffffff"))
            buttonsDarkMode(binding.btnAllBills, binding.btnCanceledBills)
            adapter.updateListbills(list)
        }

        binding.btnCanceledBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.gradient)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllBills.background = getDrawable(R.drawable.gradienttwo)
            binding.btnAllBills.setTextColor(Color.parseColor("#686868"))
            buttonsDarkMode(binding.btnCanceledBills, binding.btnAllBills)
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

//    fun updateBillList(searchTerm: String) {
//        val filteredList = list.filter { factura ->
//            factura.numero_factura.contains(searchTerm, ignoreCase = true) ||
//                    factura.cliente?.numero_dni?.contains(searchTerm, ignoreCase = true) == true ||
//                    factura.fecha.contains(searchTerm, ignoreCase = true)
//        }
//        adapter.updateListbills(filteredList)
//    }

    companion object {
        const val REQUEST_CODE = 1 // Código de solicitud, puedes elegir cualquier número entero
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val extraValue = data?.getStringExtra("filter")
            var filteredList: List<FACTURA> = emptyList()
            binding.edtSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchTerm = s.toString().trim()
                    if (extraValue != null && extraValue == "number") {
                        filteredList = list.filter { factura ->
                            factura.numero_factura.contains(searchTerm, ignoreCase = true)
                        }
                        if (filteredList.isEmpty()) {
                            binding.txtResult.visibility = View.VISIBLE
                            binding.txtResult.text = "¡No hay resultados con ese número de factura!"
                        }
                    }
                    if (extraValue != null && extraValue == "date") {
                        filteredList = list.filter { factura ->
                            factura.fecha.contains(searchTerm, ignoreCase = true)
                        }
                        if (filteredList.isEmpty()) {
                            binding.txtResult.visibility = View.VISIBLE
                            binding.txtResult.text = "¡No hay resultados con esa fecha!"
                        }
                    }
                    if (extraValue != null && extraValue == "id") {
                        filteredList = list.filter { factura ->
                            factura.cliente?.numero_dni?.contains(searchTerm, ignoreCase = true) == true
                        }
                        if (filteredList.isEmpty()) {
                            binding.txtResult.visibility = View.VISIBLE
                            binding.txtResult.text = "¡No hay resultados con ese número de identificación!"
                        }
                    }
                    adapter.updateListbills(filteredList)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "ResourceType")
    fun darkMode () {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.edtSearch.setBackgroundResource(R.drawable.searchdark)
            binding.edtSearch.setTextColor(Color.parseColor("#ffffff"))
            binding.edtSearch.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.btnFilters.setColorFilter(Color.parseColor("#47484a"))
            binding.btnAddBill.imageTintList = ColorStateList.valueOf(Color.parseColor("#121212"))
            binding.btnAddBill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#47484a"))
            binding.btnClose.setCardBackgroundColor(Color.parseColor("#47484a"))
            binding.btnCloses.setColorFilter(Color.parseColor("#121212"))
            binding.btnAllBills.setTextColor(Color.parseColor("#121212"))
            binding.btnAllBills.setBackgroundResource(R.drawable.gradientdarkwhite)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#ffffff"))
            binding.btnCanceledBills.setBackgroundResource(R.drawable.gradientdark)
            binding.divider.setBackgroundColor(Color.parseColor("#242424"))
        }
    }

    fun buttonsDarkMode (buttonClicked: Button, buttonNotClicked: Button){
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            buttonNotClicked.background = getDrawable(R.drawable.gradientdark)
            buttonNotClicked.setTextColor(Color.parseColor("#ffffff"))
            buttonClicked.background = getDrawable(R.drawable.gradientdarkwhite)
            buttonClicked.setTextColor(Color.parseColor("#121212"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRestart() {
        super.onRestart()
        filter = bundle?.getString("filter").toString()
    }

    override fun onBackPressed() {
    }

}