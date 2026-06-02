package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Detalles_orden_servicio
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemSpinner(val displayName: String, val id: Int, val extraData: Any? = null)

class FormOrdenServicioActivity : AppCompatActivity() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerMotos: Spinner
    private lateinit var spinnerTecnicos: Spinner
    private lateinit var spinnerServicios: Spinner
    private lateinit var spinnerProductos: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var listaClientes: List<ItemSpinner> = listOf()
    private var listaMotos: List<ItemSpinner> = listOf()
    private var listaTecnicos: List<ItemSpinner> = listOf()
    private var listaServicios: List<ItemSpinner> = listOf()
    private var listaProductos: List<ItemSpinner> = listOf()

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_orden_servicio)

        setupToolbar()
        initViews()
        cargarDatosDesdeBD()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        spinnerClientes = findViewById(R.id.spinnerClientes)
        spinnerMotos = findViewById(R.id.spinnerMotos)
        spinnerTecnicos = findViewById(R.id.spinnerTecnicos)
        spinnerServicios = findViewById(R.id.spinnerServicios)
        spinnerProductos = findViewById(R.id.spinnerProductos)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun cargarDatosDesdeBD() {
        val estados = arrayOf("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA")
        setupSpinner(spinnerEstado, estados.map { ItemSpinner(it, -1) })

        lifecycleScope.launch {
            try {
                listaClientes = listOf(
                    ItemSpinner("Bok", 1),
                    ItemSpinner("Rosa", 2),
                    ItemSpinner("Teodoro", 3)
                )
                setupSpinner(spinnerClientes, listaClientes)

                listaMotos = listOf(
                    ItemSpinner("DUKE 1290 (BGT657)", 2),
                    ItemSpinner("DUKE 250 (AKT654)", 3),
                    ItemSpinner("DUKE 390 (LMT564)", 4)
                )
                setupSpinner(spinnerMotos, listaMotos)

                listaTecnicos = listOf(
                    ItemSpinner("Técnico 001", 1),
                    ItemSpinner("Técnico 002", 2),
                    ItemSpinner("Técnico 003", 3)
                )
                setupSpinner(spinnerTecnicos, listaTecnicos)

                listaServicios = listOf(
                    ItemSpinner("Mantenimiento preventivo", 1, ExtraServicio(garantia = 30, precio = 180.0)),
                    ItemSpinner("Reparación por daños", 2, ExtraServicio(garantia = 30, precio = 200.0)),
                    ItemSpinner("Diagnosticos motor", 4, ExtraServicio(garantia = 10, precio = 600.0))
                )
                setupSpinner(spinnerServicios, listaServicios)

                listaProductos = listOf(
                    ItemSpinner("Aceite Motorex", 1, ExtraProducto(garantia = 35, precio = 12000.0)),
                    ItemSpinner("Cadena Rombo", 2, ExtraProducto(garantia = 30, precio = 180000.0)),
                    ItemSpinner("Chaqueta Rocket DUKE", 4, ExtraProducto(garantia = 10, precio = 179999.99))
                )
                setupSpinner(spinnerProductos, listaProductos)

            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, lista: List<ItemSpinner>) {
        val nombres = lista.map { it.displayName }
        val adapter = ArrayAdapter(this, R.layout.item_spinner_selected, nombres)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        btnCancelar.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarOrden() }
    }

    private fun guardarOrden() {
        val posCliente = spinnerClientes.selectedItemPosition
        val posMoto = spinnerMotos.selectedItemPosition
        val posTecnico = spinnerTecnicos.selectedItemPosition
        val posServicio = spinnerServicios.selectedItemPosition
        val posProducto = spinnerProductos.selectedItemPosition
        val estado = spinnerEstado.selectedItem.toString()

        if (posCliente < 0 || posMoto < 0 || posTecnico < 0) {
            Toast.makeText(this, "❌ Debes seleccionar Cliente, Moto y Técnico", Toast.LENGTH_SHORT).show()
            return
        }

        val idCliente = listaClientes[posCliente].id
        val idMoto = listaMotos[posMoto].id
        val idTecnico = listaTecnicos[posTecnico].id
        val servicioSeleccionado = if (posServicio >= 0) listaServicios[posServicio] else null
        val productoSeleccionado = if (posProducto >= 0) listaProductos[posProducto] else null

        if (servicioSeleccionado == null && productoSeleccionado == null) {
            Toast.makeText(this, "❌ Selecciona al menos un Servicio o Producto", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val ahora = formatoFecha.format(Date())
                val estimada = formatoFecha.format(Date(System.currentTimeMillis() + 86400000))

                val orden = Orden_servicio(
                    idOrden_servicio = null,
                    idClientes = idCliente,
                    idAdministrador = 1,
                    idTecnicos = idTecnico,
                    idMotos = idMoto,
                    fechaInicio = ahora,
                    fechaEstimada = estimada,
                    fechaFin = null,
                    estado = estado
                )

                // 1. GUARDAR ORDEN PRINCIPAL
                val responseOrden = ApiAndroid.apiService.crearOrdenServicio(orden)

                // ✅ CORRECCIÓN: .body() y .message() son MÉTODOS en Retrofit Response
                if (!responseOrden.isSuccessful || responseOrden.body() == null) {
                    throw Exception("Error al crear orden: ${responseOrden.message()}")
                }

                // ✅ CORRECCIÓN: Acceder al ID dentro de la estructura ApiResponse -> data -> orden
                val idOrdenCreada = responseOrden.body()?.data?.idOrden_servicio
                if (idOrdenCreada == null) throw Exception("No se recibió ID de orden")

                // 2. PREPARAR DETALLES
                val detalles = mutableListOf<Detalles_orden_servicio>()

                if (servicioSeleccionado != null) {
                    val extra = servicioSeleccionado.extraData as? ExtraServicio
                    detalles.add(
                        Detalles_orden_servicio(
                            idOrden = idOrdenCreada,
                            idServicios = servicioSeleccionado.id,
                            idProductos = null,
                            garantia = extra?.garantia,
                            precio = extra?.precio
                        )
                    )
                }

                if (productoSeleccionado != null) {
                    val extra = productoSeleccionado.extraData as? ExtraProducto
                    detalles.add(
                        Detalles_orden_servicio(
                            idOrden = idOrdenCreada,
                            idServicios = null,
                            idProductos = productoSeleccionado.id,
                            garantia = extra?.garantia,
                            precio = extra?.precio
                        )
                    )
                }

                // 3. GUARDAR DETALLES
                for (detalle in detalles) {
                    val responseDetalle = ApiAndroid.apiService.crearDetalleOrden(detalle)

                    // ✅ CORRECCIÓN: crearDetalleOrden retorna ApiResponse directo (sin Response wrapper)
                    if (!responseDetalle.success) {
                        Toast.makeText(this@FormOrdenServicioActivity,
                            "⚠️ Falló al guardar un detalle, pero la orden se creó", Toast.LENGTH_SHORT).show()
                    }
                }

                Toast.makeText(this@FormOrdenServicioActivity, "✅ Orden creada exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (!isFinishing) {
                    btnGuardar.isEnabled = true
                    btnGuardar.text = "GUARDAR ORDEN"
                }
            }
        }
    }
}

// Clases auxiliares para llevar datos extra desde el Spinner
data class ExtraServicio(val garantia: Int? = null, val precio: Double? = null)
data class ExtraProducto(val garantia: Int? = null, val precio: Double? = null)