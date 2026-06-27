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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
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

    // ✅ CORREGIDO: Uso de withContext para evitar errores de tipo 'Any' y mejorar inferencia
    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                // Retrofit ejecuta automáticamente en hilo IO
                val clientesResponse = ApiAndroid.apiService.obtenerClientes()
                val motosResponse = ApiAndroid.apiService.obtenerMotos()
                val serviciosResponse = ApiAndroid.apiService.obtenerServicios()
                val productosResponse = ApiAndroid.apiService.obtenerProductos()

                // Extracción segura
                val clientes = clientesResponse.body()?.data ?: emptyList()
                val motos = motosResponse.body()?.data ?: emptyList()
                val servicios = serviciosResponse.body()?.data ?: emptyList()
                val productos = productosResponse.body()?.data ?: emptyList()

                listaClientes = clientes.map { c ->
                    ItemSpinner(
                        displayName = c.nombre ?: "Cliente sin nombre",
                        id = c.id ?: 0,
                        precio = 0.0,
                        garantia = "0",
                        realName = c.nombre ?: ""
                    )
                }

                listaMotos = motos.map { m ->
                    ItemSpinner(
                        displayName = "${m.modelo ?: "Moto"} (${m.placa ?: ""})",
                        id = m.idMotos ?: 0,
                        precio = 0.0,
                        garantia = "0",
                        realName = m.modelo ?: ""
                    )
                }

                listaServicios = listOf(ItemSpinner(displayName = "Ninguno", id = -1)) + servicios.map { s ->
                    ItemSpinner(
                        displayName = "${s.nombre ?: "Servicio"} - \$${s.precio}",
                        id = s.idServicios ?: -1,
                        precio = s.precio ?: 0.0,
                        garantia = s.garantia?.toString() ?: "0",
                        realName = s.nombre ?: "Servicio"
                    )
                }

                listaProductos = listOf(ItemSpinner(displayName = "Ninguno", id = -1)) + productos.map { p ->
                    ItemSpinner(
                        displayName = "${p.nombre ?: "Producto"} - \$${p.precio}",
                        id = p.idProductos ?: -1,
                        precio = p.precio ?: 0.0,
                        garantia = p.garantia?.toString() ?: "0",
                        realName = p.nombre ?: "Producto"
                    )
                }

                withContext(Dispatchers.Main) {
                    setupSpinner(spinnerClientes, listaClientes)
                    setupSpinner(spinnerMotos, listaMotos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormOrdenServicioActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createKtmSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                    view.setBackgroundColor(Color.parseColor("#252525"))
                    view.setPadding(12, 12, 48, 12)
                    view.textSize = 15f
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                    view.setBackgroundColor(Color.parseColor("#252525"))
                    view.setPadding(48, 12, 12, 12)
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
        findViewById<MaterialButton>(R.id.btnCancelar)?.setOnClickListener { finish() }
        btnAgregarDetalle.setOnClickListener { mostrarModalSeleccionDB() }
        btnNuevoCliente.setOnClickListener { mostrarModalNuevoCliente() }
        btnNuevaMoto.setOnClickListener { mostrarModalNuevaMoto() }
        btnGuardar.setOnClickListener { guardarOrdenCompleta() }
    }

    // 🟠 MODAL CLIENTE - TEMA KTM
    private fun mostrarModalNuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registrar Nuevo Cliente")

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

        val etUbicacion = addKtmField("Ubicación:", "Ej: Bogotá")
        val etNombre = addKtmField("Nombre completo:", "Ej: Carlos Gómez")
        val etUsuario = addKtmField("Usuario:", "Ej: carlos99")
        val etPassword = addKtmField("Contraseña:", "Mínimo 6 caracteres", isPass = true)
        val etTipoDoc = addKtmField("Tipo Documento:", "Ej: CC, TI, CE")
        val etCorreo = addKtmField("Correo:", "Ej: carlos@email.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val etTelefono = addKtmField("Teléfono:", "3001234567", InputType.TYPE_CLASS_PHONE)

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
                            Toast.makeText(this@FormOrdenServicioActivity, "Cliente registrado exitosamente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "El servidor no devolvió los datos del cliente", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorRaw = response.errorBody()?.string() ?: response.body()?.message ?: "Error desconocido"
                        val mensajeUsuario = when {
                            errorRaw.contains("Duplicate entry", ignoreCase = true) -> "El usuario ya está registrado."
                            errorRaw.contains("correo", ignoreCase = true) -> "El correo ya está en uso."
                            else -> "Error: $errorRaw"
                        }
                        Toast.makeText(this@FormOrdenServicioActivity, mensajeUsuario, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("API_CLIENTE", "Excepción: ${e.message}", e)
                    Toast.makeText(this@FormOrdenServicioActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🟠 MODAL MOTO - TEMA KTM
    private fun mostrarModalNuevaMoto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registrar Nueva Moto")

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
            text = "Dueño (Cliente):"
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

        val etPlaca = addKtmField("Placa:", "Ej: BGT657")
        val etModelo = addKtmField("Modelo:", "Ej: 390")
        val etMarca = addKtmField("Marca:", "Ej: KTM, Honda")
        val etRecorrido = addKtmField("Recorrido (km):", "0", InputType.TYPE_CLASS_NUMBER)

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
                    Toast.makeText(this@FormOrdenServicioActivity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val posCliente = spClienteMoto.selectedItemPosition
                val idCliente = if (posCliente >= 0 && posCliente < listaClientes.size) listaClientes[posCliente].id
                else {
                    Toast.makeText(this@FormOrdenServicioActivity, "Selecciona un cliente válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (idCliente <= 0) {
                    Toast.makeText(this@FormOrdenServicioActivity, "El cliente seleccionado no tiene un ID válido", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val moto = Moto(idMotos = null, idClientes = idCliente, placa = placa, modelo = modelo, marca = marca, recorrido = recorrido)
                dialog.dismiss()

                lifecycleScope.launch {
                    try {
                        val response = ApiAndroid.apiService.crearMoto(moto)
                        if (response.success) {
                            Toast.makeText(this@FormOrdenServicioActivity, "Moto registrada", Toast.LENGTH_LONG).show()
                            val nuevaId = response.data?.insertId ?: response.data?.idMotos
                            if (nuevaId != null) {
                                listaMotos = listaMotos.toMutableList() + ItemSpinner(displayName = "$modelo ($placa)", id = nuevaId, 0.0, "0", "$modelo ($placa)")
                                setupSpinner(spinnerMotos, listaMotos)
                                if (spinnerMotos.count > 0) spinnerMotos.setSelection(spinnerMotos.count - 1)
                            } else {
                                Toast.makeText(this@FormOrdenServicioActivity, "Moto creada pero no se recibió su ID. Recarga la app.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@FormOrdenServicioActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarModalSeleccionDB() {
        if (listaServicios.isEmpty() && listaProductos.isEmpty()) {
            Toast.makeText(this@FormOrdenServicioActivity, "Aún no hay servicios o productos en la base de datos", Toast.LENGTH_LONG).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar desde Base de Datos")
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        val labelServ = TextView(this).apply {
            text = "Servicio:"
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
            text = "Producto (Opcional):"
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
        builder.setPositiveButton("Agregar") { _, _ ->
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
                Toast.makeText(this@FormOrdenServicioActivity, "El servicio o producto ya está en la lista", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@FormOrdenServicioActivity, "Agregado a la orden", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // ✅ GUARDADO ROBUSTO
    private fun guardarOrdenCompleta() {
        val posCliente = spinnerClientes.selectedItemPosition
        val posMoto = spinnerMotos.selectedItemPosition

        if (posCliente < 0 || posMoto < 0) {
            Toast.makeText(this, "Selecciona Cliente y Moto", Toast.LENGTH_SHORT).show()
            return
        }
        if (listaDetalles.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un servicio o producto", Toast.LENGTH_SHORT).show()
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
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)
                if (!responseOrden.isSuccessful) {
                    throw Exception("Error HTTP: ${responseOrden.code()}")
                }

                val jsonString = responseOrden.body()?.string() ?: "{}"
                val json = JSONObject(jsonString)
                if (!json.optBoolean("success", false)) {
                    throw Exception(json.optString("message", "Error al crear la orden"))
                }

                val data = json.optJSONObject("data") ?: json
                val idOrdenCreada = data.optInt("ID_ORDEN_SERVICIO")
                if (idOrdenCreada == 0) {
                    throw Exception("El servidor no devolvió un ID válido para la orden.")
                }

                Log.d("GUARDAR_ORDEN", "ID recibido correctamente: $idOrdenCreada")

                val detallesAGuardar = listaDetalles.map { it.copy(idOrden = idOrdenCreada) }
                var detallesFallidos = 0

                detallesAGuardar.forEach { detalle ->
                    try {
                        val responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)
                        if (responseDetalle?.success != true) {
                            detallesFallidos++
                            Log.w("DETALLES", "Falló detalle: ${responseDetalle?.message}")
                        }
                    } catch (e: Exception) {
                        detallesFallidos++
                        Log.e("DETALLES_500", "Error insertando detalle: ${e.message}")
                    }
                }

                withContext(Dispatchers.Main) {
                    if (detallesFallidos > 0) {
                        Toast.makeText(this@FormOrdenServicioActivity,
                            "Orden creada pero $detallesFallidos detalle(s) fallaron.",
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@FormOrdenServicioActivity,
                            "Orden creada exitosamente!",
                            Toast.LENGTH_LONG).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormOrdenServicioActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("GUARDAR_ORDEN", "Stack trace:", e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btnGuardar.isEnabled = true
                    btnGuardar.text = "GUARDAR ORDEN"
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}