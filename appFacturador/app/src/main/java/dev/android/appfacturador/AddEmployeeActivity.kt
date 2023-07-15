package dev.android.appfacturador

import android.R
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.database.EmployeeDao
import dev.android.appfacturador.databinding.ActivityAddEmployeeBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.SpeechToTextUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddEmployeeActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddEmployeeBinding
    private lateinit var email: String
    private var oldEmailEdit: String = ""
    private lateinit var shop: String
    private var id = ""
    private lateinit var spinnerDNI: Spinner
    private lateinit var spinnerType: Spinner
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1
    private val REQUEST_CODE_SPEECH_TO_TEXT2 = 2
    private val REQUEST_CODE_SPEECH_TO_TEXT3 = 3
    private val REQUEST_CODE_SPEECH_TO_TEXT4 = 4
    private val REQUEST_CODE_SPEECH_TO_TEXT5 = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEmployeeBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initialize()
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        events()

    }

    private fun initialize() {
        spinnerDNI = binding.spnDNI
        val adapterDNI = ArrayAdapter(this, R.layout.simple_spinner_item, Constants.TYPE_DNI)
        adapterDNI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDNI.adapter = adapterDNI
        spinnerType = binding.spnType
        val adapterType = ArrayAdapter(this, R.layout.simple_spinner_item, Constants.TYPE_EMPLOYEE)
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapterType
        val bundle = intent.extras
        bundle?.let {
            val employee = it.getSerializable(Constants.KEY_EMPLOYEE) as EMPLEADO
            id = employee.id
            binding.textView8.text =
                employee.primer_nombre.first().toString() + employee.apellido_paterno.first()
                    .toString()
            binding.edtNameEmployee.setText(employee.primer_nombre + " " + employee.segundo_nombre)
            binding.edtLastNameEmployee.setText(employee.apellido_paterno + " " + employee.apellido_materno)
            binding.edtNumDNI.setText(employee.numero_dni)
            binding.edtEmailEmployee.setText(employee.correo_electronico)
            this.oldEmailEdit = employee.correo_electronico
            binding.edtPasswordEmployee.setText(employee.clave)
            val employeeTypeDNI = employee.tipo_dni
            val positionDNI = Constants.TYPE_DNI.indexOf(employeeTypeDNI)
            spinnerDNI.setSelection(positionDNI)
            var employeeType = ""
            if (employee.tipo_empleado.equals("V")) {
                employeeType = "Vendedor"
            } else {
                employeeType = "Administrador"
            }
            val positionType = Constants.TYPE_DNI.indexOf(employeeType)
            spinnerType.setSelection(positionType)
            binding.btnAdd.text = "ACTUALIZAR"

        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtNameEmployee.setText("")
            binding.edtLastNameEmployee.setText("")
            binding.edtNumDNI.setText("")
            binding.edtEmailEmployee.setText("")
            binding.edtPasswordEmployee.setText("")
        }
        binding.edtNameEmployee.requestFocus()
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                shop = empleado.negocio
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddEmployeeActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun events() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener {
            var typeEmployee = "V"
            if (spinnerType.selectedItem.toString().equals("Administrador")) {
                typeEmployee = "A"
            }
            val typeDNI = spinnerDNI.selectedItem.toString()
            val numberDNI = binding.edtNumDNI.text.toString()
            val fullName = (binding.edtNameEmployee.text.toString()).split(" ")
            val firstName = fullName[0].toLowerCase().capitalize()
            val secondName = fullName[1].toLowerCase().capitalize()
            val fullLastName = (binding.edtLastNameEmployee.text.toString()).split(" ")
            val firstLastName = fullLastName[0].toLowerCase().capitalize()
            val secondLastName = fullLastName[1].toLowerCase().capitalize()
            val email = binding.edtEmailEmployee.text.toString()
            val password = binding.edtPasswordEmployee.text.toString()
            if (fullName.isEmpty() || fullLastName.isEmpty() || numberDNI.isEmpty() ||
                email.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(
                    this,
                    "La clave debe ser de al menos 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var employeeData =
                    EMPLEADO(
                        id,
                        secondLastName,
                        firstLastName,
                        password,
                        email,
                        numberDNI,
                        firstName,
                        secondName,
                        typeDNI, typeEmployee,
                        shop
                    )
                if (employeeData.id.isEmpty()) {
                    addEmployee(employeeData) { employee ->
                        if (employee) {
                            Toast.makeText(
                                this,
                                "¡Empleado agregado exitosamente!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, EmployeeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this,
                                "Correo ya registrado, ingrese uno nuevo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    updateEmployee(employeeData) { employee ->
                        if (employee) {
                            setResult(RESULT_OK)
                            Toast.makeText(this, "Actualizando datos...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, EmployeeActivity::class.java)
                            Handler().postDelayed({
                                startActivity(intent)
                            }, 2000)
                        } else {
                            Toast.makeText(
                                this,
                                "Correo ya registrado, ingrese uno nuevo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }
        eventsMicro()
    }

    private fun eventsMicro() {
        binding.btnMicEmployeeNames.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
        binding.btnMicEmployeeLastNames.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT2)
        }
        binding.btnMicIDNumber.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT3)
        }
        binding.btnMicEmail.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT4)
        }
        binding.btnMicPassword.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT5)
        }
    }

    private fun validateEmail(empleado: EMPLEADO, callback: (Boolean) -> Unit) {
        if (this.oldEmailEdit != empleado.correo_electronico) {
            val auth = FirebaseAuth.getInstance()
            auth.fetchSignInMethodsForEmail(empleado.correo_electronico)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        val validated = signInMethods == null || signInMethods.isEmpty()
                        callback(validated)
                    } else {
                        Log.d("Agregar", "Error al verificar el correo electrónico")
                        callback(false)
                    }

                }
        } else {
            callback(true)
        }
    }

    private fun addUser(empleado: EMPLEADO) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(empleado.correo_electronico, empleado.clave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Agregar", "Usuario agregado con éxito")
                } else {
                    Log.d("Agregar", "Error al agregar usuario")
                }
            }
    }


    private fun addEmployee(empleado: EMPLEADO, callback: (Boolean) -> Unit) {
        validateEmail(empleado) { validated ->
            if (validated) {
                val retrofitBuilder = Retrofit.Builder()
                    .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(EmployeeDao::class.java)
                val retrofit = retrofitBuilder.addEmployee(empleado)
                retrofit.enqueue(
                    object : Callback<EMPLEADO> {
                        override fun onFailure(call: Call<EMPLEADO>, t: Throwable) {
                            Log.d("Agregar", "Error al agregar empleado")
                            callback(false)
                        }

                        override fun onResponse(
                            call: Call<EMPLEADO>,
                            response: Response<EMPLEADO>
                        ) {
                            addUser(empleado)
                            Log.d("Agregar", "Empleado agregado con éxito")
                            callback(true)
                        }
                    }
                )
            } else {
                callback(false)
            }
        }
    }

    private fun updateEmployee(empleado: EMPLEADO, callback: (Boolean) -> Unit) {
        validateEmail(empleado) { validated ->
            if (validated) {
                val retrofitBuilder = Retrofit.Builder()
                    .baseUrl("https://appfacturador-b516d-default-rtdb.firebaseio.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(EmployeeDao::class.java)
                val retrofit = retrofitBuilder.updateEmployee(empleado.id, empleado)
                retrofit.enqueue(
                    object : Callback<EMPLEADO> {
                        override fun onFailure(call: Call<EMPLEADO>, t: Throwable) {
                            Log.d("Agregar", "Error al agregar empleado")
                            callback(false)
                        }

                        override fun onResponse(
                            call: Call<EMPLEADO>,
                            response: Response<EMPLEADO>
                        ) {
                            addUser(empleado)
                            Log.d("Agregar", "Empleado agregado con éxito")
                            callback(true)
                        }
                    }
                )
            } else {
                callback(false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtNameEmployee.setText(spokenText)
                    }
                }
                REQUEST_CODE_SPEECH_TO_TEXT2 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtLastNameEmployee.setText(spokenText)
                    }
                }
                REQUEST_CODE_SPEECH_TO_TEXT3 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtNumDNI.setText(filteredText)
                    }
                }
                REQUEST_CODE_SPEECH_TO_TEXT4 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtEmailEmployee.setText(filteredText)
                    }
                }
                REQUEST_CODE_SPEECH_TO_TEXT5 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtPasswordEmployee.setText(filteredText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
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