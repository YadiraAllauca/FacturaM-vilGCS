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
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    private val typeIVA = arrayOf("0%", "12%", "14%")
    lateinit var spinner: Spinner
    private lateinit var message: String
    var id = ""
    private lateinit var storageReference: StorageReference
    private val storage_path = "products/*"

    private var photo = "imagen"
    private val progressDialog: ProgressDialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initialize()

        //usuario y tienda actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()

        storageReference = FirebaseStorage.getInstance().reference

        var image: Uri? = null

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.imgProduct.setImageURI(it)
                if (it != null) {
                    image = it
//                    uploadImage(image!!)
                }
            })

        binding.btnBack.setOnClickListener{
            val intent = Intent(baseContext, ProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }

        binding.btnAdd.setOnClickListener {
            val product = binding.edtProduct.text.toString()
            val price = binding.edtPrice.text.toString()
            val iva = spinner.selectedItem.toString()
            val discount = binding.edtDiscount.text.toString()
            val qrCode = "1234567890"

            if (product.isEmpty() || price.isEmpty() || iva.isEmpty() || discount.isEmpty() || qrCode.isEmpty() || image == null) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                var productData =
                    PRODUCTO(
                        id,
                        product,
                        price.toFloat(),
                        discount.toInt(),
                        iva,
                        qrCode,
                        "",
                        shop
                    )
                if (productData.id.isEmpty()) {
//                    addProduct(productData)
                    uploadImage(image!!, productData)
                    Toast.makeText(this, "¡Producto agregado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                } else {
//                    updateProduct(productData)
                    uploadImage(image!!, productData)
                    Toast.makeText(this, "¡Producto actualizado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                }
                val intent = Intent(baseContext, ProductActivity::class.java)
                startActivity(intent)
            }
        }
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

    private fun addProduct(producto: PRODUCTO) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductDao::class.java)
        val retrofit = retrofitBuilder.addProduct(producto)
        retrofit.enqueue(
            object : Callback<PRODUCTO> {
                override fun onFailure(call: Call<PRODUCTO>, t: Throwable) {
                    Log.d("Agregar", "Error al agregar producto")
                }

                override fun onResponse(call: Call<PRODUCTO>, response: Response<PRODUCTO>) {
                    Log.d("Agregar", "Producto agregado con éxito")
                }
            }
        )
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

                override fun onResponse(call: Call<PRODUCTO>, response: Response<PRODUCTO>) {
                    Log.d("Actualizar", "Datos actualizados")
                }
            }
        )
    }

    @SuppressLint("ResourceType")
    fun initialize() {
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
            Picasso.get().load(product.imagen).error(R.drawable.load).into(binding.imgProduct)
            val productTypeIVA = product.id_categoria_impuesto
            val position = typeIVA.indexOf(productTypeIVA)
            spinner.setSelection(position)

        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtProduct.setText("")
            binding.edtPrice.setText("")
            binding.edtDiscount.setText("")
            binding.imgProduct.setImageResource(load)
        }
        binding.edtProduct.requestFocus()
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage(image_url: Uri, producto: PRODUCTO) {
        progressDialog?.setMessage("Actualizando foto")
        progressDialog?.show()
        var uriTask: Task<Uri>?
        var download_uri = ""
        val rute_storage_photo: String =
            storage_path + " " + photo + System.currentTimeMillis().toString()
        val reference: StorageReference = storageReference.child(rute_storage_photo)
        reference.putFile(image_url).addOnSuccessListener { taskSnapshot ->
            uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask!!.isSuccessful)
                uriTask!!.addOnSuccessListener(OnSuccessListener<Uri> {
//                    download_uri = uri.toString()
//                    val map: HashMap<String, Any> = HashMap()
//                    map["imagen"] = download_uri
//                    mfirestore?.collection("Product")?.document(id)?.update(map)
//
//                    // Agregar el downloadUrl a productData
//                    progressDialog?.dismiss()
                })

                val storageRef = FirebaseStorage.getInstance().getReference(rute_storage_photo)
                storageRef.metadata.addOnSuccessListener { metadata ->

            val storageRef = FirebaseStorage.getInstance().getReference(rute_storage_photo)
            storageRef.metadata.addOnSuccessListener { metadata ->

                if (metadata != null && metadata.sizeBytes > 0) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
//                        Toast.makeText(this, downloadUrl, Toast.LENGTH_LONG).show()
                        producto.imagen = downloadUrl
                        if (producto.id.isEmpty()) {
                            producto.id = System.currentTimeMillis().toString()
                            addProduct(producto)
                        } else {
                            updateProduct(producto)
                        }
                    }.addOnFailureListener { exception ->
                        // Manejar el caso en el que no se pudo obtener la URL de descarga
                        Toast.makeText(
                            this,
                            "Error al obtener la URL de descarga",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog?.dismiss()
                    }
                } else {
                    // Manejar el caso en el que el archivo no existe
                    Toast.makeText(this, "El archivo no existe", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(
                this,
                "Error al cargar foto",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    }
}
