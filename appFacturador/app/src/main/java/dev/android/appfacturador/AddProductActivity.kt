package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.picasso.Picasso
import dev.android.appfacturador.R.drawable.load
import dev.android.appfacturador.database.ProductDao
import dev.android.appfacturador.databinding.ActivityAddProductBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class AddProductActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddProductBinding
    lateinit var email: String
    lateinit var shop: String
    private val typeIVA = arrayOf("0", "12", "14")
    lateinit var spinner: Spinner
    lateinit var codigoBarras: String
    var id = ""
    private lateinit var storageReference: StorageReference
    private val storage_path = "products/*"
    var imageBD: String = ""
    var image: Uri? = null
    private var imageStorage = ""
    private var photo = "imagen"
    private val progressDialog: ProgressDialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        //usuario y tienda actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        initialize()

        storageReference = FirebaseStorage.getInstance().reference

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                if (it != null) {
                    binding.imgProduct.setImageURI(it)
                    image = it
                }
            })

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }

        binding.btnScanner.setOnClickListener {
            initScanner()
        }

        binding.btnAdd.setOnClickListener {
            val product = binding.edtProduct.text.toString()
            val price = binding.edtPrice.text.toString()
            val iva = spinner.selectedItem.toString()
            val discount = binding.edtDiscount.text.toString()
            val barcode = binding.edtCodigoBarras.text.toString()
            var img = ""

            if (product.isEmpty() || price.isEmpty() || iva.isEmpty() || discount.isEmpty() || barcode.isEmpty() || (image == null && imageBD.isEmpty())) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                if (!imageBD.isEmpty() && image == null) {
                    //actualizar pero misma imagen
                    img = imageBD
                } else {
                    img = "null"
                }
                var productData =
                    PRODUCTO(
                        id,
                        product,
                        price.toFloat(),
                        discount.toInt(),
                        iva,
                        barcode,
                        img,
                        shop
                    )
                if (productData.id.isEmpty()) {
                    addNewProduct(productData)
                    Toast.makeText(this, "¡Producto agregado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (!imageBD.isEmpty() && image != null) {
                        uploadImage(image!!, productData.id)
                    }
                    updateProduct(productData)
                    Toast.makeText(this, "¡Producto actualizado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                }
                val intent = Intent(baseContext, ProductActivity::class.java)
                startActivity(intent)
            }
        }
    }

    @SuppressLint("ResourceType")
    fun initialize() {
        image = null
        imageBD = ""
        imageStorage = ""

        spinner = binding.spnIVA
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeIVA)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val bundle = intent.extras
        bundle?.let {
            val product = bundle.getSerializable(Constants.KEY_PRODUCT) as PRODUCTO
            id = product.id
            binding.btnAdd.text = "ACTUALIZAR"
            binding.txtTittleRegister.text = "Editar producto"
            binding.edtProduct.setText(product.nombre)
            binding.edtPrice.setText(product.precio.toString())
            binding.edtDiscount.setText(product.max_descuento.toString())
            binding.edtCodigoBarras.setText(product.codigo_barras.toString())
            Picasso.get().load(product.imagen).error(R.drawable.load).into(binding.imgProduct)
            imageBD = product.imagen //agregar la imagen a una variable
            val productTypeIVA = product.id_categoria_impuesto
            val position = typeIVA.indexOf(productTypeIVA)
            spinner.setSelection(position)
        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtProduct.setText("")
            binding.edtPrice.setText("")
            binding.edtDiscount.setText("")
            binding.edtCodigoBarras.setText("")
            binding.imgProduct.setImageResource(load)
        }
        binding.edtProduct.requestFocus()
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13)
        integrator.setPrompt("Código de Barras")
        integrator.initiateScan()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            } else {
                codigoBarras = result.contents
                binding.edtCodigoBarras.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.orderByChild("correoElectronico").equalTo(email)
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
                        this@AddProductActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNewProduct(producto: PRODUCTO) {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Product")

        val newProductRef = productsRef.push() // Crea un nuevo nodo en la referencia de "Product"
        val newProductId = newProductRef.key // Obtiene el ID del nuevo producto

        // Guarda el producto en la base de datos
        newProductRef.setValue(producto)
            .addOnSuccessListener {
                Log.d("Agregar", "Producto agregado con éxito. ID: $newProductId")
                if (imageBD.isEmpty() && image != null) {
                    uploadImage(image!!, newProductId.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Agregar", "Error al agregar producto: ${exception.message}")
            }
    }

    private fun updateProduct(producto: PRODUCTO) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductDao::class.java)
        val retrofit = retrofitBuilder.updateProduct(producto.id, producto)
        retrofit.enqueue(
            object : Callback<PRODUCTO> {
                override fun onFailure(call: Call<PRODUCTO>, t: Throwable) {
                    Log.d("Actualizar", "Error al actualizar datos")
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<PRODUCTO>, response: Response<PRODUCTO>) {
                    Log.d("Actualizar", "Datos actualizados")
                }
            }
        )
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage(imageUrl: Uri, productId: String) {
        progressDialog?.setMessage("Actualizando foto")
        progressDialog?.show()

        val storagePath = "$storage_path $photo${System.currentTimeMillis()}"
        val reference: StorageReference = storageReference.child(storagePath)

        reference.putFile(imageUrl)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    updateImageUrlInDatabase(productId, downloadUrl)
                }.addOnFailureListener { exception ->
                    showToast("Error al obtener la URL de descarga")
                    progressDialog?.dismiss()
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error al cargar foto")
            }
    }

    private fun updateImageUrlInDatabase(productId: String, imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val productRef = database.getReference("Product")

        productRef.child(productId).child("imagen").setValue(imageUrl)
            .addOnSuccessListener {
                showToast("Imagen subida exitosamente")
            }
            .addOnFailureListener { exception ->
                showToast("Error al subir la imagen: ${exception.message}")
            }
            .addOnCompleteListener {
                progressDialog?.dismiss()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@AddProductActivity, message, Toast.LENGTH_SHORT).show()
    }
}
