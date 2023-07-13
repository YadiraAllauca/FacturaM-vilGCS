package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemBillBinding
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.model.PRODUCTO

class BillAdapter(var bills: List<FACTURA> = emptyList()) :
    RecyclerView.Adapter<BillAdapter.BillAdapterViewHolder>() {
    lateinit var setOnClickListenerBillEdit: (FACTURA) -> Unit

    inner class BillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemBillBinding = ItemBillBinding.bind(itemView)
        fun bind(bill: FACTURA) = with(binding) {
            binding.txtBillNumber.text = bill.numero_factura
            binding.txtClienteID.text = bill.cliente?.numero_dni
            binding.txtDate.text = bill.fecha

            binding.btnDetalle.setOnClickListener {
                setOnClickListenerBillEdit(bill)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BillAdapter.BillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return BillAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillAdapterViewHolder, position: Int) {
        val membership: FACTURA = bills[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return bills.size
    }

    fun updateListbills(bills: List<FACTURA>) {
        this.bills = bills
        notifyDataSetChanged()
    }

}