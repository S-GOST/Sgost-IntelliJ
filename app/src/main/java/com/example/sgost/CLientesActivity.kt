package com.example.sgost

// ✅ IMPORTACIÓN CRÍTICA FALTANTE
import com.example.sgost.model.Cliente

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged

class ClientesActivity : AppCompatActivity() {

    // Referencias a los campos
    private lateinit var txtNombre: EditText
    private lateinit var txtCorreo: EditText
    private lateinit var txtTipoDocumento: EditText
    private lateinit var txtTelefono: EditText

    private lateinit var btnGuardar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button

    // Base de datos temporal en memoria (simulación)
    // Usamos TipoDocumento como clave única (Key)
    private val registrosDB = mutableMapOf<String, Cliente>()
    private var tipoDocEditando: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        // Inicializar vistas
        txtNombre = findViewById(R.id.txtNombre)
        txtCorreo = findViewById(R.id.txtCorreo)
        txtTipoDocumento = findViewById(R.id.txtTipoDocumento)
        txtTelefono = findViewById(R.id.txtTelefono)

        btnGuardar = findViewById(R.id.btnGuardar)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnEditar = findViewById(R.id.btnEditar)
        btnEliminar = findViewById(R.id.btnEliminar)

        // Configurar acciones
        btnGuardar.setOnClickListener { guardarRegistro() }
        btnBuscar.setOnClickListener { buscarRegistro() }
        btnEditar.setOnClickListener { editarRegistro() }
        btnEliminar.setOnClickListener { eliminarRegistro() }

        // Cancelar modo edición si el usuario cambia el documento manualmente
        txtTipoDocumento.doAfterTextChanged {
            if (tipoDocEditando != null && !txtTipoDocumento.text.isNullOrBlank()) {
                tipoDocEditando = null
                Toast.makeText(this@ClientesActivity, "Modo edición cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarRegistro() {
        val nombre = txtNombre.text.toString().trim()
        val correo = txtCorreo.text.toString().trim()
        val tipoDocumento = txtTipoDocumento.text.toString().trim()
        val telefono = txtTelefono.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || tipoDocumento.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "⚠️ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Si estamos editando y el TipoDocumento cambió, eliminamos el registro antiguo
        if (tipoDocEditando != null && tipoDocEditando != tipoDocumento) {
            registrosDB.remove(tipoDocEditando)
            tipoDocEditando = null
        }

        // ✅ CORREGIDO: Solo 4 parámetros, coincidiendo con tu data class
        registrosDB[tipoDocumento] = Cliente(nombre, correo, tipoDocumento, telefono)
        Toast.makeText(this, "✅ Registro guardado correctamente", Toast.LENGTH_SHORT).show()
        limpiarCampos()
    }

    private fun buscarRegistro() {
        val tipoDocumento = txtTipoDocumento.text.toString().trim()
        if (tipoDocumento.isEmpty()) {
            Toast.makeText(this, "🔍 Ingresa un TipoDocumento para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val registro = registrosDB[tipoDocumento]
        if (registro != null) {
            txtNombre.setText(registro.Nombre)
            txtCorreo.setText(registro.Correo)
            txtTipoDocumento.setText(registro.TipoDocumento)
            txtTelefono.setText(registro.Telefono)
            tipoDocEditando = tipoDocumento
            Toast.makeText(this, "Registro encontrado. Puedes editar y guardar.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "❌ Registro no encontrado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            tipoDocEditando = null
        }
    }

    private fun editarRegistro() {
        val tipoDocumento = txtTipoDocumento.text.toString().trim()
        if (tipoDocumento.isEmpty()) {
            Toast.makeText(this, "Primero busca un registro o ingresa un TipoDocumento", Toast.LENGTH_SHORT).show()
            return
        }
        val registro = registrosDB[tipoDocumento]
        if (registro != null) {
            tipoDocEditando = tipoDocumento
            Toast.makeText(this, "Modo edición activado. Modifica los datos y presiona GUARDAR.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "No existe un registro con ese TipoDocumento. Usa BUSCAR primero.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarRegistro() {
        val tipoDocumento = txtTipoDocumento.text.toString().trim()
        if (tipoDocumento.isEmpty()) {
            Toast.makeText(this, "Ingresa el TipoDocumento del registro a eliminar", Toast.LENGTH_SHORT).show()
            return
        }
        val eliminado = registrosDB.remove(tipoDocumento)
        if (eliminado != null) {
            Toast.makeText(this, "🗑️ Registro eliminado: ${eliminado.Nombre}", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            tipoDocEditando = null // ✅ CORREGIDO: antes decía TipoDocumentoEditando
        } else {
            Toast.makeText(this, "No se encontró registro con ese TipoDocumento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        txtNombre.text.clear()
        txtCorreo.text.clear()
        txtTipoDocumento.text.clear()
        txtTelefono.text.clear()
    }
}