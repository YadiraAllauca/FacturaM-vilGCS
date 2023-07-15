package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils.substring
import android.text.TextWatcher
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.ProductHolder.productList
import dev.android.appfacturador.databinding.ActivityBillDetailBinding
import dev.android.appfacturador.databinding.ActivityCreditNoteBinding
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.utils.Constants

class CreditNoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreditNoteBinding
    lateinit var email: String
    lateinit var shop: String
    lateinit var creditNotePriceEditText: EditText
    var price: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditNoteBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.edtClient.isEnabled = false
        binding.edtBill.isEnabled = false

        initialize()
        setupActions()
    }

    fun initialize(){
        val bundle = intent.extras
        bundle?.let{
            val bill = bundle.getSerializable(Constants.KEY_BILL) as FACTURA
            binding.edtBill.setText(bill.numero_factura)
            binding.edtClient.setText(bill.cliente?.numero_dni.toString())
            binding.edtPrice2.setText("$"+String.format("%.2f", bill.total))
            binding.txtSubtotal.text = binding.edtPrice2.text
            binding.txtTotalBill.text = binding.edtPrice2.text
            price = bill.total
        }
    }

    fun setupActions(){
        val dollarSign = "$"
        creditNotePriceEditText = binding.edtPrice2
        creditNotePriceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val priceWithoutDollar = s.toString().substring(1)
                    val newPrice = priceWithoutDollar.toFloatOrNull()

                    if (newPrice != null) {
                        binding.txtSubtotal.text = "$" + String.format("%.2f", newPrice)
                        binding.txtTotalBill.text = "$" + String.format("%.2f", newPrice)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.edtPrice2.setText(dollarSign)
                    binding.edtPrice2.setSelection(binding.edtPrice2.length())
                }
            }
        })

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCreditNote.setOnClickListener {
            val bundle = intent.extras
            val bill = bundle?.getSerializable(Constants.KEY_BILL) as FACTURA
            updateInvoiceStatus(bill.id)
            finish()
        }
    }

    private fun updateInvoiceStatus(invoiceKey: String) {
        val database = FirebaseDatabase.getInstance()
        val invoicesRef = database.getReference("Factura")
        val invoiceRef = invoicesRef.child(invoiceKey)

        invoiceRef.child("estado").setValue("-1")
            .addOnSuccessListener {
                showToast("Factura anulada correctamente")
            }
            .addOnFailureListener { exception ->
                showToast("Error al actualizar la factura: ${exception.message}")
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}