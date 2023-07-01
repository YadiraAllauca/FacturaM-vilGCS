package dev.android.appfacturador

import dev.android.appfacturador.model.PRODUCTO

object ProductHolder {
    data class ProductItem(val product: PRODUCTO, var quantity: Int)

    val productList: MutableList<ProductItem> = mutableListOf()

    fun updateQuantity(index: Int, quantity: Int) {
        if (index in 0 until productList.size) {
            productList[index].quantity = quantity
        }
    }

    fun clearList(){
        productList.clear()
    }
}