package com.example.sgost.data

import com.example.sgost.api.ApiAndroid
import com.example.sgost.model.Producto
import com.example.sgost.model.Servicio

object ProductoRepository {

    suspend fun obtenerProductosDesdeApi(): List<Producto> {
        return try {
            // 1️⃣ Obtener productos reales
            val responseProductos = ApiAndroid.apiService.obtenerProductos()
            val productos = mutableListOf<Producto>()

            if (responseProductos.success == true) {
                responseProductos.data?.let { lista ->
                    productos.addAll(
                        lista.map { producto ->
                            Producto(
                                idProductos = producto.idProductos,
                                categoria = producto.categoria,
                                marca = producto.marca,
                                nombre = producto.nombre,
                                garantiaRaw = producto.garantiaRaw,
                                precioRaw = producto.precioRaw,
                                cantidad = producto.cantidad,
                                estado = producto.estado,
                                tipo = "Producto"
                            )
                        }
                    )
                }
            }

            // 2️⃣ Intentar obtener servicios reales
            try {
                val responseServicios = ApiAndroid.apiService.obtenerServicios()
                if (responseServicios.success == true) {
                    responseServicios.data?.let { lista ->
                        productos.addAll(
                            lista.map { servicio ->
                                Producto(
                                    idProductos = servicio.idServicios,
                                    categoria = servicio.categoria,
                                    marca = null,
                                    nombre = servicio.nombre,
                                    garantiaRaw = servicio.garantiaRaw,
                                    precioRaw = servicio.precioRaw,
                                    cantidad = null,
                                    estado = servicio.estado,
                                    tipo = "Servicio"
                                )
                            }
                        )
                    }
                } else {
                    // Si falla (401), usamos servicios de ejemplo
                    productos.addAll(obtenerServiciosEjemplo())
                }
            } catch (e: Exception) {
                // Si hay excepción, usamos servicios de ejemplo
                productos.addAll(obtenerServiciosEjemplo())
            }

            // 3️⃣ Si NO hay productos (ni reales ni de ejemplo), usamos fallback completo
            if (productos.isEmpty()) {
                obtenerProductosEjemplo()
            } else {
                productos
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla la conexión, usamos el fallback completo
            obtenerProductosEjemplo()
        }
    }

    // Servicios de ejemplo (los que están en la base de datos según la imagen)
    private fun obtenerServiciosEjemplo(): List<Producto> {
        return listOf(
            Producto(
                idProductos = 1,
                nombre = "Mantenimiento preventivo",
                categoria = "Mantenimientos",
                marca = null,
                garantiaRaw = "100",
                precioRaw = "212000",
                cantidad = null,
                estado = "No disponible",
                tipo = "Servicio"
            ),
            Producto(
                idProductos = 2,
                nombre = "Reparación por daños",
                categoria = "Reparaciones",
                marca = null,
                garantiaRaw = "30",
                precioRaw = "200000",
                cantidad = null,
                estado = "Disponible",
                tipo = "Servicio"
            ),
            Producto(
                idProductos = 3,
                nombre = "Instalaciones de accesorios",
                categoria = "Instalaciones",
                marca = null,
                garantiaRaw = "14",
                precioRaw = "300000",
                cantidad = null,
                estado = "Disponible",
                tipo = "Servicio"
            ),
            Producto(
                idProductos = 4,
                nombre = "Diagnosticos motor",
                categoria = "Diagnosticos",
                marca = null,
                garantiaRaw = "10",
                precioRaw = "600000",
                cantidad = null,
                estado = "Disponible",
                tipo = "Servicio"
            )
        )
    }

    // Fallback completo (productos y servicios de ejemplo)
    private fun obtenerProductosEjemplo(): List<Producto> {
        return listOf(
            Producto(
                idProductos = 1,
                nombre = "Filtro de aire (ejemplo)",
                categoria = "Filtros",
                marca = "KTM",
                garantiaRaw = "6",
                precioRaw = "45.0",
                cantidad = 10,
                estado = "Disponible",
                tipo = "Producto"
            ),
            Producto(
                idProductos = 2,
                nombre = "Kit de cadena (ejemplo)",
                categoria = "Transmisión",
                marca = "KTM",
                garantiaRaw = "12",
                precioRaw = "150.0",
                cantidad = 5,
                estado = "Disponible",
                tipo = "Producto"
            ),
            Producto(
                idProductos = 3,
                nombre = "Batería de litio (ejemplo)",
                categoria = "Eléctricos",
                marca = "KTM",
                garantiaRaw = "18",
                precioRaw = "95.0",
                cantidad = 8,
                estado = "Disponible",
                tipo = "Producto"
            ),
            Producto(
                idProductos = 4,
                nombre = "Cambio de aceite (ejemplo)",
                categoria = "Mantenimiento",
                marca = null,
                garantiaRaw = "3",
                precioRaw = "120.0",
                cantidad = null,
                estado = "Activo",
                tipo = "Servicio"
            ),
            Producto(
                idProductos = 5,
                nombre = "Revisión de frenos (ejemplo)",
                categoria = "Frenos",
                marca = null,
                garantiaRaw = "3",
                precioRaw = "80.0",
                cantidad = null,
                estado = "Activo",
                tipo = "Servicio"
            ),
            Producto(
                idProductos = 6,
                nombre = "Alineación de ruedas (ejemplo)",
                categoria = "Suspensión",
                marca = null,
                garantiaRaw = "1",
                precioRaw = "60.0",
                cantidad = null,
                estado = "Activo",
                tipo = "Servicio"
            )
        )
    }
}