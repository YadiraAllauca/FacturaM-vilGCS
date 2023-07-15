package dev.android.appfacturador

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.databinding.ActivityClientBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants.Companion.KEY_CLIENT
import dev.android.appfacturador.utils.SpeechToTextUtil

class ClientActivity : AppCompatActivity() {
    lateinit var binding: ActivityClientBinding
    private lateinit var email: String
    private lateinit var shop: String
    private var list: MutableList<CLIENTE> = ArrayList()
    private val adapter: ClientAdapter by lazy {
        ClientAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val instanceFirebase = Firebase.database
    private val db = instanceFirebase.getReference("Cliente")
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        events()
        recyclerView = binding.rvClients
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val usuariosRef = instanceFirebase.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                shop = empleado.negocio
                                loadData()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@ClientActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun events() {
        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "client")
            }
            startActivity(intent)
        }

        binding.btnAddClient.setOnClickListener {
            val intent = Intent(this, AddClientActivity::class.java)
            startActivity(intent)
        }
        adapter.setOnClickClient = {
            val bundle = Bundle().apply {
                putSerializable(KEY_CLIENT, it)
            }
            val intent = Intent(this, AddClientActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }
        binding.btnMicSearch.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this@ClientActivity, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
        search()
    }

    private fun loadData() {
        var listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { child ->
                    val negocio = child.child("negocio").value?.toString()
                    if (negocio == shop) {
                        val client: CLIENTE? =
                            child.key?.let {
                                CLIENTE(
                                    child.key.toString(),
                                    child.child("tipo_dni").value.toString(),
                                    child.child("numero_dni").value.toString(),
                                    child.child("primer_nombre").value.toString(),
                                    child.child("segundo_nombre").value.toString(),
                                    child.child("apellido_paterno").value.toString(),
                                    child.child("apellido_materno").value.toString(),
                                    child.child("correo_electronico").value.toString(),
                                    child.child("telefono").value.toString(),
                                    child.child("direccion").value.toString(),
                                    child.child("negocio").value.toString()
                                )
                            }
                        client?.let { list.add(it) }
                    }
                }
                adapter.updateListClients(list)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        db.addValueEventListener(listen)
    }

    private fun search() = with(binding) {
        edtSearchClient.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(
                filterText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (filterText?.length!! > 0) {
                    val filterList = list.filter { client ->
                        val fullName =
                            "${client.primer_nombre} ${client.segundo_nombre} ${client.apellido_paterno} ${client.apellido_materno}"
                        fullName.uppercase().startsWith(filterText.toString().uppercase()) ||
                                client.numero_dni.uppercase()
                                    .startsWith(filterText.toString().uppercase())
                    }
                    adapter.updateListClients(filterList)
                } else {
                    loadData()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtSearchClient.setText(spokenText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}