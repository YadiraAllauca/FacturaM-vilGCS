package dev.android.appfacturador

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import dev.android.appfacturador.databinding.ActivityProductBinding
import dev.android.appfacturador.model.PRODUCTO

object ProductHolder: BaseObservable() {
    data class ProductItem(val product: PRODUCTO?, var quantity: Int, var discount: Int){
        constructor(): this(null, 0,0)
    }

    val productList: MutableList<ProductItem> = mutableListOf()

    fun updateQuantity(index: Int, quantity: Int) {
        if (index in 0 until productList.size) {
            productList[index].quantity = quantity
        }
    }

    fun updateDiscount(index: Int, discount: Int) {
        if (index in 0 until productList.size) {
            productList[index].discount = discount
        }
    }

    private var itemCount: Int = 0

    fun getItemCount(): Int {
        return itemCount
    }
}