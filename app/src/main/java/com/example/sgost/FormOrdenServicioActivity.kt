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
    val realName: String = ""
)

class FormOrdenServicioActivity : AppCompatActivity() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerMotos: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnAgregarDetalle: MaterialButton
    private lateinit var btnNuevaMoto: MaterialButton
    private lateinit var rvOrdenDetalles: RecyclerView

    private lateinit var detalleAdapter: DetalleOrdenAdapter
    private var listaDetalles: MutableList<Detalles_orden_servicio> = mutableListOf()

    private var listaMotos = emptyList<ItemSpinner>()
    private var listaServicios = emptyList<ItemSpinner>()
    private var listaProductos = emptyList<ItemSpinner>()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))
    private var idClienteLogueado: Int = 0

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
        obtenerIdClienteLogueado()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        spinnerClientes = findViewById(R.id.spinnerClientes)
        spinnerMotos = findViewById(R.id.spinnerMotos)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnAgregarDetalle = findViewById(R.id.btnAgregarDetalle)
        btnNuevaMoto = findViewById(R.id.btnNuevaMoto)
        rvOrdenDetalles = findViewById(R.id.rvOrdenDetalles)
    }

    private fun setupRecyclerView() {
        rvOrdenDetalles.layoutManager = LinearLayoutManager(this)
        detalleAdapter = DetalleOrdenAdapter()
        rvOrdenDetalles.adapter = detalleAdapter
    }

    // 🔑 RECUPERAR ID DEL USUARIO LOGUEADO
    private fun obtenerIdClienteLogueado() {
        val prefs = getSharedPreferences("sgost_prefs", MODE_PRIVATE)
        idClienteLogueado = prefs.getInt("user_id", 0)
        Log.d("SESSION", "👤 ID Cliente Logueado: $idClienteLogueado")
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val motosResponse = ApiAndroid.apiService.obtenerMotos()
                val serviciosResponse = ApiAndroid.apiService.obtenerServicios()
                val productosResponse = ApiAndroid.apiService.obtenerProductos()

                val motos = motosResponse.body()?.data ?: emptyList()
                val servicios = serviciosResponse.body()?.data ?: emptyList()
                val productos = productosResponse.body()?.data ?: emptyList()

                listaMotos = motos.filter { it.idClientes == idClienteLogueado }.map { m ->
                    ItemSpinner(
                        displayName = "${m.modelo ?: "Moto"} (${m.placa ?: ""})",
                        id = m.idMotos ?: 0,
                        precio = 0.0,
                        garantia = "0",
                        realName = m.modelo ?: ""
                    )
                }

                listaServicios = listOf(ItemSpinner("Ninguno", -1)) + servicios.map { s ->
                    ItemSpinner(
                        displayName = "${s.nombre ?: "Servicio"} - \$${s.precio}",
                        id = s.idServicios ?: -1,
                        precio = s.precio ?: 0.0,
                        garantia = s.garantia?.toString() ?: "0",
                        realName = s.nombre ?: "Servicio"
                    )
                }

                listaProductos = listOf(ItemSpinner("Ninguno", -1)) + productos.map { p ->
                    ItemSpinner(
                        displayName = "${p.nombre ?: "Producto"} - \$${p.precio}",
                        id = p.idProductos ?: -1,
                        precio = p.precio ?: 0.0,
                        garantia = p.garantia?.toString() ?: "0",
                        realName = p.nombre ?: "Producto"
                    )
                }

                withContext(Dispatchers.Main) {
                    spinnerClientes.isEnabled = false
                    spinnerClientes.adapter = createKtmSpinnerAdapter(listOf("Usuario Logueado ($idClienteLogueado)"))
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
        spinner.adapter = createKtmSpinnerAdapter(lista.map { it.displayName })
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnCancelar)?.setOnClickListener { finish() }
        btnAgregarDetalle.setOnClickListener { mostrarModalSeleccionDB() }
        btnNuevaMoto.setOnClickListener { mostrarModalNuevaMoto() }
        btnGuardar.setOnClickListener { guardarOrdenCompleta() }
    }

    // 🟠 MODAL MOTO (VINCULADA AUTOMÁTICAMENTE AL CLIENTE LOGUEADO)
    // 🟠 MODAL MOTO (CORREGIDO EL ERROR DE CONTEXTO Y NULABILITY)
    // 🟠 MODAL MOTO (REFRESCO INMEDIATO EN EL SPINNER)
    // 🟠 MODAL MOTO (CORREGIDO: Contexto y Nullability)
    // 🟠 MODAL MOTO (EXTRACCIÓN DE ID ROBUSTA)
    // 🟠 MODAL MOTO (CORREGIDO: Contexto y Nullability)
    // 🟠 MODAL MOTO (CORREGIDO: Contexto y Extracción de ID)
    private fun mostrarModalNuevaMoto() {
        if (idClienteLogueado <= 0) {
            Toast.makeText(this, "⚠️ Debes iniciar sesión para agregar una moto.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registrar Nueva Moto")
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = ColorDrawable(Color.parseColor("#1A1A1A"))
        }

        // Texto informativo
        layout.addView(TextView(this).apply {
            text = "Dueño: Usuario Logueado ($idClienteLogueado)"
            setTextColor(Color.parseColor("#FF6600"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
        })

        // ✅ CORREGIDO: Uso explícito de Contexto (this@FormOrdenServicioActivity)
        fun addField(label: String, hint: String, type: Int = InputType.TYPE_CLASS_TEXT) =
            EditText(this@FormOrdenServicioActivity).apply {
                layout.addView(TextView(this@FormOrdenServicioActivity).apply {
                    text = label
                    setTextColor(Color.parseColor("#FF6600"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 12 }
                })

                setHint(hint)
                inputType = type
                setTextColor(Color.WHITE)
                setHintTextColor(Color.parseColor("#888888"))
                background = ColorDrawable(Color.parseColor("#252525"))
                setPadding(12, 12, 12, 12)
                textSize = 15f
                textCursorDrawable = ColorDrawable(Color.parseColor("#FF6600"))
                layout.addView(this)
            }

        val etPlaca = addField("Placa:", "Ej: BGT657")
        val etModelo = addField("Modelo:", "Ej: 390")
        val etMarca = addField("Marca:", "Ej: KTM, Honda")
        val etRecorrido = addField("Recorrido (km):", "0", InputType.TYPE_CLASS_NUMBER)

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

        val dialog = builder.create()
        dialog.setView(layout)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#1A1A1A")))
            setDimAmount(0.5f)
            setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        dialog.show()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRegister.setOnClickListener {
            val placa = etPlaca.text.toString().trim()
            val modelo = etModelo.text.toString().trim()
            val marca = etMarca.text.toString().trim()

            if (placa.isEmpty() || modelo.isEmpty() || marca.isEmpty()) {
                Toast.makeText(this@FormOrdenServicioActivity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val moto = Moto(
                idMotos = null,
                idClientes = idClienteLogueado,
                placa = placa,
                modelo = modelo,
                marca = marca,
                recorrido = etRecorrido.text.toString().trim()
            )

            dialog.dismiss()

            lifecycleScope.launch {
                try {
                    val response = ApiAndroid.apiService.crearMoto(moto)
                    if (!response.isSuccessful || response.body()?.success != true) {
                        val msg = response.body()?.message ?: response.errorBody()?.string() ?: "Error desconocido"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: $msg", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    val data = response.body()?.data

                    // ✅ CORREGIDO: Extracción del ID compatible con tu modelo
                    val nuevaId: Int = when (data) {
                        is Moto -> {
                            // Tu Logcat muestra: Moto(..., insertId=6, ...)
                            // Aquí capturamos el ID del objeto Moto directamente
                            (data.insertId ?: data.idMotos) ?: -1
                        }
                        is Map<*, *> -> {
                            // Caso si usas ApiResponse<Any> en el futuro
                            val raw = data["insertId"] ?: data["idMotos"]
                            when (raw) {
                                is Int -> raw
                                is Number -> raw.toInt()
                                else -> -1
                            }
                        }
                        else -> -1
                    }

                    withContext(Dispatchers.Main) {
                        if (nuevaId > 0) {
                            listaMotos = listaMotos.toMutableList() + ItemSpinner(
                                displayName = "$modelo ($placa)",
                                id = nuevaId,
                                precio = 0.0,
                                garantia = "0",
                                realName = modelo
                            )

                            val nuevoAdapter = createKtmSpinnerAdapter(listaMotos.map { it.displayName })
                            spinnerMotos.adapter = nuevoAdapter
                            nuevoAdapter.notifyDataSetChanged()

                            // ✅ Actualización inmediata del Spinner
                            spinnerMotos.post {
                                spinnerMotos.setSelection(listaMotos.lastIndex)
                                spinnerMotos.requestFocus()
                            }

                            Toast.makeText(this@FormOrdenServicioActivity, "✅ Moto registrada y seleccionada", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("MOTO_UI", "❌ FALLO EXTRACCIÓN ID. Data recibida: $data")
                            Toast.makeText(this@FormOrdenServicioActivity, "⚠️ Moto creada pero no se recibió ID válido", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MOTO_API", "Error: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
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
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 24, 32, 24); setBackgroundColor(Color.parseColor("#1A1A1A"))
        }
        inputLayout.addView(TextView(this).apply {
            text = "Servicio:"; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
        })
        val spServicio = Spinner(this).apply {
            background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
            adapter = createKtmSpinnerAdapter(listaServicios.map { it.displayName })
        }
        inputLayout.addView(spServicio)
        inputLayout.addView(TextView(this).apply {
            text = "Producto (Opcional):"; setTextColor(Color.parseColor("#FF6600")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 }
        })
        val spProducto = Spinner(this).apply {
            background = ColorDrawable(Color.parseColor("#252525")); setPadding(12, 12, 12, 12)
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
                Toast.makeText(this@FormOrdenServicioActivity, "Selecciona al menos uno", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if ((selServ != null && listaDetalles.any { it.idServicios == selServ.id }) ||
                (selProd != null && listaDetalles.any { it.idProductos == selProd.id })) {
                Toast.makeText(this@FormOrdenServicioActivity, "Ya está en la lista", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val precioTotal = (selServ?.precio ?: 0.0) + (selProd?.precio ?: 0.0)
            val garantiaFinal = Math.max(selServ?.garantia?.toIntOrNull() ?: 0, selProd?.garantia?.toIntOrNull() ?: 0)

            listaDetalles.add(Detalles_orden_servicio(
                idDetalleOrden = null,
                idOrden = null,
                idServicios = selServ?.id,
                idProductos = selProd?.id,
                nombreServicio = selServ?.realName,
                nombreProducto = selProd?.realName,
                precio = precioTotal,
                garantia = garantiaFinal
            ))
            detalleAdapter.submitList(listaDetalles.toList())
            Toast.makeText(this@FormOrdenServicioActivity, "Agregado a la orden", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCELAR") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun guardarOrdenCompleta() {
        if (idClienteLogueado <= 0) {
            Toast.makeText(this, "⚠️ No hay sesión activa.", Toast.LENGTH_SHORT).show()
            return
        }
        val posMoto = spinnerMotos.selectedItemPosition
        if (posMoto < 0 || posMoto >= listaMotos.size) {
            Toast.makeText(this, "Selecciona una Moto válida", Toast.LENGTH_SHORT).show()
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
                    idClientes = idClienteLogueado,
                    idMotos = listaMotos[posMoto].id,
                    idAdministrador = 1,
                    idTecnicos = 1,
                    fechaInicio = formatoFecha.format(Date()),
                    fechaEstimada = formatoFecha.format(Date(System.currentTimeMillis() + 86400000)),
                    fechaFin = null,
                    estado = "PENDIENTE"
                )

                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)
                if (!responseOrden.isSuccessful) throw Exception("Error HTTP: ${responseOrden.code()}")

                val jsonString = responseOrden.body()?.string() ?: "{}"
                val json = JSONObject(jsonString)
                if (!json.optBoolean("success", false)) throw Exception(json.optString("message", "Error al crear la orden"))

                val data = json.optJSONObject("data") ?: json
                val idOrdenCreada = data.optInt("ID_ORDEN_SERVICIO", 0).takeIf { it != 0 } ?: data.optInt("insertId", 0)
                if (idOrdenCreada == 0) throw Exception("El servidor no devolvió un ID válido para la orden.")
                Log.d("GUARDAR_ORDEN", "✅ ID recibido: $idOrdenCreada")

                val detallesAGuardar = listaDetalles.map { it.copy(idOrden = idOrdenCreada) }
                var detallesFallidos = 0

                detallesAGuardar.forEach { detalle ->
                    try {
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
                        Toast.makeText(this@FormOrdenServicioActivity, "✅ Orden creada exitosamente!", Toast.LENGTH_LONG).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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