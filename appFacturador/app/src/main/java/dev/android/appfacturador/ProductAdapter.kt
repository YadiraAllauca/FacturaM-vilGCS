package dev.android.appfacturador

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductAdapter(var products: List<PRODUCTO> = emptyList()) :
    RecyclerView.Adapter<ProductAdapter.ProductAdapterViewHolder>() {
    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductEdit: (PRODUCTO) -> Unit

    inner class ProductAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemProductBinding = ItemProductBinding.bind(itemView)
        fun bind(product: PRODUCTO) = with(binding) {
            txtProduct.text = product.nombre
            txtPrice.text = "$" + product.precio.toString()
            txtIVA.text = product.id_categoria_impuesto + "%"

            if (!product.imagen.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(product.imagen)
                    .override(300, 300) // Establece el tamaño deseado
                    .centerCrop()
                    .placeholder(R.drawable.load)
                    .into(imgProduct)
            }

            val resources = root.resources
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
                btnProductInfo.setCardBackgroundColor(Color.parseColor("#202427"))
                btnEditProduct.setCardBackgroundColor(Color.parseColor("#202427"))
                btnEdit.setColorFilter(Color.parseColor("#ffffff"))
                txtProduct.setTextColor(Color.parseColor("#ffffff"))
                txtPrice.setTextColor(Color.parseColor("#ffffff"))
                btnDeleteProduct.setColorFilter(Color.parseColor("#ffffff"))
                imgProduct.alpha = 0.6f
            }

            btnDeleteProduct.setOnClickListener {
                setOnClickListenerProductDelete(product)
            }
            btnEditProduct.setOnClickListener {
                setOnClickListenerProductEdit(product)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ProductAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductAdapterViewHolder, position: Int) {
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

    fun darkModeProduct (resources: Resources) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark

        }
    }

}