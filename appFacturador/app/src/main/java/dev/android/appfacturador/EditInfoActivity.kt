package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import dev.android.appfacturador.databinding.ActivityEditInfoBinding

class EditInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        darkMode()
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode () {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNI.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNINumber.setTextColor(Color.parseColor("#ffffff"))
            binding.txtEmail.setTextColor(Color.parseColor("#ffffff"))
            binding.txtPhone.setTextColor(Color.parseColor("#ffffff"))
            binding.txtAddress.setTextColor(Color.parseColor("#ffffff"))
            binding.btnMicName.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicIDNumber.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicEmail.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicPhoneNumber.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicAddres.setColorFilter(Color.parseColor("#ffffff"))
            binding.edtName.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtNumDNI.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtEmail.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtPhone.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtAddress.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.btnAdd.setBackgroundResource(dev.android.appfacturador.R.drawable.gradientdark)
            binding.btnAdd.setTextColor(Color.parseColor("#121212"))
        }
    }

    private fun showExitConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Advertencia")
        alertDialogBuilder.setMessage("Todos los cambios se perderán. ¿Desea continuar?")
        alertDialogBuilder.setPositiveButton("Salir") { dialogInterface: DialogInterface, _: Int ->
            // Salir de la aplicación
            finish()
        }
        alertDialogBuilder.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
            // Cancelar la acción de salida
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }
}