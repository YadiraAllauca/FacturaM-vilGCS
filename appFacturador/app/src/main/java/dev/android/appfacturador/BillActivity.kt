package dev.android.appfacturador

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.core.content.ContextCompat
import dev.android.appfacturador.databinding.ActivityBillBinding

class BillActivity : AppCompatActivity() {
    lateinit var binding: ActivityBillBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.btnFilters.setOnClickListener {
            val intent = Intent(this, FilterBillActivity::class.java)
            startActivity(intent)
        }

        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "bill")
            }
            startActivity(intent)
        }

        binding.btnAddBill.setOnClickListener {
            val intent = Intent(this, AddBillActivity::class.java)
            startActivity(intent)
        }

        binding.btnCanceledBills.background = getDrawable(R.drawable.degradado2)
        binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))

        binding.btnAllBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.degradado2)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#686868"))
            binding.btnAllBills.background = getDrawable(R.drawable.degradado)
            binding.btnAllBills.setTextColor(Color.parseColor("#ffffff"))
        }

        binding.btnCanceledBills.setOnClickListener {
            binding.btnCanceledBills.background = getDrawable(R.drawable.degradado)
            binding.btnCanceledBills.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllBills.background = getDrawable(R.drawable.degradado2)
            binding.btnAllBills.setTextColor(Color.parseColor("#686868"))
        }
    }
}