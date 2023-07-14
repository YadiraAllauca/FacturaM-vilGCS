package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import dev.android.appfacturador.databinding.ActivityBillDetailBinding
import dev.android.appfacturador.databinding.ActivityCreditNoteBinding

class CreditNoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreditNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditNoteBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.edtClient.isEnabled = false
        binding.edtBill.isEnabled = false
    }
}