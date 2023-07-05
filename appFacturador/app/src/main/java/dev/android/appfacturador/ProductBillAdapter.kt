package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemBillProductsBinding

class ProductBillAdapter(var products: List<ProductHolder.ProductItem> = emptyList()) :
    RecyclerView.Adapter<ProductBillAdapter.ProductBillAdapterViewHolder>() {

    lateinit var addTextChangedListenerAmount: (Int, Int) -> Unit
    lateinit var addTextChangedListenerDiscount: (Int, Int) -> Unit

    inner class ProductBillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemBillProductsBinding = ItemBillProductsBinding.bind(itemView)
        fun bind(productItem: ProductHolder.ProductItem, position: Int) = with(binding) {
            val product = productItem.product
            val quantity = productItem.quantity
            val discount = productItem.discount

            binding.txtProductNameBill.text = product?.nombre
            if (product != null) {
                binding.txtUnitPrice.text = "$" + String.format("%.2f", product.precio)
            }
            binding.edtAmount.setText(quantity.toString())
            binding.edtDiscount.setText(String.format("%d", discount))
            if (product != null) {
                binding.txtTotal.text = String.format("%.2f", product.precio*quantity)
            }

            binding.edtDiscount.addTextChangedListener {
                val newDiscount = it.toString().toIntOrNull()
                if (newDiscount != null) {
                    addTextChangedListenerDiscount(position, newDiscount)
                }
            }
            binding.edtAmount.addTextChangedListener {
                val updatedQuantity = it.toString().toIntOrNull()
                if(updatedQuantity!=null) {
                    if (product != null) {
                        binding.txtTotal.text = String.format("%.2f", product.precio*updatedQuantity)
                    }
                    addTextChangedListenerAmount(position, updatedQuantity)
                }
            }
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