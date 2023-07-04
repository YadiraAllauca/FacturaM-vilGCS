package dev.android.appfacturador

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import dev.android.appfacturador.databinding.ItemBillProductsBinding
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.PRODUCTO
import java.util.prefs.PreferenceChangeListener

class ProductBillAdapter(var products: List<ProductHolder.ProductItem> = emptyList()) :
    RecyclerView.Adapter<ProductBillAdapter.ProductBillAdapterViewHolder>() {

    lateinit var addTextChangedListenerAmount: (Int, Int) -> Unit
    lateinit var addTextChangedListenerDiscount: (Int, Float) -> Unit

    inner class ProductBillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemBillProductsBinding = ItemBillProductsBinding.bind(itemView)
        fun bind(productItem: ProductHolder.ProductItem, position: Int) = with(binding) {
            val product = productItem.product
            val quantity = productItem.quantity

            binding.txtProductNameBill.text = product.nombre
            binding.txtUnitPrice.text = "$" + String.format("%.2f", product.precio * (100-product.id_categoria_impuesto.toFloat()) / 100)
            binding.edtAmount.setText(quantity.toString())
            binding.edtDiscount.setText(String.format("%.1f", product.max_descuento.toFloat())+"%")
            ProductHolder.updateDiscount(position, product.max_descuento.toFloat())
            //String.format("%.2f", product.max_descuento.toFloat())

            binding.edtAmount.addTextChangedListener {
                val updatedQuantity = it.toString().toIntOrNull()
                if(updatedQuantity!=null) {
                    addTextChangedListenerAmount(position, updatedQuantity)
                }
            }

            binding.edtDiscount.addTextChangedListener {
                val newDiscount = it.toString().toFloatOrNull()
                if (newDiscount != null) {
                    addTextChangedListenerDiscount(position, newDiscount)
                }
            }

            /*binding.edtAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newQuantity = s?.toString()?.toIntOrNull()
                    if (newQuantity != null) {
                        ProductHolder.updateQuantity(position, newQuantity)
                        // adapter.notifyDataSetChanged()
                    }
                }
            })

            binding.edtDiscount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newDiscount = s?.toString()?.toFloatOrNull()
                    if (newDiscount != null) {
                        ProductHolder.updateDiscount(position, newDiscount)
                        // adapter.notifyDataSetChanged()
                    }
                }
            })

             */
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductBillAdapter.ProductBillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill_products, parent, false)
        return ProductBillAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductBillAdapterViewHolder, position: Int) {
        val productItem = products[position]
        holder.bind(productItem, position)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateListProducts(products: List<ProductHolder.ProductItem>) {
        this.products = products
        notifyDataSetChanged()
    }



}