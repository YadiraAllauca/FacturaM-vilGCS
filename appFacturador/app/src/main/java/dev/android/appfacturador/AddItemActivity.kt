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
import com.google.zxing.integration.android.IntentIntegrator
import dev.android.appfacturador.databinding.ActivityAddItemBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO

class AddItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddItemBinding
    lateinit var email: String
    lateinit var shop: String
    private val fb = Firebase.database
    private val dr = fb.getReference("Product")
    private val adapter: ProductItemBillAdapter by lazy {
        ProductItemBillAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private var list: MutableList<PRODUCTO> = ArrayList()
    private var addedList: MutableList<PRODUCTO> = mutableListOf()
    lateinit var searchEditText: EditText
    lateinit var barcode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()

        //recycle view
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        getShop()

        searchEditText = binding.edtBuscador
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                updateProductList(searchTerm)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnScanner.setOnClickListener {
            initScanner()
        }

        binding.btnAddedProducts.background = getDrawable(R.drawable.degradado2)
        binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))

        binding.btnAllProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.degradado2)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))
            binding.btnAllProducts.background = getDrawable(R.drawable.degradado)
            binding.btnAllProducts.setTextColor(Color.parseColor("#ffffff"))
            adapter.updateListProducts(list)
        }

        binding.btnAddedProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.degradado)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllProducts.background = getDrawable(R.drawable.degradado2)
            binding.btnAllProducts.setTextColor(Color.parseColor("#686868"))

            var pro = ProductHolder.productList.map { it.product }.toMutableList()
            var filteredProducts = list.filter { pro.contains(it) }.toMutableList()
            adapter.updateListProducts(filteredProducts)
        }

        binding.btnAddItems.setOnClickListener{
            val intent = Intent(this, AddBillActivity::class.java).apply {}
            startActivity(intent)
        }
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
                        this@AddItemActivity,
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
                        val product: PRODUCTO? =
                            child.key?.let {
                                PRODUCTO(
                                    child.key.toString(),
                                    child.child("nombre").value.toString(),
                                    child.child("precio").value.toString().toFloat(),
                                    child.child("max_descuento").value.toString().toInt(),
                                    child.child("id_categoria_impuesto").value.toString(),
                                    child.child("codigo_barras").value.toString(),
                                    child.child("imagen").value.toString(),
                                    child.child("negocio").value.toString()
                                )
                            }
                        product?.let { list.add(it) }
                    }
                }
                adapter.updateListProducts(list)
                recyclerView.adapter = adapter

                adapter.onCheckedChangeListener = { product, isChecked ->
                    if (isChecked) {
                        val productItem = ProductHolder.ProductItem(product, 1, product.max_descuento.toInt())
                        ProductHolder.productList.add(productItem)
                        addedList.add(product)
                    } else {
                        addedList.remove(product)
                        val position = ProductHolder.productList.indexOfFirst { it.product?.nombre == product.nombre }
                        ProductHolder.productList.removeAt(position)
                    }
                    print(addedList.size)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

    fun updateProductList(searchTerm: String) {
        val filteredList = list.filter { product ->
            product.nombre.contains(searchTerm, ignoreCase = true) || product.codigo_barras.contains(searchTerm, ignoreCase = true)
        }
        adapter.updateListProducts(filteredList)
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13)
        integrator.setPrompt("Código de Barras")
        integrator.initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if(result.contents == null){
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }else{
                barcode = result.contents
                binding.edtBuscador.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}