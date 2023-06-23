package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemClientBinding
import dev.android.appfacturador.model.CLIENTE

class ClientAdapter (var clients: List<CLIENTE> = emptyList()) :
    RecyclerView.Adapter<ClientAdapter.ClientAdapterViewHolder>() {
    lateinit var setOnClickClient: (CLIENTE) -> Unit
    inner class ClientAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemClientBinding = ItemClientBinding.bind(itemView)
        fun bind(client: CLIENTE) = with(binding) {
            binding.textView7.text = client.primer_nombre.first().toString() + client.apellido_paterno.first().toString()
            binding.txtClient.text = "${client.primer_nombre} ${client.segundo_nombre} ${client.apellido_paterno} ${client.apellido_materno}"
            binding.txtID.text = client.numero_dni

            itemView.setOnClickListener {
                setOnClickClient(client)
            }
        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClientAdapter.ClientAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ClientAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientAdapterViewHolder, position: Int) {
        val membership: CLIENTE = clients[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return clients.size
    }

    fun updateListClients(clients: List<CLIENTE>) {
        this.clients = clients
        notifyDataSetChanged()
    }

}