package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import dev.android.appfacturador.databinding.ActivityAddProductBinding

class AddProductActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.imgProduct.setImageURI(it)
            })

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }
    }
}