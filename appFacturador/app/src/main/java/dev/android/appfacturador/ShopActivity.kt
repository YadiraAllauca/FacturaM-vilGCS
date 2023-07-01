package dev.android.appfacturador

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.ProductHolder.productList
import dev.android.appfacturador.databinding.ActivityShopBinding
import dev.android.appfacturador.model.PRODUCTO

class ShopActivity : AppCompatActivity() {
    lateinit var binding: ActivityShopBinding
    private val adapter: ProductShopAdapter by lazy {
        ProductShopAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private var total: Float = 0f
    val productList: MutableList<ProductHolder.ProductItem> = ProductHolder.productList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        recyclerView = binding.rvHistory
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        loadData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Obtener email de usuario
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        Toast.makeText(this, "Valor del email: $email", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        total = productList.sumByDouble { (it.product.precio * it.quantity).toDouble() }.toFloat()

        adapter.updateListProducts(productList)
        recyclerView.adapter = adapter
        updateTotalShop()

        adapter.setOnClickListenerProductDelete = { product ->
            productList.removeAll { it.product == product }
            total = productList.sumByDouble { (it.product.precio * it.quantity).toDouble() }.toFloat()
            adapter.notifyDataSetChanged()
            updateTotalShop()
        }

        adapter.setOnClickListenerProductAdd = { position, quantity ->
            val product = productList[position]
            ProductHolder.updateQuantity(position, quantity)
            total = productList.sumByDouble { (it.product.precio * it.quantity).toDouble() }.toFloat()
            adapter.notifyDataSetChanged()
            updateTotalShop()
        }

        adapter.setOnClickListenerProductQuit = { position, quantity ->
            val product = productList[position]
            ProductHolder.updateQuantity(position, quantity)
            total = productList.sumByDouble { (it.product.precio * it.quantity).toDouble() }.toFloat()
            adapter.notifyDataSetChanged()
            updateTotalShop()
        }

        binding.btnClear.setOnClickListener{
            ProductHolder.productList.clear()
            productList.clear()
            total = productList.sumByDouble { (it.product.precio * it.quantity).toDouble() }.toFloat()
            adapter.notifyDataSetChanged()
            updateTotalShop()
        }
    }

    fun updateTotalShop(){
        binding.txtTotalCarrito.text = "$" + String.format("%.2f", total)
    }
}
