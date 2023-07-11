package dev.android.appfacturador

import android.R
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import dev.android.appfacturador.databinding.ActivityAddBillBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import android.graphics.Color
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import dev.android.appfacturador.database.BillDao
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class AddBillActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBillBinding
    lateinit var email: String
    lateinit var shop: String
    private val adapter: ProductBillAdapter by lazy {
        ProductBillAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    val productList: MutableList<ProductHolder.ProductItem> = ProductHolder.productList.toMutableList()
    lateinit var searchClienteEditText: EditText
    private var clienteEncontrado: CLIENTE? = null
    private var empleadoEncontrado: EMPLEADO? = null
    lateinit var spinner: Spinner
    var contadorNegocio = 0

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaActual = dateFormat.format(calendar.time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBillBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()

        // Inicializar vistas
        initViews()

        // Obtener negocio y cargar datos
        getShop()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.updateListProducts(productList)
        recyclerView.adapter = adapter

        spinner = binding.spinnerPay
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, Constants.TYPE_PAY)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        searchClienteEditText = binding.edtNumeroIdentificacion
        searchClienteEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                getClienteData(searchTerm)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAddClient.setOnClickListener {
            val intent = Intent(this, AddClientActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, BillActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddItem.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }

        binding.btnGenerateBill.setOnClickListener {
            getShopCounter(shop)
            val billData = createBillData()
            if (billData != null) {
                addBill(billData)
            } else {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            }
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
                                empleadoEncontrado = empleado
                                shop = empleado.negocio
                                loadData()
                                getShopCounter(shop)
                            }
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadData() {
        adapter.updateListProducts(productList)
        updateValues()

        adapter.addTextChangedListenerAmount = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateValues()
        }

        adapter.addTextChangedListenerDiscount = { position, newDiscount ->
            ProductHolder.updateDiscount(position, newDiscount)
            updateValues()
        }
    }

    private fun getClienteData(searchCliente: String) {
        val database = FirebaseDatabase.getInstance()
        val clientesRef = database.getReference("Cliente")

        clientesRef.orderByChild("numero_dni").equalTo(searchCliente)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val cliente = childSnapshot.getValue(CLIENTE::class.java)
                            if (cliente != null) {
                                clienteEncontrado = cliente
                                binding.edtNumeroIdentificacion.setTextColor(Color.BLACK)
                                binding.txtClienteName.text = cliente.primer_nombre+""+cliente.apellido_paterno
                                binding.txtClienteName.setTextColor(Color.BLACK)
                                binding.txtClienteEmail.text = cliente.correo_electronico
                                binding.txtClienteEmail.setTextColor(Color.BLACK)
                            }
                        }
                    } else {
                        binding.edtNumeroIdentificacion.setTextColor(Color.RED)
                        binding.txtClienteName.text = "Cliente no Identificado"
                        binding.txtClienteName.setTextColor(Color.RED)
                        binding.txtClienteEmail.text = "---"
                        binding.txtClienteEmail.setTextColor(Color.RED)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun createBillData(): FACTURA? {
        val id = ""
        val numero_factura = contadorNegocio.toString()
        val estado = "enviado"
        val fecha = fechaActual
        val cliente = clienteEncontrado
        val empleado = empleadoEncontrado
        val forma_pago = spinner.selectedItem.toString()
        val subtotal = calculateSubtotal()
        val descuento = calculateTotalDiscount()
        val iva = calculateTotalIVA()
        val total = calculateTotalBill()
        val items = ProductHolder.productList
        val negocio = shop

        if (cliente != null && empleado != null && !negocio.isEmpty()) {
            return FACTURA(
                id, numero_factura, estado,
                fecha, cliente, empleado,
                forma_pago, subtotal, descuento, iva, total,
                items, negocio
            )
        }

        return null
    }

    private fun addBill(bill: FACTURA) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BillDao::class.java)
        val retrofit = retrofitBuilder.addBill(bill)
        retrofit.enqueue(
            object : Callback<FACTURA> {
                override fun onFailure(call: Call<FACTURA>, t: Throwable) {
                    Log.d("Agregar", "Error al agregar la factura")
                }
                override fun onResponse(call: Call<FACTURA>, response: Response<FACTURA>) {
                    if (response.isSuccessful) {
                        ProductHolder.productList.clear()
                        clienteEncontrado = null
                        updateShopCounter(contadorNegocio)
                        Log.d("Agregar", "Factura agregada con éxito")
                        Toast.makeText(this@AddBillActivity, "¡Factura registrada exitosamente!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(baseContext, BillActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.d("Agregar", "Error al agregar la factura")
                    }
                }
            }
        )
    }

    private fun updateShopCounter(counter: Int) {
        val database = FirebaseDatabase.getInstance()
        val negocioRef = database.getReference("Negocios")

        negocioRef.child(shop).child("contador").setValue(counter)
            .addOnSuccessListener {}
            .addOnFailureListener { error ->
                print(error)
            }
    }

    private fun getShopCounter(shopId: String) {
        val database = FirebaseDatabase.getInstance()
        val negocioRef = database.getReference("Negocios")

        negocioRef.child(shopId).child("contador").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val contador = dataSnapshot.getValue(Long::class.java)?.toInt()
                if (contador != null) {
                    contadorNegocio = contador+1
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@AddBillActivity,
                    "Error en la solicitud: " + databaseError.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateValues() {
        binding.txtDiscount.text = "$"+String.format("%.2f", calculateTotalDiscount())
        binding.txtSubtotal.text = "$"+String.format("%.2f", calculateSubtotal())
        binding.txtIva.text = "$"+String.format("%.2f", calculateTotalIVA())
        binding.txtTotalBill.text = "$"+String.format("%.2f", calculateTotalBill())
    }

    private fun calculateSubtotal(): Float {
        var totalSubtotal = 0f
        for (product in ProductHolder.productList) {
            val subtotal = product.quantity * (product.product?.precio ?: 0f)
            totalSubtotal += subtotal
        }
        return totalSubtotal
    }

    private fun calculateTotalDiscount(): Float {
        var totalDiscount = 0f
        for (product in ProductHolder.productList) {
            val discount = ((product.product?.precio ?: 0f) * product.discount / 100) * product.quantity
            totalDiscount += discount
        }
        return totalDiscount
    }

    private fun calculateTotalIVA(): Float {
        var totalIVA = 0f
        for (product in ProductHolder.productList) {
            val iva = (product.product?.id_categoria_impuesto?.toFloat() ?: 0f) * (product.product?.precio ?: 0f) / 100
            totalIVA += iva
        }
        return totalIVA
    }

    private fun calculateTotalBill(): Float {
        return calculateSubtotal() + calculateTotalIVA() - calculateTotalDiscount()
    }
}