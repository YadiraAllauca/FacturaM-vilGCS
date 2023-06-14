package dev.android.appfacturador

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.PRODUCTO

//1. Definir dónde se manejan los datos
//2. Crear el view holder
//3. Implementar métodos del adaptador
class ProductAdapter(var products: List<PRODUCTO> = emptyList()): RecyclerView.Adapter<ProductAdapter.ProductAdapterViewHolder>() {
    //Funciones para manipular la edición y eliminación de registros
    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductEdit: (PRODUCTO) -> Unit
    private val mfirestore: FirebaseFirestore? = null
    //    lateinit var setOnClickProduct: (PRODUCTO) -> Unit
    //Crear el view holder
    inner class ProductAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private var binding: ItemProductBinding = ItemProductBinding.bind(itemView)
        fun bind(product: PRODUCTO) = with(binding){
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
//            root.setOnClickListener{
//                setOnClickProduct(product)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapter.ProductAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return  ProductAdapterViewHolder(view)
    }

    //esta funcion se va a repetir tantas veces como elementos existen en la lista
    override fun onBindViewHolder(holder: ProductAdapterViewHolder, position: Int) {
        val membership: PRODUCTO = products[position]
        holder.bind(membership)
    }

    //esta funcion cuenta los elementos de la lista
    override fun getItemCount(): Int {
        return products.size
    }

    fun updateListProducts(products: List<PRODUCTO>) {
        this.products = products
        notifyDataSetChanged()
    }

}