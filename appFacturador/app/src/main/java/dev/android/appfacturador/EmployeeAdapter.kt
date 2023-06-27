package dev.android.appfacturador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemClientBinding
import dev.android.appfacturador.databinding.ItemEmployeeBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO

class EmployeeAdapter(var employees: List<EMPLEADO> = emptyList()) :
    RecyclerView.Adapter<EmployeeAdapter.EmployeeAdapterViewHolder>() {
    inner class EmployeeAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemEmployeeBinding = ItemEmployeeBinding.bind(itemView)
        fun bind(employee: EMPLEADO) = with(binding) {
            binding.txtNameLastN.text =
                employee.primer_nombre.first().toString() + employee.apellido_paterno.first()
                    .toString()
            binding.txtEmployee.text =
                "${employee.primer_nombre} ${employee.segundo_nombre} ${employee.apellido_paterno} ${employee.apellido_materno}"
            binding.txtPassword.text = employee.clave
            binding.txtEmail.text = employee.correo_electronico
            if (employee.tipo_empleado.equals("V")) {
                binding.txtType.text = "Vendedor"
            } else {
                binding.txtType.text = "Administrador"
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return EmployeeAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeAdapterViewHolder, position: Int) {
        val membership: EMPLEADO = employees[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return employees.size
    }

    fun updateListEmployees(employees: List<EMPLEADO>) {
        this.employees = employees
        notifyDataSetChanged()
    }

}