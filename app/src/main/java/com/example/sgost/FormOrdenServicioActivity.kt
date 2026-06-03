package com.example.sgost

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.DetalleOrdenAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemSpinner(val displayName: String, val id: Int)

class FormOrdenServicioActivity : AppCompatActivity() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerMotos: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnAgregarDetalle: MaterialButton
    private lateinit var btnNuevoCliente: MaterialButton
    private lateinit var btnNuevaMoto: MaterialButton
    private lateinit var rvOrdenDetalles: RecyclerView

    private lateinit var detalleAdapter: DetalleOrdenAdapter
    private var listaDetalles: MutableList<Detalles_orden_servicio> = mutableListOf()

    private var listaClientes = emptyList<ItemSpinner>()
    private var listaMotos = emptyList<ItemSpinner>()
    private var listaServicios = emptyList<ItemSpinner>()
    private var listaProductos = emptyList<ItemSpinner>()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_orden_servicio)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupRecyclerView()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        spinnerClientes = findViewById(R.id.spinnerClientes) ?: throw IllegalStateException("Falta spinnerClientes")
        spinnerMotos = findViewById(R.id.spinnerMotos) ?: throw IllegalStateException("Falta spinnerMotos")
        spinnerEstado = findViewById(R.id.spinnerEstado) ?: throw IllegalStateException("Falta spinnerEstado")
        btnGuardar = findViewById(R.id.btnGuardar) ?: throw IllegalStateException("Falta btnGuardar")
        btnAgregarDetalle = findViewById(R.id.btnAgregarDetalle) ?: throw IllegalStateException("Falta btnAgregarDetalle")
        btnNuevoCliente = findViewById(R.id.btnNuevoCliente) ?: throw IllegalStateException("Falta btnNuevoCliente")
        btnNuevaMoto = findViewById(R.id.btnNuevaMoto) ?: throw IllegalStateException("Falta btnNuevaMoto")
        rvOrdenDetalles = findViewById(R.id.rvOrdenDetalles) ?: throw IllegalStateException("Falta rvOrdenDetalles")
    }

    private fun setupRecyclerView() {
        rvOrdenDetalles.layoutManager = LinearLayoutManager(this)
        detalleAdapter = DetalleOrdenAdapter()
        rvOrdenDetalles.adapter = detalleAdapter
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val clientesJob = async { ApiAndroid.apiService.obtenerClientes() }
                val motosJob = async { ApiAndroid.apiService.obtenerMotos() }
                val serviciosJob = async { ApiAndroid.apiService.obtenerServicios() }
                val productosJob = async { ApiAndroid.apiService.obtenerProductos() }

                val clientes = clientesJob.await().data.orEmpty()
                val motos = motosJob.await().data.orEmpty()
                val servicios = serviciosJob.await().data.orEmpty()
                val productos = productosJob.await().data.orEmpty()

                // Mapeo seguro a ItemSpinner (ajusta los nombres de propiedades si tu data class los tiene diferentes)
                listaClientes = clientes.map { ItemSpinner(it.nombre ?: "Cliente", it.id ?: -1) }
                listaMotos = motos.map { ItemSpinner("${it.modelo ?: "Moto"} (${it.placa ?: ""})", it.idMotos ?: -1) }
                listaServicios = servicios.map { ItemSpinner(it.nombre ?: "Servicio", it.idServicios ?: -1) }
                listaProductos = productos.map { ItemSpinner("${it.marca ?: "Producto"} ${it.nombre ?: ""}".trim(), it.idProductos ?: -1) }

                setupSpinner(spinnerClientes, listaClientes)
                setupSpinner(spinnerMotos, listaMotos)

                val estados = listOf("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA")
                setupSpinner(spinnerEstado, estados.map { ItemSpinner(it, -1) })
                spinnerEstado.setSelection(0)

            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, lista: List<ItemSpinner>) {
        val nombres = lista.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener { finish() }
        btnAgregarDetalle.setOnClickListener { mostrarModalSeleccionDB() }
        btnNuevoCliente.setOnClickListener { mostrarModalNuevoCliente() }
        btnNuevaMoto.setOnClickListener { mostrarModalNuevaMoto() }
        btnGuardar.setOnClickListener { guardarOrdenCompleta() }
    }

    // 🆕 MODAL NUEVO CLIENTE (API REAL)
    private fun mostrarModalNuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("👤 Registrar Nuevo Cliente")

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        fun addField(label: String, hint: String, inputType: Int = InputType.TYPE_CLASS_TEXT, isPass: Boolean = false) = EditText(this@FormOrdenServicioActivity).apply {
            layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                text = label; setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
            })
            this.hint = hint
            this.inputType = if (isPass) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else inputType
            setTextColor(Color.WHITE); setPadding(0, 4, 0, 4)
            layout.addView(this)
        }

        val etUbicacion = addField("📍 Ubicación:", "Ej: Bogotá")
        val etNombre = addField("👤 Nombre completo:", "Ej: Carlos Gómez")
        val etUsuario = addField("🔑 Usuario:", "Ej: carlos99")
        val etPassword = addField("🔒 Contraseña:", "Mínimo 6 caracteres", isPass = true)
        val etTipoDoc = addField("🆔 Tipo Documento:", "Ej: CC, TI, CE")
        val etCorreo = addField("📧 Correo:", "Ej: carlos@email.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val etTelefono = addField("📱 Teléfono:", "3001234567", InputType.TYPE_CLASS_PHONE)

        scrollView.addView(layout)
        builder.setView(scrollView)

        builder.setPositiveButton("REGISTRAR") { _, _ ->
            if (etNombre.text.isNullOrBlank() || etPassword.text.isNullOrBlank()) {
                Toast.makeText(this@FormOrdenServicioActivity, "Nombre y Contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            lifecycleScope.launch {
                try {
                    val cliente = Cliente(
                        id = null,
                        ubicacion = etUbicacion.text.toString().trim(),
                        nombre = etNombre.text.toString().trim(),
                        usuario = etUsuario.text.toString().trim(),
                        contrasena = etPassword.text.toString().trim(),
                        tipoDocumento = etTipoDoc.text.toString().trim(),
                        correo = etCorreo.text.toString().trim(),
                        telefono = etTelefono.text.toString().trim()
                    )

                    val response = ApiAndroid.apiService.registrarCliente(cliente)

                    Log.d("API_CLIENTE", " Cód: ${response.code()} | HTTP OK: ${response.isSuccessful}")
                    Log.d("API_CLIENTE", "📦 Body: ${response.body()}")

                    if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                        val nuevoCliente = response.body()!!.data
                        if (nuevoCliente != null) {
                            listaClientes = listaClientes + ItemSpinner(nuevoCliente.nombre ?: "Cliente", nuevoCliente.id ?: -1)
                            setupSpinner(spinnerClientes, listaClientes)
                            spinnerClientes.setSelection(spinnerClientes.count - 1)
                            Toast.makeText(this@FormOrdenServicioActivity, "✅ Cliente registrado exitosamente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "⚠️ Error: El servidor no devolvió los datos del cliente", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // 🛡️ LEER ERRORBODY UNA SOLA VEZ (consume el stream)
                        val errorRaw = response.errorBody()?.string() ?: response.body()?.message ?: "Error desconocido"

                        val mensajeUsuario = when {
                            errorRaw.contains("Duplicate entry", ignoreCase = true) ->
                                "❌ El nombre de usuario ya está registrado. Por favor usa otro."
                            errorRaw.contains("correo", ignoreCase = true) ->
                                "❌ El correo electrónico ya está en uso."
                            errorRaw.contains("contrasena", ignoreCase = true) ->
                                "❌ La contraseña no cumple los requisitos de seguridad."
                            else ->
                                "❌ Error del servidor: $errorRaw"
                        }
                        Toast.makeText(this@FormOrdenServicioActivity, mensajeUsuario, Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("API_CLIENTE", "💥 Excepción capturada: ${e.message}", e)
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // 🆕 MODAL NUEVA MOTO (CORREGIDO)
    private fun mostrarModalNuevaMoto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🏍️ Registrar Nueva Moto")

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        layout.addView(TextView(this@FormOrdenServicioActivity).apply {
            text = "👤 Dueño (Cliente):"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
        })
        val spClienteMoto = Spinner(this@FormOrdenServicioActivity).apply {
            setPadding(0, 4, 0, 4)
            adapter = ArrayAdapter(this@FormOrdenServicioActivity, android.R.layout.simple_spinner_dropdown_item, listaClientes.map { it.displayName })
        }
        layout.addView(spClienteMoto)

        fun addField(label: String, hint: String, inputType: Int = InputType.TYPE_CLASS_TEXT) = EditText(this@FormOrdenServicioActivity).apply {
            layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                text = label; setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
            })
            this.hint = hint; this.inputType = inputType; setTextColor(Color.WHITE); setPadding(0, 4, 0, 4)
            layout.addView(this)
        }

        val etPlaca = addField("🔢 Placa:", "Ej: BGT657")
        val etModelo = addField("🏷️ Modelo:", "Ej: 390")
        val etMarca = addField("🏢 Marca:", "Ej: KTM, Honda")
        val etRecorrido = addField("🛣️ Recorrido (km):", "0", InputType.TYPE_CLASS_NUMBER)

        scrollView.addView(layout)
        builder.setView(scrollView)

        builder.setPositiveButton("REGISTRAR") { _, _ ->
            if (etPlaca.text.isNullOrBlank() || etModelo.text.isNullOrBlank()) {
                Toast.makeText(this@FormOrdenServicioActivity, "Placa y Modelo son obligatorios", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            lifecycleScope.launch {
                try {
                    val idCliente = listaClientes.getOrNull(spClienteMoto.selectedItemPosition)?.id ?: 0
                    val moto = Moto(
                        idMotos = null, // Correcto: idMoto (singular)
                        idClientes = idCliente,
                        placa = etPlaca.text.toString().trim(),
                        modelo = etModelo.text.toString().trim(),
                        marca = etMarca.text.toString().trim(),
                        recorrido = etRecorrido.text.toString().toDoubleOrNull() ?: 0.0
                    )

                    // 1. LLAMADA A LA API
                    // IMPORTANTE: crearMoto devuelve ApiResponse<Moto> DIRECTAMENTE, no un Response<...>
                    val response = ApiAndroid.apiService.crearMoto(moto)

                    Log.d("API_MOTO", "Success: ${response.success} | Data: ${response.data}")

                    // 2. VALIDACIÓN DIRECTA (Sin .body() ni .isSuccessful)
                    if (response.success) {
                        // Si el backend no devuelve la moto completa (data es null), usamos la que acabamos de crear
                        val nuevaMoto = response.data ?: Moto(
                            idMotos = (listaMotos.maxOfOrNull { it.id } ?: 0) + 1, // Generar ID local temporal
                            idClientes = moto.idClientes,
                            placa = moto.placa,
                            modelo = moto.modelo,
                            marca = moto.marca,
                            recorrido = moto.recorrido
                        )

                        listaMotos = listaMotos + ItemSpinner("${nuevaMoto.modelo} (${nuevaMoto.placa})", nuevaMoto.idMotos ?: -1)
                        setupSpinner(spinnerMotos, listaMotos)
                        spinnerMotos.setSelection(spinnerMotos.count - 1)
                        Toast.makeText(this@FormOrdenServicioActivity, "✅ Moto registrada", Toast.LENGTH_LONG).show()
                    } else {
                        // Manejo de errores del backend
                        val errorMsg = response.message ?: response.message ?: "Error desconocido"
                        Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("API_MOTO", "Error de red", e)
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun ApiResponse<Moto>.code(): String {
        return TODO("Provide the return value")
    }

    private fun mostrarModalSeleccionDB() {
        if (listaServicios.isEmpty() && listaProductos.isEmpty()) {
            Toast.makeText(this@FormOrdenServicioActivity, "⚠️ Aún no hay servicios o productos en la base de datos", Toast.LENGTH_LONG).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("📦 Agregar desde Base de Datos")
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        val labelServ = TextView(this).apply { text = "🔧 Servicio:"; setTextColor(Color.WHITE) }
        inputLayout.addView(labelServ)
        val spServicio = Spinner(this).apply {
            setPadding(0, 10, 0, 10)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            adapter = ArrayAdapter(this@FormOrdenServicioActivity, android.R.layout.simple_spinner_dropdown_item, listaServicios.map { it.displayName })
        }
        inputLayout.addView(spServicio)

        val labelProd = TextView(this).apply {
            text = "📦 Producto (Opcional):"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelProd)
        val spProducto = Spinner(this).apply {
            setPadding(0, 10, 0, 10)
            adapter = ArrayAdapter(this@FormOrdenServicioActivity, android.R.layout.simple_spinner_dropdown_item, listaProductos.map { it.displayName })
        }
        inputLayout.addView(spProducto)

        val labelPrecio = TextView(this).apply {
            text = "💰 Precio:"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelPrecio)
        val etPrecio = EditText(this).apply {
            hint = "0.00"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE)
        }
        inputLayout.addView(etPrecio)

        val labelGarantia = TextView(this).apply {
            text = "🛡️ Garantía (días):"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelGarantia)
        val etGarantia = EditText(this).apply { hint = "0"; inputType = InputType.TYPE_CLASS_NUMBER; setTextColor(Color.WHITE) }
        inputLayout.addView(etGarantia)

        builder.setView(inputLayout)
        builder.setPositiveButton("➕ Agregar") { _, _ ->
            val posServ = spServicio.selectedItemPosition
            val posProd = spProducto.selectedItemPosition
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val garantia = etGarantia.text.toString().toIntOrNull() ?: 0

            if (posServ < 0 && posProd < 0) {
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona al menos un servicio o producto", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (posServ >= 0) {
                val sel = listaServicios[posServ]
                listaDetalles.add(Detalles_orden_servicio(
                    idDetalleOrden = null, idOrden = null, idServicios = sel.id, idProductos = null,
                    nombreServicio = sel.displayName, nombreProducto = null, precio = precio, garantia = garantia
                ))
            }
            if (posProd >= 0) {
                val sel = listaProductos[posProd]
                listaDetalles.add(Detalles_orden_servicio(
                    idDetalleOrden = null, idOrden = null, idServicios = null, idProductos = sel.id,
                    nombreServicio = null, nombreProducto = sel.displayName, precio = precio, garantia = garantia
                ))
            }
            detalleAdapter.submitList(listaDetalles.toList())
            Toast.makeText(this@FormOrdenServicioActivity, "✅ Agregado a la orden", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun guardarOrdenCompleta() {
        val posCliente = spinnerClientes.selectedItemPosition
        val posMoto = spinnerMotos.selectedItemPosition
        if (posCliente < 0 || posMoto < 0) {
            Toast.makeText(this, "❌ Selecciona Cliente y Moto", Toast.LENGTH_SHORT).show()
            return
        }
        if (listaDetalles.isEmpty()) {
            Toast.makeText(this, "⚠️ Agrega al menos un servicio o producto", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val idCliente = listaClientes[posCliente].id
                val idMoto = listaMotos[posMoto].id
                val estado = spinnerEstado.selectedItem.toString()

                val orden = Orden_servicio(
                    idOrden_servicio = null, idClientes = idCliente, idAdministrador = 1,
                    idTecnicos = 1, idMotos = idMoto,
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = formatoFecha.format(Date(System.currentTimeMillis() + 86400000)),
                    fechaFin = null, estado = estado
                )

                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)
                if (!responseOrden.isSuccessful || responseOrden.body()?.success != true) {
                    throw Exception("Error API: ${responseOrden.code()} - ${responseOrden.body()?.message}")
                }

                val idOrdenCreada = responseOrden.body()!!.data!!.idOrden_servicio
                val detallesAGuardar = listaDetalles.map { it.copy(idOrden = idOrdenCreada) }

                for (detalle in detallesAGuardar) {
                    val responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)
                    if (!responseDetalle.success) {
                        Toast.makeText(this@FormOrdenServicioActivity, "⚠️ Falló al guardar un detalle: ${responseDetalle.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                Toast.makeText(this@FormOrdenServicioActivity, "✅ Orden creada exitosamente!", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnGuardar.isEnabled = true
                btnGuardar.text = "GUARDAR ORDEN"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}