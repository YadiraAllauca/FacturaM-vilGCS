package dev.android.appfacturador.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CATEGORIA_IVA(
    @SerializedName("id")
    val id: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("valor")
    val valor: String
) : Serializable // -> convierte al objeto en una secuencia de bites

data class CLIENTE(
    @SerializedName("id")
    val id: String,
    @SerializedName("tipo_dni")
    val tipo_dni: String,
    @SerializedName("numero_dni")
    val numero_dni: String,
    @SerializedName("primer_nombre")
    val primer_nombre: String,
    @SerializedName("segundo_nombre")
    val segundo_nombre: String,
    @SerializedName("apellido_paterno")
    val apellido_paterno: String,
    @SerializedName("apellido_materno")
    val apellido_materno: String,
    @SerializedName("correo_electronico")
    val email: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("direccion")
    val direccion: String,
    @SerializedName("negocio")
    val negocio: String
) : Serializable

data class PRODUCTO(
    @SerializedName("id")
    var id: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("precio")
    val precio: Float,
    @SerializedName("max_descuento")
    val max_descuento: Number,
    @SerializedName("id_categoria_impuesto")
    val id_categoria_impuesto: String,
    @SerializedName("codigo_barras")
    val codigo_barras: String,
    @SerializedName("imagen")
    var imagen: String,
    @SerializedName("negocio")
    val negocio: String
) : Serializable // -> convierte al objeto en una secuencia de bites

data class EMPLEADO(
    @SerializedName("id")
    var id: String,
    @SerializedName("apellido_materno")
    val apellido_materno: String,
    @SerializedName("apellido_paterno")
    val apellido_paterno: String,
    @SerializedName("clave")
    val clave: String,
    @SerializedName("correo_electronico")
    val correo_electronico: String,
    @SerializedName("numero_dni")
    val numero_dni: String,
    @SerializedName("primer_nombre")
    var primer_nombre: String,
    @SerializedName("segundo_nombre")
    var segundo_nombre: String,
    @SerializedName("tipo_dni")
    var tipo_dni: String,
    @SerializedName("tipo_empleado")
    var tipo_empleado: String,
    @SerializedName("negocio")
    var negocio: String
) : Serializable {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "")
}