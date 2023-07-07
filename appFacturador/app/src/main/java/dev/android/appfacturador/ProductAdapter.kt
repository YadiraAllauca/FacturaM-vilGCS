package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductAdapter(var products: List<PRODUCTO> = emptyList()) :
    RecyclerView.Adapter<ProductAdapter.ProductAdapterViewHolder>() {
    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductEdit: (PRODUCTO) -> Unit

    inner class ProductAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemProductBinding = ItemProductBinding.bind(itemView)
        fun bind(product: PRODUCTO) = with(binding) {
            binding.txtProduct.text = product.nombre
            binding.txtPrice.text = "$" + product.precio.toString()
            binding.txtIVA.text = product.id_categoria_impuesto + "%"
            Picasso.get().load(product.imagen).error(R.drawable.load).into(imgProduct)

            binding.btnDeleteProduct.setOnClickListener {
                setOnClickListenerProductDelete(product)
            }
            binding.btnEditProduct.setOnClickListener {
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

}