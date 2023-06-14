package dev.android.appfacturador

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.database.ProductDao
import dev.android.appfacturador.databinding.ActivityProductBinding
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors


class ProductActivity : AppCompatActivity() {
    lateinit var binding: ActivityProductBinding
    private var list: MutableList<PRODUCTO> = ArrayList()
    private val adapter: ProductAdapter by lazy {
        ProductAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Product")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "product")
            }
            startActivity(intent)
        }

        binding.btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        loadData()
    }

    fun loadData() {
        var listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { child ->
                    val product : PRODUCTO? =
                        child.key?.let {
                            PRODUCTO(
                                child.key.toString(),
                                child.child("nombre").value.toString(),
                                child.child("precio").value.toString().toFloat(),
                                child.child("max_descuento").value.toString().toInt(),
                                child.child("id_categoria_impuesto").value.toString(),
                                child.child("codigo_barras").value.toString(),
                                child.child("imagen").value.toString()
                            )
                        }
                    product?.let { list.add(it) }
                }
                adapter.updateListProducts(list)
                recyclerView.adapter = adapter

                adapter.setOnClickListenerProductDelete = {
                    deleteProduct(it)
                }

                adapter.setOnClickListenerProductEdit = {
                    val bundle  =  Bundle().apply {
                        putSerializable(Constants.KEY_PRODUCT, it)
                    }
                    val intent = Intent(applicationContext, AddProductActivity::class.java).putExtras(bundle)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

    fun deleteProduct(producto: PRODUCTO) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductDao::class.java)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar registro")
        builder.setMessage("¿Desea continuar?")
        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            Executors.newSingleThreadExecutor().execute {
                val retrofit = retrofitBuilder.deleteProduct(producto.id)
                retrofit.enqueue( object : Callback<PRODUCTO> {
                    override fun onResponse(call: Call<PRODUCTO>, response: Response<PRODUCTO>) {}
                    override fun onFailure(call: Call<PRODUCTO>, t: Throwable) {}
                })
                runOnUiThread {
                    Toast.makeText(this,"Registro eliminado", Toast.LENGTH_SHORT).show()
                }
            }
        })
        builder.show()
    }
}