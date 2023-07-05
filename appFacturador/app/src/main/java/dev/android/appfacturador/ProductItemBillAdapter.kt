package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import dev.android.appfacturador.databinding.ItemAddProductBillBinding
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductItemBillAdapter(var products: List<PRODUCTO> = emptyList()) :
    RecyclerView.Adapter<ProductItemBillAdapter.ProductItemBillAdapterViewHolder>() {
    lateinit var selectedItems: MutableList<PRODUCTO>
    lateinit var onCheckedChangeListener: (product: PRODUCTO, isChecked: Boolean) -> Unit

    inner class ProductItemBillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemAddProductBillBinding = ItemAddProductBillBinding.bind(itemView)
        fun bind(product: PRODUCTO) = with(binding) {
            binding.txtProductName.text = product.nombre
            binding.txtPrice.text = "$" + product.precio.toString()
            Picasso.get().load(product.imagen).error(R.drawable.load).into(imgProduct)

            val isProductSelected = ProductHolder.productList.any { it.product.nombre == product.nombre }
            binding.cbItemBill.isChecked = isProductSelected

            binding.cbItemBill.setOnClickListener { view ->
                val isChecked = (view as CheckBox).isChecked
                onCheckedChangeListener(product, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductItemBillAdapter.ProductItemBillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_product_bill, parent, false)
        return ProductItemBillAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductItemBillAdapterViewHolder, position: Int) {
        val membership: PRODUCTO = products[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateListProducts(products: List<PRODUCTO>) {
        this.products = products
        notifyDataSetChanged()
    }

}