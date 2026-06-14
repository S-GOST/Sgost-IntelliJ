package com.example.sgost

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        initViews()
        setupRecyclerView()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        spinnerClientes = findViewById(R.id.spinnerClientes)
        spinnerMotos = findViewById(R.id.spinnerMotos)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnAgregarDetalle = findViewById(R.id.btnAgregarDetalle)
        btnNuevoCliente = findViewById(R.id.btnNuevoCliente)
        btnNuevaMoto = findViewById(R.id.btnNuevaMoto)
        rvOrdenDetalles = findViewById(R.id.rvOrdenDetalles)
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

                listaServicios = listOf(ItemSpinner("Ninguno", -1)) + servicios.map {
                    ItemSpinner(
                        displayName = "${it.nombre ?: "Servicio"} - $${it.precio}",
                        id = it.idServicios ?: -1,
                        precio = it.precio ?: 0.0,
                        garantia = it.garantia?.toString() ?: "0",
                        realName = it.nombre ?: "Servicio"
                    )
                }

                listaProductos = listOf(ItemSpinner("Ninguno", -1)) + productos.map {
                    ItemSpinner(
                        displayName = "${it.marca ?: ""} ${it.nombre ?: ""}".trim() + " - $${it.precio}",
                        id = it.idProductos ?: -1,
                        precio = it.precio ?: 0.0,
                        garantia = it.garantia?.toString() ?: "0",
                        realName = "${it.marca ?: ""} ${it.nombre ?: ""}".trim()
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

    // ✅ ADAPTADOR PERSONALIZADO: TEXTO BLANCO EN CERRADO Y ABIERTO
    private fun createKtmSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                    view.setBackgroundColor(Color.parseColor("#252525"))
                    view.setPadding(12, 12, 48, 12) // Espacio derecho para la flecha
                    view.textSize = 15f
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                    view.setBackgroundColor(Color.parseColor("#252525"))
                    view.setPadding(48, 12, 12, 12) // Espacio izquierdo para el check
                    view.textSize = 15f
                }
                return view
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, lista: List<ItemSpinner>) {
        val nombres = lista.map { it.displayName }
        spinner.adapter = createKtmSpinnerAdapter(nombres)
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener { finish() }
        btnAgregarDetalle.setOnClickListener { mostrarModalSeleccionDB() }
        btnNuevoCliente.setOnClickListener { mostrarModalNuevoCliente() }
        btnNuevaMoto.setOnClickListener { mostrarModalNuevaMoto() }
        btnGuardar.setOnClickListener { guardarOrdenCompleta() }
    }

    // 🟠 MODAL CLIENTE - TEMA KTM
    private fun mostrarModalNuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("👤 Registrar Nuevo Cliente")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }

        fun addKtmField(label: String, hint: String, initialType: Int = InputType.TYPE_CLASS_TEXT, isPass: Boolean = false) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label
                    setTextColor(Color.parseColor("#FF6600"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
                })
                setHint(hint)
                this.inputType = if (isPass) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else initialType
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#888888"))
                background = ColorDrawable(Color.parseColor("#252525"))
                setPadding(12, 12, 12, 12)
                textSize = 15f
                textCursorDrawable = ColorDrawable(Color.parseColor("#FF6600"))
                layout.addView(this)
            }

        val etUbicacion = addKtmField("📍 Ubicación:", "Ej: Bogotá")
        val etNombre = addKtmField("👤 Nombre completo:", "Ej: Carlos Gómez")
        val etUsuario = addKtmField("🔑 Usuario:", "Ej: carlos99")
        val etPassword = addKtmField("🔒 Contraseña:", "Mínimo 6 caracteres", isPass = true)
        val etTipoDoc = addKtmField("🆔 Tipo Documento:", "Ej: CC, TI, CE")
        val etCorreo = addKtmField("📧 Correo:", "Ej: carlos@email.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val etTelefono = addKtmField("📱 Teléfono:", "3001234567", InputType.TYPE_CLASS_PHONE)

        val btnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 8)
            weightSum = 2f
        }

        val btnCancel = MaterialButton(this).apply {
            text = "CANCELAR"
            setTextColor(Color.parseColor("#AAAAAA"))
            background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
        }

        val btnRegister = MaterialButton(this).apply {
            text = "REGISTRAR"
            setTextColor(Color.WHITE)
            background = ColorDrawable(Color.parseColor("#FF6600"))
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        btnLayout.addView(btnCancel)
        btnLayout.addView(btnRegister)
        layout.addView(btnLayout)
        scrollView.addView(layout)
        builder.setView(scrollView)

        val dialog = builder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#1A1A1A")))
        dialog.window?.setDimAmount(0.5f)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRegister.setOnClickListener {
            if (etNombre.text.isNullOrBlank() || etPassword.text.isNullOrBlank()) {
                Toast.makeText(this@FormOrdenServicioActivity, "Nombre y Contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()

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
                            val nuevoId = nuevoCliente.insertId ?: nuevoCliente.id ?: -1
                            val nombreCliente = cliente.nombre ?: "Cliente"
                            listaClientes = listaClientes + ItemSpinner(nombreCliente, nuevoId, 0.0, "0", nombreCliente)
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
    }

    // 🟠 MODAL MOTO - TEMA KTM
    private fun mostrarModalNuevaMoto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🏍️ Registrar Nueva Moto")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }

        layout.addView(TextView(this@FormOrdenServicioActivity).apply {
            text = "👤 Dueño (Cliente):"
            setTextColor(Color.parseColor("#FF6600"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
        })

        val spClienteMoto = Spinner(this@FormOrdenServicioActivity).apply {
            background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(12, 12, 12, 12)
            adapter = createKtmSpinnerAdapter(listaClientes.map { it.displayName })
        }
        layout.addView(spClienteMoto)

        fun addKtmField(label: String, hint: String, initialType: Int = InputType.TYPE_CLASS_TEXT) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label
                    setTextColor(Color.parseColor("#FF6600"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
                })
                setHint(hint)
                this.inputType = initialType
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#888888"))
                background = ColorDrawable(Color.parseColor("#252525"))
                setPadding(12, 12, 12, 12)
                textSize = 15f
                textCursorDrawable = ColorDrawable(Color.parseColor("#FF6600"))
                layout.addView(this)
            }

        val etPlaca = addKtmField("🔢 Placa:", "Ej: BGT657")
        val etModelo = addKtmField("🏷️ Modelo:", "Ej: 390")
        val etMarca = addKtmField("🏢 Marca:", "Ej: KTM, Honda")
        val etRecorrido = addKtmField("🛣️ Recorrido (km):", "0", InputType.TYPE_CLASS_NUMBER)

        val btnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 8)
            weightSum = 2f
        }

        val btnCancel = MaterialButton(this).apply {
            text = "CANCELAR"
            setTextColor(Color.parseColor("#AAAAAA"))
            background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
        }

        val btnRegister = MaterialButton(this).apply {
            text = "REGISTRAR"
            setTextColor(Color.WHITE)
            background = ColorDrawable(Color.parseColor("#FF6600"))
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        btnLayout.addView(btnCancel)
        btnLayout.addView(btnRegister)
        layout.addView(btnLayout)
        scrollView.addView(layout)
        builder.setView(scrollView)

        val dialog = builder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#1A1A1A")))
        dialog.window?.setDimAmount(0.5f)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRegister.setOnClickListener {
            try {
                val placa = etPlaca.text.toString().trim()
                val modelo = etModelo.text.toString().trim()
                val marca = etMarca.text.toString().trim()
                val recorrido = etRecorrido.text.toString().trim().toDoubleOrNull() ?: 0.0

                if (placa.isEmpty() || modelo.isEmpty() || marca.isEmpty()) {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val posCliente = spClienteMoto.selectedItemPosition
                val idCliente = if (posCliente >= 0 && posCliente < listaClientes.size) listaClientes[posCliente].id
                else {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Selecciona un cliente válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (idCliente <= 0) {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ El cliente seleccionado no tiene un ID válido", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val moto = Moto(idMotos = null, idClientes = idCliente, placa = placa, modelo = modelo, marca = marca, recorrido = recorrido)
                dialog.dismiss()

                lifecycleScope.launch {
                    try {
                        val response = ApiAndroid.apiService.crearMoto(moto)
                        if (response.success) {
                            Toast.makeText(this@FormOrdenServicioActivity, "✅ Moto registrada", Toast.LENGTH_LONG).show()
                            val nuevaId = response.data?.insertId ?: response.data?.idMotos
                            if (nuevaId != null) {
                                listaMotos = listaMotos.toMutableList() + ItemSpinner(displayName = "$modelo ($placa)", id = nuevaId, 0.0, "0", "$modelo ($placa)")
                                setupSpinner(spinnerMotos, listaMotos)
                                if (spinnerMotos.count > 0) spinnerMotos.setSelection(spinnerMotos.count - 1)
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
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        val labelServ = TextView(this).apply {
            text = "🔧 Servicio:"
            setTextColor(Color.parseColor("#FF6600"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        inputLayout.addView(labelServ)
        val spServicio = Spinner(this).apply {
            background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
            adapter = createKtmSpinnerAdapter(listaServicios.map { it.displayName })
        }
        inputLayout.addView(spServicio)

        val labelProd = TextView(this).apply {
            text = "📦 Producto (Opcional):"
            setTextColor(Color.parseColor("#FF6600"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 }
        }
        inputLayout.addView(labelProd)
        val spProducto = Spinner(this).apply {
            background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(12, 12, 12, 12)
            adapter = createKtmSpinnerAdapter(listaProductos.map { it.displayName })
        }
        inputLayout.addView(spProducto)

        builder.setView(inputLayout)
        builder.setPositiveButton("➕ Agregar") { _, _ ->
            val posServ = spServicio.selectedItemPosition
            val posProd = spProducto.selectedItemPosition

            val selServ = if (posServ > 0) listaServicios[posServ] else null
            val selProd = if (posProd > 0) listaProductos[posProd] else null

            if (selServ == null && selProd == null) {
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona al menos un servicio o producto", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val yaExisteServ = selServ != null && listaDetalles.any { it.idServicios == selServ.id }
            val yaExisteProd = selProd != null && listaDetalles.any { it.idProductos == selProd.id }

            if (yaExisteServ || yaExisteProd) {
                Toast.makeText(this@FormOrdenServicioActivity, "⚠️ El servicio o producto ya está en la lista", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val precioTotal = (selServ?.precio ?: 0.0) + (selProd?.precio ?: 0.0)
            val garantiaFinal = Math.max(
                selServ?.garantia?.toIntOrNull() ?: 0,
                selProd?.garantia?.toIntOrNull() ?: 0
            )

            listaDetalles.add(
                Detalles_orden_servicio(
                    idDetalleOrden = null,
                    idOrden = null,
                    idServicios = selServ?.id,
                    idProductos = selProd?.id,
                    nombreServicio = selServ?.realName,
                    nombreProducto = selProd?.realName,
                    precio = precioTotal,
                    garantia = garantiaFinal
                )
            )

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

                            val detallesAGuardar = listaDetalles.distinctBy { "${it.idServicios ?: 0}-${it.idProductos ?: 0}" }
                                .map { it.copy(idOrden = idOrdenCreada) }

                            var detallesFallidos = 0

                            detallesAGuardar.forEach { detalle ->
                                try {
                                    val responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)
                                    if (responseDetalle?.success != true) {
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
                                    "⚠️ Orden creada pero $detallesFallidos detalle(s) fallaron.",
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