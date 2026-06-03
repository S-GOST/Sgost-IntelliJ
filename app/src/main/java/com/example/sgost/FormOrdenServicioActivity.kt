package com.example.sgost

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.adapter.DetalleOrdenAdapter
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Detalles_orden_servicio
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemSpinner(val displayName: String, val id: Int)

class FormOrdenServicioActivity : AppCompatActivity() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerMotos: Spinner
    private lateinit var spinnerServicios: Spinner
    private lateinit var spinnerProductos: Spinner
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
        spinnerServicios = findViewById(R.id.spinnerServicios) ?: throw IllegalStateException("Falta spinnerServicios")
        spinnerProductos = findViewById(R.id.spinnerProductos) ?: throw IllegalStateException("Falta spinnerProductos")
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
        val estados = listOf("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA")
        setupSpinner(spinnerEstado, estados.map { ItemSpinner(it, -1) })
        spinnerEstado.setSelection(0)

        // 🔽 REEMPLAZAR CON LLAMADAS A TU API CUANDO ESTÉN LISTAS
        listaClientes = listOf(ItemSpinner("Juan Pérez", 1), ItemSpinner("María López", 2))
        listaMotos = listOf(ItemSpinner("Duke 390", 1), ItemSpinner("Tracer 900", 2))
        listaServicios = listOf(ItemSpinner("Cambio Aceite", 101), ItemSpinner("Revisión Frenos", 102), ItemSpinner("Afinación", 103))
        listaProductos = listOf(ItemSpinner("Aceite Motul", 201), ItemSpinner("Filtro K&N", 202), ItemSpinner("Cadena DID", 203))

        setupSpinner(spinnerClientes, listaClientes)
        setupSpinner(spinnerMotos, listaMotos)
        setupSpinner(spinnerServicios, listaServicios)
        setupSpinner(spinnerProductos, listaProductos)
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

    // 🆕 MODAL NUEVO CLIENTE
    private fun mostrarModalNuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("👤 Registrar Nuevo Cliente")
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        val labelNombre = TextView(this).apply { text = "Nombre completo:"; setTextColor(Color.WHITE) }
        inputLayout.addView(labelNombre)
        val etNombre = EditText(this).apply { hint = "Ej: Carlos Gómez"; setTextColor(Color.WHITE) }
        inputLayout.addView(etNombre)

        val labelTel = TextView(this).apply {
            text = "Teléfono:"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelTel)
        val etTel = EditText(this).apply { hint = "3001234567"; inputType = InputType.TYPE_CLASS_PHONE; setTextColor(Color.WHITE) }
        inputLayout.addView(etTel)

        builder.setView(inputLayout)
        builder.setPositiveButton("Registrar") { _, _ ->
            val nombre = etNombre.text.toString().trim()
            val telefono = etTel.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this@FormOrdenServicioActivity, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            lifecycleScope.launch {
                try {
                    // 🔽 REEMPLAZAR CON TU API REAL:
                    // val response = ApiAndroid.apiService.registrarCliente(Cliente(nombre, telefono))
                    // val nuevoId = response.body()?.data?.idCliente ?: throw Exception("No se recibió ID")

                    val nuevoId = (listaClientes.maxOfOrNull { it.id } ?: 0) + 1
                    val nuevoCliente = ItemSpinner(nombre, nuevoId)
                    listaClientes = listaClientes + nuevoCliente
                    setupSpinner(spinnerClientes, listaClientes)
                    spinnerClientes.setSelection(spinnerClientes.count - 1)
                    Toast.makeText(this@FormOrdenServicioActivity, "✅ Cliente registrado", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // 🆕 MODAL NUEVA MOTO
    private fun mostrarModalNuevaMoto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🏍️ Registrar Nueva Moto")
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            setBackgroundColor(Color.parseColor("#1A1A1A"))
        }

        val labelMarca = TextView(this).apply { text = "Marca/Modelo:"; setTextColor(Color.WHITE) }
        inputLayout.addView(labelMarca)
        val etMarca = EditText(this).apply { hint = "Ej: BMW G 310 R"; setTextColor(Color.WHITE) }
        inputLayout.addView(etMarca)

        val labelPlaca = TextView(this).apply {
            text = "Placa:"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelPlaca)
        val etPlaca = EditText(this).apply { hint = "Ej: ABC-123"; setTextColor(Color.WHITE) }
        inputLayout.addView(etPlaca)

        builder.setView(inputLayout)
        builder.setPositiveButton("Registrar") { _, _ ->
            val modelo = etMarca.text.toString().trim()
            val placa = etPlaca.text.toString().trim()
            if (modelo.isEmpty()) {
                Toast.makeText(this@FormOrdenServicioActivity, "El modelo es obligatorio", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            lifecycleScope.launch {
                try {
                    // 🔽 REEMPLAZAR CON TU API REAL:
                    // val response = ApiAndroid.apiService.registrarMoto(Moto(modelo, placa))
                    // val nuevoId = response.body()?.data?.idMoto ?: throw Exception("No se recibió ID")

                    val nuevoId = (listaMotos.maxOfOrNull { it.id } ?: 0) + 1
                    val nuevaMoto = ItemSpinner("$modelo ($placa)", nuevoId)
                    listaMotos = listaMotos + nuevaMoto
                    setupSpinner(spinnerMotos, listaMotos)
                    spinnerMotos.setSelection(spinnerMotos.count - 1)
                    Toast.makeText(this@FormOrdenServicioActivity, "✅ Moto registrada", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun mostrarModalSeleccionDB() {
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
        }
        spServicio.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaServicios.map { it.displayName })
        inputLayout.addView(spServicio)

        val labelProd = TextView(this).apply {
            text = "📦 Producto (Opcional):"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelProd)
        val spProducto = Spinner(this).apply { setPadding(0, 10, 0, 10) }
        spProducto.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaProductos.map { it.displayName })
        inputLayout.addView(spProducto)

        val labelPrecio = TextView(this).apply {
            text = "💰 Precio:"; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10 }
        }
        inputLayout.addView(labelPrecio)
        val etPrecio = EditText(this).apply { hint = "0.00"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
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
                val selServ = listaServicios[posServ]
                listaDetalles.add(Detalles_orden_servicio(
                    idDetalleOrden = null, idOrden = null, idServicios = selServ.id, idProductos = null,
                    nombreServicio = selServ.displayName, nombreProducto = null, precio = precio, garantia = garantia
                ))
            }
            if (posProd >= 0) {
                val selProd = listaProductos[posProd]
                listaDetalles.add(Detalles_orden_servicio(
                    idDetalleOrden = null, idOrden = null, idServicios = null, idProductos = selProd.id,
                    nombreServicio = null, nombreProducto = selProd.displayName, precio = precio, garantia = garantia
                ))
            }
            detalleAdapter.submitList(listaDetalles.toList())
            Toast.makeText(this@FormOrdenServicioActivity, "✅ Agregado a la orden", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun guardarOrdenCompleta() {
        val posCliente = spinnerClientes.selectedItemPosition
        val posMoto = spinnerMotos.selectedItemPosition
        if (posCliente < 0 || posMoto < 0) {
            Toast.makeText(this, "❌ Selecciona Cliente y Moto", Toast.LENGTH_SHORT).show()
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
                if (!responseOrden.isSuccessful) throw Exception("Error API: ${responseOrden.code()}")

                val idOrdenCreada = responseOrden.body()?.data?.idOrden_servicio
                if (idOrdenCreada == null) throw Exception("No se recibió ID de orden del servidor")

                val detallesAGuardar = listaDetalles.map { it.copy(idOrden = idOrdenCreada) }
                for (detalle in detallesAGuardar) {
                    ApiAndroid.apiService.crearDetalleOrden(detalle)
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