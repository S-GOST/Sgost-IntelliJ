package com.example.sgost

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.CarritoItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CatalogoActivity : AppCompatActivity() {

    private lateinit var rvCatalogo: RecyclerView
    private lateinit var adapter: CatalogoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmptyState: LinearLayout
    private lateinit var fabCarrito: FloatingActionButton
    private lateinit var tvCartCount: TextView

    private var todosItems = listOf<CarritoItem>()
    private var filtroActual = "TODOS"

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        setupToolbar()
        initViews()
        setupAdapter()
        setupFilterChips()
        setupFab()

        // Inicializar CartManager (si no se hizo en Application)
        CartManager.init(applicationContext)

        cargarDatos()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        rvCatalogo = findViewById(R.id.rvCatalogo)
        progressBar = findViewById(R.id.progressBar)
        llEmptyState = findViewById(R.id.llEmptyState)
        fabCarrito = findViewById(R.id.fabCarrito)
        tvCartCount = findViewById(R.id.tvCartCount)
    }

    private fun setupAdapter() {
        adapter = CatalogoAdapter { item, pos ->
            CartManager.addItem(item)
            updateCartBadge()
            adapter.notifyItemChanged(pos)
        }
        rvCatalogo.layoutManager = LinearLayoutManager(this)
        rvCatalogo.adapter = adapter
    }

    private fun setupFilterChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilter)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach { id ->
                val chip = findViewById<Chip>(id)
                filtroActual = when (chip.id) {
                    R.id.chipTodos -> "TODOS"
                    R.id.chipServicios -> "SERVICIO"
                    R.id.chipProductos -> "PRODUCTO"
                    else -> "TODOS"
                }
                filtrarYMostrar()
            }
        }
    }

    private fun setupFab() {
        fabCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        if (count > 0) {
            tvCartCount.text = "$count"
            tvCartCount.visibility = View.VISIBLE
        } else {
            tvCartCount.visibility = View.GONE
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                if (ApiAndroid.isReady) {
                    val respServ = ApiAndroid.apiService.obtenerServicios()
                    val respProd = ApiAndroid.apiService.obtenerProductos()

                    val servicios = respServ.data?.map { s ->
                        CarritoItem(
                            id = s.idServicios.toString(),
                            nombre = s.nombre ?: "Sin nombre",
                            precioUnitario = s.precio ?: 0.0,
                            cantidad = 1,
                            tipo = "SERVICIO",
                            categoria = "MANTENIMIENTO" // <--- AGREGADO AQUÍ
                        )
                    } ?: emptyList()

                    val productos = respProd.data?.map { p ->
                        CarritoItem(
                            id = p.idProductos.toString(),
                            nombre = "${p.marca} ${p.nombre}".trim().ifEmpty { "Producto" },
                            precioUnitario = p.precio ?: 0.0,
                            cantidad = 1,
                            tipo = "PRODUCTO",
                            categoria = "REPUESTOS" // <--- AGREGADO AQUÍ
                        )
                    } ?: emptyList()

                    todosItems = servicios + productos
                    filtrarYMostrar()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun filtrarYMostrar() {
        // ✅ CORRECCIÓN: Usamos 'tipo' para filtrar en lugar de 'categoria'
        val filtrados = if (filtroActual == "TODOS") todosItems
        else todosItems.filter { it.tipo == filtroActual }

        adapter.submitList(filtrados)
        llEmptyState.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
        rvCatalogo.visibility = if (filtrados.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        adapter.refreshCartQuantities()
    }
}