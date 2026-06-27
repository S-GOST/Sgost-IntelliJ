package com.example.sgost.api

import com.example.sgost.model.Administrador
import com.example.sgost.model.Cliente
import com.example.sgost.model.Tecnico
import com.example.sgost.model.Orden_servicio
import com.example.sgost.model.Detalles_orden_servicio
import com.example.sgost.model.Moto
import com.example.sgost.model.Producto
import com.example.sgost.model.Servicio
import com.example.sgost.model.ApiResponse
import com.example.sgost.model.LoginRequest
import com.example.sgost.model.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 🔐 LOGIN
    @POST("api/admins/login")
    suspend fun loginAdmin(@Body request: LoginRequest): LoginResponse

    @POST("api/tecnicos/login")
    suspend fun loginTecnico(@Body request: LoginRequest): LoginResponse

    @POST("api/clientes/login")
    suspend fun loginCliente(@Body request: LoginRequest): LoginResponse

    //---------------------------------------------------------------------------------------------------------------------------------//

    // ADMINISTRADORES
    @POST("api/admins/insertar")
    suspend fun crearAdministradores(@Body administrador: Administrador): ApiResponse<Tecnico>

    @GET("api/admins/obtener")
    suspend fun obtenerAdministradores(): ApiResponse<List<Administrador>>

    @GET("api/admins/buscar/{id}")
    suspend fun obtenerAdministradoresPorId(@Path("id") id: String): Response<ApiResponse<Administrador>>

    @PUT("api/admins/actualizar/{id}")
    suspend fun actualizarAdministradores(
        @Path("id") id: String,
        @Body administrador: Administrador
    ): ApiResponse<Tecnico>

    @DELETE("api/admins/eliminar/{id}")
    suspend fun eliminarAdministradores(@Path("id") id: String): ApiResponse<Unit>

    //-------------------------------------------------------------------------------------------------------------------------------------------//

    // TÉCNICOS
    @POST("api/tecnicos/insertar")
    suspend fun crearTecnico(@Body tecnico: Tecnico): ApiResponse<Tecnico>

    @GET("api/tecnicos/obtener")
    suspend fun obtenerTecnicos(): ApiResponse<List<Tecnico>>

    @GET("api/tecnicos/buscar/{id}")
    suspend fun obtenertecnicosPorId(@Path("id") id: String): Response<ApiResponse<Tecnico>>

    @PUT("api/tecnicos/actualizar/{id}")
    suspend fun actualizarTecnico(
        @Path("id") id: String,
        @Body tecnico: Tecnico
    ): ApiResponse<Tecnico>

    @DELETE("api/tecnicos/eliminar/{id}")
    suspend fun eliminarTecnico(@Path("id") id: String): ApiResponse<Unit>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // CLIENTES
    @POST("api/clientes/insertar")
    suspend fun registrarCliente(@Body cliente: Cliente): Response<ApiResponse<Any>>

    @GET("api/clientes/obtener")
    suspend fun obtenerClientes(): Response<ApiResponse<List<Cliente>>>

    @GET("api/clientes/buscar/{id}")
    suspend fun obtenerClientePorId(@Path("id") id: String): Response<ApiResponse<Cliente>>

    @PUT("api/clientes/actualizar/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: String,
        @Body cliente: Cliente
    ): Response<ApiResponse<Cliente>>

    @DELETE("api/clientes/eliminar/{id}")
    suspend fun eliminarCliente(@Path("id") id: String): Response<ApiResponse<Unit>>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // MOTOS 🔹 NUEVO
    @POST("api/motos/insertar")
    suspend fun crearMoto(@Body moto: Moto): Response<ApiResponse<Moto>>

    @GET("api/motos/obtener")
    suspend fun obtenerMotos(): Response<ApiResponse<List<Moto>>>

    @GET("api/motos/buscar/{id}")
    suspend fun obtenerMotoPorId(@Path("id") id: String): Response<ApiResponse<Moto>>

    @PUT("api/motos/actualizar/{id}")
    suspend fun actualizarMoto(
        @Path("id") id: String,
        @Body moto: Moto
    ): ApiResponse<Moto>

    @DELETE("api/motos/eliminar/{id}")
    suspend fun eliminarMoto(@Path("id") id: String): ApiResponse<Unit>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // PRODUCTOS 🔹 NUEVO
    @POST("api/productos/insertar")
    suspend fun crearProducto(@Body producto: Producto): ApiResponse<Producto>

    @GET("api/productos/obtener")
    suspend fun obtenerProductos(): Response<ApiResponse<List<Producto>>>

    @GET("api/productos/buscar/{id}")
    suspend fun obtenerProductoPorId(@Path("id") id: String): Response<ApiResponse<Producto>>

    @PUT("api/productos/actualizar/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: String,
        @Body producto: Producto
    ): ApiResponse<Producto>

    @DELETE("api/productos/eliminar/{id}")
    suspend fun eliminarProducto(@Path("id") id: String): ApiResponse<Unit>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // SERVICIOS 🔹 NUEVO
    @POST("api/servicios/insertar")
    suspend fun crearServicio(@Body servicio: Servicio): ApiResponse<Servicio>

    @GET("api/servicios/obtener")
    suspend fun obtenerServicios(): Response<ApiResponse<List<Servicio>>>

    @GET("api/servicios/buscar/{id}")
    suspend fun obtenerServicioPorId(@Path("id") id: String): Response<ApiResponse<Servicio>>

    @PUT("api/servicios/actualizar/{id}")
    suspend fun actualizarServicio(
        @Path("id") id: String,
        @Body servicio: Servicio
    ): ApiResponse<Servicio>

    @DELETE("api/servicios/eliminar/{id}")
    suspend fun eliminarServicio(@Path("id") id: String): ApiResponse<Unit>

    //---------------------------------------------------------------------------------------------------------------------------------//

    // ORDEN SERVICIO
    @POST("api/ordenes_servicio/insertar")
    suspend fun crearOrdenServicio(@Body orden: Orden_servicio): Response<ResponseBody>

    @GET("api/ordenes_servicio/obtener")
    suspend fun obtenerOrdenServicio(): ApiResponse<List<Orden_servicio>>

    @GET("api/ordenes_servicio/mis-ordenes")
    suspend fun getMisOrdenes(@Header("Authorization") token: String): Response<ApiResponse<List<Orden_servicio>>>

    @GET("api/Ordenes_servicio/buscar/{id}")
    suspend fun obtenerOrdenServicioPorId(@Path("id") id: String): Response<ApiResponse<Orden_servicio>>

    @PUT("api/ordenes_servicio/actualizar/{id}")
    suspend fun actualizarOrdenServicio(
        @Path("id") id: String,
        @Body orden_servicio: Orden_servicio
    ): Response<ApiResponse<Orden_servicio>>

    @DELETE("api/ordenes_servicio/eliminar/{id}")
    suspend fun eliminarOrdenServicio(@Path("id") id: String): Response<ApiResponse<Unit>>

    //---------------------------------------------------------------------------------------------------------------------------------//
    // DETALLES ORDEN SERVICIO
    @POST("api/detalles_orden_servicio/insertar")
    suspend fun crearDetalleOrden(@Body detalles_orden_servicio: Detalles_orden_servicio): ApiResponse<Detalles_orden_servicio>

    @GET("api/detalles_orden_servicio/obtener")
    suspend fun obtenerDetallesOrden(): ApiResponse<List<Detalles_orden_servicio>>

    @GET("api/detalles_orden_servicio/por_orden/{idOrden}")
    suspend fun obtenerDetallesPorOrden(@Path("idOrden") id: String): ApiResponse<List<Detalles_orden_servicio>>

    @GET("api/detalles_orden_servicio/buscar/{id}")
    suspend fun obtenerDetallePorId(@Path("id") id: String): Response<ApiResponse<Detalles_orden_servicio>>

    @PUT("api/detalles_orden_servicio/actualizar/{id}")
    suspend fun actualizarDetalleOrden(
        @Path("id") id: String,
        @Body detalles_orden_servicio: Detalles_orden_servicio
    ): Response<ApiResponse<Detalles_orden_servicio>>

    @DELETE("api/detalles_orden_servicio/eliminar/{id}")
    suspend fun eliminarDetalleOrden(@Path("id") id: String): Response<ApiResponse<Unit>>
}