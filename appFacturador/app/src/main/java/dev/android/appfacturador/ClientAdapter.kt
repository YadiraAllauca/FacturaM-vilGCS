package dev.android.appfacturador

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemClientBinding
import dev.android.appfacturador.model.CLIENTE

class ClientAdapter(var clients: List<CLIENTE> = emptyList()) :
    RecyclerView.Adapter<ClientAdapter.ClientAdapterViewHolder>() {
    lateinit var setOnClickClient: (CLIENTE) -> Unit

    inner class ClientAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemClientBinding = ItemClientBinding.bind(itemView)
        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(client: CLIENTE) = with(binding) {
            binding.txtInitials.text =
                client.primer_nombre.first().toString() + client.apellido_paterno.first().toString()
            binding.txtClient.text =
                "${client.primer_nombre} ${client.segundo_nombre} ${client.apellido_paterno} ${client.apellido_materno}"
            binding.txtID.text = client.numero_dni

            val resources = root.resources
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
                btnProductInfo.setCardBackgroundColor(Color.parseColor("#121212"))
                txtClient.setTextColor(Color.parseColor("#ffffff"))
                txtInitials.setTextColor(Color.parseColor("#121212"))
                btnContainer.setCardBackgroundColor(Color.parseColor("#47484a"))
                cardProduct.outlineSpotShadowColor = Color.parseColor("#ffffff")
            }

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