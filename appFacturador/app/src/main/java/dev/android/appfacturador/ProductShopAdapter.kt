package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.android.appfacturador.databinding.ItemShopBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductShopAdapter(var products: List<ProductHolder.ProductItem> = emptyList()) :
    RecyclerView.Adapter<ProductShopAdapter.ProductShopAdapterViewHolder>() {

    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductQuit: (Int, Int) -> Unit
    lateinit var setOnClickListenerProductAdd: (Int, Int) -> Unit

    inner class ProductShopAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemShopBinding = ItemShopBinding.bind(itemView)

        fun bind(productItem: ProductHolder.ProductItem, position: Int) = with(binding) {
            val product = productItem.product
            val quantity = productItem.quantity

            binding.txtAmount.setText(quantity.toString())
            binding.txtProductNameShop.text = product?.nombre
            if (product != null) {
                binding.txtPriceShop.text = "$" + String.format("%.2f", product.precio * quantity)
                Picasso.get().load(product.imagen).error(R.drawable.load).into(imgProductShop)
            }

            binding.btnQuitProduct.setOnClickListener {
                if (product != null) {
                    setOnClickListenerProductDelete(product)
                }
            }

            binding.btnLess.setOnClickListener {
                if (quantity > 1) {
                    val updatedQuantity = quantity - 1
                    txtAmount.setText(updatedQuantity.toString())
                    if (product != null) {
                        txtPriceShop.setText("$" + (product.precio * updatedQuantity).toString())
                    }
                    setOnClickListenerProductQuit(position, updatedQuantity)
                }
            }

            binding.btnMore.setOnClickListener {
                val updatedQuantity = quantity + 1
                txtAmount.setText(updatedQuantity.toString())
                if (product != null) {
                    txtPriceShop.setText("$" + (product.precio * updatedQuantity).toString())
                }
                setOnClickListenerProductAdd(position, updatedQuantity)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductShopAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ProductShopAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductShopAdapterViewHolder, position: Int) {
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
