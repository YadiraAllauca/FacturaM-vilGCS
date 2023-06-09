package dev.android.appfacturador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)
        //prueba final git
    }
}