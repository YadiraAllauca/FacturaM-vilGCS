package dev.android.appfacturador

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.ProductHolder.productList
import dev.android.appfacturador.databinding.ActivityProductBinding
import dev.android.appfacturador.databinding.ActivityShopBinding
import dev.android.appfacturador.model.PRODUCTO

class ShopActivity : AppCompatActivity()  {
    lateinit var binding: ActivityShopBinding
    lateinit var bindingProductBinding: ActivityProductBinding
    private val adapter: ProductShopAdapter by lazy {
        ProductShopAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private var total: Float = 0f
    private val productList: MutableList<ProductHolder.ProductItem> = ProductHolder.productList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        bindingProductBinding = ActivityProductBinding.inflate(layoutInflater)
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

        binding.btnFacturar.setOnClickListener {
            productList.clear()
            val intent = Intent(this, AddBillActivity::class.java).apply {}
            startActivity(intent)
        }

        // Obtener email de usuario
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        Toast.makeText(this, "Valor del email: $email", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        total = productList.sumByDouble { ((it.product?.precio ?: 0f) * it.quantity).toDouble() }.toFloat()

        adapter.updateListProducts(productList)
        recyclerView.adapter = adapter
        updateTotalShop()

        adapter.setOnClickListenerProductDelete = { product ->
            productList.removeAll { it.product == product }
            updateTotalShop()
        }

        adapter.setOnClickListenerProductAdd = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateTotalShop()
        }

        adapter.setOnClickListenerProductQuit = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateTotalShop()
        }

        binding.btnClear.setOnClickListener{
            bindingProductBinding.imgFull.visibility = View.GONE
            ProductHolder.productList.clear()
            productList.clear()
            updateTotalShop()
        }
    }

    fun updateTotalShop(){
        total = productList.sumByDouble { ((it.product?.precio ?: 0f) * it.quantity).toDouble() }.toFloat()
        adapter.notifyDataSetChanged()
        binding.txtTotalCarrito.text = "$" + String.format("%.2f", total)
    }
}
