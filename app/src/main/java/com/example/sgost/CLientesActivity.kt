package com.example.sgost

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
    // Usamos el documento como clave única (Key)
    private val clientesDB = mutableMapOf<String, Cliente>()
    private var documentoEditando: String? = null

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
        btnGuardar.setOnClickListener { guardarCliente() }
        btnBuscar.setOnClickListener { buscarCliente() }
        btnEditar.setOnClickListener { editarCliente() }
        btnEliminar.setOnClickListener { eliminarCliente() }

        // Cancelar modo edición si el usuario cambia el documento manualmente
        txtTipoDocumento.doAfterTextChanged {
            if (documentoEditando != null && !txtTipoDocumento.text.isNullOrBlank()) {
                documentoEditando = null
                Toast.makeText(this@ClientesActivity, "Modo edición cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarCliente() {
        val documento = txtTipoDocumento.text.toString().trim()
        val nombre = txtNombre.text.toString().trim()
        val correo = txtCorreo.text.toString().trim()
        val tipoDocumento = txtTipoDocumento.text.toString().trim()
        val telefono = txtTelefono.text.toString().trim()

        if (documento.isEmpty() || nombre.isEmpty() || correo.isEmpty() || tipoDocumento.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "⚠️ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Si estamos editando y el documento cambió, eliminamos el registro antiguo
        if (documentoEditando != null && documentoEditando != documento) {
            clientesDB.remove(documentoEditando)
            documentoEditando = null
        }

        // Guardar o actualizar
        clientesDB[documento] = Cliente(documento, nombre, correo, tipoDocumento, telefono)
        Toast.makeText(this, "✅ Cliente guardado correctamente", Toast.LENGTH_SHORT).show()
        limpiarCampos()
    }

    private fun buscarCliente() {
        val documento = txtTipoDocumento.text.toString().trim()
        if (documento.isEmpty()) {
            Toast.makeText(this, "🔍 Ingresa un documento para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val cliente = clientesDB[documento]
        if (cliente != null) {
            txtNombre.setText(cliente.nombre)
            txtCorreo.setText(cliente.correo)
            txtTipoDocumento.setText(cliente.tipoDocumento)
            txtTelefono.setText(cliente.telefono) // Corregido a minúscula
            documentoEditando = documento
            Toast.makeText(this, "Cliente encontrado. Puedes editar y guardar.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "❌ Cliente no encontrado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            documentoEditando = null
        }
    }

    private fun editarCliente() {
        val documento = txtTipoDocumento.text.toString().trim()
        if (documento.isEmpty()) {
            Toast.makeText(this, "Primero busca un cliente o ingresa un documento", Toast.LENGTH_SHORT).show()
            return
        }
        val cliente = clientesDB[documento]
        if (cliente != null) {
            documentoEditando = documento
            Toast.makeText(this, "Modo edición activado. Modifica los datos y presiona GUARDAR.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "No existe un cliente con ese documento. Usa BUSCAR primero.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarCliente() {
        val documento = txtTipoDocumento.text.toString().trim()
        if (documento.isEmpty()) {
            Toast.makeText(this, "Ingresa el documento del cliente a eliminar", Toast.LENGTH_SHORT).show()
            return
        }
        val eliminado = clientesDB.remove(documento)
        if (eliminado != null) {
            Toast.makeText(this, "🗑️ Cliente eliminado: ${eliminado.nombre}", Toast.LENGTH_SHORT).show() // Corregido a minúscula
            limpiarCampos()
            documentoEditando = null
        } else {
            Toast.makeText(this, "No se encontró cliente con ese documento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        txtTipoDocumento.text.clear()
        txtNombre.text.clear()
        txtCorreo.text.clear()
        txtTelefono.text.clear()
    }

    // Clase de datos que coincide con tu esquema de base de datos
    data class Cliente(
        val documento: String,       // Equivale a ID o Número de Documento
        val nombre: String,          // Columna: Nombre
        val correo: String,          // Columna: Correo
        val tipoDocumento: String,   // Columna: TipoDocumento (ej: CC, TI)
        val telefono: String         // Columna: Telefono
    )
}