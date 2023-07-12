package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.ProductHolder.productList
import dev.android.appfacturador.databinding.ActivityBillDetailBinding
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants

class BillDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityBillDetailBinding
    private val adapter: ProductBillAdapter by lazy {
        ProductBillAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: MutableList<ProductHolder.ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillDetailBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        initialize()
        loadData()
        actions()
    }

    fun initialize(){
        val bundle = intent.extras
        bundle?.let{
            val bill = bundle.getSerializable(Constants.KEY_BILL) as FACTURA
            binding.edtNumeroIdentificacion.setText(bill.cliente?.numero_dni.toString())
            binding.txtClienteName.text = bill.cliente?.primer_nombre + " " + bill.cliente?.apellido_paterno
            binding.txtClienteEmail.text = bill.cliente?.correo_electronico
            productList = bill.items!!
            binding.txtSubtotal.text = "$"+String.format("%.2f", bill.subtotal)
            binding.txtIva.text = "$"+String.format("%.2f", bill.iva)
            binding.txtDiscount.text = "$"+String.format("%.2f", bill.descuento)
            binding.spinnerPay.text = bill.forma_pago
            binding.txtTotalBill.text = "$"+String.format("%.2f", bill.total)
        }
    }

    fun loadData(){
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.updateListProducts(productList)
        recyclerView.adapter = adapter

        adapter.areFieldsEnabled = false
        adapter.notifyDataSetChanged()
    }

    fun actions(){
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}