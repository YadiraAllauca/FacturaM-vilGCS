package dev.android.appfacturador.model

import android.net.Uri
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.DecimalFormat

data class CATEGORIA_IVA (
    @SerializedName("id")
    val id:String,
    @SerializedName("categoria")
    val categoria:String,
    @SerializedName("valor")
    val valor:String
): Serializable // -> convierte al objeto en una secuencia de bites

data class CLIENTE (
    @SerializedName("id")
    val id:String,
    @SerializedName("tipo_dni")
    val tipo_dni:String,
    @SerializedName("numero_dni")
    val numero_dni:String,
    @SerializedName("primer_nombre")
    val primer_nombre:String,
    @SerializedName("segundo_nombre")
    val segundo_nombre:String,
    @SerializedName("apellido_paterno")
    val apellido_paterno:String,
    @SerializedName("apellido_materno")
    val apellido_materno:String,
    @SerializedName("email")
    val email:String,
    @SerializedName("telefono")
    val telefono:String,
    @SerializedName("direccion")
    val direccion:String
): Serializable

data class PRODUCTO(
    @SerializedName("id")
    var id:String,
    @SerializedName("nombre")
    val nombre:String,
    @SerializedName("precio")
    val precio:Float,
    @SerializedName("max_descuento")
    val max_descuento:Number,
    @SerializedName("id_categoria_impuesto")
    val id_categoria_impuesto:String,
    @SerializedName("codigo_barras")
    val codigo_barras:String,
    @SerializedName("imagen")
    var imagen: String
): Serializable // -> convierte al objeto en una secuencia de bites

data class EMPLEADO(
    @SerializedName("id")
    var id:String,
    @SerializedName("apellidoMaterno")
    val apellidoMaterno:String,
    @SerializedName("apellidoPaterno")
    val apellidoPaterno:String,
    @SerializedName("clave")
    val clave:String,
    @SerializedName("correoElectronico")
    val correoElectronico:String,
    @SerializedName("numeroDni")
    val numeroDni:String,
    @SerializedName("primerNombre")
    var primerNombre: String,
    @SerializedName("segundoNombre")
    var segundoNombre: String,
    @SerializedName("tipoDni")
    var tipoDni: String,
    @SerializedName("tipoEmpleado")
    var tipoEmpleado: String
): Serializable{
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}