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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemSpinner(
    val displayName: String,
    val id: Int,
    val precio: Double = 0.0,
    val garantia: String = "0",
    val realName: Any = ""
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
                val clientesResponse = ApiAndroid.apiService.obtenerClientes()
                val motosResponse = ApiAndroid.apiService.obtenerMotos()
                val serviciosResponse = ApiAndroid.apiService.obtenerServicios()
                val productosResponse = ApiAndroid.apiService.obtenerProductos()

                val clientes = clientesResponse.body()?.data ?: emptyList()
                val motos = motosResponse.body()?.data ?: emptyList()
                val servicios = serviciosResponse.body()?.data ?: emptyList()
                val productos = productosResponse.body()?.data ?: emptyList()

                listaClientes = clientes.map { c ->
                    ItemSpinner(c.nombre ?: "Cliente", c.id ?: 0, 0.0, "0", c.nombre ?: "")
                }
                listaMotos = motos.map { m ->
                    ItemSpinner("${m.modelo ?: "Moto"} (${m.placa ?: ""})", m.idMotos ?: 0, 0.0, "0", m.modelo ?: "")
                }
                listaServicios = listOf(ItemSpinner("Ninguno", -1)) + servicios.map { s ->
                    ItemSpinner("${s.nombre ?: "Servicio"} - \$${s.precio}", s.idServicios ?: -1, s.precio ?: 0.0, s.garantia?.toString() ?: "0", s.nombre ?: "Servicio")
                }
                listaProductos = listOf(ItemSpinner("Ninguno", -1)) + productos.map { p ->
                    ItemSpinner("${p.nombre ?: "Producto"} - \$${p.precio}", p.idProductos ?: -1, p.precio ?: 0.0, p.garantia?.toString() ?: "0", p.nombre ?: "Producto")
                }

                withContext(Dispatchers.Main) {
                    setupSpinner(spinnerClientes, listaClientes)
                    setupSpinner(spinnerMotos, listaMotos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormOrdenServicioActivity, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
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

    // 🟠 MODAL CLIENTE (CORREGIDO)
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
                    text = label; setTextColor(Color.parseColor("#FF6600")); textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
                })
                setHint(hint); inputType = if (isPass) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else initialType
                setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#888888"))
                background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12); textSize = 15f
                textCursorDrawable = ColorDrawable(Color.parseColor("#FF6600")); layout.addView(this)
            }

        val etUbicacion = addKtmField("Ubicación:", "Ej: Bogotá")
        val etNombre = addKtmField("Nombre completo:", "Ej: Carlos Gómez")
        val etUsuario = addKtmField("Usuario:", "Ej: carlos99")
        val etPassword = addKtmField("Contraseña:", "Mínimo 6 caracteres", isPass = true)
        val etTipoDoc = addKtmField("Tipo Documento:", "Ej: CC, TI, CE")
        val etCorreo = addKtmField("Correo:", "Ej: carlos@email.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val etTelefono = addKtmField("Teléfono:", "3001234567", InputType.TYPE_CLASS_PHONE)

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 16, 0, 8); weightSum = 2f }
        val btnCancel = MaterialButton(this).apply {
            text = "CANCELAR"; setTextColor(Color.parseColor("#AAAAAA")); background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(0, 12, 0, 12); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
        }
        val btnRegister = MaterialButton(this).apply {
            text = "REGISTRAR"; setTextColor(Color.WHITE); background = ColorDrawable(Color.parseColor("#FF6600"))
            setPadding(0, 12, 0, 12); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnLayout.addView(btnCancel); btnLayout.addView(btnRegister); layout.addView(btnLayout); scrollView.addView(layout); builder.setView(scrollView)

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
                    // ✅ CORREGIDO: Mapeo correcto de campos a la clase Cliente
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

                    // Adaptado a: Response<ApiResponse<Cliente>>
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        val nuevoId = (data as? Map<*, *>)?.get("insertId") as? Int ?: (data as? Map<*, *>)?.get("id") as? Int ?: -1
                        if (nuevoId > 0) {
                            listaClientes = listaClientes + ItemSpinner(cliente.nombre ?: "Cliente", nuevoId, 0.0, "0", cliente.nombre ?: "")
                            setupSpinner(spinnerClientes, listaClientes)
                            if (spinnerClientes.count > 0) spinnerClientes.setSelection(spinnerClientes.count - 1)
                            Toast.makeText(this@FormOrdenServicioActivity, "Cliente registrado", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "Cliente creado pero no se obtuvo ID", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val msg = response.body()?.message ?: response.errorBody()?.string() ?: "Error"
                        Toast.makeText(this@FormOrdenServicioActivity, if (msg.contains("Duplicate", ignoreCase = true)) "Usuario/CORREO ya existe" else "Error: $msg", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@FormOrdenServicioActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🟠 MODAL MOTO (CORREGIDO)
    private fun mostrarModalNuevaMoto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registrar Nueva Moto")
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 24, 32, 24); background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }
        val scrollView = ScrollView(this).apply { isFillViewport = true; background = ColorDrawable(Color.parseColor("#1A1A1A")) }
        layout.addView(TextView(this@FormOrdenServicioActivity).apply {
            text = "Dueño (Cliente):"; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
        })
        val spClienteMoto = Spinner(this@FormOrdenServicioActivity).apply {
            background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12)
            adapter = createKtmSpinnerAdapter(listaClientes.map { it.displayName })
        }
        layout.addView(spClienteMoto)

        fun addKtmField(label: String, hint: String, initialType: Int = InputType.TYPE_CLASS_TEXT) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
                })
                setHint(hint); inputType = initialType; setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#888888"))
                background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12); textSize = 15f
                textCursorDrawable = ColorDrawable(Color.parseColor("#FF6600")); layout.addView(this)
            }

        val etPlaca = addKtmField("Placa:", "Ej: BGT657")
        val etModelo = addKtmField("Modelo:", "Ej: 390")
        val etMarca = addKtmField("Marca:", "Ej: KTM, Honda")
        val etRecorrido = addKtmField("Recorrido (km):", "0", InputType.TYPE_CLASS_NUMBER)

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 16, 0, 8); weightSum = 2f }
        val btnCancel = MaterialButton(this).apply {
            text = "CANCELAR"; setTextColor(Color.parseColor("#AAAAAA")); background = ColorDrawable(Color.parseColor("#252525"))
            setPadding(0, 12, 0, 12); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
        }
        val btnRegister = MaterialButton(this).apply {
            text = "REGISTRAR"; setTextColor(Color.WHITE); background = ColorDrawable(Color.parseColor("#FF6600"))
            setPadding(0, 12, 0, 12); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnLayout.addView(btnCancel); btnLayout.addView(btnRegister); layout.addView(btnLayout); scrollView.addView(layout); builder.setView(scrollView)

        val dialog = builder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#1A1A1A")))
        dialog.window?.setDimAmount(0.5f)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRegister.setOnClickListener {
            val placa = etPlaca.text.toString().trim()
            val modelo = etModelo.text.toString().trim()
            val marca = etMarca.text.toString().trim()
            val recorrido = etRecorrido.text.toString().trim()

            if (placa.isEmpty() || modelo.isEmpty() || marca.isEmpty()) {
                Toast.makeText(this@FormOrdenServicioActivity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val posCliente = spClienteMoto.selectedItemPosition
            val idCliente = if (posCliente >= 0 && posCliente < listaClientes.size) listaClientes[posCliente].id else -1
            if (idCliente <= 0) {
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona un cliente válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ CORREGIDO: Se añade recorrido explícitamente al constructor
            val moto = Moto(
                idMotos = null,
                idClientes = idCliente,
                placa = placa,
                modelo = modelo,
                marca = marca,
                recorrido = etRecorrido.text.toString().trim()
            )
            dialog.dismiss()

            lifecycleScope.launch {
                try {
                    // Adaptado a: Response<ApiResponse<Moto>>
                    val response = ApiAndroid.apiService.crearMoto(moto)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        val nuevaId = (data as? Map<*, *>)?.get("insertId") as? Int ?: (data as? Map<*, *>)?.get("idMotos") as? Int ?: -1
                        if (nuevaId > 0) {
                            listaMotos = listaMotos.toMutableList() + ItemSpinner("$modelo ($placa)", nuevaId, 0.0, "0", "$modelo")
                            setupSpinner(spinnerMotos, listaMotos)
                            if (spinnerMotos.count > 0) spinnerMotos.setSelection(spinnerMotos.count - 1)
                            Toast.makeText(this@FormOrdenServicioActivity, "Moto registrada", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FormOrdenServicioActivity, "Moto creada pero no se recibió ID", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val msg = response.body()?.message ?: response.errorBody()?.string() ?: "Error"
                        Toast.makeText(this@FormOrdenServicioActivity, "Error: $msg", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@FormOrdenServicioActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarModalSeleccionDB() {
        if (listaServicios.isEmpty() && listaProductos.isEmpty()) {
            Toast.makeText(this@FormOrdenServicioActivity, "No hay servicios o productos en BD", Toast.LENGTH_LONG).show()
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar desde Base de Datos")
        val inputLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 24, 32, 24); setBackgroundColor(Color.parseColor("#1A1A1A")) }
        inputLayout.addView(TextView(this).apply { text = "Servicio:"; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD) })
        val spServicio = Spinner(this).apply { background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }; adapter = createKtmSpinnerAdapter(listaServicios.map { it.displayName }) }
        inputLayout.addView(spServicio)
        inputLayout.addView(TextView(this).apply { text = "Producto (Opcional):"; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } })
        val spProducto = Spinner(this).apply { background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12); adapter = createKtmSpinnerAdapter(listaProductos.map { it.displayName }) }
        inputLayout.addView(spProducto)

        builder.setView(inputLayout)
        builder.setPositiveButton("Agregar") { _, _ ->
            val posServ = spServicio.selectedItemPosition
            val posProd = spProducto.selectedItemPosition
            val selServ = if (posServ > 0) listaServicios[posServ] else null
            val selProd = if (posProd > 0) listaProductos[posProd] else null
            if (selServ == null && selProd == null) {
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona al menos uno", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if ((selServ != null && listaDetalles.any { it.idServicios == selServ.id }) || (selProd != null && listaDetalles.any { it.idProductos == selProd.id })) {
                Toast.makeText(this@FormOrdenServicioActivity, "Ya está en la lista", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val precioTotal = (selServ?.precio ?: 0.0) + (selProd?.precio ?: 0.0)
            val garantiaFinal = Math.max(selServ?.garantia?.toIntOrNull() ?: 0, selProd?.garantia?.toIntOrNull() ?: 0)
            listaDetalles.add(Detalles_orden_servicio(null, null, selServ?.id, selProd?.id,
                selServ?.realName as String?, selProd?.realName as String?, precioTotal, garantiaFinal))
            detalleAdapter.submitList(listaDetalles.toList())
            Toast.makeText(this@FormOrdenServicioActivity, "Agregado a la orden", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

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
                val orden = Orden_servicio(null, listaClientes[posCliente].id, listaMotos[posMoto].id, 1, 1, formatoFecha.format(Date()), formatoFecha.format(Date(System.currentTimeMillis() + 86400000)), null, "PENDIENTE")

                // Adaptado a: Response<ResponseBody> (JSON crudo)
                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)
                if (!responseOrden.isSuccessful) throw Exception("Error HTTP: ${responseOrden.code()}")

                val jsonString = responseOrden.body()?.string() ?: "{}"
                val json = JSONObject(jsonString)
                if (!json.optBoolean("success", false)) throw Exception(json.optString("message", "Error al crear la orden"))

                val data = json.optJSONObject("data") ?: json
                val idOrdenCreada = data.optInt("ID_ORDEN_SERVICIO", 0).takeIf { it != 0 } ?: data.optInt("insertId", 0)
                if (idOrdenCreada == 0) throw Exception("El servidor no devolvió un ID válido para la orden.")
                Log.d("GUARDAR_ORDEN", "ID recibido: $idOrdenCreada")

                val detallesAGuardar = listaDetalles.map { it.copy(idOrden = idOrdenCreada) }
                var detallesFallidos = 0

                detallesAGuardar.forEach { detalle ->
                    try {
                        // Adaptado a: ApiResponse<Detalles_orden_servicio> (sin Response wrapper)
                        val responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)
                        if (!responseDetalle.success) {
                            detallesFallidos++
                            Log.w("DETALLES", "Falló detalle: ${responseDetalle.message}")
                        }
                    } catch (e: Exception) {
                        detallesFallidos++
                        Log.e("DETALLES_500", "Error insertando detalle: ${e.message}")
                    }
                }

                withContext(Dispatchers.Main) {
                    if (detallesFallidos > 0) {
                        Toast.makeText(this@FormOrdenServicioActivity, "Orden creada pero $detallesFallidos detalle(s) fallaron.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@FormOrdenServicioActivity, "Orden creada exitosamente!", Toast.LENGTH_LONG).show()
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
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}