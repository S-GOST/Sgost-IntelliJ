package com.example.sgost

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Orden_servicio
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormOrdenServicioActivity : AppCompatActivity() {

    // 🔹 Referencias UI
    private lateinit var tvFormTitle: TextView
    private lateinit var etIdCliente: TextInputEditText
    private lateinit var etIdAdmin: TextInputEditText
    private lateinit var etIdTecnico: TextInputEditText
    private lateinit var etIdMoto: TextInputEditText
    private lateinit var etFechaInicio: TextInputEditText
    private lateinit var etFechaEstimada: TextInputEditText
    private lateinit var etFechaFin: TextInputEditText
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var ordenEditar: Orden_servicio? = null

    // 📅 Formateador de fechas (Formato que espera tu BD: yyyy-MM-dd HH:mm:ss)
    private val formatoDB = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_orden_servicio)

        setupToolbar()
        initViews()
        cargarModoEdicion()
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
        tvFormTitle = findViewById(R.id.tvFormTitle)
        etIdCliente = findViewById(R.id.etIdCliente)
        etIdAdmin = findViewById(R.id.etIdAdmin)
        etIdTecnico = findViewById(R.id.etIdTecnico)
        etIdMoto = findViewById(R.id.etIdMoto)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaEstimada = findViewById(R.id.etFechaEstimada)
        etFechaFin = findViewById(R.id.etFechaFin)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun cargarModoEdicion() {
        ordenEditar = intent.getParcelableExtra("orden_extra")

        if (ordenEditar != null) {
            val o = ordenEditar!!
            tvFormTitle.text = "Editar Orden"
            btnGuardar.text = "ACTUALIZAR"

            etIdCliente.setText(o.idClientes?.toString())
            etIdAdmin.setText(o.idAdministrador?.toString())
            etIdTecnico.setText(o.idTecnicos?.toString())
            etIdMoto.setText(o.idMotos?.toString())
            etFechaInicio.setText(o.fechaInicio)
            etFechaEstimada.setText(o.fechaEstimada)
            etFechaFin.setText(o.fechaFin)

            // Seleccionar estado en Spinner
            val estados = arrayOf("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA")
            val posicion = estados.indexOfFirst { it.equals(o.estado, ignoreCase = true) }
            spinnerEstado.setSelection(posicion.coerceAtLeast(0))

        } else {
            tvFormTitle.text = "Crear Orden de Servicio"
            btnGuardar.text = "GUARDAR"

            // 📅 Auto-fill fechas al crear
            val ahora = Calendar.getInstance()
            etFechaInicio.setText(formatoDB.format(ahora.time))

            val estimada = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            etFechaEstimada.setText(formatoDB.format(estimada.time))

            etFechaFin.setText("")
        }
    }

    private fun setupListeners() {
        // Configurar Spinner de Estado
        val estados = arrayOf("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapterEstado

        btnCancelar.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarOrden() }
    }

    private fun guardarOrden() {
        val idCliente = etIdCliente.text.toString().trim().toIntOrNull()
        val idAdmin = etIdAdmin.text.toString().trim().toIntOrNull()
        val idTecnico = etIdTecnico.text.toString().trim().toIntOrNull()
        val idMoto = etIdMoto.text.toString().trim().toIntOrNull()
        val fechaInicio = etFechaInicio.text.toString().trim()
        val fechaEstimada = etFechaEstimada.text.toString().trim()
        val fechaFin = etFechaFin.text.toString().trim()
        val estado = spinnerEstado.selectedItem.toString()

        // ✅ Validaciones básicas
        if (idCliente == null || idMoto == null || fechaInicio.isEmpty()) {
            Toast.makeText(this, "❌ Cliente, Moto y Fecha de inicio son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnCancelar.isEnabled = false
        btnGuardar.text = if (ordenEditar != null) "Actualizando..." else "Guardando..."

        lifecycleScope.launch {
            try {
                val orden = Orden_servicio(
                    idOrden_servicio = ordenEditar?.idOrden_servicio,
                    idClientes = idCliente,
                    idAdministrador = idAdmin,
                    idTecnicos = idTecnico,
                    idMotos = idMoto,
                    fechaInicio = fechaInicio,
                    fechaEstimada = fechaEstimada.takeIf { it.isNotEmpty() } ?: null,
                    fechaFin = fechaFin.takeIf { it.isNotEmpty() } ?: null,
                    estado = estado
                )

                val response = if (ordenEditar != null) {
                    ApiAndroid.apiService.actualizarOrdenServicio(orden.idOrden_servicio.toString(), orden)
                } else {
                    ApiAndroid.apiService.crearOrdenServicio(orden)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@FormOrdenServicioActivity, "✅ Orden ${if (ordenEditar != null) "actualizada" else "creada"} exitosamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@FormOrdenServicioActivity, "❌ ${response.body()?.message ?: "Error al guardar la orden"}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FormOrdenServicioActivity, "❌ Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (!isFinishing) {
                    btnGuardar.isEnabled = true
                    btnCancelar.isEnabled = true
                    btnGuardar.text = if (ordenEditar != null) "ACTUALIZAR" else "GUARDAR"
                }
            }
        }
    }
}