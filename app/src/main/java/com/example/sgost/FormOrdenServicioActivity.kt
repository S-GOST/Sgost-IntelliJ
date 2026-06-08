package com.example.sgost

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ MODIFICADO: Se agregó 'realName' para guardar el nombre exacto sin el precio
data class ItemSpinner(
    val displayName: String,
    val id: Int,
    val precio: Double = 0.0,
    val garantia: String = "0",
    val realName: String = ""
)

class FormOrdenServicioActivity : AppCompatActivity() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerMotos: Spinner
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

                listaClientes = clientes.filter { it.id != null }.map {
                    ItemSpinner(it.nombre ?: "Cliente", it.id!!, 0.0, "0", it.nombre ?: "")
                }
                listaMotos = motos.filter { it.idMotos != null }.map {
                    ItemSpinner("${it.modelo ?: "Moto"} (${it.placa ?: ""})", it.idMotos!!, 0.0, "0", it.modelo ?: "")
                }

                // ✅ CORREGIDO: Servicios con nombre real
                listaServicios = servicios.map {
                    val precio = it.precio ?: 0.0
                    val nombreReal = it.nombre ?: "Servicio"
                    ItemSpinner(
                        displayName = "$nombreReal - $${it.precio}",
                        id = it.idServicios ?: -1,
                        precio = precio,
                        garantia = it.garantia?.toString() ?: "0",
                        realName = nombreReal
                    )
                }

                // ✅ CORREGIDO: Productos con nombre real
                listaProductos = productos.map {
                    val precio = it.precio ?: 0.0
                    val nombreReal = "${it.marca ?: ""} ${it.nombre ?: ""}".trim()
                    ItemSpinner(
                        displayName = "$nombreReal - $${it.precio}",
                        id = it.idProductos ?: -1,
                        precio = precio,
                        garantia = it.garantia?.toString() ?: "0",
                        realName = nombreReal
                    )
                }

                setupSpinner(spinnerClientes, listaClientes)
                setupSpinner(spinnerMotos, listaMotos)

            } catch (e: Exception) {
                Log.e("CARGA_DATOS", "Error al cargar listas", e)
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

    private fun mostrarModalNuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("👤 Registrar Nuevo Cliente")

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        fun addField(label: String, hint: String, inputType: Int = InputType.TYPE_CLASS_TEXT, isPass: Boolean = false) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label; setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 8 }
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
                        id = null, ubicacion = etUbicacion.text.toString().trim(), nombre = etNombre.text.toString().trim(),
                        usuario = etUsuario.text.toString().trim(), contrasena = etPassword.text.toString().trim(),
                        tipoDocumento = etTipoDoc.text.toString().trim(), correo = etCorreo.text.toString().trim(),
                        telefono = etTelefono.text.toString().trim()
                    )

                    val response = ApiAndroid.apiService.registrarCliente(cliente)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val nuevoCliente = response.body()!!.data
                        if (nuevoCliente != null) {
                            listaClientes = listaClientes + ItemSpinner(nuevoCliente.nombre ?: "Cliente", nuevoCliente.id ?: -1, 0.0, "0", nuevoCliente.nombre ?: "")
                            setupSpinner(spinnerClientes, listaClientes)
                            if (spinnerClientes.count > 0) spinnerClientes.setSelection(spinnerClientes.count - 1)
                            Toast.makeText(this@FormOrdenServicioActivity, "✅ Cliente registrado exitosamente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "⚠️ El servidor no devolvió los datos del cliente", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorRaw = response.errorBody()?.string() ?: response.body()?.message ?: "Error desconocido"
                        val mensajeUsuario = when {
                            errorRaw.contains("Duplicate entry", ignoreCase = true) -> "❌ El usuario ya está registrado."
                            errorRaw.contains("correo", ignoreCase = true) -> "❌ El correo ya está en uso."
                            else -> "❌ Error: $errorRaw"
                        }
                        Toast.makeText(this@FormOrdenServicioActivity, mensajeUsuario, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("API_CLIENTE", "💥 Excepción: ${e.message}", e)
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        })
        val spClienteMoto = Spinner(this@FormOrdenServicioActivity).apply {
            setPadding(0, 4, 0, 4)
            adapter = ArrayAdapter(this@FormOrdenServicioActivity, android.R.layout.simple_spinner_dropdown_item, listaClientes.map { it.displayName })
        }
        layout.addView(spClienteMoto)

        fun addField(label: String, hint: String, inputType: Int = InputType.TYPE_CLASS_TEXT) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label; setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 8 }
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

        builder.setPositiveButton("REGISTRAR") { dialog, _ ->
            try {
                val placa = etPlaca.text.toString().trim()
                val modelo = etModelo.text.toString().trim()
                val marca = etMarca.text.toString().trim()
                val recorrido = etRecorrido.text.toString().trim().toDoubleOrNull() ?: 0.0

                if (placa.isEmpty() || modelo.isEmpty() || marca.isEmpty()) {
                    Toast.makeText(this, "❌ Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val posCliente = spClienteMoto.selectedItemPosition
                val idCliente = if (posCliente >= 0 && posCliente < listaClientes.size) listaClientes[posCliente].id
                else {
                    Toast.makeText(this, "❌ Selecciona un cliente válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (idCliente <= 0) {
                    Toast.makeText(this, "❌ El cliente seleccionado no tiene un ID válido", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val moto = Moto(idMotos = null, idClientes = idCliente, placa = placa, modelo = modelo, marca = marca, recorrido = recorrido)

                lifecycleScope.launch {
                    try {
                        val response = ApiAndroid.apiService.crearMoto(moto)
                        if (response.success) {
                            Toast.makeText(this@FormOrdenServicioActivity, "✅ Moto registrada", Toast.LENGTH_LONG).show()
                            val nuevaId = response.data?.idMotos
                            if (nuevaId != null) {
                                listaMotos = listaMotos.toMutableList() + ItemSpinner(displayName = "$modelo ($placa)", id = nuevaId, 0.0, "0", "$modelo ($placa)")
                                setupSpinner(spinnerMotos, listaMotos)
                                if (spinnerMotos.count > 0) spinnerMotos.setSelection(spinnerMotos.count - 1)
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this@FormOrdenServicioActivity, "⚠️ Moto creada pero no se recibió su ID. Recarga la app.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
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

        builder.setView(inputLayout)
        builder.setPositiveButton("➕ Agregar") { _, _ ->
            val posServ = spServicio.selectedItemPosition
            val posProd = spProducto.selectedItemPosition

            if (posServ < 0 && posProd < 0) {
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona al menos un servicio o producto", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            var agregado = false

            // ✅ AGREGAR SERVICIO SIN DUPLICADOS
            if (posServ >= 0) {
                val sel = listaServicios[posServ]
                if (!listaDetalles.any { it.idServicios == sel.id }) {
                    listaDetalles.add(
                        Detalles_orden_servicio(
                            idDetalleOrden = null, idOrden = null, idServicios = sel.id, idProductos = null,
                            nombreServicio = sel.realName,
                            nombreProducto = null,
                            precio = sel.precio,           // ✅ Envia Double directamente
                            garantia = sel.garantia.toInt() // ✅ Convierte a Int
                        )
                    )
                    agregado = true
                }
                // ... resto del código
            }

            if (posProd >= 0) {
                val sel = listaProductos[posProd]
                if (!listaDetalles.any { it.idProductos == sel.id }) {
                    listaDetalles.add(
                        Detalles_orden_servicio(
                            idDetalleOrden = null, idOrden = null, idServicios = null, idProductos = sel.id,
                            nombreServicio = null,
                            nombreProducto = sel.realName,
                            precio = sel.precio,           // ✅ Envia Double directamente
                            garantia = sel.garantia.toInt() // ✅ Convierte a Int
                        )
                    )
                    agregado = true
                } else {
                    Toast.makeText(this@FormOrdenServicioActivity, "⚠️ El producto ya está en la lista", Toast.LENGTH_SHORT).show()
                }
            }

            if (agregado) {
                detalleAdapter.submitList(listaDetalles.toList())
                Toast.makeText(this@FormOrdenServicioActivity, "✅ Agregado a la orden", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // 💾 GUARDAR ORDEN COMPLETA
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
                // 1. Crear el objeto Orden
                val orden = Orden_servicio(
                    idOrden_servicio = null,
                    idClientes = listaClientes[posCliente].id,
                    idMotos = listaMotos[posMoto].id,
                    idAdministrador = 1,
                    idTecnicos = 1,
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = formatoFecha.format(Date(System.currentTimeMillis() + 86400000)),
                    fechaFin = formatoFecha.format(Date()),
                    estado = "PENDIENTE"
                )

                // 2. Hacer la petición de la Orden
                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)

                if (responseOrden.isSuccessful) {
                    val jsonString = responseOrden.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonString)
                    val success = jsonObject.optBoolean("success", false)

                    if (success) {
                        val data = jsonObject.optJSONObject("data")
                        val idOrdenCreada = data?.optInt("idOrden_servicio")
                            ?: data?.optInt("ID_ORDEN_SERVICIO")
                            ?: data?.optInt("insertId")
                            ?: 0

                        if (idOrdenCreada > 0) {
                            Log.d("GUARDAR_ORDEN", "✅ ID recibido correctamente: $idOrdenCreada")

                            // ✅ Se asigna el ID a cada detalle y se limpian duplicados por seguridad
                            val detallesAGuardar = listaDetalles.distinctBy { it.idServicios ?: it.idProductos }
                                .map { it.copy(idOrden = idOrdenCreada) }

                            var detallesFallidos = 0

                            detallesAGuardar.forEach { detalle ->
                                var responseDetalle: ApiResponse<Detalles_orden_servicio>? = null

                                try {
                                    responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)

                                    if (responseDetalle?.success == true) {
                                        // Éxito
                                    } else {
                                        detallesFallidos++
                                        Log.w("DETALLES", "Falló detalle (Lógica): ${responseDetalle?.message}")
                                    }
                                } catch (e: Exception) {
                                    detallesFallidos++
                                    Log.e("DETALLES_500", "Error insertando detalle (HTTP/Red): ${e.message}")
                                }
                            }

                            if (detallesFallidos > 0) {
                                Toast.makeText(this@FormOrdenServicioActivity,
                                    "⚠️ Orden creada pero $detallesFallidos detalle(s) fallaron. Revisa el logcat.",
                                    Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@FormOrdenServicioActivity,
                                    "✅ Orden creada exitosamente!",
                                    Toast.LENGTH_LONG).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            throw Exception("El servidor dijo 'success' pero no incluyó un ID válido.")
                        }
                    } else {
                        val errorMsg = jsonObject.optString("error") ?: jsonObject.optString("message") ?: "Error desconocido"
                        throw Exception("Error del servidor: $errorMsg")
                    }
                } else {
                    val errorMsg = responseOrden.errorBody()?.string() ?: "Error de conexión"
                    Log.e("GUARDAR_ORDEN", "❌ Error en orden: $errorMsg")
                    throw Exception("Error al crear orden: $errorMsg")
                }

            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("GUARDAR_ORDEN", "💥 Stack trace:", e)
            } finally {
                btnGuardar.isEnabled = true
                btnGuardar.text = "GUARDAR ORDEN"
            }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}